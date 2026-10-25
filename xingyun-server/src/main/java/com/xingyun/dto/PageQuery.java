package com.xingyun.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * 分页查询基础参数（其它查询 DTO 可继承它复用 page/size）。
 */
@Data
public class PageQuery {
    /** 当前页，默认 1 */
    @Min(value = 1, message = "页码最小为1")
    private Long page = 1L;

    /** 每页条数，默认 10，最大 100（防止一次拉全表） */
    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 100, message = "每页条数最大为100")
    private Long size = 10L;
}
