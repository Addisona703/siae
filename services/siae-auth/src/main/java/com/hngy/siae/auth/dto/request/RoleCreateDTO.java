package com.hngy.siae.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 角色创建请求DTO
 * 
 * @author KEYKB
 */
@Data
public class RoleCreateDTO {
    
    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    private String name;
    
    /**
     * 角色编码
     */
    @NotBlank(message = "角色编码不能为空")
    private String code;
    
    /**
     * 角色描述
     */
    private String description;
} 