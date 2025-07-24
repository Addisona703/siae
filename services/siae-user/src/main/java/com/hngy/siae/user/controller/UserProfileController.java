package com.hngy.siae.user.controller;

import com.hngy.siae.core.result.Result;
import com.hngy.siae.core.validation.CreateGroup;
import com.hngy.siae.core.validation.UpdateGroup;
import com.hngy.siae.user.dto.request.UserProfileDTO;
import com.hngy.siae.user.dto.response.UserProfileVO;
import com.hngy.siae.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户详情控制器
 *
 * @author KEYKB
 */
@RestController
@RequestMapping("/api/user-profiles")
@RequiredArgsConstructor
@Tag(name = "用户详情管理", description = "用户个人资料相关接口")
@Validated
public class UserProfileController {

    private final UserProfileService userProfileService;

    @PostMapping
    @Operation(summary = "创建用户详情", description = "创建新的用户详细信息")
    @ApiResponse(responseCode = "200", description = "创建成功", content = @Content(schema = @Schema(implementation = UserProfileVO.class)))
    public Result<UserProfileVO> createUserProfile(@Validated(CreateGroup.class) @RequestBody UserProfileDTO userProfileDTO) {
        return Result.success(userProfileService.createUserProfile(userProfileDTO));
    }

    @PutMapping("/{userId}")
    @Operation(summary = "更新用户详情", description = "更新指定用户ID的详细信息")
    @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = UserProfileVO.class)))
    public Result<UserProfileVO> updateUserProfile(
        @Parameter(description = "用户ID", in = ParameterIn.PATH) @PathVariable("userId") Long userId,
        @Validated(UpdateGroup.class) @RequestBody UserProfileDTO userProfileDTO) {
        return Result.success(userProfileService.updateUserProfile(userId, userProfileDTO));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "根据用户ID获取用户详情", description = "获取指定用户ID的详细资料")
    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = UserProfileVO.class)))
    public Result<UserProfileVO> getUserProfileByUserId(
        @Parameter(description = "用户ID", in = ParameterIn.PATH) @PathVariable("userId") Long userId) {
        return Result.success(userProfileService.getUserProfileByUserId(userId));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "根据邮箱获取用户详情", description = "通过邮箱地址查询用户详细信息")
    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = UserProfileVO.class)))
    public Result<UserProfileVO> getUserProfileByEmail(
        @Parameter(description = "邮箱", in = ParameterIn.PATH) @PathVariable("email") String email) {
        return Result.success(userProfileService.getUserProfileByEmail(email));
    }

    @GetMapping("/phone/{phone}")
    @Operation(summary = "根据手机号获取用户详情", description = "通过手机号码查询用户详细信息")
    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = UserProfileVO.class)))
    public Result<UserProfileVO> getUserProfileByPhone(
        @Parameter(description = "手机号", in = ParameterIn.PATH) @PathVariable("phone") String phone) {
        return Result.success(userProfileService.getUserProfileByPhone(phone));
    }
}