package com.xingyun.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xingyun.entity.VideoResource;
import org.apache.ibatis.annotations.Mapper;

/**
 * VideoResource 数据访问层（继承 BaseMapper 即拥有单表增删改查能力）
 */
@Mapper
public interface VideoResourceMapper extends BaseMapper<VideoResource> {
}
