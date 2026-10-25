package com.xingyun.service;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingyun.common.BusinessException;
import com.xingyun.common.PageResult;
import com.xingyun.dto.AdminUserQueryDTO;
import com.xingyun.entity.SysUser;
import com.xingyun.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户服务：管理端用户管理 + 公开讲师列表。
 *
 * @author xingyun
 */
@Service
public class UserService {

    @Resource
    private SysUserMapper userMapper;
    @Resource
    private org.springframework.data.redis.core.StringRedisTemplate stringRedisTemplate;

    /** 管理端分页查询用户 */
    public PageResult<SysUser> adminPage(AdminUserQueryDTO query) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(SysUser::getUsername, query.getKeyword())
                    .or().like(SysUser::getNickname, query.getKeyword())
                    .or().like(SysUser::getPhone, query.getKeyword()));
        }
        if (query.getUserType() != null) {
            wrapper.eq(SysUser::getUserType, query.getUserType());
        }
        if (query.getStatus() != null) {
            wrapper.eq(SysUser::getStatus, query.getStatus());
        }
        wrapper.orderByAsc(SysUser::getId);
        Page<SysUser> page = userMapper.selectPage(
                new Page<>(query.getPage(), query.getSize()), wrapper);
        return PageResult.of(page);
    }

    /**
     * 启用/禁用用户（status 0正常 1禁用）。
     * 【安全】禁用时同步删除该用户 Redis 中的登录态，实现「立即踢下线」；
     * 同时禁止管理员禁用/重置自己，防止把唯一超管锁死。
     */
    public void changeStatus(Long userId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("状态不合法：仅允许 0=正常 / 1=禁用");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        // 自我保护：不能禁用自己
        Long currentId = com.xingyun.security.UserContext.getUserId();
        if (userId.equals(currentId)) {
            throw new BusinessException("不能对自己的账号执行该操作");
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setStatus(status);
        userMapper.updateById(update);
        if (status == 1) {
            // 立即清除该用户所有已签发的 token，强制其重新登录
            stringRedisTemplate.delete(com.xingyun.common.Constants.REDIS_TOKEN + userId);
        }
    }

    /** 管理员重置某用户密码为系统默认密码 */
    public void resetPassword(Long userId) {
        Long currentId = com.xingyun.security.UserContext.getUserId();
        if (userId.equals(currentId)) {
            throw new BusinessException("不能重置自己的密码，请通过个人中心修改");
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(BCrypt.hashpw("123456", BCrypt.gensalt(10)));
        userMapper.updateById(update);
    }

    /** 认证讲师列表（对外展示） */
    public List<SysUser> listTeachers(String keyword) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUserType, 2).eq(SysUser::getStatus, 0);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(SysUser::getNickname, keyword);
        }
        wrapper.orderByAsc(SysUser::getId);
        return userMapper.selectList(wrapper);
    }

    /** 根据 id 集合批量查询用户，避免循环查库（防 N+1） */
    public List<SysUser> listByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return userMapper.selectBatchIds(ids);
    }

    public SysUser getById(Long id) {
        return userMapper.selectById(id);
    }
}
