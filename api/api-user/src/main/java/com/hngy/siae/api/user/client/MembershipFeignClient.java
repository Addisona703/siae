package com.hngy.siae.api.user.client;

import com.hngy.siae.api.user.dto.response.MembershipVO;
import com.hngy.siae.api.user.fallback.MembershipFeignClientFallback;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 成员服务 Feign 客户端
 * <p>
 * 提供成员相关的远程调用接口，包括成员信息查询、成员状态检查等功能。
 * 这些接口专门用于服务间调用。
 *
 * @author KEYKB
 */
@FeignClient(
    name = "siae-user",
    path = "/memberships",
    contextId = "membershipFeignClient",
    fallback = MembershipFeignClientFallback.class
)
public interface MembershipFeignClient {
    
    /**
     * 根据成员ID查询成员信息
     * <p>
     * 查询成员的基本信息，包括用户ID、生命周期状态、加入日期等。
     *
     * @param id 成员ID，不能为null
     * @return 成员信息，如果成员不存在则返回null
     */
    @GetMapping("/{id}")
    MembershipVO getMembershipById(@PathVariable("id") @NotNull Long id);
    
    /**
     * 根据用户ID查询成员信息
     * <p>
     * 通过用户ID查找对应的成员记录。一个用户只能有一条成员记录。
     *
     * @param userId 用户ID，不能为null
     * @return 成员信息，如果该用户不是成员则返回null
     */
    @GetMapping("/by-user/{userId}")
    MembershipVO getMembershipByUserId(@PathVariable("userId") @NotNull Long userId);
    
    /**
     * 检查用户是否为成员
     * <p>
     * 判断用户是否为成员（包括候选成员和正式成员）。
     *
     * @param userId 用户ID，不能为null
     * @return true表示用户是成员（候选或正式），false表示不是成员
     */
    @GetMapping("/check/{userId}")
    Boolean isMember(@PathVariable("userId") @NotNull Long userId);
    
    /**
     * 检查是否为候选成员
     * <p>
     * 判断用户是否为候选成员（生命周期状态为0）。
     *
     * @param userId 用户ID，不能为null
     * @return true表示用户是候选成员，false表示不是候选成员
     */
    @GetMapping("/check/{userId}/candidate")
    Boolean isCandidate(@PathVariable("userId") @NotNull Long userId);
    
    /**
     * 检查是否为正式成员
     * <p>
     * 判断用户是否为正式成员（生命周期状态为1）。
     *
     * @param userId 用户ID，不能为null
     * @return true表示用户是正式成员，false表示不是正式成员
     */
    @GetMapping("/check/{userId}/official")
    Boolean isOfficialMember(@PathVariable("userId") @NotNull Long userId);
}
