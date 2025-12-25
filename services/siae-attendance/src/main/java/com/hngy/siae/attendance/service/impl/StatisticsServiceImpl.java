package com.hngy.siae.attendance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hngy.siae.attendance.constant.CacheConstants;
import com.hngy.siae.attendance.dto.request.ReportGenerateDTO;
import com.hngy.siae.attendance.dto.response.AttendanceAnomalyVO;
import com.hngy.siae.attendance.dto.response.AttendanceStatisticsVO;
import com.hngy.siae.attendance.dto.response.DepartmentStatisticsVO;
import com.hngy.siae.attendance.dto.response.ReportVO;
import com.hngy.siae.attendance.entity.AttendanceAnomaly;
import com.hngy.siae.attendance.entity.AttendanceRecord;
import com.hngy.siae.attendance.entity.AttendanceStatistics;
import com.hngy.siae.attendance.entity.LeaveRequest;
import com.hngy.siae.attendance.enums.AnomalyType;
import com.hngy.siae.attendance.enums.AttendanceStatus;
import com.hngy.siae.attendance.enums.LeaveStatus;
import com.hngy.siae.attendance.mapper.AttendanceAnomalyMapper;
import com.hngy.siae.attendance.mapper.AttendanceRecordMapper;
import com.hngy.siae.attendance.mapper.AttendanceStatisticsMapper;
import com.hngy.siae.attendance.mapper.LeaveRequestMapper;
import com.hngy.siae.attendance.service.IStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 考勤统计服务实现
 *
 * @author SIAE Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements IStatisticsService {

    private final AttendanceRecordMapper attendanceRecordMapper;
    private final AttendanceAnomalyMapper attendanceAnomalyMapper;
    private final LeaveRequestMapper leaveRequestMapper;
    private final AttendanceStatisticsMapper attendanceStatisticsMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final com.hngy.siae.security.utils.SecurityUtil securityUtil;

    @Override
    public AttendanceStatisticsVO calculatePersonalStatistics(Long userId, LocalDate startDate, LocalDate endDate) {
        // 如果没有指定日期范围，默认查询当月
        if (startDate == null || endDate == null) {
            LocalDate now = LocalDate.now();
            startDate = now.withDayOfMonth(1);
            endDate = now.withDayOfMonth(now.lengthOfMonth());
        }
        
        log.info("计算个人考勤统计: userId={}, startDate={}, endDate={}", userId, startDate, endDate);

        // 尝试从缓存获取
        String cacheKey = CacheConstants.generateKey(CacheConstants.CACHE_PERSONAL_STATISTICS, userId, startDate, endDate);
        AttendanceStatisticsVO cachedStats = (AttendanceStatisticsVO) redisTemplate.opsForValue().get(cacheKey);
        if (cachedStats != null) {
            log.debug("从缓存获取个人考勤统计: userId={}", userId);
            return cachedStats;
        }

        // 查询考勤记录
        List<AttendanceRecord> records = attendanceRecordMapper.selectList(
            new LambdaQueryWrapper<AttendanceRecord>()
                .eq(AttendanceRecord::getUserId, userId)
                .between(AttendanceRecord::getAttendanceDate, startDate, endDate)
                .eq(AttendanceRecord::getDeleted, 0)
        );

        // 查询考勤异常
        List<AttendanceAnomaly> anomalies = attendanceAnomalyMapper.selectList(
            new LambdaQueryWrapper<AttendanceAnomaly>()
                .eq(AttendanceAnomaly::getUserId, userId)
                .between(AttendanceAnomaly::getAnomalyDate, startDate, endDate)
                .eq(AttendanceAnomaly::getDeleted, 0)
        );

        // 查询请假记录
        List<LeaveRequest> leaves = leaveRequestMapper.selectList(
            new LambdaQueryWrapper<LeaveRequest>()
                .eq(LeaveRequest::getUserId, userId)
                .eq(LeaveRequest::getStatus, LeaveStatus.APPROVED.getCode())
                .le(LeaveRequest::getStartDate, endDate)
                .ge(LeaveRequest::getEndDate, startDate)
                .eq(LeaveRequest::getDeleted, 0)
        );

        // 计算统计数据
        AttendanceStatisticsVO vo = new AttendanceStatisticsVO();
        vo.setUserId(userId);
        vo.setStatMonth(startDate.getYear() + "-" + String.format("%02d", startDate.getMonthValue()));

        // 计算应出勤天数（工作日天数）
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        vo.setTotalDays((int) totalDays);

        // 计算实际出勤天数（已完成的考勤记录）
        long actualDays = records.stream()
            .filter(r -> r.getStatus() == AttendanceStatus.COMPLETED)
            .count();
        vo.setActualDays((int) actualDays);

        // 统计异常次数（只统计未解决的异常，已解决的异常不计入统计）
        long lateCount = anomalies.stream()
            .filter(a -> a.getAnomalyType() == AnomalyType.LATE)
            .filter(a -> !Boolean.TRUE.equals(a.getResolved())) // 排除已解决的异常
            .count();
        vo.setLateCount((int) lateCount);

        long earlyCount = anomalies.stream()
            .filter(a -> a.getAnomalyType() == AnomalyType.EARLY_DEPARTURE)
            .filter(a -> !Boolean.TRUE.equals(a.getResolved())) // 排除已解决的异常
            .count();
        vo.setEarlyCount((int) earlyCount);

        long absenceCount = anomalies.stream()
            .filter(a -> a.getAnomalyType() == AnomalyType.ABSENCE)
            .filter(a -> a.getSuppressedByLeave() == null) // 排除被请假抑制的缺勤
            .filter(a -> !Boolean.TRUE.equals(a.getResolved())) // 排除已解决的异常
            .count();
        vo.setAbsenceCount((int) absenceCount);

        // 计算请假天数
        BigDecimal totalLeaveDays = leaves.stream()
            .map(LeaveRequest::getDays)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setLeaveDays(totalLeaveDays);

        // 计算总考勤时长
        int totalDurationMinutes = records.stream()
            .filter(r -> r.getDurationMinutes() != null)
            .mapToInt(AttendanceRecord::getDurationMinutes)
            .sum();
        vo.setTotalDurationMinutes(totalDurationMinutes);

        // 计算出勤率
        BigDecimal attendanceRate = calculateAttendanceRate(userId, startDate, endDate);
        vo.setAttendanceRate(attendanceRate);

        // 缓存结果
        redisTemplate.opsForValue().set(cacheKey, vo, 
                CacheConstants.CACHE_PERSONAL_STATISTICS_TTL, TimeUnit.SECONDS);

        log.info("个人考勤统计计算完成: userId={}, actualDays={}, attendanceRate={}%", 
            userId, actualDays, attendanceRate);

        return vo;
    }

    @Override
    public DepartmentStatisticsVO calculateDepartmentStatistics(Long departmentId, LocalDate startDate, LocalDate endDate) {
        // 如果没有指定日期范围，默认查询当月
        if (startDate == null || endDate == null) {
            LocalDate now = LocalDate.now();
            startDate = now.withDayOfMonth(1);
            endDate = now.withDayOfMonth(now.lengthOfMonth());
        }
        
        // 使用 final 变量以便在 lambda 中使用
        final LocalDate finalStartDate = startDate;
        final LocalDate finalEndDate = endDate;
        
        log.info("计算部门考勤统计: departmentId={}, startDate={}, endDate={}", departmentId, finalStartDate, finalEndDate);

        // 尝试从缓存获取
        String cacheKey = CacheConstants.generateKey(CacheConstants.CACHE_DEPARTMENT_STATISTICS, departmentId, finalStartDate, finalEndDate);
        DepartmentStatisticsVO cachedStats = (DepartmentStatisticsVO) redisTemplate.opsForValue().get(cacheKey);
        if (cachedStats != null) {
            log.debug("从缓存获取部门考勤统计: departmentId={}", departmentId);
            return cachedStats;
        }

        // TODO: 需要从用户服务获取部门成员列表
        // 这里暂时使用模拟数据，实际应该调用用户服务
        List<Long> memberIds = getMemberIdsByDepartment(departmentId);

        if (memberIds.isEmpty()) {
            log.warn("部门没有成员: departmentId={}", departmentId);
            DepartmentStatisticsVO vo = new DepartmentStatisticsVO();
            vo.setDepartmentId(departmentId);
            vo.setStatMonth(finalStartDate.getYear() + "-" + String.format("%02d", finalStartDate.getMonthValue()));
            vo.setTotalMembers(0);
            vo.setAvgAttendanceRate(BigDecimal.ZERO);
            vo.setTotalLateCount(0);
            vo.setTotalEarlyCount(0);
            vo.setTotalAbsenceCount(0);
            vo.setTotalLeaveDays(BigDecimal.ZERO);
            vo.setMemberStatistics(new ArrayList<>());
            return vo;
        }

        // 计算每个成员的统计
        List<AttendanceStatisticsVO> memberStatistics = memberIds.stream()
            .map(memberId -> calculatePersonalStatistics(memberId, finalStartDate, finalEndDate))
            .collect(Collectors.toList());

        // 汇总部门统计
        DepartmentStatisticsVO vo = new DepartmentStatisticsVO();
        vo.setDepartmentId(departmentId);
        vo.setStatMonth(startDate.getYear() + "-" + String.format("%02d", startDate.getMonthValue()));
        vo.setTotalMembers(memberStatistics.size());

        // 计算平均出勤率
        BigDecimal avgAttendanceRate = memberStatistics.stream()
            .map(AttendanceStatisticsVO::getAttendanceRate)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(memberStatistics.size()), 2, RoundingMode.HALF_UP);
        vo.setAvgAttendanceRate(avgAttendanceRate);

        // 汇总异常次数
        int totalLateCount = memberStatistics.stream()
            .mapToInt(AttendanceStatisticsVO::getLateCount)
            .sum();
        vo.setTotalLateCount(totalLateCount);

        int totalEarlyCount = memberStatistics.stream()
            .mapToInt(AttendanceStatisticsVO::getEarlyCount)
            .sum();
        vo.setTotalEarlyCount(totalEarlyCount);

        int totalAbsenceCount = memberStatistics.stream()
            .mapToInt(AttendanceStatisticsVO::getAbsenceCount)
            .sum();
        vo.setTotalAbsenceCount(totalAbsenceCount);

        // 汇总请假天数
        BigDecimal totalLeaveDays = memberStatistics.stream()
            .map(AttendanceStatisticsVO::getLeaveDays)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setTotalLeaveDays(totalLeaveDays);

        vo.setMemberStatistics(memberStatistics);

        // 缓存结果
        redisTemplate.opsForValue().set(cacheKey, vo, 
                CacheConstants.CACHE_DEPARTMENT_STATISTICS_TTL, TimeUnit.SECONDS);

        log.info("部门考勤统计计算完成: departmentId={}, totalMembers={}, avgAttendanceRate={}%", 
            departmentId, memberStatistics.size(), avgAttendanceRate);

        return vo;
    }

    @Override
    public BigDecimal calculateAttendanceRate(Long userId, LocalDate startDate, LocalDate endDate) {
        // 如果没有指定日期范围，默认查询当月
        if (startDate == null || endDate == null) {
            LocalDate now = LocalDate.now();
            startDate = now.withDayOfMonth(1);
            endDate = now.withDayOfMonth(now.lengthOfMonth());
        }
        
        log.debug("计算出勤率: userId={}, startDate={}, endDate={}", userId, startDate, endDate);

        // 计算应出勤天数
        long totalDays = ChronoUnit.DAYS.between(startDate, endDate) + 1;

        // 查询实际出勤天数
        long actualDays = attendanceRecordMapper.selectCount(
            new LambdaQueryWrapper<AttendanceRecord>()
                .eq(AttendanceRecord::getUserId, userId)
                .between(AttendanceRecord::getAttendanceDate, startDate, endDate)
                .eq(AttendanceRecord::getStatus, AttendanceStatus.COMPLETED)
                .eq(AttendanceRecord::getDeleted, 0)
        );

        // 查询请假天数（已批准的请假）
        List<LeaveRequest> leaves = leaveRequestMapper.selectList(
            new LambdaQueryWrapper<LeaveRequest>()
                .eq(LeaveRequest::getUserId, userId)
                .eq(LeaveRequest::getStatus, LeaveStatus.APPROVED.getCode())
                .le(LeaveRequest::getStartDate, endDate)
                .ge(LeaveRequest::getEndDate, startDate)
                .eq(LeaveRequest::getDeleted, 0)
        );

        BigDecimal totalLeaveDays = leaves.stream()
            .map(LeaveRequest::getDays)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 计算出勤率 = (实际出勤天数 / (应出勤天数 - 请假天数)) × 100
        BigDecimal requiredDays = BigDecimal.valueOf(totalDays).subtract(totalLeaveDays);
        
        if (requiredDays.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal attendanceRate = BigDecimal.valueOf(actualDays)
            .divide(requiredDays, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100))
            .setScale(2, RoundingMode.HALF_UP);

        log.debug("出勤率计算完成: userId={}, actualDays={}, requiredDays={}, rate={}%", 
            userId, actualDays, requiredDays, attendanceRate);

        return attendanceRate;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateMonthlyStatistics(String month) {
        log.info("开始生成月度统计: month={}", month);

        try {
            // 解析月份
            YearMonth yearMonth = YearMonth.parse(month);
            LocalDate startDate = yearMonth.atDay(1);
            LocalDate endDate = yearMonth.atEndOfMonth();

            // TODO: 获取所有需要统计的用户ID列表
            // 这里暂时使用查询所有有考勤记录的用户
            List<Long> userIds = attendanceRecordMapper.selectList(
                new LambdaQueryWrapper<AttendanceRecord>()
                    .between(AttendanceRecord::getAttendanceDate, startDate, endDate)
                    .eq(AttendanceRecord::getDeleted, 0)
                    .select(AttendanceRecord::getUserId)
            ).stream()
                .map(AttendanceRecord::getUserId)
                .distinct()
                .collect(Collectors.toList());

            log.info("需要生成统计的用户数量: {}", userIds.size());

            // 为每个用户生成统计
            for (Long userId : userIds) {
                try {
                    generateUserMonthlyStatistics(userId, month, startDate, endDate);
                } catch (Exception e) {
                    log.error("生成用户月度统计失败: userId={}, month={}", userId, month, e);
                    // 继续处理下一个用户
                }
            }

            log.info("月度统计生成完成: month={}, userCount={}", month, userIds.size());
        } catch (Exception e) {
            log.error("生成月度统计失败: month={}", month, e);
            throw e;
        }
    }

    /**
     * 生成单个用户的月度统计
     */
    private void generateUserMonthlyStatistics(Long userId, String month, LocalDate startDate, LocalDate endDate) {
        log.debug("生成用户月度统计: userId={}, month={}", userId, month);

        // 计算统计数据
        AttendanceStatisticsVO statisticsVO = calculatePersonalStatistics(userId, startDate, endDate);

        // 检查是否已存在统计记录
        AttendanceStatistics existingStats = attendanceStatisticsMapper.selectOne(
            new LambdaQueryWrapper<AttendanceStatistics>()
                .eq(AttendanceStatistics::getUserId, userId)
                .eq(AttendanceStatistics::getStatMonth, month)
        );

        AttendanceStatistics statistics = new AttendanceStatistics();
        if (existingStats != null) {
            statistics.setId(existingStats.getId());
        }

        // 复制数据
        statistics.setUserId(userId);
        statistics.setStatMonth(month);
        statistics.setTotalDays(statisticsVO.getTotalDays());
        statistics.setActualDays(statisticsVO.getActualDays());
        statistics.setLateCount(statisticsVO.getLateCount());
        statistics.setEarlyCount(statisticsVO.getEarlyCount());
        statistics.setAbsenceCount(statisticsVO.getAbsenceCount());
        statistics.setLeaveDays(statisticsVO.getLeaveDays());
        statistics.setTotalDurationMinutes(statisticsVO.getTotalDurationMinutes());
        statistics.setAttendanceRate(statisticsVO.getAttendanceRate());

        // 保存或更新
        if (existingStats != null) {
            attendanceStatisticsMapper.updateById(statistics);
            log.debug("更新用户月度统计: userId={}, month={}", userId, month);
        } else {
            attendanceStatisticsMapper.insert(statistics);
            log.debug("创建用户月度统计: userId={}, month={}", userId, month);
        }
    }

    @Override
    public ReportVO generateReport(ReportGenerateDTO dto) {
        log.info("生成报表: reportType={}, startDate={}, endDate={}, format={}", 
            dto.getReportType(), dto.getStartDate(), dto.getEndDate(), dto.getFormat());

        // 验证报表类型
        if (dto.getReportType() == null) {
            throw new IllegalArgumentException("报表类型不能为空");
        }

        // 设置默认格式
        String format = dto.getFormat() != null ? dto.getFormat().toLowerCase() : "csv";
        if (!format.equals("csv") && !format.equals("pdf")) {
            log.warn("不支持的报表格式: {}, 使用默认格式csv", format);
            format = "csv";
        }

        // 根据报表类型生成报表
        byte[] reportData;
        switch (dto.getReportType().toLowerCase()) {
            case "monthly":
                reportData = generateMonthlyReportData(dto, format);
                break;
            case "anomaly":
                reportData = generateAnomalyReportData(dto, format);
                break;
            case "department":
                reportData = generateDepartmentReportData(dto, format);
                break;
            default:
                throw new IllegalArgumentException("不支持的报表类型: " + dto.getReportType());
        }

        // 生成报表文件名
        String reportId = UUID.randomUUID().toString();
        String fileName = generateReportFileName(dto, reportId, format);

        // TODO: 实际生产环境中应该将文件上传到文件服务（如MinIO、OSS等）
        // 这里暂时保存到本地临时目录或返回Base64编码
        String fileUrl = saveReportFile(reportId, fileName, reportData);

        // 构建返回结果
        ReportVO vo = new ReportVO();
        vo.setReportId(reportId);
        vo.setReportType(dto.getReportType());
        vo.setReportName(generateReportName(dto));
        vo.setFormat(format);
        vo.setGeneratedAt(LocalDateTime.now());
        vo.setFileUrl(fileUrl);
        
        // TODO: 从安全上下文获取当前用户信息
        vo.setGeneratedBy(1L);
        vo.setGeneratorName("System");

        log.info("报表生成完成: reportId={}, reportName={}, format={}, size={} bytes", 
            vo.getReportId(), vo.getReportName(), format, reportData.length);

        return vo;
    }

    /**
     * 生成月度考勤报表数据
     */
    private byte[] generateMonthlyReportData(ReportGenerateDTO dto, String format) {
        log.debug("生成月度考勤报表数据");

        // 获取用户ID列表
        List<Long> userIds = dto.getUserIds();
        if (userIds == null || userIds.isEmpty()) {
            // 如果没有指定用户，查询所有有考勤记录的用户
            userIds = attendanceRecordMapper.selectList(
                new LambdaQueryWrapper<AttendanceRecord>()
                    .between(AttendanceRecord::getAttendanceDate, dto.getStartDate(), dto.getEndDate())
                    .eq(AttendanceRecord::getDeleted, 0)
                    .select(AttendanceRecord::getUserId)
            ).stream()
                .map(AttendanceRecord::getUserId)
                .distinct()
                .collect(Collectors.toList());
        }

        // 计算每个用户的统计数据
        List<AttendanceStatisticsVO> statistics = userIds.stream()
            .map(userId -> calculatePersonalStatistics(userId, dto.getStartDate(), dto.getEndDate()))
            .collect(Collectors.toList());

        // 根据格式生成报表
        if ("pdf".equals(format)) {
            return com.hngy.siae.attendance.util.PdfReportGenerator.generateMonthlyReport(statistics);
        } else {
            return com.hngy.siae.attendance.util.CsvReportGenerator.generateMonthlyReport(statistics);
        }
    }

    /**
     * 生成考勤异常报表数据
     */
    private byte[] generateAnomalyReportData(ReportGenerateDTO dto, String format) {
        log.debug("生成考勤异常报表数据");

        // 查询异常记录
        LambdaQueryWrapper<AttendanceAnomaly> queryWrapper = new LambdaQueryWrapper<AttendanceAnomaly>()
            .between(AttendanceAnomaly::getAnomalyDate, dto.getStartDate(), dto.getEndDate())
            .eq(AttendanceAnomaly::getDeleted, 0);

        // 如果指定了用户ID，添加过滤条件
        if (dto.getUserIds() != null && !dto.getUserIds().isEmpty()) {
            queryWrapper.in(AttendanceAnomaly::getUserId, dto.getUserIds());
        }

        // 按异常类型和用户ID排序
        queryWrapper.orderByAsc(AttendanceAnomaly::getAnomalyType, AttendanceAnomaly::getUserId);

        List<AttendanceAnomaly> anomalies = attendanceAnomalyMapper.selectList(queryWrapper);

        // 转换为VO
        List<AttendanceAnomalyVO> anomalyVOs = anomalies.stream()
            .map(this::convertToAnomalyVO)
            .collect(Collectors.toList());

        // 根据格式生成报表
        if ("pdf".equals(format)) {
            return com.hngy.siae.attendance.util.PdfReportGenerator.generateAnomalyReport(anomalyVOs);
        } else {
            return com.hngy.siae.attendance.util.CsvReportGenerator.generateAnomalyReport(anomalyVOs);
        }
    }

    /**
     * 生成部门考勤报表数据
     */
    private byte[] generateDepartmentReportData(ReportGenerateDTO dto, String format) {
        log.debug("生成部门考勤报表数据");

        // 获取部门ID列表
        List<Long> departmentIds = dto.getDepartmentIds();
        if (departmentIds == null || departmentIds.isEmpty()) {
            throw new IllegalArgumentException("部门考勤报表必须指定部门ID");
        }

        // 为每个部门生成统计数据
        List<AttendanceStatisticsVO> allStatistics = new ArrayList<>();
        for (Long departmentId : departmentIds) {
            DepartmentStatisticsVO deptStats = calculateDepartmentStatistics(
                departmentId, dto.getStartDate(), dto.getEndDate());
            allStatistics.addAll(deptStats.getMemberStatistics());
        }

        // 根据格式生成报表
        if ("pdf".equals(format)) {
            return com.hngy.siae.attendance.util.PdfReportGenerator.generateMonthlyReport(allStatistics);
        } else {
            return com.hngy.siae.attendance.util.CsvReportGenerator.generateMonthlyReport(allStatistics);
        }
    }

    /**
     * 转换异常实体为VO
     */
    private AttendanceAnomalyVO convertToAnomalyVO(AttendanceAnomaly anomaly) {
        AttendanceAnomalyVO vo = new AttendanceAnomalyVO();
        BeanUtils.copyProperties(anomaly, vo);
        
        // 异常类型枚举会自动复制
        // TODO: 如果需要，可以从用户服务获取用户名称和处理人名称
        
        return vo;
    }

    /**
     * 保存报表文件
     * TODO: 实际应该上传到文件服务
     */
    private String saveReportFile(String reportId, String fileName, byte[] data) {
        // 这里暂时返回一个模拟的URL
        // 实际生产环境中应该：
        // 1. 将文件上传到MinIO/OSS等对象存储
        // 2. 或者保存到本地文件系统的指定目录
        // 3. 返回可访问的URL
        
        log.debug("保存报表文件: reportId={}, fileName={}, size={} bytes", reportId, fileName, data.length);
        
        // 返回模拟URL
        return "/api/v1/attendance/reports/download/" + reportId;
    }

    /**
     * 生成报表文件名
     */
    private String generateReportFileName(ReportGenerateDTO dto, String reportId, String format) {
        String typeName = switch (dto.getReportType().toLowerCase()) {
            case "monthly" -> "monthly_attendance";
            case "anomaly" -> "attendance_anomaly";
            case "department" -> "department_attendance";
            default -> "attendance_report";
        };

        return String.format("%s_%s_%s_%s.%s", 
            typeName,
            dto.getStartDate().toString(),
            dto.getEndDate().toString(),
            reportId.substring(0, 8),
            format);
    }

    /**
     * 生成报表名称
     */
    private String generateReportName(ReportGenerateDTO dto) {
        String typeName = switch (dto.getReportType()) {
            case "monthly" -> "月度考勤报表";
            case "anomaly" -> "考勤异常报表";
            case "department" -> "部门考勤报表";
            default -> "考勤报表";
        };

        return String.format("%s_%s_%s", 
            typeName, 
            dto.getStartDate(), 
            dto.getEndDate());
    }

    /**
     * 获取部门成员ID列表
     * TODO: 实际应该调用用户服务获取
     */
    private List<Long> getMemberIdsByDepartment(Long departmentId) {
        // 暂时返回空列表，实际应该调用用户服务
        log.warn("getMemberIdsByDepartment 方法需要实现用户服务调用: departmentId={}", departmentId);
        return new ArrayList<>();
    }

    @Override
    @Cacheable(value = CacheConstants.CACHE_ACTIVITY_STATISTICS, key = "#activityId")
    public com.hngy.siae.attendance.dto.response.ActivityAttendanceStatisticsVO calculateActivityStatistics(Long activityId) {
        log.info("计算活动考勤统计，活动ID: {}", activityId);

        // 查询活动的所有考勤记录
        LambdaQueryWrapper<AttendanceRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceRecord::getAttendanceType, com.hngy.siae.attendance.enums.AttendanceType.ACTIVITY)
                .eq(AttendanceRecord::getRelatedId, activityId);

        List<AttendanceRecord> records = attendanceRecordMapper.selectList(queryWrapper);

        // 创建统计VO
        com.hngy.siae.attendance.dto.response.ActivityAttendanceStatisticsVO statistics = 
                new com.hngy.siae.attendance.dto.response.ActivityAttendanceStatisticsVO();
        statistics.setActivityId(activityId);
        statistics.setTotalRecords(records.size());

        if (records.isEmpty()) {
            log.info("活动 {} 暂无考勤记录", activityId);
            statistics.setCheckInCount(0);
            statistics.setCheckOutCount(0);
            statistics.setCompletedCount(0);
            statistics.setAbnormalCount(0);
            statistics.setAverageDurationMinutes(0);
            return statistics;
        }

        // 统计签到人数
        long checkInCount = records.stream()
                .filter(r -> r.getCheckInTime() != null)
                .count();

        // 统计签退人数
        long checkOutCount = records.stream()
                .filter(r -> r.getCheckOutTime() != null)
                .count();

        // 统计完成考勤人数（签到且签退）
        long completedCount = records.stream()
                .filter(r -> r.getCheckInTime() != null && r.getCheckOutTime() != null)
                .count();

        // 统计异常记录数
        long abnormalCount = records.stream()
                .filter(r -> AttendanceStatus.ABNORMAL.equals(r.getStatus()))
                .count();

        // 计算平均考勤时长（分钟）
        double avgDuration = records.stream()
                .filter(r -> r.getDurationMinutes() != null && r.getDurationMinutes() > 0)
                .mapToInt(AttendanceRecord::getDurationMinutes)
                .average()
                .orElse(0.0);

        statistics.setCheckInCount((int) checkInCount);
        statistics.setCheckOutCount((int) checkOutCount);
        statistics.setCompletedCount((int) completedCount);
        statistics.setAbnormalCount((int) abnormalCount);
        statistics.setAverageDurationMinutes((int) Math.round(avgDuration));

        log.info("活动考勤统计完成: activityId={}, totalRecords={}, checkInCount={}, checkOutCount={}, completedCount={}, abnormalCount={}, avgDuration={}分钟",
                activityId, statistics.getTotalRecords(), statistics.getCheckInCount(), 
                statistics.getCheckOutCount(), statistics.getCompletedCount(), 
                statistics.getAbnormalCount(), statistics.getAverageDurationMinutes());

        return statistics;
    }

    @Override
    public AttendanceStatisticsVO getMyCurrentMonthStatistics() {
        Long currentUserId = securityUtil.getCurrentUserId();
        log.info("查询个人当月考勤统计: userId={}", currentUserId);
        
        // 查询当月统计
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        
        return calculatePersonalStatistics(currentUserId, startDate, endDate);
    }

    @Override
    public BigDecimal getMyAttendanceRate(LocalDate startDate, LocalDate endDate) {
        Long currentUserId = securityUtil.getCurrentUserId();
        log.info("查询个人出勤率: userId={}, startDate={}, endDate={}", 
                currentUserId, startDate, endDate);
        
        return calculateAttendanceRate(currentUserId, startDate, endDate);
    }

    @Override
    public void exportReport(String reportType, LocalDate startDate, LocalDate endDate, 
                            String format, jakarta.servlet.http.HttpServletResponse response) {
        log.info("导出考勤报表: reportType={}, startDate={}, endDate={}, format={}", 
                reportType, startDate, endDate, format);
        
        // 构建报表生成DTO
        ReportGenerateDTO dto = new ReportGenerateDTO();
        dto.setReportType(reportType);
        dto.setStartDate(startDate);
        dto.setEndDate(endDate);
        dto.setFormat(format);
        
        // 设置响应头并输出文件
        try {
            String fileName;
            String contentType;
            
            if ("pdf".equalsIgnoreCase(format)) {
                fileName = "attendance_report_" + LocalDateTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".pdf";
                contentType = "application/pdf";
            } else {
                fileName = "attendance_report_" + LocalDateTime.now().format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) + ".csv";
                contentType = "text/csv";
            }
            
            // 设置文件名（URL 编码以支持中文）
            String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8")
                    .replaceAll("\\+", "%20");
            
            response.setContentType(contentType);
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + encodedFileName + "\"");
            
            // 直接生成报表内容
            byte[] content = null;
            switch (reportType.toLowerCase()) {
                case "monthly":
                    content = generateMonthlyReportData(dto, format);
                    break;
                case "anomaly":
                    content = generateAnomalyReportData(dto, format);
                    break;
                case "department":
                    content = generateDepartmentReportData(dto, format);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的报表类型: " + reportType);
            }
            
            if (content != null && content.length > 0) {
                response.setContentLength(content.length);
                response.getOutputStream().write(content);
                response.getOutputStream().flush();
                
                log.info("导出考勤报表成功: fileName={}, size={} bytes", fileName, content.length);
            } else {
                log.warn("报表内容为空");
                response.setStatus(jakarta.servlet.http.HttpServletResponse.SC_NO_CONTENT);
            }
            
        } catch (Exception e) {
            log.error("导出考勤报表失败", e);
            throw new RuntimeException("导出考勤报表失败: " + e.getMessage(), e);
        }
    }
}
