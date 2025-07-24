package com.hngy.siae.core.permissions;

/**
 * 内容模块权限常量定义
 *
 * 命名规范：模块:资源:操作（如 content:article:view）
 * 常量命名规范：CONTENT_资源_操作（如 CONTENT_ARTICLE_VIEW）
 *
 * @author KEYKB
 * @date 2025/07/18
 */
public class ContentPermissions {

    // ==================== 内容管理权限 ====================
    /** 发布内容 */
    public static final String CONTENT_PUBLISH = "content:publish";

    /** 编辑内容 */
    public static final String CONTENT_EDIT = "content:edit";

    /** 删除内容 */
    public static final String CONTENT_DELETE = "content:delete";

    /** 查询内容 */
    public static final String CONTENT_QUERY = "content:query";

    /** 查询内容列表 */
    public static final String CONTENT_LIST_VIEW = "content:list:view";

    /** 查询热门内容 */
    public static final String CONTENT_HOT_VIEW = "content:hot:view";

    // ==================== 分类管理权限 ====================
    /** 创建分类 */
    public static final String CONTENT_CATEGORY_CREATE = "content:category:create";

    /** 编辑分类 */
    public static final String CONTENT_CATEGORY_EDIT = "content:category:edit";

    /** 删除分类 */
    public static final String CONTENT_CATEGORY_DELETE = "content:category:delete";

    /** 查询分类 */
    public static final String CONTENT_CATEGORY_VIEW = "content:category:view";

    /** 启用/禁用分类 */
    public static final String CONTENT_CATEGORY_TOGGLE = "content:category:toggle";

    // ==================== 标签管理权限 ====================
    /** 创建标签 */
    public static final String CONTENT_TAG_CREATE = "content:tag:create";

    /** 编辑标签 */
    public static final String CONTENT_TAG_EDIT = "content:tag:edit";

    /** 删除标签 */
    public static final String CONTENT_TAG_DELETE = "content:tag:delete";

    /** 查询标签 */
    public static final String CONTENT_TAG_VIEW = "content:tag:view";

    // ==================== 用户交互权限 ====================
    /** 记录用户行为（点赞、收藏、浏览等） */
    public static final String CONTENT_INTERACTION_RECORD = "content:interaction:record";

    /** 取消用户行为 */
    public static final String CONTENT_INTERACTION_CANCEL = "content:interaction:cancel";

    // ==================== 统计查询权限 ====================
    /** 查看内容统计 */
    public static final String CONTENT_STATISTICS_VIEW = "content:statistics:view";

    /** 更新内容统计 */
    public static final String CONTENT_STATISTICS_UPDATE = "content:statistics:update";

    // ==================== 审核管理权限 ====================
    /** 处理内容审核 */
    public static final String CONTENT_AUDIT_HANDLE = "content:audit:handle";

    /** 查看审核列表 */
    public static final String CONTENT_AUDIT_VIEW = "content:audit:view";

    /** 审核通过 */
    public static final String CONTENT_AUDIT_APPROVE = "content:audit:approve";

    /** 审核拒绝 */
    public static final String CONTENT_AUDIT_REJECT = "content:audit:reject";

    // ==================== 评论管理权限（预留） ====================
    /** 创建评论 */
    public static final String CONTENT_COMMENT_CREATE = "content:comment:create";

    /** 编辑评论 */
    public static final String CONTENT_COMMENT_EDIT = "content:comment:edit";

    /** 删除评论 */
    public static final String CONTENT_COMMENT_DELETE = "content:comment:delete";

    /** 查询评论 */
    public static final String CONTENT_COMMENT_VIEW = "content:comment:view";

}
