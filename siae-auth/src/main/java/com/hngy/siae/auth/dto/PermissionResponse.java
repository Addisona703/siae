package com.hngy.siae.auth.dto;

import lombok.Data;

/**
 * 权限响应DTO
 * 
 * @author KEYKB
 */
@Data
public class PermissionResponse {
    
    /**
     * 权限ID
     */
    private Long id;
    
    /**
     * 权限名称
     */
    private String name;
    
    /**
     * 权限编码
     */
    private String code;
    
    /**
     * 权限类型：menu菜单、button按钮
     */
    private String type;
    
    /**
     * 父权限ID
     */
    private Long parentId;
    
    /**
     * 路由地址
     */
    private String path;
    
    /**
     * 组件路径
     */
    private String component;
    
    /**
     * 菜单图标
     */
    private String icon;
    
    /**
     * 排序值
     */
    private Integer sortOrder;
    
    /**
     * 状态：0禁用，1启用
     */
    private Integer status;
} 