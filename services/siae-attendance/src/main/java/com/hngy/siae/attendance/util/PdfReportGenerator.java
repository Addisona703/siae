package com.hngy.siae.attendance.util;

import com.hngy.siae.attendance.dto.response.AttendanceAnomalyVO;
import com.hngy.siae.attendance.dto.response.AttendanceStatisticsVO;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * PDF报表生成器
 * 
 * 注意：这是一个简化的实现，生成HTML格式的报表
 * 实际生产环境中应该使用专业的PDF库（如iText、Apache PDFBox）
 * 或者使用HTML转PDF服务（如wkhtmltopdf、Puppeteer）
 *
 * @author SIAE Team
 */
@Slf4j
public class PdfReportGenerator {

    /**
     * 生成月度考勤报表PDF（HTML格式）
     *
     * @param statistics 统计数据列表
     * @return HTML字节数组
     */
    public static byte[] generateMonthlyReport(List<AttendanceStatisticsVO> statistics) {
        log.debug("生成月度考勤报表PDF, 记录数: {}", statistics.size());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);

        try {
            writer.write("<!DOCTYPE html>\n");
            writer.write("<html>\n");
            writer.write("<head>\n");
            writer.write("<meta charset=\"UTF-8\">\n");
            writer.write("<title>月度考勤报表</title>\n");
            writer.write("<style>\n");
            writer.write("body { font-family: Arial, sans-serif; margin: 20px; }\n");
            writer.write("h1 { color: #333; text-align: center; }\n");
            writer.write("table { width: 100%; border-collapse: collapse; margin-top: 20px; }\n");
            writer.write("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
            writer.write("th { background-color: #4CAF50; color: white; }\n");
            writer.write("tr:nth-child(even) { background-color: #f2f2f2; }\n");
            writer.write(".summary { margin: 20px 0; padding: 10px; background-color: #e7f3fe; border-left: 4px solid #2196F3; }\n");
            writer.write("</style>\n");
            writer.write("</head>\n");
            writer.write("<body>\n");
            writer.write("<h1>月度考勤报表</h1>\n");

            // 添加汇总信息
            if (!statistics.isEmpty()) {
                int totalMembers = statistics.size();
                double avgAttendanceRate = statistics.stream()
                    .mapToDouble(s -> s.getAttendanceRate().doubleValue())
                    .average()
                    .orElse(0.0);
                int totalLateCount = statistics.stream()
                    .mapToInt(AttendanceStatisticsVO::getLateCount)
                    .sum();
                int totalEarlyCount = statistics.stream()
                    .mapToInt(AttendanceStatisticsVO::getEarlyCount)
                    .sum();
                int totalAbsenceCount = statistics.stream()
                    .mapToInt(AttendanceStatisticsVO::getAbsenceCount)
                    .sum();

                writer.write("<div class=\"summary\">\n");
                writer.write("<h3>汇总信息</h3>\n");
                writer.write("<p>总人数: " + totalMembers + "</p>\n");
                writer.write("<p>平均出勤率: " + String.format("%.2f", avgAttendanceRate) + "%</p>\n");
                writer.write("<p>总迟到次数: " + totalLateCount + "</p>\n");
                writer.write("<p>总早退次数: " + totalEarlyCount + "</p>\n");
                writer.write("<p>总缺勤次数: " + totalAbsenceCount + "</p>\n");
                writer.write("</div>\n");
            }

            // 添加详细数据表格
            writer.write("<table>\n");
            writer.write("<thead>\n");
            writer.write("<tr>\n");
            writer.write("<th>用户ID</th>\n");
            writer.write("<th>统计月份</th>\n");
            writer.write("<th>应出勤天数</th>\n");
            writer.write("<th>实际出勤天数</th>\n");
            writer.write("<th>迟到次数</th>\n");
            writer.write("<th>早退次数</th>\n");
            writer.write("<th>缺勤次数</th>\n");
            writer.write("<th>请假天数</th>\n");
            writer.write("<th>总考勤时长(分钟)</th>\n");
            writer.write("<th>出勤率(%)</th>\n");
            writer.write("</tr>\n");
            writer.write("</thead>\n");
            writer.write("<tbody>\n");

            for (AttendanceStatisticsVO stat : statistics) {
                writer.write("<tr>\n");
                writer.write("<td>" + stat.getUserId() + "</td>\n");
                writer.write("<td>" + stat.getStatMonth() + "</td>\n");
                writer.write("<td>" + stat.getTotalDays() + "</td>\n");
                writer.write("<td>" + stat.getActualDays() + "</td>\n");
                writer.write("<td>" + stat.getLateCount() + "</td>\n");
                writer.write("<td>" + stat.getEarlyCount() + "</td>\n");
                writer.write("<td>" + stat.getAbsenceCount() + "</td>\n");
                writer.write("<td>" + stat.getLeaveDays() + "</td>\n");
                writer.write("<td>" + stat.getTotalDurationMinutes() + "</td>\n");
                writer.write("<td>" + stat.getAttendanceRate() + "</td>\n");
                writer.write("</tr>\n");
            }

            writer.write("</tbody>\n");
            writer.write("</table>\n");
            writer.write("</body>\n");
            writer.write("</html>\n");

            writer.flush();
            log.debug("月度考勤报表PDF生成完成");
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("生成月度考勤报表PDF失败", e);
            throw new RuntimeException("生成PDF报表失败", e);
        } finally {
            try {
                writer.close();
                baos.close();
            } catch (Exception e) {
                log.error("关闭流失败", e);
            }
        }
    }

    /**
     * 生成考勤异常报表PDF（HTML格式）
     *
     * @param anomalies 异常数据列表
     * @return HTML字节数组
     */
    public static byte[] generateAnomalyReport(List<AttendanceAnomalyVO> anomalies) {
        log.debug("生成考勤异常报表PDF, 记录数: {}", anomalies.size());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(baos, StandardCharsets.UTF_8);

        try {
            writer.write("<!DOCTYPE html>\n");
            writer.write("<html>\n");
            writer.write("<head>\n");
            writer.write("<meta charset=\"UTF-8\">\n");
            writer.write("<title>考勤异常报表</title>\n");
            writer.write("<style>\n");
            writer.write("body { font-family: Arial, sans-serif; margin: 20px; }\n");
            writer.write("h1 { color: #333; text-align: center; }\n");
            writer.write("table { width: 100%; border-collapse: collapse; margin-top: 20px; }\n");
            writer.write("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
            writer.write("th { background-color: #f44336; color: white; }\n");
            writer.write("tr:nth-child(even) { background-color: #f2f2f2; }\n");
            writer.write(".summary { margin: 20px 0; padding: 10px; background-color: #ffebee; border-left: 4px solid #f44336; }\n");
            writer.write(".anomaly-type { padding: 2px 8px; border-radius: 3px; color: white; }\n");
            writer.write(".late { background-color: #ff9800; }\n");
            writer.write(".early { background-color: #2196F3; }\n");
            writer.write(".absence { background-color: #f44336; }\n");
            writer.write("</style>\n");
            writer.write("</head>\n");
            writer.write("<body>\n");
            writer.write("<h1>考勤异常报表</h1>\n");

            // 添加汇总信息（按异常类型分组）
            if (!anomalies.isEmpty()) {
                long lateCount = anomalies.stream().filter(a -> a.getAnomalyType() != null && "迟到".equals(a.getAnomalyType().getDescription())).count();
                long earlyCount = anomalies.stream().filter(a -> a.getAnomalyType() != null && "早退".equals(a.getAnomalyType().getDescription())).count();
                long absenceCount = anomalies.stream().filter(a -> a.getAnomalyType() != null && "缺勤".equals(a.getAnomalyType().getDescription())).count();
                long missingCheckInCount = anomalies.stream().filter(a -> a.getAnomalyType() != null && "漏签到".equals(a.getAnomalyType().getDescription())).count();
                long missingCheckOutCount = anomalies.stream().filter(a -> a.getAnomalyType() != null && "漏签退".equals(a.getAnomalyType().getDescription())).count();

                writer.write("<div class=\"summary\">\n");
                writer.write("<h3>异常统计</h3>\n");
                writer.write("<p>总异常数: " + anomalies.size() + "</p>\n");
                writer.write("<p>迟到: " + lateCount + " 次</p>\n");
                writer.write("<p>早退: " + earlyCount + " 次</p>\n");
                writer.write("<p>缺勤: " + absenceCount + " 次</p>\n");
                writer.write("<p>漏签到: " + missingCheckInCount + " 次</p>\n");
                writer.write("<p>漏签退: " + missingCheckOutCount + " 次</p>\n");
                writer.write("</div>\n");
            }

            // 添加详细数据表格
            writer.write("<table>\n");
            writer.write("<thead>\n");
            writer.write("<tr>\n");
            writer.write("<th>异常ID</th>\n");
            writer.write("<th>用户ID</th>\n");
            writer.write("<th>异常类型</th>\n");
            writer.write("<th>异常日期</th>\n");
            writer.write("<th>异常时长(分钟)</th>\n");
            writer.write("<th>异常描述</th>\n");
            writer.write("<th>是否已处理</th>\n");
            writer.write("<th>处理说明</th>\n");
            writer.write("</tr>\n");
            writer.write("</thead>\n");
            writer.write("<tbody>\n");

            for (AttendanceAnomalyVO anomaly : anomalies) {
                writer.write("<tr>\n");
                writer.write("<td>" + anomaly.getId() + "</td>\n");
                writer.write("<td>" + anomaly.getUserId() + "</td>\n");
                writer.write("<td>" + (anomaly.getAnomalyType() != null ? escapeHtml(anomaly.getAnomalyType().getDescription()) : "") + "</td>\n");
                writer.write("<td>" + (anomaly.getAnomalyDate() != null ? anomaly.getAnomalyDate().toString() : "") + "</td>\n");
                writer.write("<td>" + (anomaly.getDurationMinutes() != null ? anomaly.getDurationMinutes() : "") + "</td>\n");
                writer.write("<td>" + escapeHtml(anomaly.getDescription()) + "</td>\n");
                writer.write("<td>" + (anomaly.getResolved() != null && anomaly.getResolved() ? "是" : "否") + "</td>\n");
                writer.write("<td>" + escapeHtml(anomaly.getHandlerNote()) + "</td>\n");
                writer.write("</tr>\n");
            }

            writer.write("</tbody>\n");
            writer.write("</table>\n");
            writer.write("</body>\n");
            writer.write("</html>\n");

            writer.flush();
            log.debug("考勤异常报表PDF生成完成");
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("生成考勤异常报表PDF失败", e);
            throw new RuntimeException("生成PDF报表失败", e);
        } finally {
            try {
                writer.close();
                baos.close();
            } catch (Exception e) {
                log.error("关闭流失败", e);
            }
        }
    }

    /**
     * 转义HTML特殊字符
     */
    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
