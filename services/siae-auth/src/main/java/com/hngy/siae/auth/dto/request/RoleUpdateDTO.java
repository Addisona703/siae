package com.hngy.siae.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 角色更新请求DTO
 * 
 * @author KEYKB
 */
@Data
public class RoleUpdateDTO {
    
    /**
     * 角色名称
     */
    @NotBlank(message = "角色名称不能为空")
    private String name;
    
    /**
     * 角色描述
     */
    private String description;
    
    /**
     * 状态：0禁用，1启用
     */
    private Integer status;
} 