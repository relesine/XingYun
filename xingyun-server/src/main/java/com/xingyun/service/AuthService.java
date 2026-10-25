package com.xingyun.service;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xingyun.common.BusinessException;
import com.xingyun.common.Constants;
import com.xingyun.dto.LoginDTO;
import com.xingyun.dto.PasswordDTO;
import com.xingyun.dto.RegisterDTO;
import com.xingyun.dto.UserUpdateDTO;
import com.xingyun.entity.SysRole;
import com.xingyun.entity.SysUser;
import com.xingyun.entity.SysUserRole;
import com.xingyun.mapper.SysRoleMapper;
import com.xingyun.mapper.SysUserMapper;
import com.xingyun.mapper.SysUserRoleMapper;
import com.xingyun.security.JwtUtils;
import com.xingyun.security.LoginUser;
import com.xingyun.security.UserContext;
import com.xingyun.vo.LoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务：注册、登录、登出、当前用户、资料与密码修改。
 *
 * @author xingyun
 */
@Slf4j
@Service
public class AuthService {

    @Resource
    private SysUserMapper userMapper;
    @Resource
    private SysUserRoleMapper userRoleMapper;
    @Resource
    private SysRoleMapper roleMapper;
    @Resource
    private JwtUtils jwtUtils;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private ObjectMapper objectMapper;

    public LoginVO login(LoginDTO dto) {
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (user == null) {
            throw new BusinessException("账号不存在，请先注册");
        }
        // BCrypt 校验密码（不能用明文比对，数据库只存密文）
        if (!BCrypt.checkpw(dto.getPassword(), user.getPassword())) {
            throw new BusinessException("账号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 1) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }
        String token = jwtUtils.createToken(user.getId(), user.getUsername());

        // 组装登录用户信息，写入 Redis（24h 过期），后续拦截器从 Redis 还原
        LoginUser loginUser = new LoginUser();
        loginUser.setId(user.getId());
        loginUser.setUsername(user.getUsername());
        loginUser.setNickname(user.getNickname());
        loginUser.setUserType(user.getUserType());
        loginUser.setAvatar(user.getAvatar());
        loginUser.setRoles(loadRoleCodes(user.getId()));
        saveLoginCache(loginUser);

        SysUser update = new SysUser();
        update.setId(user.getId());
        update.setLastLoginTime(new Date());
        userMapper.updateById(update);

        LoginVO vo = new LoginVO();
        vo.setToken(token);
        vo.setUserInfo(loginUser);
        log.info("用户登录成功：{}", user.getUsername());
        return vo;
    }

    /** 注册（默认注册为学员） */
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO dto) {
        Long count = userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, dto.getUsername()));
        if (count != null && count > 0) {
            throw new BusinessException("账号已被注册，请换一个");
        }
        SysUser user = new SysUser();
        user.setUsername(dto.getUsername());
        user.setPassword(BCrypt.hashpw(dto.getPassword(), BCrypt.gensalt(10)));
        user.setNickname(dto.getNickname() == null ? dto.getUsername() : dto.getNickname());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setGender(0);
        user.setUserType(Constants.TYPE_STUDENT);
        user.setStudyMinutes(0);
        user.setIntegral(0);
        user.setStatus(0);
        userMapper.insert(user);

        // 关联学员角色（STUDENT 的 role_id=1）
        SysUserRole ur = new SysUserRole();
        ur.setUserId(user.getId());
        ur.setRoleId(1L);
        userRoleMapper.insert(ur);
    }

    /** 登出：删除 Redis 登录态即可让 token 立即失效 */
    public void logout() {
        Long userId = UserContext.getUserId();
        if (userId != null) {
            stringRedisTemplate.delete(Constants.REDIS_TOKEN + userId);
        }
    }

    public SysUser currentUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException(401, "未登录");
        }
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        return user;
    }

    public void updateProfile(UserUpdateDTO dto) {
        Long userId = UserContext.getUserId();
        SysUser user = new SysUser();
        user.setId(userId);
        user.setNickname(dto.getNickname());
        user.setGender(dto.getGender());
        user.setAvatar(dto.getAvatar());
        user.setEmail(dto.getEmail());
        user.setPhone(dto.getPhone());
        user.setIntro(dto.getIntro());
        user.setTitle(dto.getTitle());
        userMapper.updateById(user);
        // 同步刷新 Redis 登录态里的昵称头像
        LoginUser loginUser = UserContext.get();
        if (loginUser != null) {
            loginUser.setNickname(dto.getNickname());
            loginUser.setAvatar(dto.getAvatar());
            saveLoginCache(loginUser);
        }
    }

    /** 修改密码：先校验原密码，再更新为新密文 */
    public void changePassword(PasswordDTO dto) {
        if (dto.getOldPassword().equals(dto.getNewPassword())) {
            throw new BusinessException("新密码不能与原密码相同");
        }
        Long userId = UserContext.getUserId();
        SysUser user = userMapper.selectById(userId);
        if (!BCrypt.checkpw(dto.getOldPassword(), user.getPassword())) {
            throw new BusinessException("原密码不正确");
        }
        SysUser update = new SysUser();
        update.setId(userId);
        update.setPassword(BCrypt.hashpw(dto.getNewPassword(), BCrypt.gensalt(10)));
        userMapper.updateById(update);
    }

    /** 查询某用户拥有的角色编码集合 */
    private List<String> loadRoleCodes(Long userId) {
        List<SysUserRole> urs = userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>()
                .eq(SysUserRole::getUserId, userId));
        List<String> codes = new ArrayList<>();
        for (SysUserRole ur : urs) {
            SysRole role = roleMapper.selectById(ur.getRoleId());
            if (role != null) {
                codes.add(role.getRoleCode());
            }
        }
        return codes;
    }

    private void saveLoginCache(LoginUser loginUser) {
        try {
            stringRedisTemplate.opsForValue().set(
                    Constants.REDIS_TOKEN + loginUser.getId(),
                    objectMapper.writeValueAsString(loginUser),
                    24, TimeUnit.HOURS);
        } catch (Exception e) {
            throw new BusinessException("登录状态写入失败：" + e.getMessage());
        }
    }
}
