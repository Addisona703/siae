package com.hngy.siae.auth.dto;

import lombok.Data;

/**
 * 角色响应DTO
 * 
 * @author KEYKB
 */
@Data
public class RoleResponse {
    
    /**
     * 角色ID
     */
    private Long roleId;
    
    /**
     * 角色名称
     */
    private String name;
    
    /**
     * 角色编码
     */
    private String code;
    
    /**
     * 角色描述
     */
    private String description;
    
    /**
     * 状态：0禁用，1启用
     */
    private Integer status;
} 