package com.hngy.siae.ai.tool;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * 工具参数验证器
 * <p>
 * 提供AI工具函数的参数验证功能，确保输入参数的有效性。
 * 验证失败时返回清晰的错误消息。
 * <p>
 * Requirements: 5.2
 *
 * @author SIAE Team
 */
@Component
public class ToolParameterValidator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 验证结果
     */
    public record ValidationResult(boolean valid, List<String> errors) {
        public static ValidationResult success() {
            return new ValidationResult(true, List.of());
        }

        public static ValidationResult failure(List<String> errors) {
            return new ValidationResult(false, errors);
        }

        public static ValidationResult failure(String error) {
            return new ValidationResult(false, List.of(error));
        }
    }

    /**
     * 验证成员获奖查询参数
     *
     * @param memberName 成员姓名
     * @param studentId  学号
     * @return 验证结果
     */
    public ValidationResult validateMemberAwardsQuery(String memberName, String studentId) {
        List<String> errors = new ArrayList<>();

        // 至少需要提供一个查询条件
        if (!StringUtils.hasText(memberName) && !StringUtils.hasText(studentId)) {
            errors.add("请至少提供成员姓名或学号之一作为查询条件");
        }

        // 验证姓名长度
        if (StringUtils.hasText(memberName) && memberName.trim().length() > 50) {
            errors.add("成员姓名长度不能超过50个字符");
        }

        // 验证学号格式（如果提供）
        if (StringUtils.hasText(studentId)) {
            String trimmedStudentId = studentId.trim();
            if (trimmedStudentId.length() > 20) {
                errors.add("学号长度不能超过20个字符");
            }
            if (!trimmedStudentId.matches("^[A-Za-z0-9]+$")) {
                errors.add("学号只能包含字母和数字");
            }
        }

        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }

    /**
     * 验证获奖统计查询参数
     *
     * @param typeId    奖项类型ID
     * @param levelId   奖项等级ID
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 验证结果
     */
    public ValidationResult validateAwardStatisticsQuery(Long typeId, Long levelId, 
                                                          String startDate, String endDate) {
        List<String> errors = new ArrayList<>();

        // 验证ID为正数
        if (typeId != null && typeId <= 0) {
            errors.add("奖项类型ID必须为正整数");
        }
        if (levelId != null && levelId <= 0) {
            errors.add("奖项等级ID必须为正整数");
        }

        // 验证日期格式
        LocalDate parsedStartDate = null;
        LocalDate parsedEndDate = null;

        if (StringUtils.hasText(startDate)) {
            parsedStartDate = parseDate(startDate.trim());
            if (parsedStartDate == null) {
                errors.add("开始日期格式无效，请使用yyyy-MM-dd格式");
            }
        }

        if (StringUtils.hasText(endDate)) {
            parsedEndDate = parseDate(endDate.trim());
            if (parsedEndDate == null) {
                errors.add("结束日期格式无效，请使用yyyy-MM-dd格式");
            }
        }

        // 验证日期范围
        if (parsedStartDate != null && parsedEndDate != null) {
            if (parsedStartDate.isAfter(parsedEndDate)) {
                errors.add("开始日期不能晚于结束日期");
            }
        }

        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }

    /**
     * 验证成员查询参数
     *
     * @param name       成员姓名
     * @param department 部门名称
     * @param position   职位名称
     * @return 验证结果
     */
    public ValidationResult validateMemberQuery(String name, String department, String position) {
        List<String> errors = new ArrayList<>();

        // 至少需要提供一个查询条件
        if (!StringUtils.hasText(name) && !StringUtils.hasText(department) && !StringUtils.hasText(position)) {
            errors.add("请至少提供姓名、部门或职位之一作为查询条件");
        }

        // 验证字段长度
        if (StringUtils.hasText(name) && name.trim().length() > 50) {
            errors.add("成员姓名长度不能超过50个字符");
        }
        if (StringUtils.hasText(department) && department.trim().length() > 50) {
            errors.add("部门名称长度不能超过50个字符");
        }
        if (StringUtils.hasText(position) && position.trim().length() > 50) {
            errors.add("职位名称长度不能超过50个字符");
        }

        return errors.isEmpty() ? ValidationResult.success() : ValidationResult.failure(errors);
    }

    /**
     * 解析日期字符串
     *
     * @param dateStr 日期字符串
     * @return 解析后的LocalDate，解析失败返回null
     */
    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
