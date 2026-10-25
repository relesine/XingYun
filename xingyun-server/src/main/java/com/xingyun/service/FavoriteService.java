package com.xingyun.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingyun.entity.Course;
import com.xingyun.entity.CourseFavorite;
import com.xingyun.mapper.CourseFavoriteMapper;
import com.xingyun.mapper.CourseMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** 课程收藏服务：切换收藏、我的收藏 */
@Service
public class FavoriteService {

    @Resource
    private CourseFavoriteMapper favoriteMapper;
    @Resource
    private CourseMapper courseMapper;

    /** 切换收藏状态，返回当前是否已收藏 */
    public Map<String, Object> toggle(Long userId, Long courseId) {
        CourseFavorite exist = favoriteMapper.selectOne(new LambdaQueryWrapper<CourseFavorite>()
                .eq(CourseFavorite::getUserId, userId).eq(CourseFavorite::getCourseId, courseId));
        boolean nowFavorite;
        if (exist == null) {
            CourseFavorite f = new CourseFavorite();
            f.setUserId(userId);
            f.setCourseId(courseId);
            favoriteMapper.insert(f);
            nowFavorite = true;
        } else {
            favoriteMapper.deleteById(exist.getId());
            nowFavorite = false;
        }
        Map<String, Object> result = new HashMap<>();
        result.put("favorite", nowFavorite);
        return result;
    }

    /** 我的收藏（返回收藏的课程列表） */
    public List<Course> myFavorites(Long userId) {
        List<CourseFavorite> favs = favoriteMapper.selectList(new LambdaQueryWrapper<CourseFavorite>()
                .eq(CourseFavorite::getUserId, userId).orderByDesc(CourseFavorite::getId));
        if (favs.isEmpty()) {
            return List.of();
        }
        Set<Long> ids = favs.stream().map(CourseFavorite::getCourseId).collect(Collectors.toSet());
        return courseMapper.selectBatchIds(ids);
    }

    /** 是否已收藏 */
    public boolean isFavorite(Long userId, Long courseId) {
        if (userId == null) {
            return false;
        }
        Long c = favoriteMapper.selectCount(new LambdaQueryWrapper<CourseFavorite>()
                .eq(CourseFavorite::getUserId, userId).eq(CourseFavorite::getCourseId, courseId));
        return c != null && c > 0;
    }
}
