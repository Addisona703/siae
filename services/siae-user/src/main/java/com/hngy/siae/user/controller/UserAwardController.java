package com.hngy.siae.user.controller;

import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.permissions.RoleConstants;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.user.dto.request.UserAwardCreateDTO;
import com.hngy.siae.user.dto.request.UserAwardQueryDTO;
import com.hngy.siae.user.dto.request.UserAwardUpdateDTO;
import com.hngy.siae.user.dto.response.UserAwardVO;
import com.hngy.siae.user.service.UserAwardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import static com.hngy.siae.core.permissions.UserPermissions.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;



/**
 * 用户获奖记录控制器
 * <p>
 * 提供用户获奖记录管理相关的REST API接口，包括获奖记录的增删改查操作。
 * 所有接口都需要相应的权限才能访问。
 *
 * @author KEYKB
 */
@RestController
@RequestMapping("/awards")
@RequiredArgsConstructor
@Tag(name = "用户获奖记录管理", description = "用户获奖记录相关接口")
public class UserAwardController {

    private final UserAwardService userAwardService;

    /**
     * 创建用户获奖记录
     *
     * @param userAwardCreateDTO 用户获奖记录创建参数
     * @return 创建成功的用户获奖记录信息
     */
    @PostMapping
    @Operation(summary = "创建用户获奖记录", description = "创建新的用户获奖记录")
    @SiaeAuthorize(RoleConstants.MEMBER_LEVEL + " and hasAuthority('" + USER_AWARD_CREATE + "')")
    public Result<UserAwardVO> createUserAward(
            @Parameter(description = "用户获奖记录创建参数") @Valid @RequestBody UserAwardCreateDTO userAwardCreateDTO) {
        return Result.success(userAwardService.createUserAward(userAwardCreateDTO));
    }

    /**
     * 更新用户获奖记录信息
     *
     * @param id 获奖记录ID
     * @param userAwardUpdateDTO 用户获奖记录更新参数
     * @return 更新后的用户获奖记录信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新用户获奖记录", description = "更新用户获奖记录信息")
    @SiaeAuthorize(RoleConstants.MEMBER_LEVEL + " and hasAuthority('" + USER_AWARD_UPDATE + "')")
    public Result<UserAwardVO> updateUserAward(
            @Parameter(description = "获奖记录ID") @PathVariable("id") @NotNull Long id,
            @Parameter(description = "用户获奖记录更新参数") @Valid @RequestBody UserAwardUpdateDTO userAwardUpdateDTO) {
        userAwardUpdateDTO.setId(id);
        return Result.success(userAwardService.updateUserAward(userAwardUpdateDTO));
    }

    /**
     * 根据ID获取用户获奖记录信息
     *
     * @param id 获奖记录ID
     * @return 用户获奖记录详细信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取用户获奖记录", description = "根据ID查询用户获奖记录详细信息")
    @SiaeAuthorize(RoleConstants.MEMBER_LEVEL + " and hasAuthority('" + USER_AWARD_VIEW + "')")
    public Result<UserAwardVO> getUserAwardById(
            @Parameter(description = "获奖记录ID") @PathVariable("id") @NotNull Long id) {
        return Result.success(userAwardService.getUserAwardById(id));
    }

    /**
     * 根据用户ID分页获取用户获奖记录列表
     *
     * @param userId 用户ID
     * @param pageDTO 分页参数
     * @return 用户获奖记录分页列表
     */
    @PostMapping("/user/{userId}/page")
    @Operation(summary = "根据用户ID分页获取用户获奖记录列表", description = "根据用户ID分页查询该用户的获奖记录")
    @SiaeAuthorize(RoleConstants.MEMBER_LEVEL + " and hasAuthority('" + USER_AWARD_LIST + "')")
    public Result<PageVO<UserAwardVO>> pageUserAwardsByUserId(
            @Parameter(description = "用户ID") @PathVariable("userId") @NotNull Long userId,
            @Parameter(description = "分页参数") @Valid @RequestBody PageDTO<Void> pageDTO) {
        return Result.success(userAwardService.pageUserAwardsByUserId(userId, pageDTO));
    }

    /**
     * 分页查询用户获奖记录列表
     *
     * @param pageDTO 分页查询参数
     * @return 分页用户获奖记录列表
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询用户获奖记录", description = "根据条件分页查询用户获奖记录列表")
    @SiaeAuthorize(RoleConstants.MEMBER_LEVEL + " and hasAuthority('" + USER_AWARD_LIST + "')")
    public Result<PageVO<UserAwardVO>> listUserAwardsByPage(
            @Parameter(description = "分页查询参数") @Valid @RequestBody PageDTO<UserAwardQueryDTO> pageDTO) {
        return Result.success(userAwardService.listUserAwardsByPage(pageDTO));
    }

    /**
     * 根据ID删除用户获奖记录
     *
     * @param id 获奖记录ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "根据ID删除用户获奖记录", description = "删除指定ID的用户获奖记录")
    @SiaeAuthorize(RoleConstants.ADMIN_LEVEL + " and hasAuthority('" + USER_AWARD_DELETE + "')")
    public Result<Boolean> deleteUserAward(
            @Parameter(description = "获奖记录ID") @PathVariable("id") @NotNull Long id) {
        return Result.success(userAwardService.deleteUserAward(id));
    }
}