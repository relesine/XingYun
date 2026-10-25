package com.xingyun.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xingyun.common.BusinessException;
import com.xingyun.common.PageResult;
import com.xingyun.dto.CommentDTO;
import com.xingyun.entity.Course;
import com.xingyun.entity.CourseComment;
import com.xingyun.entity.SysUser;
import com.xingyun.mapper.CourseCommentMapper;
import com.xingyun.mapper.SysUserMapper;
import com.xingyun.vo.CommentVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** 课程评论服务 */
@Service
public class CommentService {

    @Resource
    private CourseCommentMapper commentMapper;
    @Resource
    private SysUserMapper userMapper;
    @Resource
    private CourseService courseService;

    /** 某课程的公开评论（仅显示状态），附带评论人昵称头像 */
    public List<CommentVO> listByCourse(Long courseId) {
        List<CourseComment> list = commentMapper.selectList(new LambdaQueryWrapper<CourseComment>()
                .eq(CourseComment::getCourseId, courseId)
                .eq(CourseComment::getStatus, 1)
                .orderByDesc(CourseComment::getId));
        return fill(list);
    }

    /**
     * 发表评论。
     * 【业务规则】只有「已购该课」或「免费课」的学员才能评论/评分，
     * 防止未购学员刷评论；内容做去空格与长度校验。
     */
    public void add(CommentDTO dto, Long userId) {
        String content = dto.getContent() == null ? "" : dto.getContent().trim();
        if (content.isEmpty()) {
            throw new BusinessException("评论内容不能为空");
        }
        if (content.length() > 500) {
            throw new BusinessException("评论内容不能超过500字");
        }
        Course course = courseService.getCourseEntity(dto.getCourseId());
        if (!courseService.hasPurchased(userId, course)) {
            throw new BusinessException(403, "购买课程后才能评论评分");
        }
        CourseComment c = new CourseComment();
        c.setCourseId(dto.getCourseId());
        c.setUserId(userId);
        c.setParentId(dto.getParentId() == null ? 0L : dto.getParentId());
        c.setContent(content);
        c.setScore(dto.getScore());
        c.setStatus(1); // 学习项目默认直接展示，可在后台改为先审后发
        commentMapper.insert(c);
    }

    /** 管理端评论分页（可按课程筛选） */
    public PageResult<CommentVO> adminPage(long page, long size, Long courseId) {
        LambdaQueryWrapper<CourseComment> w = new LambdaQueryWrapper<CourseComment>().orderByDesc(CourseComment::getId);
        if (courseId != null) {
            w.eq(CourseComment::getCourseId, courseId);
        }
        Page<CourseComment> p = commentMapper.selectPage(new Page<>(page, size), w);
        PageResult<CommentVO> r = new PageResult<>();
        r.setRecords(fill(p.getRecords()));
        r.setTotal(p.getTotal());
        r.setCurrent(p.getCurrent());
        r.setSize(p.getSize());
        return r;
    }

    /** 屏蔽/恢复评论 */
    public void changeStatus(Long id, Integer status) {
        CourseComment up = new CourseComment();
        up.setId(id);
        up.setStatus(status);
        commentMapper.updateById(up);
    }

    public void delete(Long id) {
        commentMapper.deleteById(id);
    }

    private List<CommentVO> fill(List<CourseComment> list) {
        if (list.isEmpty()) {
            return new ArrayList<>();
        }
        Set<Long> ids = list.stream().map(CourseComment::getUserId).collect(Collectors.toSet());
        Map<Long, SysUser> userMap = userMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(SysUser::getId, u -> u));
        List<CommentVO> result = new ArrayList<>();
        for (CourseComment c : list) {
            CommentVO vo = new CommentVO();
            BeanUtils.copyProperties(c, vo);
            SysUser u = userMap.get(c.getUserId());
            if (u != null) {
                vo.setNickname(u.getNickname());
                vo.setAvatar(u.getAvatar());
            }
            result.add(vo);
        }
        return result;
    }
}
