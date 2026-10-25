package com.xingyun.common;

/**
 * 全局常量。把魔法值统一收敛在这里，避免代码里到处写字符串。
 *
 * @author xingyun
 */
public class Constants {

    private Constants() {
    }

    /** Redis key 统一前缀，便于在 Redis 客户端中识别 */
    public static final String REDIS_PREFIX = "xingyun:";

    /** 登录 token 在 Redis 中的缓存前缀 */
    public static final String REDIS_TOKEN = REDIS_PREFIX + "token:";

    /** 视频防盗链签名缓存前缀 */
    public static final String REDIS_VIDEO_SIGN = REDIS_PREFIX + "video:sign:";

    /** 验证码/短信等临时缓存前缀 */
    public static final String REDIS_CODE = REDIS_PREFIX + "code:";

    /** 角色编码：学员 */
    public static final String ROLE_STUDENT = "STUDENT";
    /** 角色编码：讲师 */
    public static final String ROLE_TEACHER = "TEACHER";
    /** 角色编码：管理员 */
    public static final String ROLE_ADMIN = "ADMIN";

    /** 用户类型 */
    public static final int TYPE_STUDENT = 1;
    public static final int TYPE_TEACHER = 2;
    public static final int TYPE_ADMIN = 3;

    /** 付费课程免费试看的课时数量（前 3 节） */
    public static final int FREE_TRIAL_COUNT = 3;

    /** 学习进度达到该百分比即视为学完，触发证书生成 */
    public static final int FINISH_PROGRESS = 100;
}
