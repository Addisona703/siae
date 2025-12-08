package com.hngy.siae.media.domain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 审计操作类型枚举
 * 
 * 定义审计日志中支持的操作类型
 * 根据设计文档要求，基础版支持以下操作：
 * - init: 初始化上传
 * - complete: 完成上传
 * - download: 下载文件
 * - delete: 删除文件
 * - update_policy: 更新访问策略
 *
 * @author SIAE Team
 */
@Getter
public enum AuditAction {
    
    /**
     * 初始化上传
     * 记录文件上传初始化操作
     */
    INIT("init", "初始化上传"),
    
    /**
     * 完成上传
     * 记录文件上传完成操作
     */
    COMPLETE("complete", "完成上传"),
    
    /**
     * 下载文件
     * 记录文件下载操作
     */
    DOWNLOAD("download", "下载文件"),
    
    /**
     * 删除文件
     * 记录文件删除操作
     */
    DELETE("delete", "删除文件"),
    
    /**
     * 更新访问策略
     * 记录访问策略变更操作（PUBLIC <-> PRIVATE）
     */
    UPDATE_POLICY("update_policy", "更新访问策略"),
    
    // 以下为扩展操作类型，基础版暂不使用
    
    /**
     * 生成签名
     * 记录签名URL生成操作
     */
    SIGN("sign", "生成签名"),
    
    /**
     * 恢复文件
     * 记录文件恢复操作
     */
    RESTORE("restore", "恢复文件"),
    
    /**
     * 更新权限
     * 记录权限更新操作
     */
    UPDATE_ACL("update_acl", "更新权限"),
    
    /**
     * 生成预览
     * 记录预览生成操作
     */
    GENERATE_PREVIEW("generate_preview", "生成预览");

    @EnumValue
    @JsonValue
    private final String value;
    private final String description;

    AuditAction(String value, String description) {
        this.value = value;
        this.description = description;
    }

}
