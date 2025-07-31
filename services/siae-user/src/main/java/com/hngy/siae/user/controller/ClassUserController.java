package com.hngy.siae.user.controller;

import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import static com.hngy.siae.core.permissions.UserPermissions.*;
import com.hngy.siae.user.dto.request.ClassUserCreateDTO;
import com.hngy.siae.user.dto.request.ClassUserQueryDTO;
import com.hngy.siae.user.dto.request.ClassUserUpdateDTO;
import com.hngy.siae.user.dto.response.ClassUserVO;
import com.hngy.siae.user.service.ClassUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 班级用户关联控制器
 * <p>
 * 提供班级用户关联管理相关的REST API接口，包括添加用户到班级、更新关联信息、查询和删除关联关系。
 * 所有接口都需要相应的权限才能访问。
 *
 * @author KEYKB
 */
@RestController
@RequestMapping("/class-users")
@RequiredArgsConstructor
@Validated
@Tag(name = "班级用户关联管理", description = "班级用户关联相关API")
public class ClassUserController {

    private final ClassUserService classUserService;

    /**
     * 添加用户到班级
     *
     * @param classUserCreateDTO 班级用户关联创建参数
     * @return 创建成功的班级用户关联信息
     */
    @PostMapping
    @Operation(summary = "添加用户到班级", description = "将用户添加到指定班级")
    @SiaeAuthorize("hasAuthority('" + USER_CLASS_CREATE + "')")
    public Result<ClassUserVO> addUserToClass(
            @Parameter(description = "班级用户关联创建参数") @Valid @RequestBody ClassUserCreateDTO classUserCreateDTO) {
        return Result.success(classUserService.addUserToClass(classUserCreateDTO));
    }

    /**
     * 更新用户班级关联信息
     *
     * @param classUserUpdateDTO 班级用户关联更新参数
     * @return 更新后的班级用户关联信息
     */
    @PutMapping
    @Operation(summary = "更新用户班级关联信息", description = "更新用户在班级中的成员类型和状态")
    @SiaeAuthorize("hasAuthority('" + USER_CLASS_UPDATE + "')")
    public Result<ClassUserVO> updateClassUser(
            @Parameter(description = "班级用户关联更新参数") @Valid @RequestBody ClassUserUpdateDTO classUserUpdateDTO) {
        return Result.success(classUserService.updateClassUser(classUserUpdateDTO));
    }

    /**
     * 根据ID获取班级用户关联信息
     *
     * @param id 关联记录ID
     * @return 班级用户关联详细信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取班级用户关联信息", description = "根据关联记录ID查询详细信息")
    @SiaeAuthorize("hasAuthority('" + USER_CLASS_VIEW + "')")
    public Result<ClassUserVO> getClassUserById(
            @Parameter(description = "关联记录ID") @PathVariable("id") @NotNull Long id) {
        return Result.success(classUserService.getClassUserById(id));
    }

    /**
     * 分页查询班级用户关联列表
     *
     * @param pageDTO 分页查询参数
     * @return 分页班级用户关联列表
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询班级用户关联列表", description = "根据条件分页查询班级下的用户列表")
    @SiaeAuthorize("hasAuthority('" + USER_CLASS_LIST + "')")
    public Result<PageVO<ClassUserVO>> listClassUsersByPage(
            @Parameter(description = "分页查询参数") @Valid @RequestBody PageDTO<ClassUserQueryDTO> pageDTO) {
        return Result.success(classUserService.listClassUsersByPage(pageDTO));
    }

    /**
     * 根据班级ID获取用户列表
     *
     * @param classId 班级ID
     * @return 班级下的用户列表
     */
    @GetMapping("/class/{classId}/users")
    @Operation(summary = "根据班级ID获取用户列表", description = "获取指定班级下的所有用户")
    @SiaeAuthorize("hasAuthority('" + USER_CLASS_LIST + "')")
    public Result<List<ClassUserVO>> listUsersByClassId(
            @Parameter(description = "班级ID") @PathVariable("classId") @NotNull Long classId) {
        return Result.success(classUserService.listUsersByClassId(classId));
    }

    /**
     * 从班级移除用户
     *
     * @param classId 班级ID
     * @param userId 用户ID
     * @return 移除结果
     */
    @DeleteMapping("/{classId}/users/{userId}")
    @Operation(summary = "从班级移除用户", description = "将指定用户从班级中移除")
    @SiaeAuthorize("hasAuthority('" + USER_CLASS_DELETE + "')")
    public Result<Boolean> removeUserFromClass(
            @Parameter(description = "班级ID") @PathVariable("classId") @NotNull Long classId,
            @Parameter(description = "用户ID") @PathVariable("userId") @NotNull Long userId) {
        return Result.success(classUserService.removeUserFromClass(classId, userId));
    }

    /**
     * 根据ID删除班级用户关联
     *
     * @param id 关联记录ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "根据ID删除班级用户关联", description = "删除指定的班级用户关联记录")
    @SiaeAuthorize("hasAuthority('" + USER_CLASS_DELETE + "')")
    public Result<Boolean> deleteClassUser(
            @Parameter(description = "关联记录ID") @PathVariable("id") @NotNull Long id) {
        return Result.success(classUserService.deleteClassUser(id));
    }
}
