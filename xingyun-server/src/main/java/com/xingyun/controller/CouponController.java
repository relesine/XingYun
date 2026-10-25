package com.xingyun.controller;

import com.xingyun.common.Result;
import com.xingyun.entity.Coupon;
import com.xingyun.security.UserContext;
import com.xingyun.service.CouponService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/** 优惠券（学员侧）：可领列表、领取、我的优惠券 */
@RestController
@RequestMapping("/coupon")
public class CouponController {

    @Resource
    private CouponService couponService;

    /** 可领取的优惠券 */
    @GetMapping("/available")
    public Result<List<Coupon>> available() {
        return Result.success(couponService.available());
    }

    /** 领取优惠券 */
    @PostMapping("/receive/{id}")
    public Result<Void> receive(@PathVariable Long id) {
        couponService.receive(id, UserContext.getUserId());
        return Result.success("领取成功", null);
    }

    /** 我的优惠券，status：0未用 1已用 2过期，空为全部 */
    @GetMapping("/my")
    public Result<List<Map<String, Object>>> my(Integer status) {
        return Result.success(couponService.myCoupons(UserContext.getUserId(), status));
    }
}
