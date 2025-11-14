package com.hngy.siae.user.feign.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 批量获取文件URL响应
 *
 * @author SIAE Team
 */
@Data
public class BatchUrlResponse {

    /**
     * 文件ID到URL的映射
     */
    private Map<String, String> urls;

    /**
     * URL过期时间
     */
    private LocalDateTime expiresAt;

    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failedCount;

}
