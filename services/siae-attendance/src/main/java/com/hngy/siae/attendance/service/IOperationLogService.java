package com.hngy.siae.attendance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.attendance.entity.OperationLog;

/**
 * 操作日志服务接口
 *
 * @author SIAE Team
 */
public interface IOperationLogService extends IService<OperationLog> {

    /**
     * 异步保存操作日志
     * 
     * @param operationLog 操作日志对象
     */
    void saveAsync(OperationLog operationLog);
}
