package com.xingyun.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/** 管理端用户分页查询 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminUserQueryDTO extends PageQuery {
    /** 关键字（账号/昵称） */
    private String keyword;
    /** 用户类型 1学员 2讲师 3管理员 */
    private Integer userType;
    /** 状态 0正常 1禁用 */
    private Integer status;
}
