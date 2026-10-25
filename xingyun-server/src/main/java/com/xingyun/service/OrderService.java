package com.xingyun.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingyun.common.BusinessException;
import com.xingyun.common.PageResult;
import com.xingyun.dto.OrderCreateDTO;
import com.xingyun.entity.*;
import com.xingyun.mapper.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单服务：创建订单（含优惠券计价）、模拟支付（幂等防重复支付）、我的订单/后台订单。
 * 关键坑点防护：免费课不下单、重复下单拦截、订单号唯一、支付只允许从“待支付”变为“已支付”。
 */
@Service
public class OrderService {

    @Resource
    private CourseOrderMapper orderMapper;
    @Resource
    private CourseMapper courseMapper;
    @Resource
    private UserCouponMapper userCouponMapper;
    @Resource
    private CouponMapper couponMapper;

    /** 创建待支付订单 */
    @Transactional(rollbackFor = Exception.class)
    public CourseOrder create(OrderCreateDTO dto, Long userId) {
        Course course = courseMapper.selectById(dto.getCourseId());
        if (course == null) {
            throw new BusinessException("课程不存在");
        }
        if (course.getIsFree() == 1) {
            throw new BusinessException("免费课程无需下单，可直接学习");
        }
        // 已购买拦截（订单重复支付/重复购买防护）
        Long owned = orderMapper.selectCount(new LambdaQueryWrapper<CourseOrder>()
                .eq(CourseOrder::getUserId, userId)
                .eq(CourseOrder::getCourseId, course.getId())
                .eq(CourseOrder::getStatus, 1));
        if (owned != null && owned > 0) {
            throw new BusinessException("您已购买该课程，请勿重复下单");
        }
        BigDecimal original = course.getPrice();
        BigDecimal discount = BigDecimal.ZERO;
        UserCoupon userCoupon = null;
        if (dto.getUserCouponId() != null) {
            userCoupon = userCouponMapper.selectById(dto.getUserCouponId());
            if (userCoupon == null || !userCoupon.getUserId().equals(userId)) {
                throw new BusinessException("优惠券不可用");
            }
            if (userCoupon.getStatus() != 0) {
                throw new BusinessException("优惠券已使用或已过期");
            }
            // 过期时间校验：券面额在有效期后才核销，防止用过期的券下单
            if (userCoupon.getExpireTime() != null && userCoupon.getExpireTime().before(new Date())) {
                throw new BusinessException("优惠券已过期");
            }
            Coupon coupon = couponMapper.selectById(userCoupon.getCouponId());
            discount = calcDiscount(coupon, original);
        }
        BigDecimal pay = original.subtract(discount).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

        CourseOrder order = new CourseOrder();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setCourseId(course.getId());
        order.setOriginalAmount(original);
        order.setDiscountAmount(discount);
        order.setPayAmount(pay);
        order.setCouponId(dto.getUserCouponId());
        order.setPayType(1);
        order.setStatus(0);
        orderMapper.insert(order);
        return order;
    }

    /**
     * 模拟支付（学习项目不接真实支付，点“立即支付”即支付成功）。
     * 幂等设计：只有待支付(0)订单可支付，重复回调/重复点击不会重复加学员。
     */
    @Transactional(rollbackFor = Exception.class)
    public void pay(String orderNo, Integer payType, Long userId) {
        CourseOrder order = orderMapper.selectOne(new LambdaQueryWrapper<CourseOrder>()
                .eq(CourseOrder::getOrderNo, orderNo));
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() == 1) {
            // 已经支付，直接幂等返回，不重复处理
            return;
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("当前订单状态无法支付");
        }
        CourseOrder update = new CourseOrder();
        update.setId(order.getId());
        update.setStatus(1);
        update.setPayType(payType == null ? 1 : payType);
        update.setPayTime(new Date());
        orderMapper.updateById(update);

        if (order.getCouponId() != null) {
            // 【并发防护】核销前二次校验券仍是「未使用」状态，防止同一张券被多笔订单重复核销
            UserCoupon current = userCouponMapper.selectById(order.getCouponId());
            if (current == null || current.getStatus() != 0) {
                throw new BusinessException("优惠券已被使用，无法完成支付");
            }
            UserCoupon uc = new UserCoupon();
            uc.setId(order.getCouponId());
            uc.setStatus(1);
            uc.setOrderId(order.getId());
            uc.setUseTime(new Date());
            userCouponMapper.updateById(uc);
        }
        // 课程学习人数 +1（去重统计：只有支付成功才加）
        // 【并发优化】改为数据库原子自增，避免读-改-写丢失更新
        Course course = courseMapper.selectById(order.getCourseId());
        if (course != null) {
            courseMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Course>()
                    .eq("id", course.getId())
                    .setSql("student_count = student_count + 1"));
        }
    }

    /** 我的订单 */
    public List<CourseOrder> myOrders(Long userId, Integer status) {
        LambdaQueryWrapper<CourseOrder> w = new LambdaQueryWrapper<CourseOrder>()
                .eq(CourseOrder::getUserId, userId).orderByDesc(CourseOrder::getId);
        if (status != null) {
            w.eq(CourseOrder::getStatus, status);
        }
        List<CourseOrder> list = orderMapper.selectList(w);
        fillCourseName(list);
        return list;
    }

    /** 批量为订单填充课程名（避免 N+1 查询，一次性把相关课程查出来做成 Map） */
    private void fillCourseName(List<CourseOrder> list) {
        if (list == null || list.isEmpty()) {
            return;
        }
        List<Long> courseIds = list.stream().map(CourseOrder::getCourseId).distinct().collect(Collectors.toList());
        Map<Long, String> nameMap = courseMapper.selectBatchIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, Course::getCourseName, (a, b) -> a));
        list.forEach(o -> o.setCourseName(nameMap.get(o.getCourseId())));
    }

    /** 取消待支付订单 */
    public void cancel(Long orderId, Long userId) {
        CourseOrder order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (order.getStatus() != 0) {
            throw new BusinessException("只能取消待支付订单");
        }
        CourseOrder up = new CourseOrder();
        up.setId(orderId);
        up.setStatus(2);
        orderMapper.updateById(up);
    }

    /** 管理端订单分页 */
    public PageResult<CourseOrder> adminPage(long page, long size, Integer status) {
        LambdaQueryWrapper<CourseOrder> w = new LambdaQueryWrapper<CourseOrder>().orderByDesc(CourseOrder::getId);
        if (status != null) {
            w.eq(CourseOrder::getStatus, status);
        }
        Page<CourseOrder> p = orderMapper.selectPage(new Page<>(page, size), w);
        fillCourseName(p.getRecords());
        return PageResult.of(p);
    }

    /** 根据优惠券与原价计算优惠金额 */
    private BigDecimal calcDiscount(Coupon coupon, BigDecimal original) {
        if (coupon == null) {
            return BigDecimal.ZERO;
        }
        switch (coupon.getCouponType()) {
            case 1: // 满减：达到门槛减固定金额
                if (original.compareTo(coupon.getThreshold()) < 0) {
                    throw new BusinessException("未满 " + coupon.getThreshold() + " 元，不能使用该券");
                }
                return coupon.getDiscountValue();
            case 2: // 立减
                return coupon.getDiscountValue();
            case 3: // 折扣：折扣值如 8.5 表示 85 折
                BigDecimal pay = original.multiply(coupon.getDiscountValue())
                        .divide(new BigDecimal("10"), 2, RoundingMode.HALF_UP);
                return original.subtract(pay);
            default:
                return BigDecimal.ZERO;
        }
    }

    /** 生成唯一订单号：XY + 14位时间 + 4位随机数 */
    private String generateOrderNo() {
        return "XY" + DateUtil.format(new Date(), "yyyyMMddHHmmss") + RandomUtil.randomNumbers(4);
    }
}
