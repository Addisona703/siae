package com.hngy.siae.user.controller;

import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.user.dto.request.UserProfileCreateDTO;
import com.hngy.siae.user.dto.request.UserProfileQueryDTO;
import com.hngy.siae.user.dto.request.UserProfileUpdateDTO;
import com.hngy.siae.user.dto.response.UserProfileVO;
import com.hngy.siae.user.service.UserProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import static com.hngy.siae.core.permissions.UserPermissions.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户详情控制器
 * <p>
 * 提供用户个人资料管理相关的REST API接口，包括用户详情的增删改查操作。
 * 所有接口都需要相应的权限才能访问。
 *
 * @author KEYKB
 */
@RestController
@RequestMapping("/profile")
@RequiredArgsConstructor
@Tag(name = "用户详情管理", description = "用户个人资料相关接口")
@Validated
public class UserProfileController {

    private final UserProfileService userProfileService;

    /**
     * 创建用户详情
     *
     * @param userProfileCreateDTO 用户详情创建参数
     * @return 创建成功的用户详情信息
     */
    @PostMapping
    @Operation(summary = "创建用户详情", description = "创建新的用户详细信息")
    @SiaeAuthorize("hasAuthority('" + USER_PROFILE_CREATE + "')")
    public Result<UserProfileVO> createUserProfile(
            @Parameter(description = "用户详情创建参数") @Valid @RequestBody UserProfileCreateDTO userProfileCreateDTO) {
        return Result.success(userProfileService.createUserProfile(userProfileCreateDTO));
    }

    /**
     * 更新用户详情信息
     *
     * @param userProfileUpdateDTO 用户详情更新参数
     * @return 更新后的用户详情信息
     */
    @PutMapping
    @Operation(summary = "更新用户详情", description = "更新用户详细信息")
    @SiaeAuthorize("hasAuthority('" + USER_PROFILE_UPDATE + "')")
    public Result<UserProfileVO> updateUserProfile(
            @Parameter(description = "用户详情更新参数") @Valid @RequestBody UserProfileUpdateDTO userProfileUpdateDTO) {
        return Result.success(userProfileService.updateUserProfile(userProfileUpdateDTO));
    }

    /**
     * 根据用户ID获取用户详情信息
     *
     * @param userId 用户ID
     * @return 用户详情信息
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "根据用户ID获取用户详情", description = "获取指定用户ID的详细资料")
    @SiaeAuthorize("hasAuthority('" + USER_PROFILE_VIEW + "')")
    public Result<UserProfileVO> getUserProfileByUserId(
            @Parameter(description = "用户ID") @PathVariable("userId") @NotNull Long userId) {
        return Result.success(userProfileService.getUserProfileByUserId(userId));
    }

    /**
     * 根据邮箱获取用户详情信息
     *
     * @param email 邮箱地址
     * @return 用户详情信息
     */
    @GetMapping("/email/{email}")
    @Operation(summary = "根据邮箱获取用户详情", description = "通过邮箱地址查询用户详细信息")
    @SiaeAuthorize("hasAuthority('" + USER_PROFILE_VIEW + "')")
    public Result<UserProfileVO> getUserProfileByEmail(
            @Parameter(description = "邮箱") @PathVariable("email") @NotBlank String email) {
        return Result.success(userProfileService.getUserProfileByEmail(email));
    }

    /**
     * 根据手机号获取用户详情信息
     *
     * @param phone 手机号码
     * @return 用户详情信息
     */
    @GetMapping("/phone/{phone}")
    @Operation(summary = "根据手机号获取用户详情", description = "通过手机号码查询用户详细信息")
    @SiaeAuthorize("hasAuthority('" + USER_PROFILE_VIEW + "')")
    public Result<UserProfileVO> getUserProfileByPhone(
            @Parameter(description = "手机号") @PathVariable("phone") @NotBlank String phone) {
        return Result.success(userProfileService.getUserProfileByPhone(phone));
    }

    /**
     * 分页查询用户详情列表
     *
     * @param pageDTO 分页查询参数
     * @return 分页用户详情列表
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询用户详情列表", description = "根据条件分页查询用户详情列表")
    @SiaeAuthorize("hasAuthority('" + USER_PROFILE_VIEW + "')")
    public Result<PageVO<UserProfileVO>> listUserProfilesByPage(
            @Parameter(description = "分页查询参数") @Valid @RequestBody PageDTO<UserProfileQueryDTO> pageDTO) {
        return Result.success(userProfileService.listUserProfilesByPage(pageDTO));
    }
}