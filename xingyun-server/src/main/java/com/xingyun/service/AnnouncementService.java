package com.xingyun.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingyun.dto.AnnouncementDTO;
import com.xingyun.entity.Announcement;
import com.xingyun.mapper.AnnouncementMapper;
import com.xingyun.security.UserContext;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/** 公告服务 */
@Service
public class AnnouncementService {

    @Resource
    private AnnouncementMapper announcementMapper;

    /** 已发布公告（前台展示） */
    public List<Announcement> published(Integer type) {
        LambdaQueryWrapper<Announcement> w = new LambdaQueryWrapper<Announcement>()
                .eq(Announcement::getStatus, 1).orderByDesc(Announcement::getPublishTime);
        if (type != null) {
            w.eq(Announcement::getType, type);
        }
        return announcementMapper.selectList(w);
    }

    /** 后台全部公告 */
    public List<Announcement> adminList() {
        return announcementMapper.selectList(new LambdaQueryWrapper<Announcement>()
                .orderByDesc(Announcement::getId));
    }

    public void save(AnnouncementDTO dto) {
        Announcement a = new Announcement();
        BeanUtils.copyProperties(dto, a);
        a.setPublisherId(UserContext.getUserId());
        if (a.getStatus() != null && a.getStatus() == 1) {
            a.setPublishTime(new Date());
        }
        if (dto.getId() == null) {
            announcementMapper.insert(a);
        } else {
            announcementMapper.updateById(a);
        }
    }

    public void delete(Long id) {
        announcementMapper.deleteById(id);
    }
}
