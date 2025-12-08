package com.hngy.siae.media.service;

import java.util.List;

/**
 * 对象存储服务接口
 * 定义与对象存储（MinIO）交互的所有操作
 * 
 * <p>职责：
 * <ul>
 *   <li>生成预签名 URL（上传/下载）</li>
 *   <li>生成公开访问 URL</li>
 *   <li>管理分片上传</li>
 *   <li>删除对象</li>
 * </ul>
 * 
 * @author SIAE Team
 */
public interface StorageService {

    /**
     * 生成预签名上传 URL
     * 用于客户端直接上传文件到对象存储
     * 
     * @param bucket 存储桶名称
     * @param objectKey 对象键（存储路径）
     * @param expirySeconds 过期时间（秒）
     * @return 预签名上传 URL
     */
    String generatePresignedUploadUrl(String bucket, String objectKey, int expirySeconds);

    /**
     * 生成预签名下载 URL
     * 用于私有文件的临时访问
     * 
     * @param bucket 存储桶名称
     * @param objectKey 对象键（存储路径）
     * @param expirySeconds 过期时间（秒）
     * @return 预签名下载 URL
     */
    String generatePresignedDownloadUrl(String bucket, String objectKey, int expirySeconds);

    /**
     * 生成公开访问 URL
     * 用于公开文件的永久访问，不带签名
     * 
     * @param bucket 存储桶名称
     * @param objectKey 对象键（存储路径）
     * @return 公开访问 URL（永久有效）
     */
    String generatePublicUrl(String bucket, String objectKey);

    /**
     * 初始化分片上传
     * 返回上传会话 ID，用于后续分片上传
     * 
     * @param bucket 存储桶名称
     * @param objectKey 对象键（存储路径）
     * @return 上传会话 ID（uploadId）
     */
    String initMultipartUpload(String bucket, String objectKey);

    /**
     * 生成分片上传预签名 URL
     * 为每个分片生成独立的上传 URL
     * 
     * @param bucket 存储桶名称
     * @param objectKey 对象键（存储路径）
     * @param uploadId 上传会话 ID
     * @param partNumber 分片编号（从 1 开始）
     * @param expirySeconds 过期时间（秒）
     * @return 分片上传预签名 URL
     */
    String generatePresignedPartUploadUrl(String bucket, String objectKey, String uploadId, 
                                          int partNumber, int expirySeconds);

    /**
     * 完成分片上传
     * 合并所有分片为完整文件
     * 
     * @param bucket 存储桶名称
     * @param objectKey 对象键（存储路径）
     * @param uploadId 上传会话 ID
     * @param parts 分片信息列表（包含 partNumber 和 ETag）
     */
    void completeMultipartUpload(String bucket, String objectKey, String uploadId, List<PartETag> parts);

    /**
     * 中断分片上传
     * 清理已上传的分片
     * 
     * @param bucket 存储桶名称
     * @param objectKey 对象键（存储路径）
     * @param uploadId 上传会话 ID
     */
    void abortMultipartUpload(String bucket, String objectKey, String uploadId);

    /**
     * 删除对象
     * 从对象存储中删除文件
     * 
     * @param bucket 存储桶名称
     * @param objectKey 对象键（存储路径）
     */
    void deleteObject(String bucket, String objectKey);

    /**
     * 批量生成分片上传预签名 URL（并行生成，提升性能）
     * 
     * @param bucket 存储桶名称
     * @param objectKey 对象键（存储路径）
     * @param uploadId 上传会话 ID
     * @param totalParts 总分片数
     * @param expirySeconds 过期时间（秒）
     * @return 分片上传预签名 URL 列表（按分片编号排序）
     */
    List<String> batchGeneratePresignedPartUploadUrls(String bucket, String objectKey, String uploadId,
                                                       int totalParts, int expirySeconds);

    /**
     * 分片 ETag 信息
     * 用于完成分片上传时验证分片完整性
     */
    record PartETag(int partNumber, String etag) {
    }
}
