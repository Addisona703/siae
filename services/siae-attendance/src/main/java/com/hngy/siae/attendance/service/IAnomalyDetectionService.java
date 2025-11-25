package com.hngy.siae.attendance.service;

import com.hngy.siae.attendance.dto.request.AnomalyHandleDTO;
import com.hngy.siae.attendance.dto.request.AnomalyQueryDTO;
import com.hngy.siae.attendance.dto.response.AttendanceAnomalyVO;
import com.hngy.siae.attendance.entity.AttendanceRecord;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;

import java.time.LocalDate;

/**
 * 考勤异常检测服务接口
 *
 * @author SIAE Team
 */
public interface IAnomalyDetectionService {

    /**
     * 检测考勤异常
     * 根据考勤规则检测迟到、早退等异常
     *
     * @param record 考勤记录
     */
    void detectAnomalies(AttendanceRecord record);

    /**
     * 自动检测缺勤
     * 定时任务调用，检测指定日期未签到的成员
     *
     * @param date 检测日期
     */
    void autoDetectAbsence(LocalDate date);

    /**
     * 处理考勤异常
     *
     * @param anomalyId 异常ID
     * @param handlerId 处理人ID
     * @param handlerNote 处理说明
     * @param resolved 是否解决
     */
    void handleAnomaly(Long anomalyId, Long handlerId, String handlerNote, Boolean resolved);

    /**
     * 通过请假抑制异常
     *
     * @param anomalyId 异常ID
     * @param leaveRequestId 请假申请ID
     */
    void suppressAnomalyByLeave(Long anomalyId, Long leaveRequestId);

    /**
     * 查询考勤异常详情
     *
     * @param id 异常记录ID
     * @return 考勤异常信息
     */
    AttendanceAnomalyVO getAnomaly(Long id);

    /**
     * 查询个人考勤异常
     *
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageVO<AttendanceAnomalyVO> getMyAnomalies(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 处理考勤异常（使用DTO，自动获取当前用户）
     *
     * @param id 异常记录ID
     * @param dto 处理DTO
     * @return 处理后的异常信息
     */
    AttendanceAnomalyVO handleAnomaly(Long id, AnomalyHandleDTO dto);

    /**
     * 查询个人考勤异常（自动获取当前用户）
     *
     * @param pageDTO 分页查询条件
     * @return 分页结果
     */
    PageVO<AttendanceAnomalyVO> getMyAnomalies(PageDTO<Void> pageDTO);

    /**
     * 查询未处理的考勤异常
     *
     * @param pageDTO 分页查询条件
     * @return 分页结果
     */
    PageVO<AttendanceAnomalyVO> getUnresolvedAnomalies(PageDTO<Void> pageDTO);

    /**
     * 分页查询考勤异常
     *
     * @param pageDTO 分页查询条件
     * @return 分页结果
     */
    PageVO<AttendanceAnomalyVO> pageQuery(PageDTO<AnomalyQueryDTO> pageDTO);
}
