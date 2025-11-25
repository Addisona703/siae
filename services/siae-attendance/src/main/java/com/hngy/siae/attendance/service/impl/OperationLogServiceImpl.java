package com.hngy.siae.attendance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.attendance.entity.OperationLog;
import com.hngy.siae.attendance.mapper.OperationLogMapper;
import com.hngy.siae.attendance.service.IOperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 操作日志服务实现
 *
 * @author SIAE Team
 */
@Slf4j
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> 
        implements IOperationLogService {

    /**
     * 异步保存操作日志
     * 
     * <p>使用异步方式保存日志，避免影响主业务流程性能</p>
     * 
     * @param operationLog 操作日志对象
     */
    @Async
    @Override
    public void saveAsync(OperationLog operationLog) {
        try {
            save(operationLog);
            log.debug("操作日志保存成功: type={}, module={}, userId={}", 
                    operationLog.getOperationType(), 
                    operationLog.getOperationModule(), 
                    operationLog.getUserId());
        } catch (Exception e) {
            log.error("操作日志保存失败: {}", operationLog, e);
            // 日志保存失败不应该影响主业务，所以只记录错误日志
        }
    }
}
