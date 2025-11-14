package com.hngy.siae.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.content.enums.TypeEnum;
import com.hngy.siae.content.dto.request.AuditDTO;
import com.hngy.siae.content.dto.request.AuditQueryDTO;
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
     */
    void handleAudit(Long id, AuditDTO auditDTO);

    /**
     * 获取审核记录分页列表
     *
     * @param pageDTO 分页查询参数
     * @return {@link PageVO }<{@link AuditVO }>
     */
    PageVO<AuditVO> getAuditPage(PageDTO<AuditQueryDTO> pageDTO);

    /**
     * 获取审核记录
     *
     * @param queryDTO 查询参数
     * @return {@link AuditVO }
     */
    AuditVO getAuditRecord(AuditQueryDTO queryDTO);
}
