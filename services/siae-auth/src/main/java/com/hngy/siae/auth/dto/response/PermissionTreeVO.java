package com.hngy.siae.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 权限树形结构响应VO
 * 
 * @author SIAE开发团队
 */
@Data
@Schema(description = "权限树形结构")
public class PermissionTreeVO {
    
    /**
     * 权限ID
     */
    @Schema(description = "权限ID", example = "1")
    private Long id;
    
    /**
     * 权限名称
     */
    @Schema(description = "权限名称", example = "系统管理")
    private String name;
    
    /**
     * 权限编码
     */
    @Schema(description = "权限编码", example = "system:manage")
    private String code;
    
    /**
     * 权限类型：menu菜单、button按钮
     */
    @Schema(description = "权限类型", example = "menu")
    private String type;
    
    /**
     * 父权限ID
     */
    @Schema(description = "父权限ID", example = "0")
    private Long parentId;
    
    /**
     * 路由地址
     */
    @Schema(description = "路由地址", example = "/system")
    private String path;
    
    /**
     * 组件路径
     */
    @Schema(description = "组件路径", example = "system/index")
    private String component;
    
    /**
     * 菜单图标
     */
    @Schema(description = "菜单图标", example = "system")
    private String icon;
    
    /**
     * 排序值
     */
    @Schema(description = "排序值", example = "1")
    private Integer sortOrder;
    
    /**
     * 状态：0禁用，1启用
     */
    @Schema(description = "状态：0禁用，1启用", example = "1")
    private Integer status;
    
    /**
     * 层级深度
     */
    @Schema(description = "层级深度，从0开始", example = "0")
    private Integer level;
    
    /**
     * 是否为叶子节点
     */
    @Schema(description = "是否为叶子节点", example = "false")
    private Boolean isLeaf;
    
    /**
     * 子权限列表
     */
    @Schema(description = "子权限列表")
    private List<PermissionTreeVO> children;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2024-01-01 12:00:00")
    private String createAt;
    
    /**
     * 更新时间
     */
    @Schema(description = "更新时间", example = "2024-01-01 12:00:00")
    private String updateAt;
}
