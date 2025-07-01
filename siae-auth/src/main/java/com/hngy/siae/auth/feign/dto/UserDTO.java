package com.hngy.siae.auth.feign.dto;

import lombok.Data;

/**
 * 用户DTO
 * 
 * @author KEYKB
 */
@Data
public class UserDTO {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码(已加密)
     */
    private String password;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 状态：0禁用，1启用
     */
    private Integer status;
} 