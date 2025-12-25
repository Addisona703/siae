package com.hngy.siae.attendance.service;

import com.hngy.siae.attendance.dto.request.AttendanceQueryDTO;
import com.hngy.siae.attendance.dto.request.CheckInDTO;
import com.hngy.siae.attendance.dto.request.CheckOutDTO;
import com.hngy.siae.attendance.dto.request.FaceCheckInDTO;
import com.hngy.siae.attendance.dto.response.AttendanceRecordDetailVO;
import com.hngy.siae.attendance.dto.response.AttendanceRecordVO;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;

import java.time.LocalDate;

/**
 * 考勤服务接口
 *
 * @author SIAE Team
 */
public interface IAttendanceService {

    /**
     * 签到
     *
     * @param dto 签到请求
     * @return 考勤记录VO
     */
    AttendanceRecordVO checkIn(CheckInDTO dto);

    /**
     * 人脸识别打卡
     *
     * @param dto 人脸识别打卡请求
     * @return 考勤记录VO
     */
    AttendanceRecordVO faceCheckIn(FaceCheckInDTO dto);

    /**
     * 查询今天的签到状态
     * 
     * @return 今天的所有签到记录
     */
    java.util.List<AttendanceRecordVO> getTodayStatus();

    /**
     * 签退
     *
     * @param dto 签退请求
     * @return 考勤记录VO
     */
    AttendanceRecordVO checkOut(CheckOutDTO dto);

    /**
     * 查询考勤记录详情
     *
     * @param id 考勤记录ID
     * @param currentUserId 当前用户ID（用于权限验证）
     * @return 考勤记录详细信息
     */
    AttendanceRecordDetailVO getRecord(Long id, Long currentUserId);

    /**
     * 分页查询考勤记录
     *
     * @param pageDTO 分页查询条件
     * @param currentUserId 当前用户ID（用于数据权限过滤）
     * @param hasListPermission 是否有列表查询权限
     * @return 分页结果
     */
    PageVO<AttendanceRecordVO> pageQuery(PageDTO<AttendanceQueryDTO> pageDTO, Long currentUserId, boolean hasListPermission);

    /**
     * 查询个人考勤历史
     *
     * @param userId 用户ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    PageVO<AttendanceRecordVO> getMyHistory(Long userId, LocalDate startDate, LocalDate endDate, Integer pageNum, Integer pageSize);

    /**
     * 检查是否是记录所有者
     *
     * @param recordId 记录ID
     * @param userId 用户ID
     * @return 是否是所有者
     */
    boolean isOwner(Long recordId, Long userId);

    /**
     * 导出考勤记录
     *
     * @param dto 导出条件
     * @return 导出文件的字节数组
     */
    byte[] exportRecords(com.hngy.siae.attendance.dto.request.AttendanceExportDTO dto);

    /**
     * 查询考勤记录详情（自动获取当前用户）
     *
     * @param id 考勤记录ID
     * @return 考勤记录详细信息
     */
    AttendanceRecordDetailVO getRecordDetail(Long id);

    /**
     * 分页查询考勤记录（自动获取当前用户和权限）
     *
     * @param pageDTO 分页查询条件
     * @return 分页结果
     */
    PageVO<AttendanceRecordVO> pageQuery(PageDTO<AttendanceQueryDTO> pageDTO);

    /**
     * 查询个人考勤历史（自动获取当前用户）
     *
     * @param pageDTO 分页查询条件
     * @return 分页结果
     */
    PageVO<AttendanceRecordVO> getMyHistory(PageDTO<AttendanceQueryDTO> pageDTO);

    /**
     * 导出考勤记录（直接写入响应）
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param memberIds 成员ID列表（逗号分隔）
     * @param format 导出格式
     * @param response HTTP响应
     */
    void exportRecords(LocalDate startDate, LocalDate endDate, String memberIds, String format, 
                      jakarta.servlet.http.HttpServletResponse response);

    /**
     * 分页查询活动考勤记录
     *
     * @param pageDTO 分页查询条件
     * @return 分页结果
     */
    PageVO<AttendanceRecordVO> pageQueryActivityRecords(PageDTO<com.hngy.siae.attendance.dto.request.AttendanceQueryDTO> pageDTO);

    /**
     * 查询活动考勤记录列表
     *
     * @param activityId 活动ID
     * @param userId 用户ID（可选）
     * @return 考勤记录列表
     */
    java.util.List<AttendanceRecordVO> listActivityRecords(Long activityId, Long userId);

    /**
     * 查询个人活动考勤历史（自动获取当前用户）
     *
     * @param pageDTO 分页查询条件
     * @return 分页结果
     */
    PageVO<AttendanceRecordVO> getMyActivityHistory(PageDTO<AttendanceQueryDTO> pageDTO);
}
