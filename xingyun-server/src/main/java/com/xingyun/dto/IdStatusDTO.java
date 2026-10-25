package com.xingyun.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/** 通用「ID + 状态」操作参数（上下架、启停、屏蔽等） */
@Data
public class IdStatusDTO {

    @NotNull(message = "ID不能为空")
    private Long id;

    @NotNull(message = "状态不能为空")
    private Integer status;
}
