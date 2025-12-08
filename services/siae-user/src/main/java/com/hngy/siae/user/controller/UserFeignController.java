package com.hngy.siae.user.controller;

import com.hngy.siae.api.user.dto.response.UserFaceAuthVO;
import com.hngy.siae.api.user.dto.response.UserProfileSimpleVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.user.dto.request.UserCreateDTO;
import com.hngy.siae.user.dto.response.UserVO;
import com.hngy.siae.user.dto.response.user.UserBasicInfoVO;
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
    public Result<UserVO> registerUser(
            @Parameter(description = "用户注册参数") @RequestBody @NotNull UserCreateDTO registerDTO) {
        return Result.success(userService.createUser(registerDTO));
    }

    /**
     * 根据用户名获取用户认证信息（包含密码）
     * <p>
     * 根据用户名查询用户认证信息，供内部服务间调用。
     * <strong>警告：返回的信息包含加密密码，仅限内部Feign调用使用！</strong>
     *
     * @param username 用户名，不能为空或空白字符串
     * @return 用户认证信息，如果用户不存在则返回null
     */
    @GetMapping("/username/{username}")
    @Operation(summary = "根据用户名查询用户认证信息", description = "供认证服务调用，返回包含密码的用户信息")
    public Result<UserBasicInfoVO> getUserByUsername(
            @Parameter(description = "用户名") @PathVariable("username") @NotBlank String username) {
        return Result.success(userService.getUserAuthByUsername(username));
    }

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return true表示用户名已存在，false表示用户名可用
     */
    @GetMapping("/exists/username/{username}")
    @Operation(summary = "检查用户名是否存在", description = "验证用户名是否已被使用")
    public Result<Boolean> checkUsernameExists(
            @Parameter(description = "用户名") @PathVariable("username") @NotBlank String username) {
        return Result.success(userService.isUsernameExists(username));
    }

    /**
     * 检查学号是否存在
     *
     * @param studentId 学号
     * @return true表示学号已存在，false表示学号可用
     */
    @GetMapping("/exists/student-id/{studentId}")
    @Operation(summary = "检查学号是否存在", description = "验证学号是否已被使用")
    public Result<Boolean> checkStudentIdExists(
            @Parameter(description = "学号") @PathVariable("studentId") @NotBlank String studentId) {
        return Result.success(userService.isStudentIdExists(studentId));
    }

    @GetMapping("/exists/user-id/{userId}")
    @Operation(summary = "检查用户ID是否存在", description = "验证用户ID是否存在")
    public Result<Boolean> checkUserIdExists(
            @Parameter(description = "用户ID") @PathVariable("userId") @NotNull Long userId) {
        return Result.success(userService.isUserIdExists(userId));
    }

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "根据用户ID获取用户信息", description = "根据用户ID查询用户基本信息")
    public Result<UserVO> getUserById(
            @Parameter(description = "用户ID") @PathVariable("userId") @NotNull Long userId) {
        return Result.success(userService.getUserById(userId));
    }

    /**
     * 批量查询用户信息
     *
     * @param userIds 用户ID列表
     * @return 用户ID -> 用户信息的映射
     */
    @GetMapping("/batch")
    @Operation(summary = "批量查询用户信息", description = "根据用户ID列表批量查询用户基本信息和昵称")
    public com.hngy.siae.core.result.Result<java.util.Map<Long, UserProfileSimpleVO>> batchGetUserProfiles(
            @Parameter(description = "用户ID列表") @RequestParam("userIds") java.util.List<Long> userIds) {
        return Result.success(userService.batchGetUserProfiles(userIds));
    }

    /**
     * 获取所有用户ID列表（用于广播通知）
     *
     * @return 所有用户的ID列表
     */
    @GetMapping("/all-ids")
    @Operation(summary = "获取所有用户ID", description = "获取系统中所有用户的ID列表，用于广播通知等场景")
    public Result<java.util.List<Long>> getAllUserIds() {
        return Result.success(userService.getAllUserIds());
    }

    /**
     * 获取用户人脸认证信息
     * <p>
     * 用于人脸识别打卡场景，返回用户的真实姓名和身份证号
     *
     * @param userId 用户ID
     * @return 用户人脸认证信息
     */
    @GetMapping("/face-auth/{userId}")
    @Operation(summary = "获取用户人脸认证信息", description = "用于人脸识别打卡，返回真实姓名和身份证号")
    public Result<UserFaceAuthVO> getUserFaceAuthInfo(
            @Parameter(description = "用户ID") @PathVariable("userId") @NotNull Long userId) {
        return Result.success(userService.getUserFaceAuthInfo(userId));
    }
}
