package com.hngy.siae.user.controller;

import com.hngy.siae.common.dto.request.PageDTO;
import com.hngy.siae.common.dto.response.PageVO;
import com.hngy.siae.common.result.Result;
import com.hngy.siae.user.dto.request.UserAwardCreateDTO;
import com.hngy.siae.user.dto.request.UserAwardQueryDTO;
import com.hngy.siae.user.dto.request.UserAwardUpdateDTO;
import com.hngy.siae.user.dto.response.UserAwardVO;
import com.hngy.siae.user.service.UserAwardService;
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

import java.util.List;

/**
 * 用户获奖记录控制器
 *
 * @author KEYKB
 */
@RestController
@RequestMapping("/api/user-awards")
@RequiredArgsConstructor
@Tag(name = "用户获奖记录管理", description = "用户获奖记录相关接口")
public class UserAwardController {

    private final UserAwardService userAwardService;

    @PostMapping
    @Operation(summary = "创建用户获奖记录")
    @ApiResponse(responseCode = "200", description = "创建成功", content = @Content(schema = @Schema(implementation = UserAwardVO.class)))
    public Result<UserAwardVO> createUserAward(@RequestBody @Validated UserAwardCreateDTO userAwardCreateDTO) {
        return Result.success(userAwardService.createUserAward(userAwardCreateDTO));
    }

    @PutMapping
    @Operation(summary = "更新用户获奖记录")
    @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = UserAwardVO.class)))
    public Result<UserAwardVO> updateUserAward(@RequestBody @Validated UserAwardUpdateDTO userAwardUpdateDTO) {
        return Result.success(userAwardService.updateUserAward(userAwardUpdateDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取用户获奖记录")
    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = UserAwardVO.class)))
    public Result<UserAwardVO> getUserAwardById(@Parameter(description = "获奖记录ID", required = true, in = ParameterIn.PATH) @PathVariable Long id) {
        return Result.success(userAwardService.getUserAwardById(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "根据用户ID获取用户获奖记录列表")
    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = List.class)))
    public Result<List<UserAwardVO>> listUserAwardsByUserId(@Parameter(description = "用户ID", required = true, in = ParameterIn.PATH) @PathVariable Long userId) {
        return Result.success(userAwardService.listUserAwardsByUserId(userId));
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询用户获奖记录")
    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = PageVO.class)))
    public Result<PageVO<UserAwardVO>> listUserAwardsByPage(@org.springframework.web.bind.annotation.RequestBody PageDTO<UserAwardQueryDTO> pageDTO) {
        return Result.success(userAwardService.listUserAwardsByPage(pageDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "根据ID删除用户获奖记录")
    @ApiResponse(responseCode = "200", description = "删除成功", content = @Content(schema = @Schema(implementation = Boolean.class)))
    public Result<Boolean> deleteUserAward(@Parameter(description = "获奖记录ID", required = true, in = ParameterIn.PATH) @PathVariable Long id) {
        return Result.success(userAwardService.deleteUserAward(id));
    }
}