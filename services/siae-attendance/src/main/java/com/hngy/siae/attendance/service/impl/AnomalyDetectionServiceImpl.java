package com.hngy.siae.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hngy.siae.api.user.client.UserFeignClient;
import com.hngy.siae.api.user.dto.response.UserProfileSimpleVO;
import com.hngy.siae.attendance.dto.request.AnomalyQueryDTO;
import com.hngy.siae.attendance.dto.response.AttendanceAnomalyVO;
import com.hngy.siae.attendance.entity.AttendanceAnomaly;
import com.hngy.siae.attendance.entity.AttendanceRecord;
import com.hngy.siae.attendance.entity.AttendanceRule;
import com.hngy.siae.attendance.entity.LeaveRequest;
import com.hngy.siae.attendance.enums.AnomalyType;
import com.hngy.siae.attendance.enums.AttendanceResultCodeEnum;
import com.hngy.siae.attendance.enums.AttendanceStatus;
import com.hngy.siae.attendance.enums.AttendanceType;
import com.hngy.siae.attendance.enums.LeaveStatus;
import com.hngy.siae.attendance.enums.RuleStatus;
import com.hngy.siae.attendance.mapper.AttendanceAnomalyMapper;
import com.hngy.siae.attendance.mapper.AttendanceRecordMapper;
import com.hngy.siae.attendance.mapper.AttendanceRuleMapper;
import com.hngy.siae.attendance.mapper.LeaveRequestMapper;
import com.hngy.siae.attendance.service.IAnomalyDetectionService;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.core.utils.PageConvertUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 考勤异常检测服务实现
 *
 * @author SIAE Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnomalyDetectionServiceImpl implements IAnomalyDetectionService {

    private final AttendanceAnomalyMapper attendanceAnomalyMapper;
    private final AttendanceRecordMapper attendanceRecordMapper;
    private final AttendanceRuleMapper attendanceRuleMapper;
    private final LeaveRequestMapper leaveRequestMapper;
    private final com.hngy.siae.security.utils.SecurityUtil securityUtil;
    private final UserFeignClient userFeignClient;

    /**
     * 检测考勤异常
     * 根据考勤规则检测迟到、早退等异常
     *
     * @param record 考勤记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void detectAnomalies(AttendanceRecord record) {
        log.info("开始检测考勤异常: recordId={}, userId={}, date={}", 
                record.getId(), record.getUserId(), record.getAttendanceDate());

        // 1. 获取适用的考勤规则
        AttendanceRule rule = attendanceRuleMapper.selectById(record.getRuleId());
        if (rule == null) {
            log.warn("未找到考勤规则: ruleId={}", record.getRuleId());
            return;
        }

        // 2. 检测迟到异常
        detectLateArrival(record, rule);

        // 3. 检测早退异常
        detectEarlyDeparture(record, rule);

        log.info("考勤异常检测完成: recordId={}", record.getId());
    }

    /**
     * 检测迟到异常
     *
     * @param record 考勤记录
     * @param rule 考勤规则
     */
    private void detectLateArrival(AttendanceRecord record, AttendanceRule rule) {
        if (record.getCheckInTime() == null) {
            return;
        }

        LocalTime checkInTime = record.getCheckInTime().toLocalTime();
        LocalTime requiredCheckInTime = rule.getCheckInStartTime();
        Integer lateThresholdMinutes = rule.getLateThresholdMinutes();

        // 如果签到时间晚于要求的签到开始时间
        if (checkInTime.isAfter(requiredCheckInTime)) {
            // 计算迟到时长（分钟）
            long lateMinutes = Duration.between(requiredCheckInTime, checkInTime).toMinutes();

            // 如果迟到时长超过阈值，创建异常记录
            if (lateThresholdMinutes != null && lateMinutes > lateThresholdMinutes) {
                // 检查是否已存在该类型的异常记录（避免重复创建）
                LambdaQueryWrapper<AttendanceAnomaly> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(AttendanceAnomaly::getAttendanceRecordId, record.getId())
                        .eq(AttendanceAnomaly::getAnomalyType, AnomalyType.LATE);
                
                Long existingCount = attendanceAnomalyMapper.selectCount(queryWrapper);
                if (existingCount > 0) {
                    log.debug("迟到异常记录已存在: recordId={}", record.getId());
                    return;
                }

                AttendanceAnomaly anomaly = new AttendanceAnomaly();
                anomaly.setAttendanceRecordId(record.getId());
                anomaly.setUserId(record.getUserId());
                anomaly.setAnomalyType(AnomalyType.LATE);
                anomaly.setAnomalyDate(record.getAttendanceDate());
                anomaly.setDurationMinutes((int) lateMinutes);
                anomaly.setDescription(String.format("迟到%d分钟", lateMinutes));
                anomaly.setResolved(false);

                attendanceAnomalyMapper.insert(anomaly);

                // 更新考勤记录状态为异常
                record.setStatus(AttendanceStatus.ABNORMAL);
                attendanceRecordMapper.updateById(record);

                log.info("检测到迟到异常: userId={}, recordId={}, lateMinutes={}", 
                        record.getUserId(), record.getId(), lateMinutes);
            }
        }
    }

    /**
     * 检测早退异常
     *
     * @param record 考勤记录
     * @param rule 考勤规则
     */
    private void detectEarlyDeparture(AttendanceRecord record, AttendanceRule rule) {
        if (record.getCheckOutTime() == null) {
            return;
        }

        LocalTime checkOutTime = record.getCheckOutTime().toLocalTime();
        LocalTime requiredCheckOutTime = rule.getCheckOutStartTime();
        Integer earlyThresholdMinutes = rule.getEarlyThresholdMinutes();

        // 如果签退时间早于要求的签退开始时间
        if (checkOutTime.isBefore(requiredCheckOutTime)) {
            // 计算早退时长（分钟）
            long earlyMinutes = Duration.between(checkOutTime, requiredCheckOutTime).toMinutes();

            // 如果早退时长超过阈值，创建异常记录
            if (earlyThresholdMinutes != null && earlyMinutes > earlyThresholdMinutes) {
                // 检查是否已存在该类型的异常记录（避免重复创建）
                LambdaQueryWrapper<AttendanceAnomaly> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(AttendanceAnomaly::getAttendanceRecordId, record.getId())
                        .eq(AttendanceAnomaly::getAnomalyType, AnomalyType.EARLY_DEPARTURE);
                
                Long existingCount = attendanceAnomalyMapper.selectCount(queryWrapper);
                if (existingCount > 0) {
                    log.debug("早退异常记录已存在: recordId={}", record.getId());
                    return;
                }

                AttendanceAnomaly anomaly = new AttendanceAnomaly();
                anomaly.setAttendanceRecordId(record.getId());
                anomaly.setUserId(record.getUserId());
                anomaly.setAnomalyType(AnomalyType.EARLY_DEPARTURE);
                anomaly.setAnomalyDate(record.getAttendanceDate());
                anomaly.setDurationMinutes((int) earlyMinutes);
                anomaly.setDescription(String.format("早退%d分钟", earlyMinutes));
                anomaly.setResolved(false);

                attendanceAnomalyMapper.insert(anomaly);

                // 更新考勤记录状态为异常
                record.setStatus(AttendanceStatus.ABNORMAL);
                attendanceRecordMapper.updateById(record);

                log.info("检测到早退异常: userId={}, recordId={}, earlyMinutes={}", 
                        record.getUserId(), record.getId(), earlyMinutes);
            }
        }
    }

    /**
     * 自动检测缺勤
     * 定时任务调用，检测指定日期未签到的成员
     *
     * @param date 检测日期
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void autoDetectAbsence(LocalDate date) {
        log.info("开始自动检测缺勤: date={}", date);
        
        try {
            // 1. 获取当天所有启用的日常考勤规则
            LambdaQueryWrapper<AttendanceRule> ruleQuery = new LambdaQueryWrapper<>();
            ruleQuery.eq(AttendanceRule::getStatus, RuleStatus.ENABLED)
                    .eq(AttendanceRule::getAttendanceType, AttendanceType.DAILY)
                    .le(AttendanceRule::getEffectiveDate, date)
                    .and(wrapper -> wrapper.isNull(AttendanceRule::getExpiryDate)
                            .or()
                            .ge(AttendanceRule::getExpiryDate, date));
            
            List<AttendanceRule> activeRules = attendanceRuleMapper.selectList(ruleQuery);
            
            if (activeRules.isEmpty()) {
                log.warn("当天没有启用的日常考勤规则，跳过缺勤检测: date={}", date);
                return;
            }
            
            log.info("找到 {} 条启用的日常考勤规则", activeRules.size());
            
            // 2. 收集所有应该考勤的成员ID
            Set<Long> requiredUserIds = new HashSet<>();
            for (AttendanceRule rule : activeRules) {
                Set<Long> userIds = getUserIdsForRule(rule);
                requiredUserIds.addAll(userIds);
            }
            
            if (requiredUserIds.isEmpty()) {
                log.warn("没有需要考勤的成员，跳过缺勤检测: date={}", date);
                return;
            }
            
            log.info("应该考勤的成员数量: {}", requiredUserIds.size());
            
            // 3. 查询当天已签到的成员
            LambdaQueryWrapper<AttendanceRecord> recordQuery = new LambdaQueryWrapper<>();
            recordQuery.eq(AttendanceRecord::getAttendanceDate, date)
                    .eq(AttendanceRecord::getAttendanceType, AttendanceType.DAILY)
                    .isNotNull(AttendanceRecord::getCheckInTime);
            
            List<AttendanceRecord> records = attendanceRecordMapper.selectList(recordQuery);
            Set<Long> checkedInUserIds = records.stream()
                    .map(AttendanceRecord::getUserId)
                    .collect(Collectors.toSet());
            
            log.info("当天已签到的成员数量: {}", checkedInUserIds.size());
            
            // 4. 查询当天有批准请假的成员
            LambdaQueryWrapper<LeaveRequest> leaveQuery = new LambdaQueryWrapper<>();
            leaveQuery.eq(LeaveRequest::getStatus, LeaveStatus.APPROVED)
                    .le(LeaveRequest::getStartDate, date)
                    .ge(LeaveRequest::getEndDate, date);
            
            List<LeaveRequest> approvedLeaves = leaveRequestMapper.selectList(leaveQuery);
            Set<Long> onLeaveUserIds = approvedLeaves.stream()
                    .map(LeaveRequest::getUserId)
                    .collect(Collectors.toSet());
            
            log.info("当天有批准请假的成员数量: {}", onLeaveUserIds.size());
            
            // 5. 找出未签到且未请假的成员（缺勤成员）
            Set<Long> absentUserIds = new HashSet<>(requiredUserIds);
            absentUserIds.removeAll(checkedInUserIds);
            absentUserIds.removeAll(onLeaveUserIds);
            
            if (absentUserIds.isEmpty()) {
                log.info("没有检测到缺勤成员: date={}", date);
                return;
            }
            
            log.info("检测到缺勤成员数量: {}", absentUserIds.size());
            
            // 6. 为每个缺勤成员创建异常记录
            int createdCount = 0;
            for (Long userId : absentUserIds) {
                // 检查是否已存在该日期的缺勤异常记录（避免重复创建）
                LambdaQueryWrapper<AttendanceAnomaly> anomalyQuery = new LambdaQueryWrapper<>();
                anomalyQuery.eq(AttendanceAnomaly::getUserId, userId)
                        .eq(AttendanceAnomaly::getAnomalyDate, date)
                        .eq(AttendanceAnomaly::getAnomalyType, AnomalyType.ABSENCE);
                
                Long existingCount = attendanceAnomalyMapper.selectCount(anomalyQuery);
                if (existingCount > 0) {
                    log.debug("缺勤异常记录已存在，跳过: userId={}, date={}", userId, date);
                    continue;
                }
                
                // 创建缺勤异常记录
                AttendanceAnomaly anomaly = new AttendanceAnomaly();
                anomaly.setAttendanceRecordId(null); // 缺勤没有考勤记录
                anomaly.setUserId(userId);
                anomaly.setAnomalyType(AnomalyType.ABSENCE);
                anomaly.setAnomalyDate(date);
                anomaly.setDurationMinutes(null); // 缺勤不计算时长
                anomaly.setDescription("未签到，系统自动检测为缺勤");
                anomaly.setResolved(false);
                
                attendanceAnomalyMapper.insert(anomaly);
                createdCount++;
                
                log.debug("创建缺勤异常记录: userId={}, date={}, anomalyId={}", 
                        userId, date, anomaly.getId());
            }
            
            log.info("自动缺勤检测完成: date={}, 创建异常记录数量: {}", date, createdCount);
            
            // 7. 发送缺勤通知（如果有消息服务）
            if (createdCount > 0) {
                sendAbsenceNotifications(absentUserIds, date);
            }
            
        } catch (Exception e) {
            log.error("自动缺勤检测失败: date={}", date, e);
            throw e;
        }
    }
    
    /**
     * 获取规则适用的用户ID集合
     *
     * @param rule 考勤规则
     * @return 用户ID集合
     */
    private Set<Long> getUserIdsForRule(AttendanceRule rule) {
        Set<Long> userIds = new HashSet<>();
        
        switch (rule.getTargetType()) {
            case ALL:
                // 全体成员规则：理论上应该查询所有活跃用户
                // 由于没有用户服务集成，这里返回空集合
                // TODO: 集成用户服务后，查询所有活跃用户
                log.debug("规则 {} 适用于全体成员，但用户服务未集成，跳过", rule.getId());
                break;
                
            case INDIVIDUAL:
                // 个人规则：直接使用目标ID列表
                if (rule.getTargetIds() != null && !rule.getTargetIds().isEmpty()) {
                    userIds.addAll(rule.getTargetIds());
                    log.debug("规则 {} 适用于个人，用户数量: {}", rule.getId(), userIds.size());
                }
                break;
                
            case DEPARTMENT:
                // 部门规则：需要查询部门下的所有用户
                // 由于没有用户服务集成，这里返回空集合
                // TODO: 集成用户服务后，根据部门ID查询用户
                log.debug("规则 {} 适用于部门，但用户服务未集成，跳过", rule.getId());
                break;
                
            default:
                log.warn("未知的规则目标类型: {}", rule.getTargetType());
                break;
        }
        
        return userIds;
    }
    
    /**
     * 发送缺勤通知
     * 
     * 注意：这是一个占位方法，实际应该通过消息队列发送通知
     * 当前实现仅记录日志
     * 
     * TODO: 集成消息服务，实现真实的通知发送逻辑
     * 可能的实现方式：
     * 1. 通过 RabbitMQ 发送通知消息
     * 2. 调用通知服务的 API
     * 3. 发送邮件或短信通知
     *
     * @param absentUserIds 缺勤用户ID集合
     * @param date 缺勤日期
     */
    private void sendAbsenceNotifications(Set<Long> absentUserIds, LocalDate date) {
        log.info("准备发送缺勤通知: date={}, 缺勤用户数量: {}", date, absentUserIds.size());
        
        // TODO: 实现通知发送逻辑
        // 示例代码（需要实际实现）：
        // for (Long userId : absentUserIds) {
        //     NotificationMessage message = NotificationMessage.builder()
        //             .userId(userId)
        //             .type(NotificationType.ABSENCE_ALERT)
        //             .title("缺勤提醒")
        //             .content(String.format("您在 %s 未签到，已被记录为缺勤", date))
        //             .build();
        //     
        //     messagingTemplate.send("notification.exchange", "absence.alert", message);
        // }
        
        log.info("缺勤通知发送完成（当前为占位实现）");
    }

    /**
     * 处理考勤异常
     *
     * @param anomalyId 异常ID
     * @param handlerId 处理人ID
     * @param handlerNote 处理说明
     * @param resolved 是否解决
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleAnomaly(Long anomalyId, Long handlerId, String handlerNote, Boolean resolved) {
        log.info("开始处理考勤异常: anomalyId={}, handlerId={}, resolved={}", 
                anomalyId, handlerId, resolved);

        // 1. 查询异常记录
        AttendanceAnomaly anomaly = attendanceAnomalyMapper.selectById(anomalyId);
        AssertUtils.notNull(anomaly, AttendanceResultCodeEnum.ANOMALY_NOT_FOUND);

        // 2. 如果标记为"未解决"，则重置处理信息，允许再次处理
        if (Boolean.FALSE.equals(resolved)) {
            anomaly.setHandlerId(null);
            anomaly.setHandlerNote(null);
            anomaly.setResolved(false);
            anomaly.setHandledAt(null);
            attendanceAnomalyMapper.updateById(anomaly);
            log.info("考勤异常已标记为未解决，重置处理信息: anomalyId={}", anomalyId);
            return;
        }

        // 3. 检查是否已处理（只有标记为"已解决"时才检查）
        AssertUtils.isFalse(Boolean.TRUE.equals(anomaly.getResolved()), 
                AttendanceResultCodeEnum.ANOMALY_ALREADY_HANDLED);

        // 4. 更新异常记录为已解决
        anomaly.setHandlerId(handlerId);
        anomaly.setHandlerNote(handlerNote);
        anomaly.setResolved(true);
        anomaly.setHandledAt(LocalDateTime.now());

        attendanceAnomalyMapper.updateById(anomaly);

        log.info("考勤异常处理完成: anomalyId={}", anomalyId);
    }

    /**
     * 通过请假抑制异常
     *
     * @param anomalyId 异常ID
     * @param leaveRequestId 请假申请ID
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void suppressAnomalyByLeave(Long anomalyId, Long leaveRequestId) {
        log.info("开始通过请假抑制异常: anomalyId={}, leaveRequestId={}", 
                anomalyId, leaveRequestId);

        // 1. 查询异常记录
        AttendanceAnomaly anomaly = attendanceAnomalyMapper.selectById(anomalyId);
        AssertUtils.notNull(anomaly, AttendanceResultCodeEnum.ANOMALY_NOT_FOUND);

        // 2. 更新异常记录，标记为被请假抑制
        anomaly.setSuppressedByLeave(leaveRequestId);
        anomaly.setResolved(true);
        anomaly.setHandledAt(LocalDateTime.now());
        anomaly.setDescription(anomaly.getDescription() + "（已请假）");

        attendanceAnomalyMapper.updateById(anomaly);

        log.info("异常抑制完成: anomalyId={}", anomalyId);
    }

    // ==================== 新增方法：简化版本（自动获取当前用户） ====================

    /**
     * 处理考勤异常（使用DTO，自动获取当前用户）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttendanceAnomalyVO handleAnomaly(
            Long id, com.hngy.siae.attendance.dto.request.AnomalyHandleDTO dto) {
        Long currentUserId = securityUtil.getCurrentUserId();
        
        // 调用原有方法
        handleAnomaly(id, currentUserId, dto.getHandlerNote(), dto.getResolved());
        
        // 重新查询返回更新后的数据
        return getAnomaly(id);
    }

    /**
     * 查询个人考勤异常（自动获取当前用户）
     */
    @Override
    public PageVO<AttendanceAnomalyVO> getMyAnomalies(
            PageDTO<Void> pageDTO) {
        Long currentUserId = securityUtil.getCurrentUserId();
        return getMyAnomalies(currentUserId, pageDTO.getPageNum(), pageDTO.getPageSize());
    }

    /**
     * 查询未处理的考勤异常
     */
    @Override
    public PageVO<AttendanceAnomalyVO> getUnresolvedAnomalies(
            PageDTO<Void> pageDTO) {
        log.info("查询未处理的考勤异常: pageNum={}, pageSize={}", pageDTO.getPageNum(), pageDTO.getPageSize());
        
        // 构建分页查询
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<AttendanceAnomaly> page = 
                PageConvertUtil.toPage(pageDTO);
        
        LambdaQueryWrapper<AttendanceAnomaly> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceAnomaly::getResolved, false)
                .orderByDesc(AttendanceAnomaly::getAnomalyDate)
                .orderByDesc(AttendanceAnomaly::getCreatedAt);
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<AttendanceAnomaly> resultPage = 
                attendanceAnomalyMapper.selectPage(page, queryWrapper);
        
        // 转换为VO并填充用户信息
        PageVO<AttendanceAnomalyVO> pageVO = PageConvertUtil.convert(
                resultPage, 
                AttendanceAnomalyVO.class
        );
        fillUserInfo(pageVO.getRecords());
        return pageVO;
    }

    /**
     * 分页查询考勤异常
     */
    @Override
    public PageVO<AttendanceAnomalyVO> pageQuery(
            PageDTO<AnomalyQueryDTO> pageDTO) {
        log.info("分页查询考勤异常: pageNum={}, pageSize={}", pageDTO.getPageNum(), pageDTO.getPageSize());
        
        AnomalyQueryDTO query = pageDTO.getParams();
        
        // 构建分页查询
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<AttendanceAnomaly> page = 
                PageConvertUtil.toPage(pageDTO);
        
        LambdaQueryWrapper<AttendanceAnomaly> queryWrapper = new LambdaQueryWrapper<>();
        
        // 用户ID条件
        if (query != null) {
            if (query.getUserId() != null) {
                queryWrapper.eq(AttendanceAnomaly::getUserId, query.getUserId());
            }
            
            // 用户ID列表条件
            if (query.getUserIds() != null && !query.getUserIds().isEmpty()) {
                queryWrapper.in(AttendanceAnomaly::getUserId, query.getUserIds());
            }
            
            // 异常类型条件
            if (query.getAnomalyType() != null) {
                queryWrapper.eq(AttendanceAnomaly::getAnomalyType, query.getAnomalyType());
            }
            
            // 日期范围条件
            if (query.getStartDate() != null) {
                queryWrapper.ge(AttendanceAnomaly::getAnomalyDate, query.getStartDate());
            }
            if (query.getEndDate() != null) {
                queryWrapper.le(AttendanceAnomaly::getAnomalyDate, query.getEndDate());
            }
            
            // 是否已处理条件
            if (query.getResolved() != null) {
                queryWrapper.eq(AttendanceAnomaly::getResolved, query.getResolved());
            }
            
            // 处理人ID条件
            if (query.getHandlerId() != null) {
                queryWrapper.eq(AttendanceAnomaly::getHandlerId, query.getHandlerId());
            }
            
            // 关键字搜索（搜索描述和处理说明）
            if (query.getKeyword() != null && !query.getKeyword().trim().isEmpty()) {
                queryWrapper.and(wrapper -> wrapper
                        .like(AttendanceAnomaly::getDescription, query.getKeyword())
                        .or()
                        .like(AttendanceAnomaly::getHandlerNote, query.getKeyword())
                );
            }
        }
        
        // 排序
        queryWrapper.orderByDesc(AttendanceAnomaly::getAnomalyDate)
                .orderByDesc(AttendanceAnomaly::getCreatedAt);
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<AttendanceAnomaly> resultPage = 
                attendanceAnomalyMapper.selectPage(page, queryWrapper);
        
        // 转换为VO并填充用户信息
        PageVO<AttendanceAnomalyVO> pageVO = PageConvertUtil.convert(
                resultPage, 
                AttendanceAnomalyVO.class
        );
        fillUserInfo(pageVO.getRecords());
        return pageVO;
    }

    /**
     * 查询考勤异常详情
     */
    @Override
    public AttendanceAnomalyVO getAnomaly(Long id) {
        log.info("查询考勤异常详情: id={}", id);
        
        AttendanceAnomaly anomaly = attendanceAnomalyMapper.selectById(id);
        AssertUtils.notNull(anomaly, AttendanceResultCodeEnum.ANOMALY_NOT_FOUND);
        
        AttendanceAnomalyVO vo = BeanConvertUtil.to(anomaly, AttendanceAnomalyVO.class);
        fillUserInfo(Collections.singletonList(vo));
        return vo;
    }

    /**
     * 查询个人考勤异常
     */
    @Override
    public PageVO<AttendanceAnomalyVO> getMyAnomalies(
            Long userId, Integer pageNum, Integer pageSize) {
        log.info("查询个人考勤异常: userId={}, pageNum={}, pageSize={}", userId, pageNum, pageSize);
        
        // 构建分页查询
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<AttendanceAnomaly> page = 
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<AttendanceAnomaly> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceAnomaly::getUserId, userId)
                .orderByDesc(AttendanceAnomaly::getAnomalyDate)
                .orderByDesc(AttendanceAnomaly::getCreatedAt);
        
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<AttendanceAnomaly> resultPage = 
                attendanceAnomalyMapper.selectPage(page, queryWrapper);
        
        // 转换为VO并填充用户信息
        PageVO<AttendanceAnomalyVO> pageVO = PageConvertUtil.convert(
                resultPage, 
                AttendanceAnomalyVO.class
        );
        fillUserInfo(pageVO.getRecords());
        return pageVO;
    }

    /**
     * 填充用户信息（用户名、处理人名称）
     *
     * @param records 异常记录列表
     */
    private void fillUserInfo(List<AttendanceAnomalyVO> records) {
        if (records == null || records.isEmpty()) {
            return;
        }

        // 收集所有需要查询的用户ID
        Set<Long> userIds = new HashSet<>();
        for (AttendanceAnomalyVO record : records) {
            if (record.getUserId() != null) {
                userIds.add(record.getUserId());
            }
            if (record.getHandlerId() != null) {
                userIds.add(record.getHandlerId());
            }
        }

        if (userIds.isEmpty()) {
            return;
        }

        // 批量查询用户信息
        Map<Long, UserProfileSimpleVO> userMap;
        try {
            userMap = userFeignClient.batchGetUserProfiles(new ArrayList<>(userIds));
        } catch (Exception e) {
            log.warn("批量查询用户信息失败: {}", e.getMessage());
            return;
        }

        if (userMap == null || userMap.isEmpty()) {
            return;
        }

        // 填充用户信息
        for (AttendanceAnomalyVO record : records) {
            // 填充用户名
            if (record.getUserId() != null) {
                UserProfileSimpleVO user = userMap.get(record.getUserId());
                if (user != null) {
                    record.setUserName(user.getNickname() != null ? user.getNickname() : user.getUsername());
                }
            }
            // 填充处理人名称
            if (record.getHandlerId() != null) {
                UserProfileSimpleVO handler = userMap.get(record.getHandlerId());
                if (handler != null) {
                    record.setHandlerName(handler.getNickname() != null ? handler.getNickname() : handler.getUsername());
                }
            }
        }
    }
}
