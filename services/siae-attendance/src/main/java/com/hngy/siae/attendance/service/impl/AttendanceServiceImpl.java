package com.hngy.siae.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.siae.api.user.client.UserFeignClient;
import com.hngy.siae.api.user.dto.response.UserProfileSimpleVO;
import com.hngy.siae.attendance.dto.request.AttendanceQueryDTO;
import com.hngy.siae.attendance.dto.request.CheckInDTO;
import com.hngy.siae.attendance.dto.request.CheckOutDTO;
import com.hngy.siae.attendance.dto.response.AttendanceAnomalyVO;
import com.hngy.siae.attendance.dto.response.AttendanceRecordDetailVO;
import com.hngy.siae.attendance.dto.response.AttendanceRecordVO;
import com.hngy.siae.attendance.entity.AttendanceAnomaly;
import com.hngy.siae.attendance.entity.AttendanceRecord;
import com.hngy.siae.attendance.entity.AttendanceRule;
import com.hngy.siae.attendance.enums.AttendanceResultCodeEnum;
import com.hngy.siae.attendance.enums.AttendanceStatus;
import com.hngy.siae.attendance.enums.AttendanceType;
import com.hngy.siae.attendance.enums.RuleStatus;
import com.hngy.siae.attendance.mapper.AttendanceAnomalyMapper;
import com.hngy.siae.attendance.mapper.AttendanceRecordMapper;
import com.hngy.siae.attendance.mapper.AttendanceRuleMapper;
import com.hngy.siae.attendance.service.IAnomalyDetectionService;
import com.hngy.siae.attendance.service.IAttendanceService;
import com.hngy.siae.attendance.util.LocationUtil;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.utils.BeanConvertUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 考勤服务实现
 *
 * @author SIAE Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements IAttendanceService {

    private final AttendanceRecordMapper attendanceRecordMapper;
    private final AttendanceRuleMapper attendanceRuleMapper;
    private final AttendanceAnomalyMapper attendanceAnomalyMapper;
    private final StringRedisTemplate redisTemplate;
    private final IAnomalyDetectionService anomalyDetectionService;
    private final com.hngy.siae.security.utils.SecurityUtil securityUtil;
    private final UserFeignClient userFeignClient;

    private static final String CHECK_IN_LOCK_PREFIX = "attendance:checkin:lock:";
    private static final long LOCK_EXPIRE_SECONDS = 300; // 5分钟

    /**
     * 签到
     *
     * @param dto 签到请求
     * @return 考勤记录VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttendanceRecordVO checkIn(CheckInDTO dto) {
        log.info("开始处理签到请求: userId={}, timestamp={}, location={}", 
                dto.getUserId(), dto.getTimestamp(), dto.getLocation());

        LocalDate attendanceDate = dto.getTimestamp().toLocalDate();
        AttendanceType attendanceType = dto.getAttendanceType() != null 
                ? AttendanceType.fromValue(dto.getAttendanceType()) 
                : AttendanceType.DAILY;

        // 1. 获取适用的考勤规则（先获取规则，用于确定时段）
        AttendanceRule rule = getApplicableRule(dto.getUserId(), attendanceDate, attendanceType, dto.getRelatedId());
        AssertUtils.notNull(rule, AttendanceResultCodeEnum.NO_APPLICABLE_RULE);

        // 2. 防重复签到检查（使用 Redis 分布式锁，基于规则ID区分不同时段）
        // 锁的key包含规则ID，这样不同时段（不同规则）可以分别签到
        String lockKey = CHECK_IN_LOCK_PREFIX + dto.getUserId() + ":" + attendanceDate + ":" + rule.getId();
        Boolean lockAcquired = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, "1", LOCK_EXPIRE_SECONDS, TimeUnit.SECONDS);

        AssertUtils.isTrue(Boolean.TRUE.equals(lockAcquired), AttendanceResultCodeEnum.DUPLICATE_CHECK_IN);

        try {
            // 3. 检查数据库中是否已有该时段（该规则）的签到记录
            LambdaQueryWrapper<AttendanceRecord> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(AttendanceRecord::getUserId, dto.getUserId())
                    .eq(AttendanceRecord::getAttendanceDate, attendanceDate)
                    .eq(AttendanceRecord::getAttendanceType, attendanceType)
                    .eq(AttendanceRecord::getRuleId, rule.getId()); // 关键：通过规则ID区分不同时段

            if (attendanceType == AttendanceType.ACTIVITY && dto.getRelatedId() != null) {
                queryWrapper.eq(AttendanceRecord::getRelatedId, dto.getRelatedId());
            }

            Long count = attendanceRecordMapper.selectCount(queryWrapper);
            AssertUtils.isTrue(count == 0, AttendanceResultCodeEnum.DUPLICATE_CHECK_IN);

            // 4. 验证签到时间窗口
            LocalTime checkInTime = dto.getTimestamp().toLocalTime();
            boolean isInTimeWindow = !checkInTime.isBefore(rule.getCheckInStartTime()) && 
                                    !checkInTime.isAfter(rule.getCheckInEndTime());
            AssertUtils.isTrue(isInTimeWindow, AttendanceResultCodeEnum.CHECK_IN_TIME_INVALID);

            // 5. 验证签到位置（如果规则要求）
            if (Boolean.TRUE.equals(rule.getLocationRequired())) {
                validateLocation(dto.getLocation(), rule);
            }

            // 6. 创建考勤记录
            AttendanceRecord record = new AttendanceRecord();
            record.setUserId(dto.getUserId());
            record.setAttendanceType(attendanceType);
            record.setRelatedId(dto.getRelatedId());
            record.setCheckInTime(dto.getTimestamp());
            record.setCheckInLocation(formatLocation(dto.getLocation()));
            record.setAttendanceDate(attendanceDate);
            record.setRuleId(rule.getId());
            record.setStatus(AttendanceStatus.IN_PROGRESS);

            attendanceRecordMapper.insert(record);

            log.info("签到成功: userId={}, recordId={}, ruleId={}, ruleName={}", 
                    dto.getUserId(), record.getId(), rule.getId(), rule.getName());

            // 7. 检测考勤异常（迟到）
            anomalyDetectionService.detectAnomalies(record);

            // 8. 转换为 VO 返回
            return BeanConvertUtil.to(record, AttendanceRecordVO.class);

        } catch (Exception e) {
            // 发生异常时删除 Redis 锁，避免锁住后续请求
            redisTemplate.delete(lockKey);
            log.warn("签到失败，已释放锁: userId={}, lockKey={}, error={}", 
                    dto.getUserId(), lockKey, e.getMessage());
            throw e;
        }
    }

    /**
     * 获取适用的考勤规则
     * 支持一天多个时段：根据当前时间选择对应时段的规则
     *
     * @param userId 用户ID
     * @param date 日期
     * @param attendanceType 考勤类型
     * @param relatedId 关联ID（活动考勤时使用）
     * @return 考勤规则
     */
    private AttendanceRule getApplicableRule(Long userId, LocalDate date, 
                                            AttendanceType attendanceType, Long relatedId) {
        LambdaQueryWrapper<AttendanceRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceRule::getStatus, RuleStatus.ENABLED)
                .eq(AttendanceRule::getAttendanceType, attendanceType)
                .le(AttendanceRule::getEffectiveDate, date)
                .and(wrapper -> wrapper.isNull(AttendanceRule::getExpiryDate)
                        .or()
                        .ge(AttendanceRule::getExpiryDate, date));

        // 如果是活动考勤，需要匹配 relatedId
        if (attendanceType == AttendanceType.ACTIVITY && relatedId != null) {
            queryWrapper.eq(AttendanceRule::getRelatedId, relatedId);
        }

        List<AttendanceRule> rules = attendanceRuleMapper.selectList(queryWrapper);

        if (rules.isEmpty()) {
            return null;
        }

        LocalTime currentTime = LocalTime.now();

        // 策略：
        // 1. 优先选择当前时间在签到时间窗口内的规则
        // 2. 如果有多个符合的规则，选择优先级最高的
        // 3. 如果没有符合的规则，选择优先级最高的规则（兜底）
        
        // 筛选出当前时间在签到窗口内的规则
        List<AttendanceRule> matchingRules = rules.stream()
                .filter(rule -> !currentTime.isBefore(rule.getCheckInStartTime()) 
                             && !currentTime.isAfter(rule.getCheckInEndTime()))
                .toList();

        // 如果有匹配的规则，选择优先级最高的
        if (!matchingRules.isEmpty()) {
            return matchingRules.stream()
                    .max(Comparator.comparing(AttendanceRule::getPriority))
                    .orElse(null);
        }

        // 如果当前时间不在任何签到窗口内，选择优先级最高的规则作为兜底
        // 这样可以给出更明确的错误提示（时间窗口验证会失败）
        return rules.stream()
                .max(Comparator.comparing(AttendanceRule::getPriority))
                .orElse(null);
    }

    /**
     * 将 LocationInfo 对象转换为字符串格式
     *
     * @param location 位置信息对象
     * @return 格式化的位置字符串 "latitude,longitude"，如果位置为空则返回null
     */
    private String formatLocation(CheckInDTO.LocationInfo location) {
        if (location == null) {
            return null;
        }
        if (location.getLatitude() != null && location.getLongitude() != null) {
            return location.getLatitude() + "," + location.getLongitude();
        }
        return null;
    }

    /**
     * 验证签到位置
     *
     * @param locationInfo 签到位置信息
     * @param rule 考勤规则
     */
    private void validateLocation(CheckInDTO.LocationInfo locationInfo, AttendanceRule rule) {
        AssertUtils.notNull(locationInfo, AttendanceResultCodeEnum.LOCATION_OUT_OF_RANGE);
        AssertUtils.notNull(locationInfo.getLatitude(), AttendanceResultCodeEnum.LOCATION_OUT_OF_RANGE);
        AssertUtils.notNull(locationInfo.getLongitude(), AttendanceResultCodeEnum.LOCATION_OUT_OF_RANGE);

        List<AttendanceRule.Location> allowedLocations = rule.getAllowedLocations();
        AssertUtils.notEmpty(allowedLocations, AttendanceResultCodeEnum.LOCATION_OUT_OF_RANGE);

        Integer radiusMeters = rule.getLocationRadiusMeters();
        if (radiusMeters == null || radiusMeters <= 0) {
            radiusMeters = 100; // 默认100米
        }

        // 将 LocationInfo 转换为字符串格式以便调用 LocationUtil
        String checkInLocation = formatLocation(locationInfo);

        // 检查签到位置是否在任一允许位置的范围内
        boolean isValid = false;
        for (AttendanceRule.Location allowedLocation : allowedLocations) {
            if (LocationUtil.isWithinRange(checkInLocation, allowedLocation, radiusMeters)) {
                isValid = true;
                break;
            }
        }

        AssertUtils.isTrue(isValid, AttendanceResultCodeEnum.LOCATION_OUT_OF_RANGE);
    }

    /**
     * 签退
     *
     * @param dto 签退请求
     * @return 考勤记录VO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttendanceRecordVO checkOut(CheckOutDTO dto) {
        log.info("开始处理签退请求: userId={}, timestamp={}, location={}", 
                dto.getUserId(), dto.getTimestamp(), dto.getLocation());

        LocalDate checkOutDate = dto.getTimestamp().toLocalDate();

        // 1. 查找当天的签到记录（状态为进行中）
        LambdaQueryWrapper<AttendanceRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceRecord::getUserId, dto.getUserId())
                .eq(AttendanceRecord::getAttendanceDate, checkOutDate)
                .eq(AttendanceRecord::getStatus, AttendanceStatus.IN_PROGRESS)
                .orderByDesc(AttendanceRecord::getCheckInTime)
                .last("LIMIT 1");

        AttendanceRecord record = attendanceRecordMapper.selectOne(queryWrapper);
        
        // 2. 签退前置检查：必须先签到
        AssertUtils.notNull(record, AttendanceResultCodeEnum.CHECK_IN_NOT_FOUND);

        // 3. 检查是否已经签退
        AssertUtils.isNull(record.getCheckOutTime(), AttendanceResultCodeEnum.ALREADY_CHECKED_OUT);

        // 4. 获取适用的考勤规则
        AttendanceRule rule = attendanceRuleMapper.selectById(record.getRuleId());
        AssertUtils.notNull(rule, AttendanceResultCodeEnum.RULE_NOT_FOUND);

        // 5. 验证签退时间窗口
        LocalTime checkOutTime = dto.getTimestamp().toLocalTime();
        boolean isInTimeWindow = !checkOutTime.isBefore(rule.getCheckOutStartTime()) && 
                                !checkOutTime.isAfter(rule.getCheckOutEndTime());
        AssertUtils.isTrue(isInTimeWindow, AttendanceResultCodeEnum.CHECK_OUT_TIME_INVALID);

        // 6. 更新考勤记录
        record.setCheckOutTime(dto.getTimestamp());
        record.setCheckOutLocation(dto.getLocation());
        
        // 7. 计算考勤时长（分钟）
        long durationMinutes = Duration.between(record.getCheckInTime(), dto.getTimestamp()).toMinutes();
        record.setDurationMinutes((int) durationMinutes);

        // 8. 先设置状态为已完成
        record.setStatus(AttendanceStatus.COMPLETED);
        attendanceRecordMapper.updateById(record);

        // 9. 检测考勤异常（早退）- 异常检测服务会自动更新状态为异常
        anomalyDetectionService.detectAnomalies(record);

        // 10. 重新加载记录以获取最新状态
        record = attendanceRecordMapper.selectById(record.getId());

        log.info("签退成功: userId={}, recordId={}, duration={}分钟, status={}", 
                dto.getUserId(), record.getId(), durationMinutes, record.getStatus());

        // 11. 转换为 VO 返回
        return BeanConvertUtil.to(record, AttendanceRecordVO.class);
    }

    /**
     * 查询考勤记录详情
     *
     * @param id 考勤记录ID
     * @param currentUserId 当前用户ID（用于权限验证）
     * @return 考勤记录详细信息
     */
    @Override
    public AttendanceRecordDetailVO getRecord(Long id, Long currentUserId) {
        log.info("查询考勤记录详情: recordId={}, currentUserId={}", id, currentUserId);

        // 1. 查询考勤记录
        AttendanceRecord record = attendanceRecordMapper.selectById(id);
        AssertUtils.notNull(record, AttendanceResultCodeEnum.RECORD_NOT_FOUND);

        // 2. 数据权限验证：只能查看自己的记录（除非有管理权限，由Controller层处理）
        // 这里只做基本验证
        if (currentUserId != null && !record.getUserId().equals(currentUserId)) {
            throw new RuntimeException("无权访问他人的考勤记录");
        }

        // 3. 转换为详细VO
        AttendanceRecordDetailVO detailVO = BeanConvertUtil.to(record, AttendanceRecordDetailVO.class);

        // 4. 查询关联的规则信息
        if (record.getRuleId() != null) {
            AttendanceRule rule = attendanceRuleMapper.selectById(record.getRuleId());
            if (rule != null) {
                detailVO.setRuleName(rule.getName());
            }
        }

        // 5. 查询关联的异常记录
        LambdaQueryWrapper<AttendanceAnomaly> anomalyWrapper = new LambdaQueryWrapper<>();
        anomalyWrapper.eq(AttendanceAnomaly::getAttendanceRecordId, id)
                .orderByDesc(AttendanceAnomaly::getCreatedAt);
        List<AttendanceAnomaly> anomalies = attendanceAnomalyMapper.selectList(anomalyWrapper);
        
        if (!CollectionUtils.isEmpty(anomalies)) {
            List<AttendanceAnomalyVO> anomalyVOs = anomalies.stream()
                    .map(anomaly -> BeanConvertUtil.to(anomaly, AttendanceAnomalyVO.class))
                    .collect(Collectors.toList());
            detailVO.setAnomalies(anomalyVOs);
        }

        log.info("查询考勤记录详情成功: recordId={}, userId={}, anomalyCount={}", 
                id, record.getUserId(), anomalies.size());

        return detailVO;
    }

    /**
     * 分页查询考勤记录
     *
     * @param pageDTO 分页查询条件
     * @param currentUserId 当前用户ID（用于数据权限过滤）
     * @param hasListPermission 是否有列表查询权限
     * @return 分页结果
     */
    @Override
    public PageVO<AttendanceRecordVO> pageQuery(PageDTO<AttendanceQueryDTO> pageDTO, 
                                                 Long currentUserId, 
                                                 boolean hasListPermission) {
        log.info("分页查询考勤记录: pageNum={}, pageSize={}, hasListPermission={}", 
                pageDTO.getPageNum(), pageDTO.getPageSize(), hasListPermission);

        AttendanceQueryDTO queryDTO = pageDTO.getParams();
        if (queryDTO == null) {
            queryDTO = new AttendanceQueryDTO();
        }

        // 数据权限过滤：如果没有列表查询权限，只能查看自己的数据
        if (!hasListPermission && currentUserId != null) {
            queryDTO.setUserId(currentUserId);
            log.info("无列表权限，限制查询当前用户数据: userId={}", currentUserId);
        }

        // 如果有用户名查询条件，先通过用户名查询用户ID列表
        if (StringUtils.hasText(queryDTO.getUserName())) {
            // TODO: 调用用户服务根据用户名查询用户ID列表
            // 这里暂时使用空列表，实际项目中需要调用用户服务
            log.warn("用户名查询功能需要集成用户服务: userName={}", queryDTO.getUserName());
            // 如果找不到用户，返回空结果
            // queryDTO.setUserIds(Collections.emptyList());
        }

        // 构建查询条件
        LambdaQueryWrapper<AttendanceRecord> queryWrapper = new LambdaQueryWrapper<>();

        // 用户ID过滤
        if (queryDTO.getUserId() != null) {
            queryWrapper.eq(AttendanceRecord::getUserId, queryDTO.getUserId());
        }

        // 用户ID列表过滤
        if (!CollectionUtils.isEmpty(queryDTO.getUserIds())) {
            queryWrapper.in(AttendanceRecord::getUserId, queryDTO.getUserIds());
        }

        // 考勤类型过滤
        if (queryDTO.getAttendanceType() != null) {
            queryWrapper.eq(AttendanceRecord::getAttendanceType, queryDTO.getAttendanceType());
        }

        // 关联ID过滤（活动考勤）
        if (queryDTO.getRelatedId() != null) {
            queryWrapper.eq(AttendanceRecord::getRelatedId, queryDTO.getRelatedId());
        }

        // 日期范围过滤
        if (queryDTO.getStartDate() != null) {
            queryWrapper.ge(AttendanceRecord::getAttendanceDate, queryDTO.getStartDate());
        }
        if (queryDTO.getEndDate() != null) {
            queryWrapper.le(AttendanceRecord::getAttendanceDate, queryDTO.getEndDate());
        }

        // 状态过滤
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(AttendanceRecord::getStatus, queryDTO.getStatus());
        }

        // 关键字搜索（备注）
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            queryWrapper.like(AttendanceRecord::getRemark, queryDTO.getKeyword());
        }

        // 排序：按考勤日期和签到时间降序
        queryWrapper.orderByDesc(AttendanceRecord::getAttendanceDate)
                .orderByDesc(AttendanceRecord::getCheckInTime);

        // 分页查询
        Page<AttendanceRecord> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        Page<AttendanceRecord> resultPage = attendanceRecordMapper.selectPage(page, queryWrapper);

        // 转换为VO
        List<AttendanceRecordVO> voList = resultPage.getRecords().stream()
                .map(record -> BeanConvertUtil.to(record, AttendanceRecordVO.class))
                .collect(Collectors.toList());

        // 填充用户信息
        fillUserInfo(voList);

        // 构建分页结果
        PageVO<AttendanceRecordVO> pageVO = new PageVO<>();
        pageVO.setTotal(resultPage.getTotal());
        pageVO.setPageNum(pageDTO.getPageNum());
        pageVO.setPageSize(pageDTO.getPageSize());
        pageVO.setRecords(voList);

        log.info("分页查询考勤记录成功: total=, records={}", resultPage.getTotal(), voList.size());

        return pageVO;
    }

    /**
     * 填充用户信息到考勤记录VO列表
     *
     * @param voList 考勤记录VO列表
     */
    private void fillUserInfo(List<AttendanceRecordVO> voList) {
        if (CollectionUtils.isEmpty(voList)) {
            return;
        }

        // 收集所有用户ID
        List<Long> userIds = voList.stream()
                .map(AttendanceRecordVO::getUserId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }

        try {
            // 调用用户服务批量查询用户信息
            Map<Long, UserProfileSimpleVO> userMap = userFeignClient.batchGetUserProfiles(userIds);

            if (userMap != null && !userMap.isEmpty()) {
                for (AttendanceRecordVO vo : voList) {
                    if (vo.getUserId() != null) {
                        UserProfileSimpleVO user = userMap.get(vo.getUserId());
                        if (user != null) {
                            // 优先使用昵称，如果没有昵称则使用用户名
                            String displayName = StringUtils.hasText(user.getNickname())
                                    ? user.getNickname()
                                    : user.getUsername();
                            vo.setUserName(displayName);

                            // 填充头像URL
                            if (StringUtils.hasText(user.getAvatarUrl())) {
                                vo.setUserAvatarUrl(user.getAvatarUrl());
                            }
                        } else {
                            // 如果用户服务中找不到该用户，使用默认名称
                            vo.setUserName("用户" + vo.getUserId());
                        }
                    }
                }
                log.info("成功填充用户信息: userCount={}", userMap.size());
            } else {
                log.warn("用户服务返回空结果，使用默认用户名");
                setDefaultUserNames(voList);
            }
        } catch (Exception e) {
            log.error("调用用户服务失败，使用默认用户名: {}", e.getMessage(), e);
            setDefaultUserNames(voList);
        }
    }

    /**
     * 设置默认用户名（当用户服务调用失败时使用）
     *
     * @param voList 考勤记录VO列表
     */
    private void setDefaultUserNames(List<AttendanceRecordVO> voList) {
        for (AttendanceRecordVO vo : voList) {
            if (vo.getUserId() != null && !StringUtils.hasText(vo.getUserName())) {
                vo.setUserName("用户" + vo.getUserId());
            }
        }
    }

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
    @Override
    public PageVO<AttendanceRecordVO> getMyHistory(Long userId, 
                                                    LocalDate startDate, 
                                                    LocalDate endDate, 
                                                    Integer pageNum, 
                                                    Integer pageSize) {
        log.info("查询个人考勤历史: userId={}, startDate={}, endDate={}, pageNum={}, pageSize={}", 
                userId, startDate, endDate, pageNum, pageSize);

        // 构建查询条件
        LambdaQueryWrapper<AttendanceRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceRecord::getUserId, userId);

        // 日期范围过滤
        if (startDate != null) {
            queryWrapper.ge(AttendanceRecord::getAttendanceDate, startDate);
        }
        if (endDate != null) {
            queryWrapper.le(AttendanceRecord::getAttendanceDate, endDate);
        }

        // 排序：按考勤日期和签到时间降序
        queryWrapper.orderByDesc(AttendanceRecord::getAttendanceDate)
                .orderByDesc(AttendanceRecord::getCheckInTime);

        // 分页查询
        Page<AttendanceRecord> page = new Page<>(pageNum, pageSize);
        Page<AttendanceRecord> resultPage = attendanceRecordMapper.selectPage(page, queryWrapper);

        // 转换为VO
        List<AttendanceRecordVO> voList = resultPage.getRecords().stream()
                .map(record -> BeanConvertUtil.to(record, AttendanceRecordVO.class))
                .collect(Collectors.toList());

        // 填充用户信息
        fillUserInfo(voList);

        // 构建分页结果
        PageVO<AttendanceRecordVO> pageVO = new PageVO<>();
        pageVO.setTotal(resultPage.getTotal());
        pageVO.setPageNum(pageNum);
        pageVO.setPageSize(pageSize);
        pageVO.setRecords(voList);

        log.info("查询个人考勤历史成功: userId={}, total={}, records={}", 
                userId, resultPage.getTotal(), voList.size());

        return pageVO;
    }

    /**
     * 检查是否是记录所有者
     *
     * @param recordId 记录ID
     * @param userId 用户ID
     * @return 是否是所有者
     */
    @Override
    public boolean isOwner(Long recordId, Long userId) {
        if (recordId == null || userId == null) {
            return false;
        }

        AttendanceRecord record = attendanceRecordMapper.selectById(recordId);
        return record != null && record.getUserId().equals(userId);
    }

    /**
     * 导出考勤记录
     *
     * @param dto 导出条件
     * @return 导出文件的字节数组
     */
    @Override
    public byte[] exportRecords(com.hngy.siae.attendance.dto.request.AttendanceExportDTO dto) {
        log.info("开始导出考勤记录: startDate={}, endDate={}, memberIds={}, format={}", 
                dto.getStartDate(), dto.getEndDate(), dto.getMemberIds(), dto.getFormat());

        // 1. 查询符合条件的考勤记录
        LambdaQueryWrapper<AttendanceRecord> queryWrapper = new LambdaQueryWrapper<>();

        // 日期范围过滤
        if (dto.getStartDate() != null) {
            queryWrapper.ge(AttendanceRecord::getAttendanceDate, dto.getStartDate());
        }
        if (dto.getEndDate() != null) {
            queryWrapper.le(AttendanceRecord::getAttendanceDate, dto.getEndDate());
        }

        // 成员ID过滤
        if (!CollectionUtils.isEmpty(dto.getMemberIds())) {
            queryWrapper.in(AttendanceRecord::getUserId, dto.getMemberIds());
        }

        // 排序：按考勤日期和签到时间升序
        queryWrapper.orderByAsc(AttendanceRecord::getAttendanceDate)
                .orderByAsc(AttendanceRecord::getCheckInTime);

        List<AttendanceRecord> records = attendanceRecordMapper.selectList(queryWrapper);

        log.info("查询到 {} 条考勤记录待导出", records.size());

        // 2. 根据格式导出
        String format = dto.getFormat() != null ? dto.getFormat().toLowerCase() : "csv";
        
        try {
            if ("excel".equals(format) || "xlsx".equals(format)) {
                return exportToExcel(records);
            } else {
                // 默认导出为 CSV
                return exportToCsv(records);
            }
        } catch (Exception e) {
            log.error("导出考勤记录失败", e);
            throw new RuntimeException("导出考勤记录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 导出为 CSV 格式
     *
     * @param records 考勤记录列表
     * @return CSV 文件字节数组
     */
    private byte[] exportToCsv(List<AttendanceRecord> records) {
        log.info("导出为 CSV 格式");

        StringBuilder csv = new StringBuilder();
        
        // CSV 表头
        csv.append("考勤记录ID,用户ID,考勤类型,考勤日期,签到时间,签退时间,考勤时长(分钟),状态,备注\n");

        // CSV 数据行
        for (AttendanceRecord record : records) {
            csv.append(record.getId()).append(",");
            csv.append(record.getUserId()).append(",");
            csv.append(record.getAttendanceType() != null ? record.getAttendanceType().getDescription() : "").append(",");
            csv.append(record.getAttendanceDate() != null ? record.getAttendanceDate().toString() : "").append(",");
            csv.append(record.getCheckInTime() != null ? record.getCheckInTime().toString() : "").append(",");
            csv.append(record.getCheckOutTime() != null ? record.getCheckOutTime().toString() : "").append(",");
            csv.append(record.getDurationMinutes() != null ? record.getDurationMinutes() : "").append(",");
            csv.append(record.getStatus() != null ? record.getStatus().getDescription() : "").append(",");
            csv.append(record.getRemark() != null ? "\"" + record.getRemark().replace("\"", "\"\"") + "\"" : "");
            csv.append("\n");
        }

        // 转换为字节数组（使用 UTF-8 编码，添加 BOM 以便 Excel 正确识别）
        try {
            byte[] bom = new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF};
            byte[] content = csv.toString().getBytes("UTF-8");
            byte[] result = new byte[bom.length + content.length];
            System.arraycopy(bom, 0, result, 0, bom.length);
            System.arraycopy(content, 0, result, bom.length, content.length);
            return result;
        } catch (Exception e) {
            log.error("CSV 编码失败", e);
            throw new RuntimeException("CSV 编码失败", e);
        }
    }

    /**
     * 导出为 Excel 格式
     *
     * @param records 考勤记录列表
     * @return Excel 文件字节数组
     */
    private byte[] exportToExcel(List<AttendanceRecord> records) {
        log.info("导出为 Excel 格式");

        try {
            // 使用 Hutool 的 ExcelWriter
            cn.hutool.poi.excel.ExcelWriter writer = cn.hutool.poi.excel.ExcelUtil.getWriter(true);

            // 设置表头
            writer.addHeaderAlias("id", "考勤记录ID");
            writer.addHeaderAlias("userId", "用户ID");
            writer.addHeaderAlias("attendanceTypeDesc", "考勤类型");
            writer.addHeaderAlias("attendanceDate", "考勤日期");
            writer.addHeaderAlias("checkInTime", "签到时间");
            writer.addHeaderAlias("checkOutTime", "签退时间");
            writer.addHeaderAlias("durationMinutes", "考勤时长(分钟)");
            writer.addHeaderAlias("statusDesc", "状态");
            writer.addHeaderAlias("remark", "备注");

            // 转换数据为 Map 列表（便于 Hutool 处理）
            List<java.util.Map<String, Object>> rows = records.stream()
                    .map(record -> {
                        java.util.Map<String, Object> row = new java.util.LinkedHashMap<>();
                        row.put("id", record.getId());
                        row.put("userId", record.getUserId());
                        row.put("attendanceTypeDesc", record.getAttendanceType() != null ? record.getAttendanceType().getDescription() : "");
                        row.put("attendanceDate", record.getAttendanceDate() != null ? record.getAttendanceDate().toString() : "");
                        row.put("checkInTime", record.getCheckInTime() != null ? record.getCheckInTime().toString() : "");
                        row.put("checkOutTime", record.getCheckOutTime() != null ? record.getCheckOutTime().toString() : "");
                        row.put("durationMinutes", record.getDurationMinutes());
                        row.put("statusDesc", record.getStatus() != null ? record.getStatus().getDescription() : "");
                        row.put("remark", record.getRemark() != null ? record.getRemark() : "");
                        return row;
                    })
                    .collect(Collectors.toList());

            // 写入数据
            writer.write(rows, true);

            // 转换为字节数组
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            writer.flush(out, true);
            writer.close();

            return out.toByteArray();
        } catch (Exception e) {
            log.error("Excel 导出失败", e);
            throw new RuntimeException("Excel 导出失败", e);
        }
    }

    // ==================== 新增方法：简化版本（自动获取当前用户） ====================

    /**
     * 查询考勤记录详情（自动获取当前用户）
     */
    @Override
    public AttendanceRecordDetailVO getRecordDetail(Long id) {
        Long currentUserId = securityUtil.getCurrentUserId();
        return getRecord(id, currentUserId);
    }

    /**
     * 分页查询考勤记录（自动获取当前用户和权限）
     */
    @Override
    public PageVO<AttendanceRecordVO> pageQuery(PageDTO<AttendanceQueryDTO> pageDTO) {
        Long currentUserId = securityUtil.getCurrentUserId();
        boolean hasListPermission = securityUtil.hasPermission(com.hngy.siae.attendance.permissions.AttendancePermissions.Record.LIST);
        return pageQuery(pageDTO, currentUserId, hasListPermission);
    }

    /**
     * 查询个人考勤历史（自动获取当前用户）
     */
    @Override
    public PageVO<AttendanceRecordVO> getMyHistory(PageDTO<AttendanceQueryDTO> pageDTO) {
        Long currentUserId = securityUtil.getCurrentUserId();
        AttendanceQueryDTO params = pageDTO.getParams();
        LocalDate startDate = params != null ? params.getStartDate() : null;
        LocalDate endDate = params != null ? params.getEndDate() : null;
        return getMyHistory(currentUserId, startDate, endDate, pageDTO.getPageNum(), pageDTO.getPageSize());
    }

    /**
     * 导出考勤记录（直接写入响应）
     */
    @Override
    public void exportRecords(LocalDate startDate, LocalDate endDate, String memberIds, 
                             String format, jakarta.servlet.http.HttpServletResponse response) {
        // 构建导出DTO
        com.hngy.siae.attendance.dto.request.AttendanceExportDTO exportDTO = 
                new com.hngy.siae.attendance.dto.request.AttendanceExportDTO();
        exportDTO.setStartDate(startDate);
        exportDTO.setEndDate(endDate);
        exportDTO.setFormat(format);
        
        // 解析成员ID列表
        if (memberIds != null && !memberIds.trim().isEmpty()) {
            List<Long> memberIdList = java.util.Arrays.stream(memberIds.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());
            exportDTO.setMemberIds(memberIdList);
        }
        
        // 执行导出
        byte[] fileBytes = exportRecords(exportDTO);
        
        // 设置响应头并写入
        try {
            String fileName;
            String contentType;
            
            if ("excel".equalsIgnoreCase(format) || "xlsx".equalsIgnoreCase(format)) {
                fileName = "attendance_records_" + java.time.LocalDateTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".xlsx";
                contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            } else {
                fileName = "attendance_records_" + java.time.LocalDateTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv";
                contentType = "text/csv";
            }
            
            String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8")
                    .replaceAll("\\+", "%20");
            
            response.setContentType(contentType);
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
            response.setContentLength(fileBytes.length);
            
            response.getOutputStream().write(fileBytes);
            response.getOutputStream().flush();
            
            log.info("导出考勤记录成功: fileName={}, size={} bytes", fileName, fileBytes.length);
            
        } catch (Exception e) {
            log.error("导出考勤记录失败", e);
            throw new RuntimeException("导出考勤记录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 分页查询活动考勤记录
     */
    @Override
    public PageVO<AttendanceRecordVO> pageQueryActivityRecords(PageDTO<AttendanceQueryDTO> pageDTO) {
        // 确保查询条件中设置了活动考勤类型
        if (pageDTO.getParams() == null) {
            pageDTO.setParams(new AttendanceQueryDTO());
        }
        pageDTO.getParams().setAttendanceType(AttendanceType.ACTIVITY);
        
        // 调用通用分页查询
        Long currentUserId = securityUtil.getCurrentUserId();
        boolean hasListPermission = securityUtil.hasPermission(com.hngy.siae.attendance.permissions.AttendancePermissions.Record.LIST);
        
        return pageQuery(pageDTO, currentUserId, hasListPermission);
    }

    /**
     * 查询活动考勤记录列表
     */
    @Override
    public List<AttendanceRecordVO> listActivityRecords(Long activityId, Long userId) {
        log.info("查询活动考勤记录列表: activityId={}, userId={}", activityId, userId);
        
        // 构建查询条件
        LambdaQueryWrapper<AttendanceRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceRecord::getAttendanceType, AttendanceType.ACTIVITY)
                .eq(AttendanceRecord::getRelatedId, activityId);
        
        if (userId != null) {
            queryWrapper.eq(AttendanceRecord::getUserId, userId);
        }
        
        queryWrapper.orderByDesc(AttendanceRecord::getCheckInTime);
        
        List<AttendanceRecord> records = attendanceRecordMapper.selectList(queryWrapper);

        List<AttendanceRecordVO> voList = records.stream()
                .map(record -> BeanConvertUtil.to(record, AttendanceRecordVO.class))
                .collect(Collectors.toList());

        // 填充用户信息
        fillUserInfo(voList);

        return voList;
    }

    /**
     * 查询个人活动考勤历史（自动获取当前用户）
     */
    @Override
    public PageVO<AttendanceRecordVO> getMyActivityHistory(PageDTO<AttendanceQueryDTO> pageDTO) {
        Long currentUserId = securityUtil.getCurrentUserId();
        AttendanceQueryDTO params = pageDTO.getParams();
        LocalDate startDate = params != null ? params.getStartDate() : null;
        LocalDate endDate = params != null ? params.getEndDate() : null;
        
        log.info("查询个人活动考勤历史: userId={}, startDate={}, endDate={}", 
                currentUserId, startDate, endDate);
        
        // 构建查询条件
        LambdaQueryWrapper<AttendanceRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceRecord::getUserId, currentUserId)
                .eq(AttendanceRecord::getAttendanceType, AttendanceType.ACTIVITY);
        
        if (startDate != null) {
            queryWrapper.ge(AttendanceRecord::getAttendanceDate, startDate);
        }
        if (endDate != null) {
            queryWrapper.le(AttendanceRecord::getAttendanceDate, endDate);
        }
        
        queryWrapper.orderByDesc(AttendanceRecord::getAttendanceDate)
                .orderByDesc(AttendanceRecord::getCheckInTime);
        
        // 分页查询
        Page<AttendanceRecord> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        Page<AttendanceRecord> resultPage = attendanceRecordMapper.selectPage(page, queryWrapper);
        
        // 转换为VO
        List<AttendanceRecordVO> voList = resultPage.getRecords().stream()
                .map(record -> BeanConvertUtil.to(record, AttendanceRecordVO.class))
                .collect(Collectors.toList());

        // 填充用户信息
        fillUserInfo(voList);

        // 构建分页结果
        PageVO<AttendanceRecordVO> pageVO = new PageVO<>();
        pageVO.setTotal(resultPage.getTotal());
        pageVO.setPageNum(pageDTO.getPageNum());
        pageVO.setPageSize(pageDTO.getPageSize());
        pageVO.setRecords(voList);

        return pageVO;
    }

}
