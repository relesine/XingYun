package com.xingyun.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingyun.common.BusinessException;
import com.xingyun.dto.CategoryDTO;
import com.xingyun.entity.Course;
import com.xingyun.entity.CourseCategory;
import com.xingyun.mapper.CourseCategoryMapper;
import com.xingyun.mapper.CourseMapper;
import com.xingyun.vo.CategoryTreeVO;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 课程分类服务：构建两级分类树、分类增删改。
 *
 * @author xingyun
 */
@Service
public class CategoryService {

    @Resource
    private CourseCategoryMapper categoryMapper;
    @Resource
    private CourseMapper courseMapper;

    /** 构建两级分类树（parent_id=0 为一级） */
    public List<CategoryTreeVO> tree() {
        List<CourseCategory> all = categoryMapper.selectList(
                new LambdaQueryWrapper<CourseCategory>().orderByAsc(CourseCategory::getSort));
        List<CategoryTreeVO> roots = all.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0L)
                .map(this::toTreeVO)
                .collect(Collectors.toList());
        Map<Long, List<CourseCategory>> childMap = all.stream()
                .filter(c -> c.getParentId() != null && c.getParentId() != 0L)
                .collect(Collectors.groupingBy(CourseCategory::getParentId));
        for (CategoryTreeVO root : roots) {
            root.setChildren(childMap.getOrDefault(root.getId(), new ArrayList<>()));
        }
        return roots;
    }

    /** 管理端：全部分类（平铺） */
    public List<CourseCategory> listAll() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<CourseCategory>().orderByAsc(CourseCategory::getSort));
    }

    public void save(CategoryDTO dto) {
        CourseCategory category = new CourseCategory();
        category.setId(dto.getId());
        category.setName(dto.getName());
        category.setParentId(dto.getParentId());
        category.setLevel(dto.getParentId() != null && dto.getParentId() != 0L ? 2 : 1);
        category.setIcon(dto.getIcon());
        category.setSort(dto.getSort());
        category.setStatus(dto.getStatus());
        if (dto.getId() == null) {
            categoryMapper.insert(category);
        } else {
            categoryMapper.updateById(category);
        }
    }

    /** 删除分类前校验其下是否还有课程，避免产生“孤儿课程” */
    public void delete(Long id) {
        Long childCount = categoryMapper.selectCount(new LambdaQueryWrapper<CourseCategory>()
                .eq(CourseCategory::getParentId, id));
        if (childCount != null && childCount > 0) {
            throw new BusinessException("该一级分类下还有子分类，无法删除");
        }
        Long courseCount = courseMapper.selectCount(new LambdaQueryWrapper<Course>()
                .eq(Course::getCategoryId, id).or().eq(Course::getCategory2Id, id));
        if (courseCount != null && courseCount > 0) {
            throw new BusinessException("该分类下还有课程，无法删除");
        }
        categoryMapper.deleteById(id);
    }

    private CategoryTreeVO toTreeVO(CourseCategory c) {
        CategoryTreeVO vo = new CategoryTreeVO();
        vo.setId(c.getId());
        vo.setName(c.getName());
        vo.setParentId(c.getParentId());
        vo.setLevel(c.getLevel());
        vo.setIcon(c.getIcon());
        vo.setSort(c.getSort());
        vo.setStatus(c.getStatus());
        return vo;
    }
}
