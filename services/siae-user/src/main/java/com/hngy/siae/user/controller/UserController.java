package com.hngy.siae.user.controller;

import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.permissions.RoleConstants;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import static com.hngy.siae.core.permissions.UserPermissions.*;
import com.hngy.siae.user.dto.request.UserCreateDTO;
import com.hngy.siae.user.dto.request.UserQueryDTO;
import com.hngy.siae.user.dto.request.UserUpdateDTO;
import com.hngy.siae.user.dto.response.UserDetailVO;
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
    @SiaeAuthorize(RoleConstants.ADMIN_LEVEL + " and hasAuthority('" + USER_UPDATE + "')")
    public Result<UserVO> updateUser(
            @Parameter(description = "用户ID") @PathVariable("id") @NotNull Long id,
            @Parameter(description = "用户更新参数") @Valid @RequestBody UserUpdateDTO userUpdateDTO) {
        userUpdateDTO.setId(id);
        UserVO result = userService.updateUser(userUpdateDTO);
        return Result.success(result);
    }

    /**
     * 查询用户（支持按ID、用户名、学号查询）
     * 优先级：id > username > studentId
     *
     * @param id 用户ID
     * @param username 用户名
     * @param studentId 学号
     * @return 用户详细信息
     */
    @GetMapping
    @Operation(summary = "查询用户详细信息", description = "根据不同条件查询用户详细信息（支持按ID、用户名、学号查询，参数可选且可同时存在）")
    @SiaeAuthorize(RoleConstants.ANY_AUTHENTICATED)
    public Result<UserDetailVO> getUser(
            @Parameter(description = "用户ID") @RequestParam(required = false) Long id,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "学号") @RequestParam(required = false) String studentId) {
        // 至少需要提供一个查询参数
        if (id == null && (username == null || username.isBlank()) && (studentId == null || studentId.isBlank())) {
            AssertUtils.fail("必须提供id、username或studentId中的至少一个参数");
        }
        
        UserDetailVO result = userService.getUserDetail(id, username, studentId);
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
    @SiaeAuthorize(RoleConstants.ANY_AUTHENTICATED)
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

    /**
     * 检查学号是否存在
     *
     * @param studentId 学号
     * @return 是否存在
     */
    @GetMapping("/check-student-id/{studentId}")
    @Operation(summary = "检查学号是否存在", description = "验证学号是否已被使用")
    public Result<Boolean> checkStudentIdExists(
            @Parameter(description = "学号") @PathVariable("studentId") @NotBlank String studentId) {
        Boolean exists = userService.isStudentIdExists(studentId);
        return Result.success(exists);
    }
}