package com.hngy.siae.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.common.dto.response.PageVO;
import com.hngy.siae.content.common.enums.TypeEnum;
import com.hngy.siae.content.common.enums.status.AuditStatusEnum;
import com.hngy.siae.content.dto.request.AuditDTO;
import com.hngy.siae.content.dto.response.AuditVO;
import com.hngy.siae.content.entity.Audit;

/**
 * 内容审核服务
 *
 * @author KEYKB
 * &#064;date: 2025/05/20
 */
public interface AuditsService extends IService<Audit> {

    /**
     * 提交审核
     *
     * @param auditDTO 提交参数
     * @return {@link Long }
     */
    Long submitAudit(AuditDTO auditDTO);

    /**
     * 处理审核
     *
     * @param id       审核数据id
     * @param auditDTO 审计数据
     * @return {@link Result }<{@link Void }>
     */
    Result<Void> handleAudit(Long id, AuditDTO auditDTO);

    /**
     * 获取审核记录
     *
     * @param page        页码
     * @param pageSize    每页数据量
     * @param targetType  目标类型
     * @param auditStatus 审核状态
     * @return {@link Result }<{@link PageVO }<{@link AuditVO }>>
     */
    Result<PageVO<AuditVO>> getAuditPage(
            Integer page, Integer pageSize,
            TypeEnum targetType, AuditStatusEnum auditStatus);

    /**
     * 获取审计记录
     *
     * @param targetId   目标ID
     * @param targetType 目标类型
     * @return {@link Result }<{@link Audit }>
     */
    Result<AuditVO> getAuditRecord(Long targetId, TypeEnum targetType);
}
