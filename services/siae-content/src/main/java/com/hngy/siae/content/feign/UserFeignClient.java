package com.hngy.siae.content.feign;

import com.hngy.siae.content.feign.dto.UserProfileDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * 用户服务Feign客户端
 *
 * @author KEYKB
 */
@FeignClient(name = "siae-user", path = "/api/v1/user/feign", contextId = "userFeignClient")
public interface UserFeignClient {

    /**
     * 批量查询用户信息
     *
     * @param userIds 用户ID列表
     * @return 用户ID -> 用户信息的映射
     */
    @GetMapping("/batch")
    Map<Long, UserProfileDTO> batchGetUserProfiles(@RequestParam("userIds") List<Long> userIds);
}
