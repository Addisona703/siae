package com.hngy.siae.user.controller;

import com.hngy.siae.api.ai.dto.response.AwardInfo;
import com.hngy.siae.api.ai.dto.response.AwardStatistics;
import com.hngy.siae.api.ai.dto.response.MemberInfo;
import com.hngy.siae.api.ai.dto.response.MemberStatistics;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.user.service.AiDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * AI服务Feign控制器
 * <p>
 * 专门用于AI服务调用的REST API接口，提供成员信息、获奖记录和统计数据查询功能。
 * 这些接口不需要用户权限验证，仅供内部微服务之间调用使用。
 *
 * @author KEYKB
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/feign/ai")
@Validated
@Tag(name = "AI服务Feign接口", description = "AI服务数据查询API")
public class AiFeignController {

    private final AiDataService aiDataService;

    /**
     * 根据成员信息查询获奖记录
     *
     * @param memberName 成员姓名，支持模糊匹配
     * @param studentId 学号，精确匹配，可选
     * @return 获奖信息列表
     */
    @GetMapping("/awards/by-member")
    @Operation(summary = "查询成员获奖记录", description = "根据成员姓名或学号查询获奖记录")
    public Result<List<AwardInfo>> getAwardsByMember(
            @Parameter(description = "成员姓名，支持模糊匹配")
            @RequestParam(value = "memberName", required = false) String memberName,
            @Parameter(description = "学号，精确匹配")
            @RequestParam(value = "studentId", required = false) String studentId) {
        log.info("AI Feign调用: 查询成员获奖记录, memberName={}, studentId={}", memberName, studentId);
        return Result.success(aiDataService.getAwardsByMember(memberName, studentId));
    }

    /**
     * 查询成员信息
     *
     * @param name 成员姓名，支持模糊匹配
     * @param department 部门名称，可选
     * @param position 职位名称，可选
     * @return 成员信息列表
     */
    @GetMapping("/members/search")
    @Operation(summary = "查询成员信息", description = "根据姓名、部门、职位查询成员信息")
    public Result<List<MemberInfo>> searchMembers(
            @Parameter(description = "成员姓名，支持模糊匹配")
            @RequestParam(value = "name", required = false) String name,
            @Parameter(description = "部门名称")
            @RequestParam(value = "department", required = false) String department,
            @Parameter(description = "职位名称")
            @RequestParam(value = "position", required = false) String position) {
        log.info("AI Feign调用: 查询成员信息, name={}, department={}, position={}", name, department, position);
        return Result.success(aiDataService.searchMembers(name, department, position));
    }

    /**
     * 获取获奖统计信息
     *
     * @param typeId 奖项类型ID，可选
     * @param levelId 奖项等级ID，可选
     * @param startDate 开始日期，格式yyyy-MM-dd，可选
     * @param endDate 结束日期，格式yyyy-MM-dd，可选
     * @return 获奖统计信息
     */
    @GetMapping("/statistics/awards")
    @Operation(summary = "获取获奖统计", description = "获取获奖统计信息，支持按类型、等级、时间范围筛选")
    public Result<AwardStatistics> getAwardStatistics(
            @Parameter(description = "奖项类型ID")
            @RequestParam(value = "typeId", required = false) Long typeId,
            @Parameter(description = "奖项等级ID")
            @RequestParam(value = "levelId", required = false) Long levelId,
            @Parameter(description = "开始日期，格式yyyy-MM-dd")
            @RequestParam(value = "startDate", required = false) String startDate,
            @Parameter(description = "结束日期，格式yyyy-MM-dd")
            @RequestParam(value = "endDate", required = false) String endDate) {
        log.info("AI Feign调用: 获取获奖统计, typeId={}, levelId={}, startDate={}, endDate={}", 
                typeId, levelId, startDate, endDate);
        return Result.success(aiDataService.getAwardStatistics(typeId, levelId, startDate, endDate));
    }

    /**
     * 获取成员统计信息
     *
     * @return 成员统计信息
     */
    @GetMapping("/statistics/members")
    @Operation(summary = "获取成员统计", description = "获取成员统计信息，包括总数、按部门/年级/职位分布")
    public Result<MemberStatistics> getMemberStatistics() {
        log.info("AI Feign调用: 获取成员统计");
        return Result.success(aiDataService.getMemberStatistics());
    }
}
