package com.hngy.siae.ai.tool;

import com.hngy.siae.api.ai.client.AiUserFeignClient;
import com.hngy.siae.api.ai.dto.response.AwardInfo;
import com.hngy.siae.api.ai.dto.response.AwardStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * 获奖查询工具
 * <p>
 * 提供AI可调用的获奖数据查询功能，使用Spring AI的@Tool注解。
 * 通过Feign客户端调用siae-user服务获取实际数据。
 * <p>
 * Requirements: 3.1, 3.2, 3.5, 5.1, 5.5
 *
 * @author SIAE Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AwardQueryTool {

    private final AiUserFeignClient aiUserFeignClient;
    private final ToolParameterValidator validator;
    private final com.hngy.siae.ai.security.PermissionChecker permissionChecker;

    /**
     * 查询成员获奖记录的工具函数
     * <p>
     * 支持按成员姓名和学号查询获奖记录。至少需要提供姓名或学号之一。
     * 
     * @param memberName 成员姓名，支持模糊匹配
     * @param studentId 学号，精确匹配，可选
     * @return 获奖记录列表
     */
    @Tool(description = "查询指定成员的获奖记录，可按成员姓名、学号查询。返回该成员的所有获奖信息，包括奖项名称、等级、类型、颁发单位、获奖日期等。")
    public List<AwardInfo> queryMemberAwards(
            @ToolParam(description = "成员姓名，支持模糊匹配") String memberName,
            @ToolParam(description = "学号，精确匹配，可选") String studentId) {
        
        log.info("Tool invoked: queryMemberAwards - memberName: {}, studentId: {}, user: {}", 
                memberName, studentId, permissionChecker.getCurrentUsername());
        
        // 权限检查 - Requirements: 7.1, 7.2, 7.3
        permissionChecker.checkAwardQueryPermission();
        
        // 参数验证
        ToolParameterValidator.ValidationResult validationResult = 
                validator.validateMemberAwardsQuery(memberName, studentId);
        if (!validationResult.valid()) {
            String errorMessage = String.join("; ", validationResult.errors());
            log.warn("queryMemberAwards validation failed: {}", errorMessage);
            throw ToolExecutionException.of(errorMessage);
        }
        
        try {
            List<AwardInfo> awards = aiUserFeignClient.getAwardsByMember(
                    StringUtils.hasText(memberName) ? memberName.trim() : null,
                    StringUtils.hasText(studentId) ? studentId.trim() : null
            );
            
            log.info("queryMemberAwards returned {} awards for user: {}", 
                    awards != null ? awards.size() : 0, permissionChecker.getCurrentUsername());
            return awards != null ? awards : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error querying member awards: {}", e.getMessage(), e);
            throw ToolExecutionException.of("查询获奖记录失败: " + e.getMessage(), e);
        }
    }

    /**
     * 查询获奖统计信息的工具函数
     * <p>
     * 支持按奖项类型、等级、时间范围统计获奖数据。
     * 
     * @param typeId 奖项类型ID，可选，不提供则统计所有类型
     * @param levelId 奖项等级ID，可选，不提供则统计所有等级
     * @param startDate 开始日期，格式yyyy-MM-dd，可选
     * @param endDate 结束日期，格式yyyy-MM-dd，可选
     * @return 获奖统计信息
     */
    @Tool(description = "查询获奖统计信息，可按奖项类型、等级、时间范围统计。返回获奖总数以及按类型、等级、年份的分布统计。")
    public AwardStatistics getAwardStatistics(
            @ToolParam(description = "奖项类型ID，可选，不提供则统计所有类型") Long typeId,
            @ToolParam(description = "奖项等级ID，可选，不提供则统计所有等级") Long levelId,
            @ToolParam(description = "开始日期，格式yyyy-MM-dd，可选") String startDate,
            @ToolParam(description = "结束日期，格式yyyy-MM-dd，可选") String endDate) {
        
        log.info("Tool invoked: getAwardStatistics - typeId: {}, levelId: {}, startDate: {}, endDate: {}, user: {}", 
                typeId, levelId, startDate, endDate, permissionChecker.getCurrentUsername());
        
        // 权限检查 - 统计信息需要管理员权限 - Requirements: 7.1, 7.2, 7.3
        permissionChecker.checkStatisticsQueryPermission();
        
        // 参数验证
        ToolParameterValidator.ValidationResult validationResult = 
                validator.validateAwardStatisticsQuery(typeId, levelId, startDate, endDate);
        if (!validationResult.valid()) {
            String errorMessage = String.join("; ", validationResult.errors());
            log.warn("getAwardStatistics validation failed: {}", errorMessage);
            throw ToolExecutionException.of(errorMessage);
        }
        
        try {
            AwardStatistics statistics = aiUserFeignClient.getAwardStatistics(
                    typeId,
                    levelId,
                    StringUtils.hasText(startDate) ? startDate.trim() : null,
                    StringUtils.hasText(endDate) ? endDate.trim() : null
            );
            
            log.info("getAwardStatistics returned statistics with totalAwards: {} for user: {}", 
                    statistics != null ? statistics.getTotalAwards() : 0,
                    permissionChecker.getCurrentUsername());
            return statistics;
        } catch (Exception e) {
            log.error("Error getting award statistics: {}", e.getMessage(), e);
            throw ToolExecutionException.of("查询获奖统计失败: " + e.getMessage(), e);
        }
    }
}
