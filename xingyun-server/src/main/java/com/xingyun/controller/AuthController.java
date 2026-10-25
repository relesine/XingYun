package com.xingyun.controller;

import com.xingyun.common.Result;
import com.xingyun.dto.LoginDTO;
import com.xingyun.dto.PasswordDTO;
import com.xingyun.dto.RegisterDTO;
import com.xingyun.dto.UserUpdateDTO;
import com.xingyun.entity.SysUser;
import com.xingyun.service.AuthService;
import com.xingyun.vo.LoginVO;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

/**
 * 认证相关：登录、注册、登出、当前用户、资料/密码。
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    /** 登录（公开） */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(authService.login(dto));
    }

    /** 注册（公开，注册后为学员） */
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO dto) {
        authService.register(dto);
        return Result.success();
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }

    /** 获取当前登录用户信息 */
    @GetMapping("/info")
    public Result<SysUser> info() {
        return Result.success(authService.currentUser());
    }

    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UserUpdateDTO dto) {
        authService.updateProfile(dto);
        return Result.success();
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordDTO dto) {
        authService.changePassword(dto);
        return Result.success();
    }
}
