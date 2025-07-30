package com.hngy.siae.auth.feign;

import com.hngy.siae.auth.feign.dto.request.UserDTO;
import com.hngy.siae.auth.feign.dto.response.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.Set;

/**
 * 用户服务Feign客户端
 * 
 * @author KEYKB
 */
@FeignClient(name = "siae-user")
public interface UserClient {
    
    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Operation(summary = "根据用户名获取用户信息", description = "通过用户名查询用户详细信息")
    @GetMapping("/api/v1/user/username/{username}")
    UserVO getUserByUsername(
            @Parameter(description = "用户名") @PathVariable("username") @NotNull String username);

    /**
     * 创建用户信息
     *
     * @param userDTO 用户创建参数
     * @return 用户信息
     */
    @Operation(summary = "创建用户", description = "创建新用户")
    @PostMapping("/create")
    UserVO createUser(
            @Parameter(description = "用户创建参数") @RequestBody @NotNull UserDTO userDTO);

    /**
     * 批量获取用户ID和用户名映射
     *
     * @param userIds 用户ID集合
     * @return 用户ID和用户名的映射关系
     */
    @Operation(summary = "批量获取用户名映射", description = "根据用户ID集合批量获取用户名映射")
    @GetMapping("/batch/usernames")
    Map<Long, String> getUserMapByIds(
            @Parameter(description = "用户ID集合") @RequestParam("userIds") @NotEmpty Set<Long> userIds);

    /**
     * 检查用户是否存在
     *
     * @param userId 用户ID
     * @return true表示用户存在，false表示用户不存在
     */
    @Operation(summary = "检查用户是否存在", description = "根据用户ID检查用户是否存在")
    @GetMapping("/exists/{userId}")
    Boolean checkUserExists(
            @Parameter(description = "用户ID") @PathVariable("userId") @NotNull Long userId);
} 