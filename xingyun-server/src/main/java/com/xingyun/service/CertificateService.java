package com.xingyun.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingyun.common.BusinessException;
import com.xingyun.entity.Certificate;
import com.xingyun.entity.Course;
import com.xingyun.entity.SysUser;
import com.xingyun.mapper.CertificateMapper;
import com.xingyun.mapper.CourseMapper;
import com.xingyun.mapper.SysUserMapper;
import com.xingyun.security.UserContext;
import com.xingyun.vo.CertificateVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 证书服务：课程全部学完后自动生成结业证书，证书号全局唯一。
 */
@Service
public class CertificateService {

    @Resource
    private CertificateMapper certificateMapper;
    @Resource
    private CourseMapper courseMapper;
    @Resource
    private SysUserMapper userMapper;

    /**
     * 尝试发证：仅当该用户该课程尚无证书时生成（幂等，重复调用不会发多张）。
     *
     * @return 新生成的证书；已存在或不满足条件返回 null
     */
    public synchronized Certificate tryIssue(Long userId, Long courseId, String courseName) {
        Long exists = certificateMapper.selectCount(new LambdaQueryWrapper<Certificate>()
                .eq(Certificate::getUserId, userId).eq(Certificate::getCourseId, courseId));
        if (exists != null && exists > 0) {
            return null;
        }
        Certificate cert = new Certificate();
        cert.setCertNo(generateCertNo());
        cert.setUserId(userId);
        cert.setCourseId(courseId);
        cert.setCertificateName("《" + courseName + "》结业证书");
        cert.setIssueTime(new Date());
        cert.setStatus(1);
        certificateMapper.insert(cert);
        return cert;
    }

    public List<CertificateVO> myCertificates(Long userId) {
        List<Certificate> list = certificateMapper.selectList(new LambdaQueryWrapper<Certificate>()
                .eq(Certificate::getUserId, userId).orderByDesc(Certificate::getId));
        return fillVO(list);
    }

    /** 证书详情（本人或管理员可看） */
    public CertificateVO detail(Long certId) {
        Certificate cert = certificateMapper.selectById(certId);
        if (cert == null) {
            throw new BusinessException("证书不存在");
        }
        return fillVO(List.of(cert)).get(0);
    }

    /** 生成证书编号：XY + yyyyMMdd + 8位随机串 */
    private String generateCertNo() {
        return "XY" + DateUtil.format(new Date(), "yyyyMMdd") + IdUtil.fastSimpleUUID().substring(0, 8).toUpperCase();
    }

    private List<CertificateVO> fillVO(List<Certificate> list) {
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> courseIds = list.stream().map(Certificate::getCourseId).collect(Collectors.toSet());
        Set<Long> userIds = list.stream().map(Certificate::getUserId).collect(Collectors.toSet());
        Map<Long, Course> courseMap = courseMapper.selectBatchIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, c -> c));
        Map<Long, SysUser> userMap = userMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));
        List<CertificateVO> result = new ArrayList<>();
        for (Certificate c : list) {
            CertificateVO vo = new CertificateVO();
            BeanUtils.copyProperties(c, vo);
            Course course = courseMap.get(c.getCourseId());
            if (course != null) {
                vo.setCourseName(course.getCourseName());
                SysUser teacher = userMapper.selectById(course.getTeacherId());
                if (teacher != null) {
                    vo.setTeacherName(teacher.getNickname());
                }
            }
            SysUser stu = userMap.get(c.getUserId());
            if (stu != null) {
                vo.setStudentName(stu.getNickname());
            }
            result.add(vo);
        }
        return result;
    }
}
