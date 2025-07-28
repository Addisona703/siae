package com.hngy.siae.auth.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 权限创建请求DTO
 * 
 * @author KEYKB
 */
@Data
public class PermissionCreateDTO {
    
    /**
     * 权限名称
     */
    @NotBlank(message = "权限名称不能为空")
    private String name;
    
    /**
     * 权限编码
     */
    @NotBlank(message = "权限编码不能为空")
    private String code;
    
    /**
     * 权限类型：menu菜单、button按钮
     */
//    @NotBlank(message = "权限类型不能为空")
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
    private Integer sortOrder = 0;

    /**
     * 状态：0禁用，1启用
     */
    @Min(value = 0, message = "是否启用该权限只能为0或1")
    @Max(value = 1, message = "是否启用该权限只能为0或1")
    private Integer status = 1;
} 