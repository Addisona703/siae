package com.hngy.siae.media.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 下载令牌实体（用于单次使用和IP绑定）
 *
 * @author SIAE Team
 */
@Data
@TableName("download_tokens")
public class DownloadToken {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    private String fileId;

    private String tenantId;

    private String userId;

    private String token;

    private String bindIp;

    private Boolean singleUse;

    private Boolean used;

    private LocalDateTime expiresAt;

    private LocalDateTime createdAt;

    private LocalDateTime usedAt;

}
