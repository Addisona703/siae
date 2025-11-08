package com.hngy.siae.media.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.media.domain.entity.AuditLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审计日志数据访问接口
 *
 * @author SIAE Team
 */
@Mapper
public interface AuditLogRepository extends BaseMapper<AuditLog> {
}
