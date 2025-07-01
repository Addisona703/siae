package com.hngy.siae.user.controller;

import com.hngy.siae.common.dto.request.PageDTO;
import com.hngy.siae.common.dto.response.PageVO;
import com.hngy.siae.common.result.Result;
import com.hngy.siae.common.validation.CreateGroup;
import com.hngy.siae.common.validation.UpdateGroup;
import com.hngy.siae.user.dto.request.LoginDTO;
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
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 *
 * @author KEYKB
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户相关接口")
public class UserController {

    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "手机号验证码登录")
    Result<UserVO> login(@RequestBody LoginDTO loginDTO) {
        return Result.success(userService.login(loginDTO));
    }

    @PostMapping("/register")
    @Operation(summary = "创建用户", description = "创建一个新用户")
    @ApiResponse(responseCode = "200", description = "创建成功", content = @Content(schema = @Schema(implementation = UserVO.class)))
    public Result<UserVO> register(@Validated(CreateGroup.class) @RequestBody UserDTO userDTO) {
        return Result.success(userService.register(userDTO));
    }

    @PutMapping
    @Operation(summary = "更新用户", description = "更新用户信息")
    @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = UserVO.class)))
    public Result<UserVO> updateUser(@Validated(UpdateGroup.class) @RequestBody UserUpdateDTO userDTO) {
        return Result.success(userService.updateUser(userDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取用户", description = "根据用户ID查询用户详细信息")
    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = UserVO.class)))
    public Result<UserVO> getUserById(
        @Parameter(description = "用户ID", in = ParameterIn.PATH) @PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @GetMapping("/username/{username}")
    @Operation(summary = "根据用户名获取用户", description = "根据用户名查询用户详细信息")
    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = UserVO.class)))
    public Result<UserVO> getUserByUsername(
        @Parameter(description = "用户名", in = ParameterIn.PATH) @PathVariable String username) {
        return Result.success(userService.getUserByUsername(username));
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询用户列表", description = "根据条件分页查询用户列表")
    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = PageVO.class)))
    public Result<PageVO<UserVO>> listUsersByPage(@RequestBody PageDTO<UserQueryDTO> pageDTO) {
        return Result.success(userService.listUsersByPage(pageDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "根据ID删除用户", description = "根据用户ID删除用户")
    @ApiResponse(responseCode = "200", description = "删除成功", content = @Content(schema = @Schema(implementation = Boolean.class)))
    public Result<Boolean> deleteUser(
        @Parameter(description = "用户ID", in = ParameterIn.PATH) @PathVariable Long id) {
        return Result.success(userService.deleteUser(id));
    }
}