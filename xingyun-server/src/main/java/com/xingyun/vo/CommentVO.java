package com.xingyun.vo;

import com.xingyun.entity.CourseComment;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 评论视图：补充评论人昵称头像 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CommentVO extends CourseComment {
    private String nickname;
    private String avatar;
}
