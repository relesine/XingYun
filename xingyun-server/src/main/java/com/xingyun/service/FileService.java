package com.xingyun.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.xingyun.common.BusinessException;
import com.xingyun.entity.VideoResource;
import com.xingyun.mapper.VideoResourceMapper;
import com.xingyun.security.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 文件上传服务（本地存储）。
 * 安全要点：
 * 1. 白名单校验扩展名，禁止 exe/jsp/sh 等可执行文件；
 * 2. 用随机 UUID 重命名，不使用原始文件名，避免中文乱码、文件覆盖和路径穿越；
 * 3. 按“类型/日期”分目录，避免单目录文件过多；
 * 4. 视频只登记相对路径，不开放静态直链，配合防盗链接口播放。
 */
@Slf4j
@Service
public class FileService {

    @Value("${xingyun.upload.path}")
    private String uploadPath;

    @Resource
    private VideoResourceMapper videoMapper;

    private static final Set<String> IMAGE_TYPES = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> VIDEO_TYPES = Set.of("mp4", "webm");
    private static final Set<String> WARE_TYPES = Set.of("pdf", "ppt", "pptx", "doc", "docx", "zip", "rar", "txt", "xlsx", "xls");

    /**
     * 上传文件
     *
     * @param file 文件
     * @param type cover 封面 / avatar 头像 / ware 课件 / video 视频
     */
    public Map<String, Object> upload(MultipartFile file, String type) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        String ext = getExt(file.getOriginalFilename());
        switch (type) {
            case "cover":
            case "avatar":
                if (!IMAGE_TYPES.contains(ext)) {
                    throw new BusinessException("图片仅支持 jpg/png/gif/webp 格式");
                }
                if (file.getSize() > 10L * 1024 * 1024) {
                    throw new BusinessException("图片大小不能超过10MB");
                }
                break;
            case "video":
                if (!VIDEO_TYPES.contains(ext)) {
                    throw new BusinessException("视频仅支持 mp4/webm 格式");
                }
                break;
            case "ware":
                if (!WARE_TYPES.contains(ext)) {
                    throw new BusinessException("课件格式不被支持");
                }
                if (file.getSize() > 100L * 1024 * 1024) {
                    throw new BusinessException("课件大小不能超过100MB");
                }
                break;
            default:
                throw new BusinessException("未知的上传类型：" + type);
        }

        // 存储目录：upload/type/yyyyMMdd/
        String datePath = DateUtil.format(new Date(), "yyyyMMdd");
        File dir = new File(uploadPath, type + File.separator + datePath);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BusinessException("创建上传目录失败：" + dir.getAbsolutePath());
        }
        String fileName = IdUtil.fastSimpleUUID() + "." + ext;
        File dest = new File(dir, fileName);
        try {
            file.transferTo(dest);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new BusinessException("文件保存失败：" + e.getMessage());
        }
        // 相对路径（数据库里存它）与可访问 URL
        String relativePath = type + "/" + datePath + "/" + fileName;
        Map<String, Object> result = new HashMap<>();
        result.put("fileName", file.getOriginalFilename());
        result.put("size", file.getSize());

        if ("video".equals(type)) {
            // 视频登记入库，默认时长 0，等待后续可由转码/前端回填
            VideoResource video = new VideoResource();
            video.setVideoName(file.getOriginalFilename());
            video.setFilePath(relativePath);
            video.setFileSize(file.getSize());
            video.setDuration(0);
            video.setPlayCount(0);
            video.setCreateBy(UserContext.getUserId());
            videoMapper.insert(video);
            result.put("videoId", video.getId());
            // 视频不返回可直链 URL，需通过签名接口播放
            result.put("filePath", relativePath);
        } else {
            result.put("url", "/upload/" + relativePath);
            result.put("filePath", relativePath);
        }
        log.info("文件上传成功：{} -> {}", type, relativePath);
        return result;
    }

    /** 根据数据库相对路径定位服务器物理文件 */
    public File locate(String relativePath) {
        // 防路径穿越：规范化后必须仍在 upload 根目录内（补分隔符，避免 upload-evil 这类前缀绕过）
        File root = new File(uploadPath);
        File target = new File(root, relativePath);
        try {
            String rootPath = root.getCanonicalPath() + File.separator;
            if (!target.getCanonicalPath().startsWith(rootPath)) {
                throw new BusinessException(403, "非法的文件路径");
            }
        } catch (IOException e) {
            throw new BusinessException("文件路径解析失败");
        }
        if (!target.exists() || !target.isFile()) {
            throw new BusinessException("文件不存在或已被移除");
        }
        return target;
    }

    private String getExt(String originalName) {
        if (originalName == null || !originalName.contains(".")) {
            throw new BusinessException("无法识别文件类型（缺少扩展名）");
        }
        return originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();
    }
}
