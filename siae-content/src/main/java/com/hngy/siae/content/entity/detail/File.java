package com.hngy.siae.content.entity.detail;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 文件详情表
 * @TableName content_file
 */
@Data
public class File {
    /**
     * 主键，自增
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关联的内容ID，外键，指向 content 表
     */
    private Long contentId;

    /**
     * 文件名称
     */
    private String fileName;

    /**
     * 文件存储路径
     */
    private String filePath;

    /**
     * 文件大小，单位：字节
     */
    private Long fileSize;

    /**
     * 文件MIME类型
     */
    private String fileType;

    /**
     * 下载次数
     */
    private Integer downloadCount = 0;

    /**
     * 创建时间，默认当前时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间，默认当前时间，更新时自动刷新
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}