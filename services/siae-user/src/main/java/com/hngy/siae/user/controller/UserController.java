package com.hngy.siae.user.controller;

import com.hngy.siae.common.dto.request.PageDTO;
import com.hngy.siae.common.dto.response.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.common.validation.CreateGroup;
import com.hngy.siae.common.validation.UpdateGroup;
import com.hngy.siae.user.dto.request.UserDTO;
import com.hngy.siae.user.dto.request.UserQueryDTO;
import com.hngy.siae.user.dto.request.UserUpdateDTO;
import com.hngy.siae.user.dto.response.UserVO;
import com.hngy.siae.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.hngy.siae.core.permissions.UserPermissions.*;

/**
 * 用户控制器
 *
 * @author KEYKB
 */
@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户相关接口")
public class UserController {

    private final UserService userService;

    /**
     * 创建用户
     *
     * @param userDTO 用户创建请求DTO
     * @return 创建的用户信息
     */
    @Operation(summary = "创建用户", description = "创建一个新用户")
    @ApiResponse(responseCode = "200", description = "创建成功",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = UserVO.class)))
    @PostMapping("/create")
    @PreAuthorize("hasAuthority('" + USER_CREATE + "')")
    public Result<UserVO> createUser(
            @Parameter(description = "用户创建请求数据，包含用户基本信息", required = true)
            @Validated(CreateGroup.class) @RequestBody UserDTO userDTO) {
        return Result.success(userService.createUser(userDTO));
    }

    /**
     * 更新用户
     *
     * @param userDTO 用户更新请求DTO
     * @return 更新后的用户信息
     */
    @Operation(summary = "更新用户", description = "更新用户信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "更新成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = UserVO.class))),
        @ApiResponse(responseCode = "404", description = "用户不存在",
            content = @Content(mediaType = "application/json"))
    })
    @PutMapping
    @PreAuthorize("hasAuthority('" + USER_UPDATE + "')")
    public Result<UserVO> updateUser(
            @Parameter(description = "用户更新请求数据，必须包含用户ID和要修改的字段", required = true)
            @Validated(UpdateGroup.class) @RequestBody UserUpdateDTO userDTO) {
        return Result.success(userService.updateUser(userDTO));
    }

    /**
     * 根据ID获取用户
     *
     * @param id 用户ID
     * @return 用户详细信息
     */
    @Operation(summary = "根据ID获取用户", description = "根据用户ID查询用户详细信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = UserVO.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误，用户ID无效",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "未授权访问",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "权限不足，需要用户查询权限",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "用户不存在",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "服务器内部错误",
            content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + USER_VIEW + "')")
    public Result<UserVO> getUserById(
            @Parameter(description = "用户ID，用于唯一标识要查询的用户", required = true, example = "1", in = ParameterIn.PATH)
            @PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "根据用户名获取用户", description = "根据用户名查询用户详细信息")
    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = UserVO.class)))
    public Result<UserVO> getUserByUsername(
        @Parameter(description = "用户名", in = ParameterIn.PATH) @PathVariable("username") String username) {
        return Result.success(userService.getUserByUsername(username));
    }

    /**
     * 分页查询用户列表
     *
     * @param pageDTO 分页查询请求DTO
     * @return 分页用户列表
     */
    @Operation(summary = "分页查询用户列表", description = "根据条件分页查询用户列表")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = PageVO.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "未授权访问",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "权限不足，需要用户列表查询权限",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "服务器内部错误",
            content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('" + USER_LIST + "')")
    public Result<PageVO<UserVO>> listUsersByPage(
            @Parameter(description = "分页查询请求数据，包含分页参数和查询条件", required = true)
            @RequestBody PageDTO<UserQueryDTO> pageDTO) {
        return Result.success(userService.listUsersByPage(pageDTO));
    }

    /**
     * 根据ID删除用户
     *
     * @param id 用户ID
     * @return 删除结果
     */
    @Operation(summary = "根据ID删除用户", description = "根据用户ID删除用户")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "删除成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Boolean.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误，用户ID无效",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "未授权访问",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "权限不足，需要用户删除权限",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "用户不存在",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "服务器内部错误",
            content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + USER_DELETE + "')")
    public Result<Boolean> deleteUser(
            @Parameter(description = "用户ID，用于唯一标识要删除的用户", required = true, example = "1", in = ParameterIn.PATH)
            @PathVariable Long id) {
        return Result.success(userService.deleteUser(id));
    }
}