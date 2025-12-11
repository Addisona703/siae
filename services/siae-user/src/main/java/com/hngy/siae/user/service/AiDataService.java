package com.hngy.siae.user.service;

import com.hngy.siae.api.ai.dto.response.AwardInfo;
import com.hngy.siae.api.ai.dto.response.AwardStatistics;
import com.hngy.siae.api.ai.dto.response.MemberInfo;
import com.hngy.siae.api.ai.dto.response.MemberStatistics;

import java.util.List;

/**
 * AI数据服务接口
 * <p>
 * 提供AI服务所需的数据查询功能，包括成员信息、获奖记录和统计数据。
 * 这些接口专门为AI工具函数设计，返回简化的数据结构。
 *
 * @author KEYKB
 */
public interface AiDataService {

    /**
     * 根据成员信息查询获奖记录
     *
     * @param memberName 成员姓名，支持模糊匹配
     * @param studentId 学号，精确匹配，可为null
     * @return 获奖信息列表
     */
    List<AwardInfo> getAwardsByMember(String memberName, String studentId);

    /**
     * 查询成员信息
     *
     * @param name 成员姓名，支持模糊匹配
     * @param department 部门名称，可为null
     * @param position 职位名称，可为null
     * @return 成员信息列表
     */
    List<MemberInfo> searchMembers(String name, String department, String position);

    /**
     * 获取获奖统计信息
     *
     * @param typeId 奖项类型ID，可为null
     * @param levelId 奖项等级ID，可为null
     * @param startDate 开始日期，格式yyyy-MM-dd，可为null
     * @param endDate 结束日期，格式yyyy-MM-dd，可为null
     * @return 获奖统计信息
     */
    AwardStatistics getAwardStatistics(Long typeId, Long levelId, String startDate, String endDate);

    /**
     * 获取成员统计信息
     *
     * @return 成员统计信息
     */
    MemberStatistics getMemberStatistics();
}
