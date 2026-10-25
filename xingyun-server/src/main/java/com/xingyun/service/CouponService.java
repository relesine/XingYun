package com.xingyun.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingyun.common.BusinessException;
import com.xingyun.dto.CouponDTO;
import com.xingyun.entity.Coupon;
import com.xingyun.entity.UserCoupon;
import com.xingyun.mapper.CouponMapper;
import com.xingyun.mapper.UserCouponMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 优惠券服务：可领列表、领取（防重复、控库存）、我的优惠券、后台维护。
 */
@Service
public class CouponService {

    @Resource
    private CouponMapper couponMapper;
    @Resource
    private UserCouponMapper userCouponMapper;

    /** 可领取的优惠券（启用且未领完） */
    public List<Coupon> available() {
        return couponMapper.selectList(new LambdaQueryWrapper<Coupon>()
                .eq(Coupon::getStatus, 1)
                .and(w -> w.eq(Coupon::getTotalCount, 0).or().apply("receive_count < total_count"))
                .orderByAsc(Coupon::getId));
    }

    /** 领取优惠券：同一用户同一券限领一张，库存扣减（原子自增，防并发超发） */
    @Transactional(rollbackFor = Exception.class)
    public void receive(Long couponId, Long userId) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null || coupon.getStatus() != 1) {
            throw new BusinessException("优惠券不存在或已停用");
        }
        Long own = userCouponMapper.selectCount(new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId).eq(UserCoupon::getCouponId, couponId));
        if (own != null && own > 0) {
            throw new BusinessException("您已领取过该优惠券，不能重复领取");
        }
        // 【并发防护】库存扣减改为条件原子更新：
        // 仅当「不限量(total_count=0)」或「已领 < 总量」时才允许 +1，防止并发超发
        int affected = couponMapper.update(null, new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<Coupon>()
                .eq("id", couponId)
                .apply("(total_count = 0 OR receive_count < total_count)")
                .setSql("receive_count = receive_count + 1"));
        if (affected == 0) {
            throw new BusinessException("优惠券已被领完");
        }
        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId);
        uc.setCouponId(couponId);
        uc.setStatus(0);
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, coupon.getValidDays() == null ? 30 : coupon.getValidDays());
        uc.setExpireTime(cal.getTime());
        userCouponMapper.insert(uc);
    }

    /** 我的优惠券（附带券详情），status: 0未用 1已用 2过期 */
    public List<Map<String, Object>> myCoupons(Long userId, Integer status) {
        LambdaQueryWrapper<UserCoupon> w = new LambdaQueryWrapper<UserCoupon>()
                .eq(UserCoupon::getUserId, userId).orderByDesc(UserCoupon::getId);
        if (status != null) {
            w.eq(UserCoupon::getStatus, status);
        }
        List<UserCoupon> list = userCouponMapper.selectList(w);
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> ids = list.stream().map(UserCoupon::getCouponId).collect(Collectors.toSet());
        Map<Long, Coupon> map = couponMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(Coupon::getId, c -> c));
        Date now = new Date();
        List<Map<String, Object>> result = new ArrayList<>();
        for (UserCoupon uc : list) {
            Map<String, Object> m = new HashMap<>();
            if (uc.getStatus() == 0 && uc.getExpireTime() != null && uc.getExpireTime().before(now)) {
                uc.setStatus(2);
                UserCoupon up = new UserCoupon();
                up.setId(uc.getId());
                up.setStatus(2);
                userCouponMapper.updateById(up);
            }
            m.put("userCoupon", uc);
            m.put("coupon", map.get(uc.getCouponId()));
            result.add(m);
        }
        return result;
    }

    /** 后台：优惠券分页/列表 */
    public List<Coupon> adminList() {
        return couponMapper.selectList(new LambdaQueryWrapper<Coupon>().orderByDesc(Coupon::getId));
    }

    public void save(CouponDTO dto) {
        Coupon coupon = new Coupon();
        BeanUtils.copyProperties(dto, coupon);
        if (dto.getId() == null) {
            coupon.setReceiveCount(0);
            couponMapper.insert(coupon);
        } else {
            couponMapper.updateById(coupon);
        }
    }

    public void delete(Long id) {
        couponMapper.deleteById(id);
    }
}
