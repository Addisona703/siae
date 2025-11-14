package com.hngy.siae.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息视图对象
 * 
 * @author KEYKB
 */
@Data
public class UserVO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    private Long id;

    /**
     * 登录名/用户名
     */
    private String username;

    /**
     * 密码（加密后）
     * 仅用于内部服务间调用（如认证服务），不会序列化到 JSON 响应中
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /**
     * 学号
     */
    private String studentId;

    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 头像文件ID
     */
    private String avatarFileId;

    /**
     * 头像访问URL（从Media服务获取）
     */
    private String avatarUrl;

    /**
     * 创建时间
     */
    private LocalDateTime createAt;

    /**
     * 更新时间
     */
    private LocalDateTime updateAt;
} 
