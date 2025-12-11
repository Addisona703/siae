package com.hngy.siae.media.service;

import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.media.domain.dto.file.*;

import java.util.List;
import java.util.Map;

/**
 * 文件服务接口
 * 定义文件管理相关的所有操作
 * 
 * <p>职责：
 * <ul>
 *   <li>文件查询：获取文件信息、查询文件列表</li>
 *   <li>URL 生成：根据访问策略生成不同类型的 URL</li>
 *   <li>文件更新：更新文件元数据、访问策略</li>
 *   <li>文件删除：软删除文件、清理缓存</li>
 * </ul>
 * 
 * @author SIAE Team
 */
public interface IFileService {

    /**
     * 查询文件列表
     * 支持按租户、所有者、标签、状态等条件过滤
     * 自动过滤已删除的文件
     * 
     * @param pageDTO 分页查询参数
     * @return 分页结果
     */
    PageVO<FileInfoVO> queryFiles(PageDTO<FileQueryDTO> pageDTO);

    /**
     * 获取文件详情
     * 
     * @param fileId 文件ID
     * @return 文件信息
     * @throws com.hngy.siae.core.exception.BusinessException 如果文件不存在或已删除
     */
    FileInfoVO getFileById(String fileId);

    /**
     * 获取单个文件的访问 URL
     * 根据文件的访问策略返回不同类型的 URL：
     * - PUBLIC: 返回永久 URL（不带签名）
     * - PRIVATE: 返回临时签名 URL
     * 
     * @param fileId 文件ID
     * @param expirySeconds URL过期时间（秒），仅对私有文件有效
     * @return 文件访问 URL
     */
    String getFileUrl(String fileId, Integer expirySeconds);

    /**
     * 批量获取文件访问 URL
     * 根据文件的访问策略生成不同类型的URL：
     * - PUBLIC: 生成永久公开URL（不带签名）
     * - PRIVATE: 生成临时签名URL
     * 
     * @param request 批量URL请求参数
     * @return 包含URL映射和统计信息的响应对象
     */
    BatchUrlVO batchGetFileUrls(BatchUrlDTO request);

    /**
     * 更新文件元数据
     * 支持更新访问策略、业务标签、ACL、扩展字段
     * 更新访问策略时会清除 URL 缓存
     * 
     * @param fileId 文件ID
     * @param request 更新请求
     * @return 更新后的文件信息
     */
    FileInfoVO updateFile(String fileId, FileUpdateDTO request);

    /**
     * 软删除文件
     * - 设置 deleted_at 字段
     * - 从对象存储删除实际文件
     * - 清除 URL 缓存
     * - 记录审计日志
     * 
     * @param fileId 文件ID
     */
    void deleteFile(String fileId);

    /**
     * 恢复已删除的文件
     * 
     * @param fileId 文件ID
     * @return 恢复后的文件信息
     */
    FileInfoVO restoreFile(String fileId);

    /**
     * 预览文件
     * 直接以 inline 方式输出图片、PDF、文档等可预览文件
     * 
     * @param fileId 文件ID
     * @param response HTTP响应对象
     */
    void previewFile(String fileId, jakarta.servlet.http.HttpServletResponse response);

    /**
     * 批量删除文件
     * 用于其他服务删除关联的媒体文件（如删除内容时清理关联的视频、图片等）
     * 
     * @param fileIds 文件ID列表
     * @return 批量删除结果，包含成功和失败的文件ID
     */
    BatchDeleteVO batchDeleteFiles(List<String> fileIds);

    /**
     * 获取文件字节数据
     * 供内部服务调用，用于获取文件内容（如AI服务分析图片）
     * 
     * @param fileId 文件ID
     * @return 文件字节数组
     */
    byte[] getFileBytes(String fileId);

}
