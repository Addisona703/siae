package com.hngy.siae.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 第三方认证表实体类
 * 存储用户与第三方平台（微信、QQ、GitHub等）的绑定关系
 *
 * @author KEYKB
 */
@Data
@TableName("user_third_party_auth")
public class UserThirdPartyAuth implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 关联到我们系统内的用户ID
     */
    private Long userId;

    /**
     * 第三方平台名称 (如: 'wechat', 'qq')
     */
    private String provider;

    /**
     * 用户在第三方平台的唯一ID (如 openid)
     */
    private String providerUserId;

    /**
     * 用户在第三方平台的昵称 (冗余)
     */
    private String nicknameOnProvider;

    /**
     * 用户在第三方平台的头像URL (冗余)
     */
    private String avatarOnProvider;

    /**
     * 第三方平台的访问令牌 (加密存储)
     */
    private String accessToken;

    /**
     * 绑定时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 