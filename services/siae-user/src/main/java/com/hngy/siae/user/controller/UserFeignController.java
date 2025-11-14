package com.hngy.siae.user.controller;

import com.hngy.siae.user.dto.request.UserCreateDTO;
import com.hngy.siae.user.dto.response.UserVO;
import com.hngy.siae.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
@RequestMapping("/feign")
@Validated
@Tag(name = "用户Feign接口", description = "用户服务间调用API")
public class UserFeignController {

    private final UserService userService;

    /**
     * 用户注册（基础信息）
     * 供认证服务调用，只需填写基础必要信息
     *
     * @param registerDTO 用户注册参数
     * @return 用户信息
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册接口，只需填写基础必要信息，供认证服务调用")
    public UserVO registerUser(
            @Parameter(description = "用户注册参数") @RequestBody @NotNull UserCreateDTO registerDTO) {
        return userService.createUser(registerDTO);
    }

    /**
     * 根据用户名获取用户信息
     * <p>
     * 根据用户名查询用户信息，供内部服务间调用。
     *
     * @param username 用户名，不能为空或空白字符串
     * @return 用户信息，如果用户不存在则返回null
     */
    @GetMapping("/username/{username}")
    @Operation(summary = "根据用户名查询用户", description = "供认证服务调用，返回用户信息")
    public UserVO getUserByUsername(
            @Parameter(description = "用户名") @PathVariable("username") @NotBlank String username) {
        return userService.getUserByUsername(username);
    }

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return true表示用户名已存在，false表示用户名可用
     */
    @GetMapping("/exists/username/{username}")
    @Operation(summary = "检查用户名是否存在", description = "验证用户名是否已被使用")
    public Boolean checkUsernameExists(
            @Parameter(description = "用户名") @PathVariable("username") @NotBlank String username) {
        return userService.isUsernameExists(username);
    }

    /**
     * 检查学号是否存在
     *
     * @param studentId 学号
     * @return true表示学号已存在，false表示学号可用
     */
    @GetMapping("/exists/student-id/{studentId}")
    @Operation(summary = "检查学号是否存在", description = "验证学号是否已被使用")
    public Boolean checkStudentIdExists(
            @Parameter(description = "学号") @PathVariable("studentId") @NotBlank String studentId) {
        return userService.isStudentIdExists(studentId);
    }

    @GetMapping("/exists/user-id/{userId}")
    @Operation(summary = "检查用户ID是否存在", description = "验证用户ID是否存在")
    public void checkUserIdExists(
            @Parameter(description = "用户ID") @PathVariable("userId") @NotNull Long userId) {
        userService.assertUserExists(userId);
    }
}
