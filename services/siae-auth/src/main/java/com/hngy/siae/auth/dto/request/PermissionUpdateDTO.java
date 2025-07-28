package com.hngy.siae.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 权限更新请求DTO
 * 
 * @author KEYKB
 */
@Data
@Schema(description = "权限更新请求参数")
public class PermissionUpdateDTO {
    
    /**
     * 权限ID
     */
    @NotNull(message = "权限ID不能为空")
    @Schema(description = "权限ID", example = "1")
    private Long id;
    
    /**
     * 权限名称
     */
    @NotBlank(message = "权限名称不能为空")
    @Schema(description = "权限名称", example = "用户管理")
    private String name;
    
    /**
     * 权限编码
     */
    @NotBlank(message = "权限编码不能为空")
    @Schema(description = "权限编码", example = "user:manage")
    private String code;
    
    /**
     * 权限类型：menu菜单、button按钮
     */
    @NotBlank(message = "权限类型不能为空")
    @Schema(description = "权限类型：menu菜单、button按钮", example = "menu")
    private String type;
    
    /**
     * 父权限ID
     */
    @Schema(description = "父权限ID", example = "0")
    private Long parentId;
    
    /**
     * 路由地址
     */
    @Schema(description = "路由地址", example = "/user")
    private String path;
    
    /**
     * 组件路径
     */
    @Schema(description = "组件路径", example = "user/index")
    private String component;
    
    /**
     * 菜单图标
     */
    @Schema(description = "菜单图标", example = "user")
    private String icon;
    
    /**
     * 排序值
     */
    @Schema(description = "排序值", example = "1")
    private Integer sortOrder;

    /**
     * 状态：0禁用，1启用
     */
    @Min(value = 0, message = "状态值只能为0或1")
    @Max(value = 1, message = "状态值只能为0或1")
    @Schema(description = "状态：0禁用，1启用", example = "1")
    private Integer status = 1;
}
