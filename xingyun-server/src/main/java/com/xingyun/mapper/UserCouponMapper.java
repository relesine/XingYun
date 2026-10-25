package com.xingyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xingyun.entity.UserCoupon;
import org.apache.ibatis.annotations.Mapper;

/**
 * UserCoupon 数据访问层（继承 BaseMapper 即拥有单表增删改查能力）
 */
@Mapper
public interface UserCouponMapper extends BaseMapper<UserCoupon> {
}
