package com.xingyun.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 视频资源实体。file_path 仅服务端持有，不直接暴露给前端。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("video_resource")
public class VideoResource extends BaseEntity {
    private String videoName;
    private String filePath;
    private Long fileSize;
    private Integer duration;
    private String cover;
    private String md5;
    private Integer playCount;

    @TableField(fill = FieldFill.INSERT)
    private Long createBy;
}
