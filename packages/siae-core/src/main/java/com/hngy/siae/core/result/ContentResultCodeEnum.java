package com.hngy.siae.core.result;

import lombok.Getter;

/**
 * 内容模块错误码枚举
 * 错误码范围：30000-39999
 *
 * @author KEYKB
 */
@Getter
public enum ContentResultCodeEnum implements IResultCode {

    // ========== 审核相关 30000-30099 ==========
    AUDIT_ALREADY_EXISTS(30001, "审核请求已存在，不能重复请求"),
    AUDIT_SUBMIT_FAILED(30002, "提交审核请求失败"),
    AUDIT_NOT_FOUND(30003, "这条审核申请不存在"),
    AUDIT_ALREADY_HANDLED(30004, "审核已被处理，请勿重复审核"),
    AUDIT_HANDLE_FAILED(30005, "处理审核失败，数据可能已被其他用户修改，请刷新后重试"),
    AUDIT_RECORD_NOT_FOUND(30006, "未找到相关审核记录"),
    AUDIT_QUERY_PARAM_NULL(30007, "查询参数不能为空"),
    AUDIT_TARGET_ID_NULL(30008, "目标对象ID不能为空"),
    AUDIT_TARGET_TYPE_NULL(30009, "目标对象类型不能为空"),
    AUDIT_VIEW_NO_PERMISSION(30010, "无权查看审核信息，非管理员只能查看自己的审核记录"),

    // ========== 内容相关 30100-30199 ==========
    CONTENT_NOT_FOUND(30100, "内容不存在"),
    CONTENT_UPDATE_STATUS_FAILED(30101, "更新内容状态失败"),
    CONTENT_DUPLICATE_SUBMIT(30102, "请勿重复提交相同内容"),
    CONTENT_TYPE_NOT_SUPPORTED(30103, "不支持的内容类型"),
    CONTENT_NOT_EXISTS(30104, "这个内容不存在无法更新"),
    CONTENT_INSERT_FAILED(30105, "内容插入失败"),
    CONTENT_UPDATE_FAILED(30106, "内容更新失败"),
    CONTENT_DELETE_INVALID_OPERATION(30107, "无效操作"),
    CONTENT_TRASH_FAILED(30108, "放入回收站操作失败"),
    CONTENT_PERMANENT_DELETE_FAILED(30109, "永久删除操作失败"),
    CONTENT_DELETE_PERMISSION_DENIED(30110, "无权删除该内容，只能删除自己创建的内容"),

    // ========== 评论相关 30200-30299 ==========
    COMMENT_NOT_FOUND(30200, "评论不存在"),
    COMMENT_UPDATE_STATUS_FAILED(30201, "更新评论状态失败"),
    COMMENT_CREATE_FAILED(30202, "评论创建失败"),
    COMMENT_UPDATE_FAILED(30203, "评论更新失败"),
    COMMENT_DELETE_FAILED(30204, "删除评论失败"),
    COMMENT_DELETE_NO_PERMISSION(30205, "无权删除该评论，只能删除自己创建的评论或自己内容下的评论"),
    COMMENT_UPDATE_NO_PERMISSION(30206, "无权更新该评论，只能更新自己创建的评论"),

    // ========== 分类相关 30300-30399 ==========
    CATEGORY_NAME_EXISTS(30300, "分类名称已存在"),
    CATEGORY_CREATE_FAILED(30301, "新增分类失败"),
    CATEGORY_NOT_FOUND(30302, "分类不存在"),
    CATEGORY_NAME_OR_CODE_EXISTS(30303, "分类名称或编码已存在"),
    CATEGORY_UPDATE_FAILED(30304, "更新分类失败"),
    CATEGORY_HAS_CHILDREN(30305, "该分类下存在子分类，无法删除"),
    CATEGORY_DELETE_FAILED(30306, "删除分类失败"),
    CATEGORY_UPDATE_STATUS_FAILED(30307, "分类状态更新失败"),

    // ========== 标签相关 30400-30499 ==========
    TAG_ALREADY_EXISTS(30400, "标签已存在"),
    TAG_SAVE_FAILED(30401, "标签保存失败，请重试"),
    TAG_NOT_FOUND(30402, "标签不存在"),
    TAG_UPDATE_FAILED(30403, "更新标签失败"),
    TAG_DELETE_FAILED(30404, "删除标签失败"),
    TAG_PARTIAL_NOT_EXISTS(30405, "部分标签不存在"),
    TAG_RELATION_INSERT_FAILED(30406, "批量插入标签关系失败"),
    TAG_RELATION_DELETE_FAILED(30407, "标签-内容关联删除错误"),

    // ========== 统计相关 30500-30599 ==========
    STATISTICS_CREATE_FAILED(30500, "创建统计表失败"),
    STATISTICS_NOT_FOUND(30501, "统计表不存在"),
    STATISTICS_UPDATE_FAILED(30502, "修改统计表信息失败"),

    // ========== 交互相关 30600-30699 ==========
    INTERACTION_UPDATE_STATUS_FAILED(30600, "更新状态失败"),

    // ========== 收藏相关 30700-30799 ==========
    FAVORITE_FOLDER_NAME_EXISTS(30700, "收藏夹名称已存在"),
    FAVORITE_FOLDER_CREATE_FAILED(30701, "创建收藏夹失败"),
    FAVORITE_FOLDER_NOT_FOUND(30702, "收藏夹不存在"),
    FAVORITE_FOLDER_DELETED(30703, "收藏夹已被删除"),
    FAVORITE_FOLDER_UPDATE_FAILED(30704, "更新收藏夹失败"),
    FAVORITE_FOLDER_DEFAULT_CANNOT_DELETE(30705, "默认收藏夹不能删除"),
    FAVORITE_FOLDER_DELETE_FAILED(30706, "删除收藏夹失败"),
    FAVORITE_FOLDER_NO_PERMISSION(30707, "无权操作该收藏夹"),
    FAVORITE_ITEM_ALREADY_EXISTS(30710, "该内容已在此收藏夹中"),
    FAVORITE_ITEM_ADD_FAILED(30711, "添加收藏失败"),
    FAVORITE_ITEM_NOT_FOUND(30712, "收藏不存在"),
    FAVORITE_ITEM_UPDATE_FAILED(30713, "更新收藏失败"),
    FAVORITE_ITEM_REMOVE_FAILED(30714, "取消收藏失败"),
    FAVORITE_DEFAULT_FOLDER_CREATE_FAILED(30715, "创建默认收藏夹失败"),

    // ========== 内容详情相关 30800-30899 ==========
    CONTENT_DETAIL_NOT_FOUND(30800, "获取详情失败，该内容详情不存在"),
    ARTICLE_DETAIL_INSERT_FAILED(30801, "文章详情插入失败"),
    ARTICLE_DETAIL_UPDATE_FAILED(30802, "文章详情更新失败"),
    FILE_DETAIL_INSERT_FAILED(30803, "文件详情插入失败"),
    FILE_DETAIL_UPDATE_FAILED(30804, "文件详情更新失败"),
    NOTE_DETAIL_INSERT_FAILED(30805, "笔记详情插入失败"),
    NOTE_DETAIL_UPDATE_FAILED(30806, "笔记详情更新失败"),
    QUESTION_DETAIL_INSERT_FAILED(30807, "问答详情插入失败"),
    QUESTION_DETAIL_UPDATE_FAILED(30808, "问答详情更新失败"),
    VIDEO_DETAIL_INSERT_FAILED(30809, "视频详情插入失败"),
    VIDEO_DETAIL_UPDATE_FAILED(30810, "视频详情更新失败"),

    // ========== 类型相关 30900-30999 ==========
    UNKNOWN_TYPE(30900, "未知的类型");

    private final Integer code;
    private final String message;

    ContentResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
