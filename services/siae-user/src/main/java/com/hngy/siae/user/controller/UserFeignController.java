package com.hngy.siae.user.controller;

import com.hngy.siae.user.dto.request.UserCreateDTO;
import com.hngy.siae.user.dto.response.UserBasicVO;
import com.hngy.siae.user.dto.response.UserVO;
import com.hngy.siae.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

/**
 * 用户Feign控制器
 * <p>
 * 专门用于服务间调用的REST API接口，提供用户相关的内部服务调用功能。
 * 这些接口不需要用户权限验证，仅供内部微服务之间调用使用。
 *
 * @author KEYKB
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/internal")
@Validated
@Tag(name = "用户Feign接口", description = "用户服务间调用API")
public class UserFeignController {

    private final UserService userService;

    /**
     * 创建用户（Feign专用接口）
     *
     * @param userDTO 用户创建参数
     * @return 用户信息
     */
    @PostMapping("/create")
    @Operation(summary = "创建用户", description = "创建新用户（Feign专用接口）")
    public UserVO createUserForFeign(
            @Parameter(description = "用户创建参数") @RequestBody @NotNull UserCreateDTO userDTO) {
        return userService.createUser(userDTO);
    }

    /**
     * 批量获取用户ID和用户名映射
     *
     * @param userIds 用户ID集合
     * @return 用户ID和用户名的映射关系
     */
    @GetMapping("/batch/usernames")
    @Operation(summary = "批量获取用户名映射", description = "根据用户ID集合批量获取用户名映射")
    public Map<Long, String> getUserMapByIds(
            @Parameter(description = "用户ID集合") @RequestParam("userIds") @NotEmpty Set<Long> userIds) {
        return userService.getUserMapByIds(userIds);
    }

    /**
     * 根据用户名获取用户基本信息（Feign专用接口）
     * <p>
     * 根据用户名查询用户的基本认证信息，包括用户ID、用户名和加密密码。
     * 此接口专门用于内部服务间的身份验证和用户查找。
     * <p>
     * <strong>安全警告：</strong>此接口返回用户密码信息，仅限内部服务调用使用，
     * 不得暴露给外部API，以防止敏感信息泄露。
     *
     * @param username 用户名，不能为空或空白字符串
     * @return 用户基本信息，如果用户不存在则返回null
     */
    @GetMapping("/username/{username}")
    @Operation(summary = "根据用户名获取用户基本信息", description = "根据用户名查询用户基本认证信息（Feign专用接口）")
    public UserBasicVO getUserByUsername(
            @Parameter(description = "用户名") @PathVariable("username") @NotBlank String username) {
        return userService.getUserByUsernameClient(username);
    }

    /**
     * 检查用户是否存在
     *
     * @param userId 用户ID
     * @return true表示用户存在，false表示用户不存在
     */
    @GetMapping("/exists/{userId}")
    @Operation(summary = "检查用户是否存在", description = "根据用户ID检查用户是否存在")
    public Boolean checkUserExists(
            @Parameter(description = "用户ID") @PathVariable("userId") @NotNull Long userId) {
        return userService.checkUserExists(userId);
    }
}
