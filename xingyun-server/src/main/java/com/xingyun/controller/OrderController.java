package com.xingyun.controller;

import com.xingyun.common.Result;
import com.xingyun.dto.OrderCreateDTO;
import com.xingyun.dto.PayDTO;
import com.xingyun.entity.CourseOrder;
import com.xingyun.security.UserContext;
import com.xingyun.service.OrderService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/** 课程订单：下单、模拟支付、我的订单、取消 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private OrderService orderService;

    /** 创建待支付订单 */
    @PostMapping("/create")
    public Result<CourseOrder> create(@Valid @RequestBody OrderCreateDTO dto) {
        return Result.success(orderService.create(dto, UserContext.getUserId()));
    }

    /** 模拟支付（幂等，防重复支付） */
    @PostMapping("/pay")
    public Result<Void> pay(@Valid @RequestBody PayDTO dto) {
        orderService.pay(dto.getOrderNo(), dto.getPayType(), UserContext.getUserId());
        return Result.success("支付成功", null);
    }

    /** 我的订单，status 可空表示全部 */
    @GetMapping("/my")
    public Result<List<CourseOrder>> my(Integer status) {
        return Result.success(orderService.myOrders(UserContext.getUserId(), status));
    }

    /** 取消待支付订单 */
    @PostMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancel(id, UserContext.getUserId());
        return Result.success();
    }
}
