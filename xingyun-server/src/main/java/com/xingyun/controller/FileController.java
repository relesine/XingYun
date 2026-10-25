package com.xingyun.controller;

import com.xingyun.common.Result;
import com.xingyun.service.FileService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 文件上传控制器。
 * 用法：form-data 上传，参数 file=文件，type=cover|avatar|ware|video
 */
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private FileService fileService;

    @PostMapping("/upload")
    public Result<Map<String, Object>> upload(@RequestParam("file") MultipartFile file,
                                              @RequestParam("type") String type) {
        return Result.success("上传成功", fileService.upload(file, type));
    }
}
