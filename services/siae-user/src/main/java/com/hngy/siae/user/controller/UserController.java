package com.hngy.siae.user.controller;

import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import static com.hngy.siae.core.permissions.UserPermissions.*;
import com.hngy.siae.user.dto.request.UserCreateDTO;
import com.hngy.siae.user.dto.request.UserQueryDTO;
import com.hngy.siae.user.dto.request.UserUpdateDTO;
import com.hngy.siae.user.dto.response.UserVO;
import com.hngy.siae.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 * <p>
 * 提供用户管理相关的REST API接口，包括用户的增删改查操作。
 * 所有接口都需要相应的权限才能访问。
 *
 * @author KEYKB
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Validated
@Tag(name = "用户管理", description = "用户相关API")
public class UserController {

    private final UserService userService;

    /**
     * 创建用户
     *
     * @param userCreateDTO 用户创建参数
     * @return 创建成功的用户信息
     */
    @PostMapping
    @Operation(summary = "创建用户", description = "创建一个新用户")
    @SiaeAuthorize("hasAuthority('" + USER_CREATE + "')")
    public Result<UserVO> createUser(
            @Parameter(description = "用户创建参数") @Valid @RequestBody UserCreateDTO userCreateDTO) {
        UserVO result = userService.createUser(userCreateDTO);
        return Result.success(result);
    }

    /**
     * 更新用户信息
     *
     * @param id 用户ID
     * @param userUpdateDTO 用户更新参数
     * @return 更新后的用户信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新用户", description = "更新用户信息")
    @SiaeAuthorize("hasAuthority('" + USER_UPDATE + "')")
    public Result<UserVO> updateUser(
            @Parameter(description = "用户ID") @PathVariable("id") @NotNull Long id,
            @Parameter(description = "用户更新参数") @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        userUpdateDTO.setId(id); // 确保ID一致性
        UserVO result = userService.updateUser(userUpdateDTO);
        return Result.success(result);
    }

    /**
     * 根据ID获取用户信息
     *
     * @param id 用户ID
     * @return 用户详细信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取用户", description = "根据用户ID查询用户详细信息")
    @SiaeAuthorize("hasAuthority('" + USER_VIEW + "')")
    public Result<UserVO> getUserById(
            @Parameter(description = "用户ID") @PathVariable("id") @NotNull Long id) {
        UserVO result = userService.getUserById(id);
        return Result.success(result);
    }

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户详细信息
     */
    @GetMapping("/username/{username}")
    @Operation(summary = "根据用户名获取用户", description = "根据用户名查询用户详细信息")
    @SiaeAuthorize("hasAuthority('" + USER_VIEW + "')")
    public Result<UserVO> getUserByUsername(
            @Parameter(description = "用户名") @PathVariable("username") @NotBlank String username) {
        UserVO result = userService.getUserByUsername(username);
        return Result.success(result);
    }

    /**
     * 分页查询用户列表
     *
     * @param pageDTO 分页查询参数
     * @return 分页用户列表
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询用户列表", description = "根据条件分页查询用户列表")
    @SiaeAuthorize("hasAuthority('" + USER_LIST + "')")
    public Result<PageVO<UserVO>> listUsersByPage(
            @Parameter(description = "分页查询参数") @Valid @RequestBody PageDTO<UserQueryDTO> pageDTO) {
        PageVO<UserVO> result = userService.listUsersByPage(pageDTO);
        return Result.success(result);
    }

    /**
     * 根据ID删除用户
     *
     * @param id 用户ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "根据ID删除用户", description = "根据用户ID删除用户")
    @SiaeAuthorize("hasAuthority('" + USER_DELETE + "')")
    public Result<Boolean> deleteUser(
            @Parameter(description = "用户ID") @PathVariable("id") @NotNull Long id) {
        Boolean result = userService.deleteUser(id);
        return Result.success(result);
    }
}