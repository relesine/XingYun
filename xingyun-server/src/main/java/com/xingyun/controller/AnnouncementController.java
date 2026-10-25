package com.xingyun.controller;

import com.xingyun.common.Result;
import com.xingyun.entity.Announcement;
import com.xingyun.service.AnnouncementService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/** 平台公告（前台） */
@RestController
@RequestMapping("/announcement")
public class AnnouncementController {

    @Resource
    private AnnouncementService announcementService;

    /** 已发布公告列表（公开） */
    @GetMapping("/list")
    public Result<List<Announcement>> list(@RequestParam(required = false) Integer type) {
        return Result.success(announcementService.published(type));
    }
}
