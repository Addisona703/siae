package com.hngy.siae.auth.feign;

import com.hngy.siae.auth.common.ApiResult;
import com.hngy.siae.auth.feign.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 用户服务Feign客户端
 * 
 * @author KEYKB
 */
@FeignClient(name = "siae-user", path = "/api/v1/users")
public interface UserClient {
    
    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    @GetMapping("/by-username")
    ApiResult<UserDTO> getUserByUsername(@RequestParam("username") String username);
    
    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/{userId}")
    ApiResult<UserDTO> getUserById(@PathVariable("userId") Long userId);
} 