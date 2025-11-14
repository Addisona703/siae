package com.hngy.siae.user.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量获取文件URL请求
 *
 * @author SIAE Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchUrlRequest {

    /**
     * 文件ID列表
     */
    private List<String> fileIds;

    /**
     * URL过期时间（秒），默认24小时
     */
    private Integer expirySeconds = 86400;

}
