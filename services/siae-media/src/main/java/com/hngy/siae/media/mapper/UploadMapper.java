package com.hngy.siae.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.siae.media.domain.entity.Upload;
import com.hngy.siae.media.domain.enums.UploadStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 上传会话数据访问接口
 * 
 * 继承 MyBatis-Plus 的 BaseMapper，提供基础的 CRUD 操作
 * 
 * 自定义查询方法：
 * - 按状态查询上传会话
 * - 按过期时间查询上传会话（用于清理过期会话）
 * - 按文件ID查询上传会话
 * - 按租户查询上传会话
 * 
 * Requirements: 1.1, 1.6
 *
 * @author SIAE Team
 */
@Mapper
public interface UploadMapper extends BaseMapper<Upload> {

    /**
     * 按状态查询上传会话列表（支持分页）
     * 
     * 按创建时间倒序排列
     * 
     * @param page 分页对象
     * @param status 上传状态
     * @return 分页结果
     * 
     * Requirements: 1.1
     */
    @Select("SELECT * FROM uploads " +
            "WHERE status = #{status} " +
            "ORDER BY created_at DESC")
    IPage<Upload> selectByStatus(Page<Upload> page, @Param("status") UploadStatus status);

    /**
     * 按租户和状态查询上传会话列表（支持分页）
     * 
     * @param page 分页对象
     * @param tenantId 租户ID
     * @param status 上传状态
     * @return 分页结果
     * 
     * Requirements: 1.1
     */
    @Select("SELECT * FROM uploads " +
            "WHERE tenant_id = #{tenantId} AND status = #{status} " +
            "ORDER BY created_at DESC")
    IPage<Upload> selectByTenantIdAndStatus(Page<Upload> page, 
                                            @Param("tenantId") String tenantId, 
                                            @Param("status") UploadStatus status);

    /**
     * 查询已过期的上传会话
     * 
     * 查询条件：expire_at < 当前时间 且 状态不是 completed
     * 用于定时任务清理过期会话
     * 
     * @param now 当前时间
     * @return 过期的上传会话列表
     * 
     * Requirements: 1.6
     */
    @Select("SELECT * FROM uploads " +
            "WHERE expire_at < #{now} " +
            "AND status NOT IN ('completed', 'aborted') " +
            "ORDER BY expire_at ASC")
    List<Upload> selectExpiredUploads(@Param("now") LocalDateTime now);

    /**
     * 查询指定时间之前过期的上传会话（支持分页）
     * 
     * 用于批量清理过期会话
     * 
     * @param page 分页对象
     * @param expireBefore 过期时间阈值
     * @return 分页结果
     * 
     * Requirements: 1.6
     */
    @Select("SELECT * FROM uploads " +
            "WHERE expire_at < #{expireBefore} " +
            "AND status NOT IN ('completed', 'aborted') " +
            "ORDER BY expire_at ASC")
    IPage<Upload> selectExpiredUploadsBefore(Page<Upload> page, @Param("expireBefore") LocalDateTime expireBefore);

    /**
     * 按文件ID查询上传会话
     * 
     * 一个文件可能有多个上传会话（如重试、断点续传）
     * 按创建时间倒序，最新的会话在前
     * 
     * @param fileId 文件ID
     * @return 上传会话列表
     * 
     * Requirements: 1.1
     */
    @Select("SELECT * FROM uploads " +
            "WHERE file_id = #{fileId} " +
            "ORDER BY created_at DESC")
    List<Upload> selectByFileId(@Param("fileId") String fileId);

    /**
     * 按文件ID查询最新的上传会话
     * 
     * @param fileId 文件ID
     * @return 最新的上传会话
     * 
     * Requirements: 1.1
     */
    @Select("SELECT * FROM uploads " +
            "WHERE file_id = #{fileId} " +
            "ORDER BY created_at DESC " +
            "LIMIT 1")
    Upload selectLatestByFileId(@Param("fileId") String fileId);

    /**
     * 按租户查询上传会话列表（支持分页）
     * 
     * @param page 分页对象
     * @param tenantId 租户ID
     * @return 分页结果
     * 
     * Requirements: 1.1
     */
    @Select("SELECT * FROM uploads " +
            "WHERE tenant_id = #{tenantId} " +
            "ORDER BY created_at DESC")
    IPage<Upload> selectByTenantId(Page<Upload> page, @Param("tenantId") String tenantId);

    /**
     * 查询进行中的分片上传会话
     * 
     * 查询条件：multipart=true 且状态为 in_progress
     * 用于监控和管理分片上传
     * 
     * @return 进行中的分片上传会话列表
     * 
     * Requirements: 1.1
     */
    @Select("SELECT * FROM uploads " +
            "WHERE multipart = true " +
            "AND status = 'in_progress' " +
            "ORDER BY created_at DESC")
    List<Upload> selectInProgressMultipartUploads();

    /**
     * 统计租户的上传会话数量（按状态）
     * 
     * @param tenantId 租户ID
     * @param status 上传状态
     * @return 会话数量
     * 
     * Requirements: 1.1
     */
    @Select("SELECT COUNT(*) FROM uploads " +
            "WHERE tenant_id = #{tenantId} AND status = #{status}")
    long countByTenantIdAndStatus(@Param("tenantId") String tenantId, @Param("status") UploadStatus status);

    /**
     * 统计过期的上传会话数量
     * 
     * @param now 当前时间
     * @return 过期会话数量
     * 
     * Requirements: 1.6
     */
    @Select("SELECT COUNT(*) FROM uploads " +
            "WHERE expire_at < #{now} " +
            "AND status NOT IN ('completed', 'aborted')")
    long countExpiredUploads(@Param("now") LocalDateTime now);
}
