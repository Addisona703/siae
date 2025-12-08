package com.hngy.siae.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.siae.content.dto.response.audit.AuditVO;
import com.hngy.siae.content.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 审核历史记录 Mapper 接口
 * 基础 CRUD 使用 MyBatis-Plus，复杂查询使用 XML
 * 
 * @author Kiro
 * @description 针对表【audit_log(审核历史记录表)】的数据库操作Mapper
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {

    /**
     * 分页查询审核列表（关联内容/评论详情）
     *
     * @param page        分页对象
     * @param targetType  目标类型（可选）
     * @param auditStatus 审核状态（可选）
     * @return 审核VO分页结果
     */
    IPage<AuditVO> selectAuditPageWithDetail(Page<AuditVO> page,
                                              @Param("targetType") Integer targetType,
                                              @Param("auditStatus") Integer auditStatus);

    /**
     * 查询指定目标的所有审核历史记录（按时间倒序）
     * Requirements: 4.3
     *
     * @param targetId   目标ID
     * @param targetType 目标类型
     * @return 审核历史记录列表
     */
    List<AuditLog> selectAuditLogsByTarget(@Param("targetId") Long targetId, 
                                            @Param("targetType") Integer targetType);

    /**
     * 查询指定目标的最新审核记录
     * Requirements: 4.4
     *
     * @param targetId   目标ID
     * @param targetType 目标类型
     * @return 最新的审核记录
     */
    AuditLog selectLatestAuditLog(@Param("targetId") Long targetId, 
                                   @Param("targetType") Integer targetType);

    /**
     * 统计指定目标的审核记录数量
     *
     * @param targetId   目标ID
     * @param targetType 目标类型
     * @return 记录数量
     */
    Long countByTarget(@Param("targetId") Long targetId, 
                       @Param("targetType") Integer targetType);
}
