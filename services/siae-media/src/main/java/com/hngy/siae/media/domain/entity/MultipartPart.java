package com.hngy.siae.media.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分片上传记录实体
 * 映射到 multipart_parts 表，记录分片上传的每个分片信息
 * 
 * 使用复合主键：(upload_id, part_number)
 * 每个上传会话可以有多个分片记录
 * 
 * 关联关系：
 * - 多对一关联到 Upload 实体（通过 uploadId 外键）
 * - 当上传会话被删除时，相关的分片记录会级联删除
 * 
 * 字段说明：
 * - uploadId: 上传会话ID，外键关联到 uploads 表
 * - partNumber: 分片编号，从1开始
 * - etag: MinIO返回的ETag标识，用于合并分片
 * - size: 分片大小（字节）
 * - checksum: 分片SHA256校验和，用于验证完整性
 * - uploadedAt: 分片上传完成时间
 *
 * @author SIAE Team
 */
@Data
@TableName("multipart_parts")
public class MultipartPart implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 上传会话ID
     * 外键，关联到 uploads 表的 upload_id 字段
     * 与 partNumber 组成复合主键
     */
    @TableField("upload_id")
    private String uploadId;

    /**
     * 分片编号（从1开始）
     * 与 uploadId 组成复合主键
     * 用于标识分片的顺序
     */
    @TableField("part_number")
    private Integer partNumber;

    /**
     * ETag标识
     * MinIO返回的ETag，用于合并分片时验证
     * 格式：32位十六进制字符串（MD5哈希）
     */
    @TableField("etag")
    private String etag;

    /**
     * 分片大小（字节）
     * 记录实际上传的分片大小
     * 最后一个分片可能小于标准分片大小
     */
    @TableField("size")
    private Long size;

    /**
     * 分片校验和
     * SHA256哈希值，用于验证分片完整性
     */
    @TableField("checksum")
    private String checksum;

    /**
     * 上传完成时间
     * 记录分片上传成功的时间戳
     */
    @TableField("uploaded_at")
    private LocalDateTime uploadedAt;

}
