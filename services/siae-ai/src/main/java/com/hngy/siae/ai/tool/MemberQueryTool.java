package com.hngy.siae.ai.tool;

import com.hngy.siae.api.ai.client.AiUserFeignClient;
import com.hngy.siae.api.ai.dto.response.MemberInfo;
import com.hngy.siae.api.ai.dto.response.MemberStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * 成员查询工具
 * <p>
 * 提供AI可调用的成员数据查询功能，使用Spring AI的@Tool注解。
 * 通过Feign客户端调用siae-user服务获取实际数据。
 * <p>
 * Requirements: 4.1, 4.2, 4.3, 4.4, 5.1, 5.5
 *
 * @author SIAE Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemberQueryTool {

    private final AiUserFeignClient aiUserFeignClient;
    private final ToolParameterValidator validator;
    private final com.hngy.siae.ai.security.PermissionChecker permissionChecker;

    /**
     * 查询成员信息的工具函数
     * <p>
     * 支持按姓名、部门、职位查询成员信息。
     * 所有参数都是可选的，但至少需要提供一个查询条件。
     * 
     * @param name 成员姓名，支持模糊匹配
     * @param department 部门名称，可选，如：技术部、宣传部
     * @param position 职位名称，可选，如：部长、副部长、干事
     * @return 成员信息列表
     */
    @Tool(description = "查询成员信息，可按姓名、部门、职位查询。返回成员的基本信息，包括姓名、学号、部门、职位、年级、专业等。")
    public List<MemberInfo> queryMembers(
            @ToolParam(description = "成员姓名，支持模糊匹配") String name,
            @ToolParam(description = "部门名称，可选，如：技术部、宣传部") String department,
            @ToolParam(description = "职位名称，可选，如：部长、副部长、干事") String position) {
        
        log.info("Tool invoked: queryMembers - name: {}, department: {}, position: {}, user: {}", 
                name, department, position, permissionChecker.getCurrentUsername());
        
        // 权限检查 - Requirements: 7.1, 7.2, 7.3
        permissionChecker.checkMemberQueryPermission();
        
        // 参数验证
        ToolParameterValidator.ValidationResult validationResult = 
                validator.validateMemberQuery(name, department, position);
        if (!validationResult.valid()) {
            String errorMessage = String.join("; ", validationResult.errors());
            log.warn("queryMembers validation failed: {}", errorMessage);
            throw ToolExecutionException.of(errorMessage);
        }
        
        try {
            List<MemberInfo> members = aiUserFeignClient.searchMembers(
                    StringUtils.hasText(name) ? name.trim() : null,
                    StringUtils.hasText(department) ? department.trim() : null,
                    StringUtils.hasText(position) ? position.trim() : null
            );
            
            log.info("queryMembers returned {} members for user: {}", 
                    members != null ? members.size() : 0, permissionChecker.getCurrentUsername());
            return members != null ? members : Collections.emptyList();
        } catch (Exception e) {
            log.error("Error querying members: {}", e.getMessage(), e);
            throw ToolExecutionException.of("查询成员信息失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取成员统计数据的工具函数
     * <p>
     * 返回成员总数以及按部门、年级、职位的分布统计。
     * 
     * @return 成员统计信息
     */
    @Tool(description = "获取成员统计数据，包括成员总数、各部门人数、各年级分布、各职位分布等统计信息。")
    public MemberStatistics getMemberStatistics() {
        log.info("Tool invoked: getMemberStatistics, user: {}", 
                permissionChecker.getCurrentUsername());
        
        // 权限检查 - 统计信息需要管理员权限 - Requirements: 7.1, 7.2, 7.3
        permissionChecker.checkStatisticsQueryPermission();
        
        try {
            MemberStatistics statistics = aiUserFeignClient.getMemberStatistics();
            
            log.info("getMemberStatistics returned statistics with totalMembers: {} for user: {}", 
                    statistics != null ? statistics.getTotalMembers() : 0,
                    permissionChecker.getCurrentUsername());
            return statistics;
        } catch (Exception e) {
            log.error("Error getting member statistics: {}", e.getMessage(), e);
            throw ToolExecutionException.of("查询成员统计失败: " + e.getMessage(), e);
        }
    }
}
