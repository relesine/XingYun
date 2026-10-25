package com.xingyun.controller;

import com.xingyun.common.Result;
import com.xingyun.entity.VideoResource;
import com.xingyun.security.UserContext;
import com.xingyun.service.FileService;
import com.xingyun.service.VideoService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * 视频控制器：申请防盗链签名 + 输出支持断点拖拽(Range 206)的视频流。
 */
@RestController
@RequestMapping("/video")
public class VideoController {

    /** 每次分片传输 1MB，避免一次性把大视频读进内存（防止内存溢出） */
    private static final long CHUNK_SIZE = 1024 * 1024L;

    @Resource
    private VideoService videoService;
    @Resource
    private FileService fileService;

    /** 申请播放签名（需登录） */
    @PostMapping("/sign")
    public Result<Map<String, Object>> sign(@RequestBody Map<String, Long> body) {
        Long lessonId = body.get("lessonId");
        return Result.success(videoService.applySign(lessonId, UserContext.getUserId()));
    }

    /**
     * 视频流输出（白名单接口，凭 5 分钟有效 sign 访问，直接拼链接无 sign 一律拒绝）。
     * 支持 HTTP Range：浏览器可快进/拖动，每次只读取需要的字节区间。
     * 【健壮性】对畸形 Range、越界 Range 返回 416（范围不满足），并对负数/非法字符做防护，避免 500 与负数长度。
     */
    @GetMapping("/stream")
    public ResponseEntity<ResourceRegion> stream(@RequestParam String sign, HttpServletRequest request) {
        // 1. 校验签名（失败会抛业务异常并由全局处理器拦截）
        VideoResource video = videoService.verifySign(sign);
        File file = fileService.locate(video.getFilePath());
        FileSystemResource resource = new FileSystemResource(file);
        long fileLength = file.length();
        if (fileLength <= 0) {
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .header(HttpHeaders.CONTENT_RANGE, "bytes */0")
                    .build();
        }

        // 2. 根据 Range 头手动计算要返回的字节区间（支持视频拖拽，每次最多返回一个分片）
        String rangeHeader = request.getHeader(HttpHeaders.RANGE);
        ResourceRegion region;
        HttpStatus status = HttpStatus.OK;
        long start = 0;
        long end = fileLength - 1;
        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            // 形如 bytes=1024- / bytes=1024-2048 / bytes=-1024（末1024字节）
            String range = rangeHeader.substring(6).trim();
            int dash = range.indexOf('-');
            if (dash <= 0 || dash >= range.length() - 1) {
                // 缺少 '-' 或没有边界值：非法请求
                return rangeNotSatisfiable(fileLength);
            }
            String s1 = range.substring(0, dash).trim();
            String s2 = range.substring(dash + 1).trim();
            boolean suffixMode = s1.isEmpty();
            try {
                if (suffixMode) {
                    // 后缀区间：bytes=-N 表示最后 N 个字节
                    long suffixLen = Long.parseLong(s2);
                    if (suffixLen <= 0) {
                        return rangeNotSatisfiable(fileLength);
                    }
                    start = Math.max(fileLength - suffixLen, 0);
                    end = fileLength - 1;
                } else {
                    start = Long.parseLong(s1);
                    if (!s2.isEmpty()) {
                        end = Long.parseLong(s2);
                    } else {
                        // open-range：每次给一个分片大小
                        end = Math.min(start + CHUNK_SIZE - 1, fileLength - 1);
                    }
                }
            } catch (NumberFormatException e) {
                // 非数字字节区间：非法请求
                return rangeNotSatisfiable(fileLength);
            }
            if (start < 0 || start >= fileLength || start > end) {
                return rangeNotSatisfiable(fileLength);
            }
            end = Math.min(end, fileLength - 1);
            status = HttpStatus.PARTIAL_CONTENT; // 206
        } else {
            end = Math.min(CHUNK_SIZE - 1, fileLength - 1);
        }
        long length = end - start + 1;
        region = new ResourceRegion(resource, start, length);
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        try {
            mediaType = new MediaType("video", file.getName().substring(file.getName().lastIndexOf('.') + 1));
        } catch (Exception ignored) {
        }
        return ResponseEntity.status(status)
                .contentType(mediaType)
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                .body(region);
    }

    /** 416：Range 不满足，附带文件总长度提示客户端 */
    private ResponseEntity<ResourceRegion> rangeNotSatisfiable(long fileLength) {
        return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileLength)
                .build();
    }
}
