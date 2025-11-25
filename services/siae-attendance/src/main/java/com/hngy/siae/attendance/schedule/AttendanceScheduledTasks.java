package com.hngy.siae.attendance.schedule;

import com.hngy.siae.attendance.service.IAnomalyDetectionService;
import com.hngy.siae.attendance.service.IStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * 考勤定时任务
 *
 * @author SIAE Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AttendanceScheduledTasks {

    private final IAnomalyDetectionService anomalyDetectionService;
    private final IStatisticsService statisticsService;

    /**
     * 自动检测缺勤定时任务
     * 每天23:00执行，检测当天未签到的成员
     */
    @Scheduled(cron = "0 0 23 * * ?")
    public void detectDailyAbsence() {
        LocalDate today = LocalDate.now();
        log.info("开始执行自动缺勤检测定时任务: date={}", today);
        
        try {
            anomalyDetectionService.autoDetectAbsence(today);
            log.info("自动缺勤检测定时任务执行成功: date={}", today);
        } catch (Exception e) {
            log.error("自动缺勤检测定时任务执行失败: date={}", today, e);
            // 不抛出异常，避免影响后续定时任务的执行
        }
    }

    /**
     * 生成月度统计定时任务
     * 每月1日凌晨1:00执行，生成上个月的统计数据
     */
    @Scheduled(cron = "0 0 1 1 * ?")
    public void generateMonthlyStatistics() {
        // 生成上个月的统计
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        String month = lastMonth.toString();
        
        log.info("开始执行月度统计生成定时任务: month={}", month);
        
        try {
            statisticsService.generateMonthlyStatistics(month);
            log.info("月度统计生成定时任务执行成功: month={}", month);
        } catch (Exception e) {
            log.error("月度统计生成定时任务执行失败: month={}", month, e);
            // 不抛出异常，避免影响后续定时任务的执行
        }
    }
}
