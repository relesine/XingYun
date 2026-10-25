package com.xingyun.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xingyun.common.BusinessException;
import com.xingyun.common.Constants;
import com.xingyun.entity.Course;
import com.xingyun.entity.CourseLesson;
import com.xingyun.entity.VideoResource;
import com.xingyun.mapper.CourseLessonMapper;
import com.xingyun.mapper.VideoResourceMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 视频服务：核心是「防盗链签名」机制。
 * 流程：前端请求签名 -> 后端校验登录与购买/试看权限 -> 发一个 5 分钟有效的随机 sign ->
 *      前端拿 sign 请求视频流 -> 后端校验 sign 有效才输出文件字节。
 * 这样视频真实路径永远不暴露，直接拼链接/外站盗链都会因为没有有效 sign 而被拒绝。
 */
@Service
public class VideoService {

    /** 签名有效期 5 分钟 */
    private static final long SIGN_EXPIRE_SECONDS = 300;

    @Resource
    private VideoResourceMapper videoMapper;
    @Resource
    private CourseLessonMapper lessonMapper;
    @Resource
    private CourseService courseService;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 申请播放签名
     *
     * @param lessonId 课时ID
     * @param userId   当前用户（由拦截器保证已登录）
     * @return sign 与可访问地址
     */
    public Map<String, Object> applySign(Long lessonId, Long userId) {
        CourseLesson lesson = lessonMapper.selectById(lessonId);
        if (lesson == null || lesson.getVideoId() == null) {
            throw new BusinessException("该课时暂无视频");
        }
        Course course = courseService.getCourseEntity(lesson.getCourseId());
        boolean purchased = courseService.hasPurchased(userId, course);
        // 计算课时是第几节（用于前3节试看判断）
        Long sortIndex = lessonMapper.selectCount(new LambdaQueryWrapper<CourseLesson>()
                .eq(CourseLesson::getCourseId, course.getId())
                .le(CourseLesson::getSort, lesson.getSort()));
        int index = sortIndex == null ? 1 : sortIndex.intValue();
        boolean can = courseService.canPlay(course, index, lesson, purchased);
        if (!can) {
            throw new BusinessException(403, "该课时为付费内容，请先购买课程");
        }
        String sign = IdUtil.fastSimpleUUID();
        stringRedisTemplate.opsForValue().set(Constants.REDIS_VIDEO_SIGN + sign,
                lesson.getVideoId() + ":" + userId, SIGN_EXPIRE_SECONDS, TimeUnit.SECONDS);

        Map<String, Object> result = new HashMap<>();
        result.put("sign", sign);
        result.put("videoId", lesson.getVideoId());
        result.put("expireSeconds", SIGN_EXPIRE_SECONDS);
        // 视频流地址（前端拼接 baseURL 使用）
        result.put("streamUrl", "/video/stream?sign=" + sign);
        return result;
    }

    /**
     * 校验签名并返回要播放的视频资源；同时做「同一用户同一视频24h只计一次播放量」。
     */
    public VideoResource verifySign(String sign) {
        String cache = stringRedisTemplate.opsForValue().get(Constants.REDIS_VIDEO_SIGN + sign);
        if (cache == null) {
            throw new BusinessException(403, "视频签名无效或已过期，请重新获取播放地址");
        }
        String[] arr = cache.split(":");
        Long videoId = Long.valueOf(arr[0]);
        Long userId = Long.valueOf(arr[1]);
        VideoResource video = videoMapper.selectById(videoId);
        if (video == null) {
            throw new BusinessException("视频资源不存在");
        }
        // 播放量去重：Redis setIfAbsent（SETNX）实现 24 小时内同用户同视频只 +1
        String playKey = Constants.REDIS_PREFIX + "video:played:" + userId + ":" + videoId;
        Boolean first = stringRedisTemplate.opsForValue().setIfAbsent(playKey, "1", 24, TimeUnit.HOURS);
        if (Boolean.TRUE.equals(first)) {
            VideoResource update = new VideoResource();
            update.setId(videoId);
            update.setPlayCount(video.getPlayCount() == null ? 1 : video.getPlayCount() + 1);
            videoMapper.updateById(update);
            video.setPlayCount(update.getPlayCount());
        }
        return video;
    }

    /** 新增视频资源记录（上传成功后调用） */
    public Long createRecord(VideoResource video) {
        videoMapper.insert(video);
        return video.getId();
    }

    public VideoResource getById(Long id) {
        return videoMapper.selectById(id);
    }

    /** 视频库分页（讲师选视频用，简单返回全部） */
    public java.util.List<VideoResource> listAll() {
        return videoMapper.selectList(new LambdaQueryWrapper<VideoResource>()
                .orderByDesc(VideoResource::getId).last("limit 200"));
    }
}
