package com.hngy.siae.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 网关用户信息传递对象
 * 仅包含基础用户信息，权限查询在服务层进行
 *
 * @author KEYKB
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayUserInfo {
    /**
     * 用户ID - 用于后续权限查询
     */
    private Long userId;

    /**
     * 用户名 - 用于日志和显示
     */
    private String username;

    /**
     * JWT过期时间 - 用于验证token是否仍然有效
     */
    private Long jwtExpireTime;

    /**
     * 网关处理时间戳 - 用于验证信息传递的时效性
     */
    private Long gatewayTimestamp;
}