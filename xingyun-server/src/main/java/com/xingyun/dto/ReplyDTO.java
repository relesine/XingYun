package com.xingyun.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/** 讲师/管理员回复问答参数 */
@Data
public class ReplyDTO {

    @NotNull(message = "问答ID不能为空")
    private Long qaId;

    @NotNull(message = "回复内容不能为空")
    private String replyContent;
}
