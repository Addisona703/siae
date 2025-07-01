package com.hngy.siae.user.dto.response;

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
     * 用户名
     */
    private String username;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;

    /**
     * 状态：0禁用，1启用
     */
    private Integer status;
    
    /**
     * 最后登录IP
     */
    private String lastLoginIp;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 