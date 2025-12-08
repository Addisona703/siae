package com.hngy.siae.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.siae.media.domain.entity.AuditLog;
import com.hngy.siae.media.domain.enums.ActorType;
import com.hngy.siae.media.domain.enums.AuditAction;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志数据访问接口
 * 
 * 继承 MyBatis-Plus 的 BaseMapper，提供基础的 CRUD 操作
 * 
 * 自定义查询方法：
 * - 按文件查询审计日志
 * - 按租户和操作类型查询审计日志
 * - 按时间范围查询审计日志
 * - 按操作者查询审计日志
 * 
 * Requirements: 2.1
 *
 * @author SIAE Team
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {

    /**
     * 按文件查询审计日志（支持分页）
     * 
     * 按发生时间倒序排列
     * 用于查看文件的操作历史
     * 
     * @param page 分页对象
     * @param fileId 文件ID
     * @return 分页结果
     * 
     * Requirements: 2.1
     */
    @Select("SELECT * FROM audit_logs " +
            "WHERE file_id = #{fileId} " +
            "ORDER BY occurred_at DESC")
    IPage<AuditLog> selectByFileId(Page<AuditLog> page, @Param("fileId") String fileId);

    /**
     * 按租户查询审计日志（支持分页）
     * 
     * 按发生时间倒序排列
     * 
     * @param page 分页对象
     * @param tenantId 租户ID
     * @return 分页结果
     * 
     * Requirements: 2.1
     */
    @Select("SELECT * FROM audit_logs " +
            "WHERE tenant_id = #{tenantId} " +
            "ORDER BY occurred_at DESC")
    IPage<AuditLog> selectByTenantId(Page<AuditLog> page, @Param("tenantId") String tenantId);

    /**
     * 按租户和操作类型查询审计日志（支持分页）
     * 
     * 按发生时间倒序排列
     * 用于统计和分析特定类型的操作
     * 
     * @param page 分页对象
     * @param tenantId 租户ID
     * @param action 操作类型
     * @return 分页结果
     * 
     * Requirements: 2.1
     */
    @Select("SELECT * FROM audit_logs " +
            "WHERE tenant_id = #{tenantId} AND action = #{action} " +
            "ORDER BY occurred_at DESC")
    IPage<AuditLog> selectByTenantIdAndAction(Page<AuditLog> page, 
                                               @Param("tenantId") String tenantId, 
                                               @Param("action") AuditAction action);

    /**
     * 按操作类型查询审计日志（支持分页）
     * 
     * @param page 分页对象
     * @param action 操作类型
     * @return 分页结果
     * 
     * Requirements: 2.1
     */
    @Select("SELECT * FROM audit_logs " +
            "WHERE action = #{action} " +
            "ORDER BY occurred_at DESC")
    IPage<AuditLog> selectByAction(Page<AuditLog> page, @Param("action") AuditAction action);

    /**
     * 按时间范围查询审计日志（支持分页）
     * 
     * 用于生成时间段内的操作报告
     * 
     * @param page 分页对象
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分页结果
     * 
     * Requirements: 2.1
     */
    @Select("SELECT * FROM audit_logs " +
            "WHERE occurred_at BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY occurred_at DESC")
    IPage<AuditLog> selectByTimeRange(Page<AuditLog> page, 
                                      @Param("startTime") LocalDateTime startTime, 
                                      @Param("endTime") LocalDateTime endTime);

    /**
     * 按租户和时间范围查询审计日志（支持分页）
     * 
     * @param page 分页对象
     * @param tenantId 租户ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分页结果
     * 
     * Requirements: 2.1
     */
    @Select("SELECT * FROM audit_logs " +
            "WHERE tenant_id = #{tenantId} " +
            "AND occurred_at BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY occurred_at DESC")
    IPage<AuditLog> selectByTenantIdAndTimeRange(Page<AuditLog> page, 
                                                  @Param("tenantId") String tenantId, 
                                                  @Param("startTime") LocalDateTime startTime, 
                                                  @Param("endTime") LocalDateTime endTime);

    /**
     * 按操作者查询审计日志（支持分页）
     * 
     * 用于追踪特定用户或服务的操作
     * 
     * @param page 分页对象
     * @param actorType 操作者类型
     * @param actorId 操作者ID
     * @return 分页结果
     * 
     * Requirements: 2.1
     */
    @Select("SELECT * FROM audit_logs " +
            "WHERE actor_type = #{actorType} AND actor_id = #{actorId} " +
            "ORDER BY occurred_at DESC")
    IPage<AuditLog> selectByActor(Page<AuditLog> page, 
                                   @Param("actorType") ActorType actorType, 
                                   @Param("actorId") String actorId);

    /**
     * 按租户、操作类型和时间范围查询审计日志（支持分页）
     * 
     * 组合查询，用于详细的审计分析
     * 
     * @param page 分页对象
     * @param tenantId 租户ID
     * @param action 操作类型
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分页结果
     * 
     * Requirements: 2.1
     */
    @Select("SELECT * FROM audit_logs " +
            "WHERE tenant_id = #{tenantId} " +
            "AND action = #{action} " +
            "AND occurred_at BETWEEN #{startTime} AND #{endTime} " +
            "ORDER BY occurred_at DESC")
    IPage<AuditLog> selectByTenantIdAndActionAndTimeRange(Page<AuditLog> page, 
                                                           @Param("tenantId") String tenantId, 
                                                           @Param("action") AuditAction action, 
                                                           @Param("startTime") LocalDateTime startTime, 
                                                           @Param("endTime") LocalDateTime endTime);

    /**
     * 统计租户的操作次数（按操作类型）
     * 
     * 用于生成操作统计报告
     * 
     * @param tenantId 租户ID
     * @param action 操作类型
     * @return 操作次数
     * 
     * Requirements: 2.1
     */
    @Select("SELECT COUNT(*) FROM audit_logs " +
            "WHERE tenant_id = #{tenantId} AND action = #{action}")
    long countByTenantIdAndAction(@Param("tenantId") String tenantId, @Param("action") AuditAction action);

    /**
     * 统计时间范围内的操作次数
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 操作次数
     * 
     * Requirements: 2.1
     */
    @Select("SELECT COUNT(*) FROM audit_logs " +
            "WHERE occurred_at BETWEEN #{startTime} AND #{endTime}")
    long countByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 查询文件的最近一次操作
     * 
     * 用于快速获取文件的最新操作记录
     * 
     * @param fileId 文件ID
     * @return 最近的审计日志
     * 
     * Requirements: 2.1
     */
    @Select("SELECT * FROM audit_logs " +
            "WHERE file_id = #{fileId} " +
            "ORDER BY occurred_at DESC " +
            "LIMIT 1")
    AuditLog selectLatestByFileId(@Param("fileId") String fileId);

    /**
     * 按IP地址查询审计日志（支持分页）
     * 
     * 用于安全审计，追踪特定IP的操作
     * 
     * @param page 分页对象
     * @param ip IP地址
     * @return 分页结果
     * 
     * Requirements: 2.1
     */
    @Select("SELECT * FROM audit_logs " +
            "WHERE ip = #{ip} " +
            "ORDER BY occurred_at DESC")
    IPage<AuditLog> selectByIp(Page<AuditLog> page, @Param("ip") String ip);

    /**
     * 查询指定时间之前的审计日志（用于归档或清理）
     * 
     * @param before 时间阈值
     * @return 审计日志列表
     * 
     * Requirements: 2.1
     */
    @Select("SELECT * FROM audit_logs " +
            "WHERE occurred_at < #{before} " +
            "ORDER BY occurred_at ASC")
    List<AuditLog> selectBeforeTime(@Param("before") LocalDateTime before);
}
