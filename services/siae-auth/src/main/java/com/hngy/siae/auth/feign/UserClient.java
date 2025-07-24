package com.hngy.siae.auth.feign;

import com.hngy.siae.auth.feign.dto.request.UserDTO;
import com.hngy.siae.auth.feign.dto.response.UserVO;
import com.hngy.siae.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 用户服务Feign客户端
 * 
 * @author KEYKB
 */
@FeignClient(name = "siae-user", path = "/api/v1/user")
public interface UserClient {
    
    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名 
     * @return 用户信息
     */
    @GetMapping("/username/{username}")
    UserVO getUserByUsername(@PathVariable("username") String username);

    /**
     * 创建用户信息
     *
     * @param userDTO 用户实体类
     * @return 用户信息
     */
    @PostMapping("/create")
    @Operation(summary = "创建用户")
    UserVO createUser(@RequestBody UserDTO userDTO);
} 