package com.hngy.siae.attendance.util;

import cn.hutool.core.io.IoUtil;
import com.hngy.siae.attendance.dto.response.AttendanceAnomalyVO;
import com.hngy.siae.attendance.dto.response.AttendanceStatisticsVO;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * CSV报表生成器
 *
 * @author SIAE Team
 */
@Slf4j
public class CsvReportGenerator {

    private static final String CSV_SEPARATOR = ",";
    private static final String LINE_SEPARATOR = "\n";

    /**
     * 生成月度考勤报表CSV
     *
     * @param statistics 统计数据列表
     * @return CSV字节数组
     */
    public static byte[] generateMonthlyReport(List<AttendanceStatisticsVO> statistics) {
        log.debug("生成月度考勤报表CSV, 记录数: {}", statistics.size());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);

        try {
            // 写入BOM以支持Excel正确识别UTF-8
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);

            // 写入表头
            writer.write("用户ID,统计月份,应出勤天数,实际出勤天数,迟到次数,早退次数,缺勤次数,请假天数,总考勤时长(分钟),出勤率(%)");
            writer.write(LINE_SEPARATOR);

            // 写入数据行
            for (AttendanceStatisticsVO stat : statistics) {
                writer.write(String.valueOf(stat.getUserId()));
                writer.write(CSV_SEPARATOR);
                writer.write(stat.getStatMonth());
                writer.write(CSV_SEPARATOR);
                writer.write(String.valueOf(stat.getTotalDays()));
                writer.write(CSV_SEPARATOR);
                writer.write(String.valueOf(stat.getActualDays()));
                writer.write(CSV_SEPARATOR);
                writer.write(String.valueOf(stat.getLateCount()));
                writer.write(CSV_SEPARATOR);
                writer.write(String.valueOf(stat.getEarlyCount()));
                writer.write(CSV_SEPARATOR);
                writer.write(String.valueOf(stat.getAbsenceCount()));
                writer.write(CSV_SEPARATOR);
                writer.write(String.valueOf(stat.getLeaveDays()));
                writer.write(CSV_SEPARATOR);
                writer.write(String.valueOf(stat.getTotalDurationMinutes()));
                writer.write(CSV_SEPARATOR);
                writer.write(String.valueOf(stat.getAttendanceRate()));
                writer.write(LINE_SEPARATOR);
            }

            writer.flush();
            log.debug("月度考勤报表CSV生成完成");
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("生成月度考勤报表CSV失败", e);
            throw new RuntimeException("生成CSV报表失败", e);
        } finally {
            IoUtil.close(writer);
            IoUtil.close(baos);
        }
    }

    /**
     * 生成考勤异常报表CSV
     *
     * @param anomalies 异常数据列表
     * @return CSV字节数组
     */
    public static byte[] generateAnomalyReport(List<AttendanceAnomalyVO> anomalies) {
        log.debug("生成考勤异常报表CSV, 记录数: {}", anomalies.size());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);

        try {
            // 写入BOM以支持Excel正确识别UTF-8
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);

            // 写入表头
            writer.write("异常ID,用户ID,考勤记录ID,异常类型,异常日期,异常时长(分钟),异常描述,是否已处理,处理人ID,处理说明");
            writer.write(LINE_SEPARATOR);

            // 写入数据行
            for (AttendanceAnomalyVO anomaly : anomalies) {
                writer.write(String.valueOf(anomaly.getId()));
                writer.write(CSV_SEPARATOR);
                writer.write(String.valueOf(anomaly.getUserId()));
                writer.write(CSV_SEPARATOR);
                writer.write(anomaly.getAttendanceRecordId() != null ? String.valueOf(anomaly.getAttendanceRecordId()) : "");
                writer.write(CSV_SEPARATOR);
                writer.write(anomaly.getAnomalyType() != null ? anomaly.getAnomalyType().getDescription() : "");
                writer.write(CSV_SEPARATOR);
                writer.write(anomaly.getAnomalyDate() != null ? anomaly.getAnomalyDate().toString() : "");
                writer.write(CSV_SEPARATOR);
                writer.write(anomaly.getDurationMinutes() != null ? String.valueOf(anomaly.getDurationMinutes()) : "");
                writer.write(CSV_SEPARATOR);
                writer.write(escapeCSV(anomaly.getDescription()));
                writer.write(CSV_SEPARATOR);
                writer.write(anomaly.getResolved() != null ? (anomaly.getResolved() ? "是" : "否") : "否");
                writer.write(CSV_SEPARATOR);
                writer.write(anomaly.getHandlerId() != null ? String.valueOf(anomaly.getHandlerId()) : "");
                writer.write(CSV_SEPARATOR);
                writer.write(escapeCSV(anomaly.getHandlerNote()));
                writer.write(LINE_SEPARATOR);
            }

            writer.flush();
            log.debug("考勤异常报表CSV生成完成");
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("生成考勤异常报表CSV失败", e);
            throw new RuntimeException("生成CSV报表失败", e);
        } finally {
            IoUtil.close(writer);
            IoUtil.close(baos);
        }
    }

    /**
     * 转义CSV字段（处理包含逗号、引号、换行符的情况）
     */
    private static String escapeCSV(String value) {
        if (value == null) {
            return "";
        }
        
        // 如果包含逗号、引号或换行符，需要用引号包裹，并转义内部的引号
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }
}
