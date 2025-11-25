package com.hngy.siae.attendance.enums;

import com.hngy.siae.core.result.IResultCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 考勤服务结果码枚举
 *
 * @author SIAE Team
 */
@Getter
@AllArgsConstructor
public enum AttendanceResultCodeEnum implements IResultCode {

    // 签到签退相关 (5001-5100)
    DUPLICATE_CHECK_IN(5001, "今日已签到，请勿重复签到"),
    LOCATION_OUT_OF_RANGE(5002, "签到位置超出允许范围"),
    CHECK_IN_NOT_FOUND(5003, "未找到签到记录，请先签到"),
    ALREADY_CHECKED_OUT(5004, "已签退，请勿重复签退"),
    CHECK_IN_TIME_INVALID(5005, "签到时间不在允许范围内"),
    CHECK_OUT_TIME_INVALID(5006, "签退时间不在允许范围内"),

    // 考勤记录相关 (5101-5200)
    RECORD_NOT_FOUND(5101, "考勤记录不存在"),
    ATTENDANCE_RECORD_NOT_FOUND(5101, "考勤记录不存在"),
    ATTENDANCE_RECORD_ACCESS_DENIED(5102, "无权访问该考勤记录"),

    // 请假相关 (5201-5300)
    LEAVE_REQUEST_NOT_FOUND(5201, "请假申请不存在"),
    LEAVE_DATE_INVALID(5202, "请假日期无效，结束日期必须晚于或等于开始日期"),
    LEAVE_CONFLICT(5203, "请假时间与已批准的请假冲突"),
    LEAVE_STATUS_INVALID(5204, "请假状态无效，无法进行该操作"),
    LEAVE_APPROVAL_PERMISSION_DENIED(5205, "无权审批该请假申请"),
    LEAVE_ALREADY_PROCESSED(5206, "该请假申请已处理，无法重复操作"),

    // 考勤异常相关 (5301-5400)
    ANOMALY_NOT_FOUND(5301, "考勤异常记录不存在"),
    ANOMALY_ALREADY_HANDLED(5302, "该异常已处理"),

    // 考勤规则相关 (5401-5500)
    RULE_NOT_FOUND(5401, "考勤规则不存在"),
    RULE_TIME_WINDOW_INVALID(5402, "考勤规则时间窗口配置无效"),
    RULE_CONFLICT(5403, "考勤规则冲突"),
    NO_APPLICABLE_RULE(5404, "未找到适用的考勤规则"),

    // 统计相关 (5501-5600)
    STATISTICS_NOT_FOUND(5501, "统计数据不存在"),
    STATISTICS_CALCULATION_FAILED(5502, "统计数据计算失败"),

    // 系统相关 (5601-5700)
    RATE_LIMIT_EXCEEDED(5601, "请求过于频繁，请稍后再试");

    private final Integer code;
    private final String message;
}
