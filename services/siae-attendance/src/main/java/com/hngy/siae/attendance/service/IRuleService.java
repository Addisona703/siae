package com.hngy.siae.attendance.service;

import com.hngy.siae.attendance.dto.request.AttendanceRuleCreateDTO;
import com.hngy.siae.attendance.dto.request.AttendanceRuleUpdateDTO;
import com.hngy.siae.attendance.dto.response.AttendanceRuleVO;
import com.hngy.siae.attendance.entity.AttendanceRule;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤规则服务接口
 *
 * @author SIAE Team
 */
public interface IRuleService {

    /**
     * 创建考勤规则
     *
     * @param dto 创建考勤规则DTO
     * @return 考勤规则VO
     */
    AttendanceRuleVO createRule(AttendanceRuleCreateDTO dto);

    /**
     * 更新考勤规则
     *
     * @param id 规则ID
     * @param dto 更新考勤规则DTO
     * @return 考勤规则VO
     */
    AttendanceRuleVO updateRule(Long id, AttendanceRuleUpdateDTO dto);

    /**
     * 删除考勤规则
     *
     * @param id 规则ID
     * @return 是否删除成功
     */
    Boolean deleteRule(Long id);

    /**
     * 获取适用规则（返回优先级最高的规则）
     *
     * @param userId 用户ID
     * @param date 日期
     * @return 适用的考勤规则
     */
    AttendanceRule getApplicableRule(Long userId, LocalDate date);

    /**
     * 获取所有适用规则（支持多规则并发场景）
     *
     * @param userId 用户ID
     * @param date 日期
     * @return 所有适用的考勤规则列表，按优先级降序排序
     */
    List<AttendanceRule> getAllApplicableRules(Long userId, LocalDate date);

    /**
     * 验证规则有效性
     *
     * @param rule 考勤规则
     */
    void validateRule(AttendanceRule rule);

    /**
     * 查询规则列表
     *
     * @return 规则列表
     */
    List<AttendanceRuleVO> listRules();

    /**
     * 查询规则详情
     *
     * @param id 规则ID
     * @return 规则详细信息
     */
    com.hngy.siae.attendance.dto.response.AttendanceRuleDetailVO getRuleDetail(Long id);

    /**
     * 启用规则
     *
     * @param id 规则ID
     * @return 是否成功
     */
    Boolean enableRule(Long id);

    /**
     * 禁用规则
     *
     * @param id 规则ID
     * @return 是否成功
     */
    Boolean disableRule(Long id);

    /**
     * 查询活动考勤规则
     *
     * @param activityId 活动ID
     * @return 活动考勤规则
     */
    AttendanceRule getActivityRule(Long activityId);

    /**
     * 查询规则列表（支持过滤）
     *
     * @param status 规则状态（可选）
     * @param attendanceType 考勤类型（可选）
     * @param targetType 适用对象类型（可选）
     * @return 规则列表
     */
    List<AttendanceRuleVO> listRules(com.hngy.siae.attendance.enums.RuleStatus status, String attendanceType, String targetType);

    /**
     * 创建活动考勤规则
     *
     * @param dto 创建考勤规则DTO
     * @return 考勤规则VO
     */
    AttendanceRuleVO createActivityRule(AttendanceRuleCreateDTO dto);

    /**
     * 查询活动考勤规则列表
     *
     * @param activityId 活动ID（可选）
     * @return 活动考勤规则列表
     */
    List<AttendanceRuleVO> listActivityRules(Long activityId);
}
