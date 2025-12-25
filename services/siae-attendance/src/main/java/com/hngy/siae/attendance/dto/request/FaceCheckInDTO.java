package com.hngy.siae.attendance.dto.request;

import lombok.Data;

/**
 * 人脸识别打卡请求DTO
 * <p>
 * 用于移动端人脸识别打卡成功后提交考勤记录
 * 直接从安全上下文获取当前用户ID，无需传递任何参数
 * 不需要传递位置信息，后端会自动设置固定位置
 * 签到时间使用当前时间，考勤类型默认为日常考勤
 *
 * @author KEYKB
 */
@Data
public class FaceCheckInDTO {
    // 无需任何参数，所有信息从上下文获取或使用默认值
}
