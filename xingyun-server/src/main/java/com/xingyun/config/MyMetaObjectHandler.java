package com.xingyun.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.xingyun.security.LoginUser;
import com.xingyun.security.UserContext;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * 通用字段自动填充：新增时自动写 create_time/update_time/create_by，
 * 更新时自动写 update_time/update_by，业务代码无需手动 set，减少遗漏。
 *
 * @author xingyun
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        Date now = new Date();
        this.strictInsertFill(metaObject, "createTime", Date.class, now);
        this.strictInsertFill(metaObject, "updateTime", Date.class, now);
        Long userId = currentUserId();
        if (userId != null) {
            this.strictInsertFill(metaObject, "createBy", Long.class, userId);
            this.strictInsertFill(metaObject, "updateBy", Long.class, userId);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());
        Long userId = currentUserId();
        if (userId != null) {
            this.strictUpdateFill(metaObject, "updateBy", Long.class, userId);
        }
    }

    private Long currentUserId() {
        LoginUser u = UserContext.get();
        return u == null ? null : u.getId();
    }
}
