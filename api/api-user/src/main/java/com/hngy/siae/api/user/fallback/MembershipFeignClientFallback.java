package com.hngy.siae.api.user.fallback;

import com.hngy.siae.core.exception.ServiceException;
import com.hngy.siae.api.user.client.MembershipFeignClient;
import com.hngy.siae.api.user.dto.response.MembershipVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * MembershipFeignClient 降级处理类
 * <p>
 * 当成员服务不可用时，执行降级逻辑。
 * 主要用于网络故障、服务宕机等无法获取响应的情况。
 *
 * @author KEYKB
 */
@Slf4j
@Component
public class MembershipFeignClientFallback implements MembershipFeignClient {
    
    @Override
    public MembershipVO getMembershipById(Long id) {
        log.error("根据成员ID查询成员信息服务不可用，触发降级。成员ID: {}", id);
        throw new ServiceException(503, "成员服务暂时不可用，请稍后重试");
    }
    
    @Override
    public MembershipVO getMembershipByUserId(Long userId) {
        log.error("根据用户ID查询成员信息服务不可用，触发降级。用户ID: {}", userId);
        throw new ServiceException(503, "成员服务暂时不可用，请稍后重试");
    }
    
    @Override
    public Boolean isMember(Long userId) {
        log.error("检查用户是否为成员服务不可用，触发降级。用户ID: {}", userId);
        // 降级策略：保守处理，返回 false（假设不是成员）
        // 这样可以避免在服务恢复前授予不应有的权限
        log.warn("降级策略：返回 false（假设不是成员），避免授予不应有的权限");
        return false;
    }
    
    @Override
    public Boolean isCandidate(Long userId) {
        log.error("检查是否为候选成员服务不可用，触发降级。用户ID: {}", userId);
        // 降级策略：保守处理，返回 false（假设不是候选成员）
        log.warn("降级策略：返回 false（假设不是候选成员），避免授予不应有的权限");
        return false;
    }
    
    @Override
    public Boolean isOfficialMember(Long userId) {
        log.error("检查是否为正式成员服务不可用，触发降级。用户ID: {}", userId);
        // 降级策略：保守处理，返回 false（假设不是正式成员）
        log.warn("降级策略：返回 false（假设不是正式成员），避免授予不应有的权限");
        return false;
    }
}
