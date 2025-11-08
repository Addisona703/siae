package com.hngy.siae.media.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 分片上传记录实体
 *
 * @author SIAE Team
 */
@Data
@TableName("multipart_parts")
public class MultipartPart {

    private String uploadId;

    private Integer partNumber;

    private String etag;

    private Long size;

    private String checksum;

    private LocalDateTime uploadedAt;

}
