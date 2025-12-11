package com.hngy.siae.api.ai.client;

import com.hngy.siae.api.ai.dto.response.AwardInfo;
import com.hngy.siae.api.ai.dto.response.AwardStatistics;
import com.hngy.siae.api.ai.dto.response.MemberInfo;
import com.hngy.siae.api.ai.dto.response.MemberStatistics;
import com.hngy.siae.api.ai.fallback.AiUserFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * AI服务用户数据 Feign 客户端
 * <p>
 * 提供AI服务所需的用户、成员、获奖数据查询接口。
 * 这些接口专门用于AI服务调用siae-user服务获取数据。
 *
 * @author KEYKB
 */
@FeignClient(
    name = "siae-user",
    path = "/api/v1/user/feign/ai",
    contextId = "aiUserFeignClient",
    fallback = AiUserFeignClientFallback.class
)
public interface AiUserFeignClient {
    
    /**
     * 根据成员信息查询获奖记录
     * <p>
     * 支持按成员姓名和学号查询获奖记录。
     *
     * @param memberName 成员姓名，支持模糊匹配
     * @param studentId 学号，可选，精确匹配
     * @return 获奖信息列表
     */
    @GetMapping("/awards/by-member")
    List<AwardInfo> getAwardsByMember(
        @RequestParam(value = "memberName", required = false) String memberName,
        @RequestParam(value = "studentId", required = false) String studentId
    );
    
    /**
     * 查询成员信息
     * <p>
     * 支持按姓名、部门、职位查询成员信息。
     *
     * @param name 成员姓名，支持模糊匹配
     * @param department 部门名称，可选
     * @param position 职位名称，可选
     * @return 成员信息列表
     */
    @GetMapping("/members/search")
    List<MemberInfo> searchMembers(
        @RequestParam(value = "name", required = false) String name,
        @RequestParam(value = "department", required = false) String department,
        @RequestParam(value = "position", required = false) String position
    );
    
    /**
     * 获取获奖统计信息
     * <p>
     * 支持按奖项类型、等级、时间范围统计获奖数据。
     *
     * @param typeId 奖项类型ID，可选
     * @param levelId 奖项等级ID，可选
     * @param startDate 开始日期，格式yyyy-MM-dd，可选
     * @param endDate 结束日期，格式yyyy-MM-dd，可选
     * @return 获奖统计信息
     */
    @GetMapping("/statistics/awards")
    AwardStatistics getAwardStatistics(
        @RequestParam(value = "typeId", required = false) Long typeId,
        @RequestParam(value = "levelId", required = false) Long levelId,
        @RequestParam(value = "startDate", required = false) String startDate,
        @RequestParam(value = "endDate", required = false) String endDate
    );
    
    /**
     * 获取成员统计信息
     * <p>
     * 返回成员总数、按部门/年级/职位的分布统计。
     *
     * @return 成员统计信息
     */
    @GetMapping("/statistics/members")
    MemberStatistics getMemberStatistics();
}
