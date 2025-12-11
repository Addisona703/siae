package com.hngy.siae.api.ai.fallback;

import com.hngy.siae.api.ai.client.AiUserFeignClient;
import com.hngy.siae.api.ai.dto.response.AwardInfo;
import com.hngy.siae.api.ai.dto.response.AwardStatistics;
import com.hngy.siae.api.ai.dto.response.MemberInfo;
import com.hngy.siae.api.ai.dto.response.MemberStatistics;
import com.hngy.siae.core.exception.ServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * AiUserFeignClient 降级处理类
 * <p>
 * 当用户服务不可用时，执行降级逻辑。
 * 主要用于网络故障、服务宕机等无法获取响应的情况。
 *
 * @author KEYKB
 */
@Slf4j
@Component
public class AiUserFeignClientFallback implements AiUserFeignClient {
    
    @Override
    public List<AwardInfo> getAwardsByMember(String memberName, String studentId) {
        log.error("查询成员获奖记录服务不可用，触发降级。成员姓名: {}, 学号: {}", memberName, studentId);
        throw new ServiceException(503, "用户服务暂时不可用，无法查询获奖记录，请稍后重试");
    }
    
    @Override
    public List<MemberInfo> searchMembers(String name, String department, String position) {
        log.error("查询成员信息服务不可用，触发降级。姓名: {}, 部门: {}, 职位: {}", name, department, position);
        throw new ServiceException(503, "用户服务暂时不可用，无法查询成员信息，请稍后重试");
    }
    
    @Override
    public AwardStatistics getAwardStatistics(Long typeId, Long levelId, String startDate, String endDate) {
        log.error("查询获奖统计服务不可用，触发降级。类型ID: {}, 等级ID: {}, 开始日期: {}, 结束日期: {}", 
            typeId, levelId, startDate, endDate);
        throw new ServiceException(503, "用户服务暂时不可用，无法查询获奖统计，请稍后重试");
    }
    
    @Override
    public MemberStatistics getMemberStatistics() {
        log.error("查询成员统计服务不可用，触发降级");
        throw new ServiceException(503, "用户服务暂时不可用，无法查询成员统计，请稍后重试");
    }
}
