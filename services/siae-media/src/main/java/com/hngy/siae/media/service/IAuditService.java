package com.hngy.siae.media.service;

import com.hngy.siae.media.domain.enums.AccessPolicy;
import com.hngy.siae.media.domain.enums.ActorType;
import com.hngy.siae.media.domain.enums.AuditAction;

import java.util.Map;

/**
 * 审计日志服务接口
 * 
 * 定义审计日志记录的核心方法
 * 所有文件操作都应该记录审计日志，用于：
 * - 安全审计和合规性
 * - 操作追踪和问题排查
 * - 统计分析和报表生成
 * 
 * 审计日志记录采用异步方式，不影响主业务流程
 * 
 * Requirements: 2.1
 *
 * @author SIAE Team
 */
public interface IAuditService {

    /**
     * 记录审计日志（通用方法）
     * 
     * 异步记录审计日志，不阻塞主业务流程
     * 如果记录失败，只记录错误日志，不影响业务
     * 
     * @param fileId 文件ID（可选，某些操作可能不关联具体文件）
     * @param tenantId 租户ID
     * @param actorType 操作者类型（service/user/system）
     * @param actorId 操作者ID
     * @param action 操作类型
     * @param ip IP地址（可选）
     * @param userAgent 用户代理（可选）
     * @param metadata 元数据（可选，存储操作相关的额外信息）
     * 
     * Requirements: 2.1
     */
    void log(String fileId, String tenantId, ActorType actorType, String actorId,
             AuditAction action, String ip, String userAgent, Map<String, Object> metadata);

    /**
     * 记录上传初始化审计
     * 
     * 记录文件上传初始化操作
     * 元数据应包含：文件名、文件大小、MIME类型、访问策略等
     * 
     * @param fileId 文件ID
     * @param tenantId 租户ID
     * @param actorId 操作者ID（通常是用户ID）
     * @param metadata 元数据（文件信息）
     * 
     * Requirements: 2.1
     */
    void logUploadInit(String fileId, String tenantId, String actorId, Map<String, Object> metadata);

    /**
     * 记录上传完成审计
     * 
     * 记录文件上传完成操作
     * 元数据应包含：文件ID、存储路径、文件大小等
     * 
     * @param fileId 文件ID
     * @param tenantId 租户ID
     * @param actorId 操作者ID（通常是用户ID）
     * @param metadata 元数据（上传结果信息）
     * 
     * Requirements: 2.1
     */
    void logUploadComplete(String fileId, String tenantId, String actorId, Map<String, Object> metadata);

    /**
     * 记录文件删除审计
     * 
     * 记录文件删除操作
     * 元数据应包含：文件名、文件大小、删除原因等
     * 
     * @param fileId 文件ID
     * @param tenantId 租户ID
     * @param actorId 操作者ID（通常是用户ID）
     * @param metadata 元数据（删除信息）
     * 
     * Requirements: 2.1
     */
    void logFileDelete(String fileId, String tenantId, String actorId, Map<String, Object> metadata);

    /**
     * 记录访问策略更新审计
     * 
     * 记录文件访问策略变更操作（PUBLIC <-> PRIVATE）
     * 元数据应包含：旧策略、新策略、变更原因等
     * 
     * @param fileId 文件ID
     * @param tenantId 租户ID
     * @param actorId 操作者ID（通常是用户ID）
     * @param oldPolicy 旧的访问策略
     * @param newPolicy 新的访问策略
     * 
     * Requirements: 2.1
     */
    void logAccessPolicyUpdate(String fileId, String tenantId, String actorId, 
                               AccessPolicy oldPolicy, AccessPolicy newPolicy);

    /**
     * 记录文件下载审计
     * 
     * 记录文件下载操作
     * 元数据应包含：文件名、下载方式等
     * 
     * @param fileId 文件ID
     * @param tenantId 租户ID
     * @param actorId 操作者ID（通常是用户ID）
     * @param ip IP地址
     * @param userAgent 用户代理
     * @param metadata 元数据（下载信息）
     * 
     * Requirements: 2.1
     */
    void logFileDownload(String fileId, String tenantId, String actorId, 
                        String ip, String userAgent, Map<String, Object> metadata);

    /**
     * 记录文件更新审计
     * 
     * 记录文件元数据更新操作
     * 元数据应包含：变更的字段、旧值、新值等
     * 
     * @param fileId 文件ID
     * @param tenantId 租户ID
     * @param actorId 操作者ID（通常是用户ID）
     * @param metadata 元数据（更新信息）
     * 
     * Requirements: 2.1
     */
    void logFileUpdate(String fileId, String tenantId, String actorId, Map<String, Object> metadata);

    /**
     * 记录文件恢复审计
     * 
     * 记录文件恢复操作（从已删除状态恢复）
     * 元数据应包含：文件名、文件大小、恢复原因等
     * 
     * @param fileId 文件ID
     * @param tenantId 租户ID
     * @param actorId 操作者ID（通常是用户ID）
     * @param metadata 元数据（恢复信息）
     * 
     * Requirements: 2.1
     */
    void logFileRestore(String fileId, String tenantId, String actorId, Map<String, Object> metadata);
}
