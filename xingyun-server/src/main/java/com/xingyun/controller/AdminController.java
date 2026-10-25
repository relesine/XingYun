package com.xingyun.controller;

import com.xingyun.common.PageResult;
import com.xingyun.common.Result;
import com.xingyun.dto.*;
import com.xingyun.entity.Announcement;
import com.xingyun.entity.Coupon;
import com.xingyun.entity.CourseCategory;
import com.xingyun.entity.SysUser;
import com.xingyun.entity.CourseOrder;
import com.xingyun.service.*;
import com.xingyun.vo.CommentVO;
import com.xingyun.vo.CourseVO;
import com.xingyun.vo.DashboardVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 平台管理后台控制器：所有 /admin/** 接口仅平台管理员可访问（拦截器按前缀鉴权）。
 */
@RestController
@RequestMapping("/admin")
public class AdminController {

    @Resource
    private StatsService statsService;
    @Resource
    private UserService userService;
    @Resource
    private CourseService courseService;
    @Resource
    private OrderService orderService;
    @Resource
    private CommentService commentService;
    @Resource
    private CategoryService categoryService;
    @Resource
    private CouponService couponService;
    @Resource
    private AnnouncementService announcementService;

    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard() {
        return Result.success(statsService.dashboard());
    }

    @PostMapping("/user/page")
    public Result<PageResult<SysUser>> userPage(@RequestBody AdminUserQueryDTO query) {
        return Result.success(userService.adminPage(query));
    }

    @PostMapping("/user/status")
    public Result<Void> userStatus(@RequestBody IdStatusDTO dto) {
        userService.changeStatus(dto.getId(), dto.getStatus());
        return Result.success();
    }

    @PostMapping("/user/resetPwd/{id}")
    public Result<Void> resetPwd(@PathVariable Long id) {
        userService.resetPassword(id);
        return Result.success("已重置为系统默认密码", null);
    }

    @PostMapping("/course/page")
    public Result<PageResult<CourseVO>> coursePage(@RequestBody CourseQueryDTO query) {
        return Result.success(courseService.adminPage(query));
    }

    @PostMapping("/course/audit")
    public Result<Void> audit(@Valid @RequestBody AuditDTO dto) {
        courseService.audit(dto.getCourseId(), dto.getAuditStatus(), dto.getRejectReason());
        return Result.success();
    }

    @PostMapping("/course/shelf")
    public Result<Void> shelf(@RequestBody IdStatusDTO dto) {
        courseService.changeShelf(dto.getId(), dto.getStatus());
        return Result.success();
    }

    @PostMapping("/course/recommend")
    public Result<Void> recommend(@RequestBody IdStatusDTO dto) {
        courseService.changeRecommend(dto.getId(), dto.getStatus());
        return Result.success();
    }

    @DeleteMapping("/course/{id}")
    public Result<Void> deleteCourse(@PathVariable Long id) {
        courseService.delete(id);
        return Result.success();
    }

    @PostMapping("/order/page")
    public Result<PageResult<CourseOrder>> orderPage(@RequestBody PageQuery query) {
        Integer status = null;
        return Result.success(orderService.adminPage(query.getPage(), query.getSize(), status));
    }

    @PostMapping("/comment/page")
    public Result<PageResult<CommentVO>> commentPage(@RequestBody PageQuery query) {
        return Result.success(commentService.adminPage(query.getPage(), query.getSize(), null));
    }

    @PostMapping("/comment/status")
    public Result<Void> commentStatus(@RequestBody IdStatusDTO dto) {
        commentService.changeStatus(dto.getId(), dto.getStatus());
        return Result.success();
    }

    @GetMapping("/category/list")
    public Result<List<CourseCategory>> categoryList() {
        return Result.success(categoryService.listAll());
    }

    @PostMapping("/category/save")
    public Result<Void> categorySave(@Valid @RequestBody CategoryDTO dto) {
        categoryService.save(dto);
        return Result.success();
    }

    @DeleteMapping("/category/{id}")
    public Result<Void> categoryDelete(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }

    @GetMapping("/coupon/list")
    public Result<List<Coupon>> couponList() {
        return Result.success(couponService.adminList());
    }

    @PostMapping("/coupon/save")
    public Result<Void> couponSave(@Valid @RequestBody CouponDTO dto) {
        couponService.save(dto);
        return Result.success();
    }

    @DeleteMapping("/coupon/{id}")
    public Result<Void> couponDelete(@PathVariable Long id) {
        couponService.delete(id);
        return Result.success();
    }

    @GetMapping("/announcement/list")
    public Result<List<Announcement>> announcementList() {
        return Result.success(announcementService.adminList());
    }

    @PostMapping("/announcement/save")
    public Result<Void> announcementSave(@Valid @RequestBody AnnouncementDTO dto) {
        announcementService.save(dto);
        return Result.success();
    }

    @DeleteMapping("/announcement/{id}")
    public Result<Void> announcementDelete(@PathVariable Long id) {
        announcementService.delete(id);
        return Result.success();
    }
}
