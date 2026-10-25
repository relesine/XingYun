package com.xingyun.controller;

import com.xingyun.common.Result;
import com.xingyun.security.UserContext;
import com.xingyun.service.CertificateService;
import com.xingyun.vo.CertificateVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/** 学习证书 */
@RestController
@RequestMapping("/cert")
public class CertificateController {

    @Resource
    private CertificateService certificateService;

    @GetMapping("/my")
    public Result<List<CertificateVO>> my() {
        return Result.success(certificateService.myCertificates(UserContext.getUserId()));
    }

    @GetMapping("/detail/{id}")
    public Result<CertificateVO> detail(@PathVariable Long id) {
        return Result.success(certificateService.detail(id));
    }
}
