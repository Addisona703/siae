package com.hngy.siae.content.permissions;

/**
 * 内容服务权限常量
 * 定义内容服务相关的权限标识符
 *
 * @author Siae Studio
 */
public final class ContentPermissions {

    private ContentPermissions() {
        // 工具类，禁止实例化
    }

    // ==================== 内容管理权限 ====================
    
    /**
     * 内容发布权限
     */
    public static final String CONTENT_PUBLISH = "content:content:publish";
    
    /**
     * 内容编辑权限
     */
    public static final String CONTENT_EDIT = "content:content:edit";
    
    /**
     * 内容删除权限
     */
    public static final String CONTENT_DELETE = "content:content:delete";
    
    /**
     * 内容查询权限
     */
    public static final String CONTENT_QUERY = "content:content:query";
    
    /**
     * 内容列表查看权限
     */
    public static final String CONTENT_LIST_VIEW = "content:content:list";

    // ==================== 分类管理权限 ====================
    
    /**
     * 分类创建权限
     */
    public static final String CONTENT_CATEGORY_CREATE = "content:category:create";
    
    /**
     * 分类编辑权限
     */
    public static final String CONTENT_CATEGORY_EDIT = "content:category:edit";
    
    /**
     * 分类删除权限
     */
    public static final String CONTENT_CATEGORY_DELETE = "content:category:delete";
    
    /**
     * 分类查看权限
     */
    public static final String CONTENT_CATEGORY_VIEW = "content:category:view";
    
    /**
     * 分类启用/禁用权限
     */
    public static final String CONTENT_CATEGORY_TOGGLE = "content:category:toggle";

    // ==================== 标签管理权限 ====================
    
    /**
     * 标签创建权限
     */
    public static final String CONTENT_TAG_CREATE = "content:tag:create";
    
    /**
     * 标签编辑权限
     */
    public static final String CONTENT_TAG_EDIT = "content:tag:edit";
    
    /**
     * 标签删除权限
     */
    public static final String CONTENT_TAG_DELETE = "content:tag:delete";
    
    /**
     * 标签查看权限
     */
    public static final String CONTENT_TAG_VIEW = "content:tag:view";

    // ==================== 审核管理权限 ====================
    
    /**
     * 审核处理权限
     */
    public static final String CONTENT_AUDIT_HANDLE = "content:audit:handle";
    
    /**
     * 审核查看权限
     */
    public static final String CONTENT_AUDIT_VIEW = "content:audit:view";

    // ==================== 收藏管理权限 ====================
    
    /**
     * 收藏管理权限（创建/更新/删除收藏夹）
     */
    public static final String CONTENT_FAVORITE_MANAGE = "content:favorite:manage";
    
    /**
     * 收藏查看权限
     */
    public static final String CONTENT_FAVORITE_VIEW = "content:favorite:view";
    
    /**
     * 添加收藏权限
     */
    public static final String CONTENT_FAVORITE_ADD = "content:favorite:add";
    
    /**
     * 移除收藏权限
     */
    public static final String CONTENT_FAVORITE_REMOVE = "content:favorite:remove";

    // ==================== 统计管理权限 ====================
    
    /**
     * 统计创建权限
     */
    public static final String CONTENT_STATISTICS_CREATE = "content:statistics:create";
    
    /**
     * 统计查看权限
     */
    public static final String CONTENT_STATISTICS_VIEW = "content:statistics:view";
    
    /**
     * 统计更新权限
     */
    public static final String CONTENT_STATISTICS_UPDATE = "content:statistics:update";
}
