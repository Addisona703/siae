package com.hngy.siae.media.service;

import com.hngy.siae.media.domain.dto.upload.*;

/**
 * 上传服务接口
 * 定义文件上传流程的所有操作
 * 
 * <p>职责：
 * <ul>
 *   <li>初始化上传（创建文件记录和上传会话）</li>
 *   <li>生成预签名 URL（支持单文件和分片上传）</li>
 *   <li>完成上传（验证并更新文件状态）</li>
 *   <li>刷新上传 URL（重新生成预签名 URL）</li>
 *   <li>中断上传（清理资源）</li>
 *   <li>生成存储路径（根据访问策略）</li>
 * </ul>
 * 
 * <p>核心流程：
 * <pre>
 * 1. 客户端调用 initUpload() 初始化上传
 * 2. 服务端创建文件记录和上传会话
 * 3. 服务端生成预签名 URL 并返回
 * 4. 客户端使用预签名 URL 直接上传到 MinIO
 * 5. 客户端调用 completeUpload() 通知上传完成
 * 6. 服务端验证并更新文件状态
 * </pre>
 * 
 * <p>支持场景：
 * <ul>
 *   <li>小文件单次上传（文件大小 <= 100MB）</li>
 *   <li>大文件分片上传（文件大小 > 100MB）</li>
 *   <li>断点续传（通过刷新 URL）</li>
 *   <li>公开/私有文件（通过访问策略）</li>
 * </ul>
 * 
 * Requirements: 1.1, 1.4, 1.5
 * 
 * @author SIAE Team
 */
public interface UploadService {

    /**
     * 初始化上传
     * 
     * <p>执行步骤：
     * <ol>
     *   <li>验证请求参数（文件名、大小、租户ID等）</li>
     *   <li>创建文件记录（状态为 INIT）</li>
     *   <li>创建上传会话（记录上传配置）</li>
     *   <li>根据访问策略生成存储路径（public/private）</li>
     *   <li>生成预签名 URL（单文件或分片）</li>
     *   <li>返回上传会话信息</li>
     * </ol>
     * 
     * <p>分片上传判断：
     * <ul>
     *   <li>如果请求中指定了 multipart.enabled=true，启用分片上传</li>
     *   <li>如果文件大小 > 100MB，自动启用分片上传</li>
     *   <li>分片大小默认为 10MB，可通过 multipart.partSize 自定义</li>
     * </ul>
     * 
     * <p>存储路径格式：
     * <ul>
     *   <li>PUBLIC: {tenant-id}/public/{timestamp}/{filename}</li>
     *   <li>PRIVATE: {tenant-id}/private/{timestamp}/{filename}</li>
     * </ul>
     * 
     * @param request 上传初始化请求
     * @return 上传初始化响应（包含上传会话ID、文件ID、预签名URL等）
     * @throws IllegalArgumentException 如果请求参数无效
     * @throws StorageException 如果对象存储操作失败
     * 
     * Requirements: 1.1, 1.2, 1.3, 1.5, 7.1, 7.2
     */
    UploadInitVO initUpload(UploadInitDTO request);

    /**
     * 完成上传
     * 
     * <p>执行步骤：
     * <ol>
     *   <li>验证上传会话存在且有效</li>
     *   <li>验证上传会话未过期</li>
     *   <li>如果是分片上传，验证所有分片的 ETag</li>
     *   <li>更新文件状态为 COMPLETED</li>
     *   <li>更新上传会话状态为 completed</li>
     *   <li>记录审计日志</li>
     * </ol>
     * 
     * <p>分片验证：
     * <ul>
     *   <li>验证提交的分片数量与总分片数一致</li>
     *   <li>验证每个分片的 partNumber 连续且从 1 开始</li>
     *   <li>验证每个分片都有有效的 ETag</li>
     * </ul>
     * 
     * <p>事务保证：
     * <ul>
     *   <li>使用 @Transactional 确保数据一致性</li>
     *   <li>如果验证失败，回滚所有数据库操作</li>
     *   <li>如果对象存储操作失败，回滚数据库操作</li>
     * </ul>
     * 
     * @param uploadId 上传会话ID
     * @param request 上传完成请求（包含分片信息）
     * @return 上传完成响应
     * @throws ResourceNotFoundException 如果上传会话不存在
     * @throws IllegalStateException 如果上传会话状态无效或已过期
     * @throws IllegalArgumentException 如果分片信息无效
     * 
     * Requirements: 1.4, 8.3, 8.4
     */
    UploadCompleteVO completeUpload(String uploadId, UploadCompleteDTO request);

    /**
     * 刷新上传 URL
     * 
     * <p>使用场景：
     * <ul>
     *   <li>预签名 URL 即将过期，需要重新生成</li>
     *   <li>断点续传，需要获取新的上传 URL</li>
     *   <li>网络中断后重试上传</li>
     * </ul>
     * 
     * <p>执行步骤：
     * <ol>
     *   <li>验证上传会话存在且有效</li>
     *   <li>验证上传会话未过期</li>
     *   <li>验证上传会话状态为 init 或 in_progress</li>
     *   <li>重新生成预签名 URL</li>
     *   <li>返回新的 URL 和过期时间</li>
     * </ol>
     * 
     * <p>注意事项：
     * <ul>
     *   <li>不会创建新的上传会话，只刷新现有会话的 URL</li>
     *   <li>不会修改文件记录或上传会话的其他信息</li>
     *   <li>如果是分片上传，可以指定需要刷新的分片编号</li>
     * </ul>
     * 
     * @param uploadId 上传会话ID
     * @param request 刷新请求（可选：指定需要刷新的分片编号）
     * @return 刷新响应（包含新的预签名URL和过期时间）
     * @throws ResourceNotFoundException 如果上传会话不存在
     * @throws IllegalStateException 如果上传会话状态无效或已过期
     * 
     * Requirements: 1.1
     */
    UploadRefreshVO refreshUpload(String uploadId, UploadRefreshDTO request);

    /**
     * 中断上传
     * 
     * <p>使用场景：
     * <ul>
     *   <li>用户主动取消上传</li>
     *   <li>上传失败需要清理资源</li>
     *   <li>上传会话过期需要清理</li>
     * </ul>
     * 
     * <p>执行步骤：
     * <ol>
     *   <li>验证上传会话存在</li>
     *   <li>如果是分片上传，调用对象存储中断分片上传</li>
     *   <li>更新上传会话状态为 aborted</li>
     *   <li>更新文件状态为 failed</li>
     *   <li>记录审计日志</li>
     * </ol>
     * 
     * <p>资源清理：
     * <ul>
     *   <li>清理对象存储中已上传的分片</li>
     *   <li>不删除数据库记录（保留审计信息）</li>
     *   <li>标记上传会话为已中止</li>
     * </ul>
     * 
     * @param uploadId 上传会话ID
     * @throws ResourceNotFoundException 如果上传会话不存在
     * 
     * Requirements: 8.5
     */
    void abortUpload(String uploadId);

    /**
     * 生成存储路径
     * 
     * <p>路径格式：
     * <pre>
     * {tenant-id}/{access-policy}/{timestamp}/{filename}
     * </pre>
     * 
     * <p>示例：
     * <ul>
     *   <li>公开文件: tenant-001/public/1699876543210/avatar.jpg</li>
     *   <li>私有文件: tenant-001/private/1699876543210/document.pdf</li>
     * </ul>
     * 
     * <p>路径组成：
     * <ul>
     *   <li>tenant-id: 租户ID，实现租户隔离</li>
     *   <li>access-policy: 访问策略目录（public 或 private）</li>
     *   <li>timestamp: 时间戳（毫秒），确保路径唯一性</li>
     *   <li>filename: 原始文件名（URL 编码）</li>
     * </ul>
     * 
     * <p>特殊处理：
     * <ul>
     *   <li>保留文件扩展名</li>
     *   <li>文件名中的特殊字符进行 URL 编码</li>
     *   <li>使用时间戳确保同名文件不冲突</li>
     * </ul>
     * 
     * @param tenantId 租户ID
     * @param accessPolicy 访问策略（PUBLIC/PRIVATE）
     * @param filename 原始文件名
     * @return 存储路径（storage key）
     * 
     * Requirements: 7.1, 7.2, 7.3, 7.4, 7.5
     */
    String generateStorageKey(String tenantId, com.hngy.siae.media.domain.enums.AccessPolicy accessPolicy, String filename);

    /**
     * 查询上传状态
     * 
     * <p>用于异步合并分片后，前端轮询查询处理结果
     * 
     * <p>返回信息：
     * <ul>
     *   <li>文件ID</li>
     *   <li>当前状态（PROCESSING/COMPLETED/FAILED）</li>
     *   <li>文件访问URL（仅当状态为COMPLETED时有效）</li>
     * </ul>
     * 
     * @param uploadId 上传会话ID
     * @return 上传状态响应
     * @throws ResourceNotFoundException 如果上传会话不存在
     */
    UploadStatusVO getUploadStatus(String uploadId);
}
