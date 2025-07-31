package com.hngy.siae.user.controller;

import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.user.dto.request.ClassInfoCreateDTO;
import com.hngy.siae.user.dto.request.ClassInfoQueryDTO;
import com.hngy.siae.user.dto.request.ClassInfoUpdateDTO;
import com.hngy.siae.user.dto.response.ClassInfoVO;
import com.hngy.siae.user.service.ClassInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import static com.hngy.siae.core.permissions.UserPermissions.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 班级控制器
 * <p>
 * 提供班级信息管理相关的REST API接口，包括班级的增删改查操作。
 * 所有接口都需要相应的权限才能访问。
 *
 * @author KEYKB
 */
@RestController
@RequestMapping("/classes")
@RequiredArgsConstructor
@Tag(name = "班级管理", description = "班级信息相关接口")
public class ClassInfoController {

    private final ClassInfoService classInfoService;

    /**
     * 创建班级
     *
     * @param classInfoCreateDTO 班级创建参数
     * @return 创建成功的班级信息
     */
    @PostMapping
    @Operation(summary = "创建班级", description = "创建新的班级信息")
    @SiaeAuthorize("hasAuthority('" + USER_CLASS_CREATE + "')")
    public Result<ClassInfoVO> createClass(
            @Parameter(description = "班级创建参数") @Valid @RequestBody ClassInfoCreateDTO classInfoCreateDTO) {
        return Result.success(classInfoService.createClass(classInfoCreateDTO));
    }

    /**
     * 更新班级信息
     *
     * @param classInfoUpdateDTO 班级更新参数
     * @return 更新后的班级信息
     */
    @PutMapping
    @Operation(summary = "更新班级", description = "更新班级信息")
    @SiaeAuthorize("hasAuthority('" + USER_CLASS_UPDATE + "')")
    public Result<ClassInfoVO> updateClass(
            @Parameter(description = "班级更新参数") @Valid @RequestBody ClassInfoUpdateDTO classInfoUpdateDTO) {
        return Result.success(classInfoService.updateClass(classInfoUpdateDTO));
    }

    /**
     * 根据ID获取班级信息
     *
     * @param id 班级ID
     * @return 班级详细信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取班级", description = "根据班级ID获取班级详细信息")
    @SiaeAuthorize("hasAuthority('" + USER_CLASS_VIEW + "')")
    public Result<ClassInfoVO> getClassById(
            @Parameter(description = "班级ID") @PathVariable("id") @NotNull Long id) {
        return Result.success(classInfoService.getClassById(id));
    }

    /**
     * 分页查询班级列表
     *
     * @param pageDTO 分页查询参数
     * @return 分页班级列表
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询班级列表", description = "根据条件分页查询班级列表")
    @SiaeAuthorize("hasAuthority('" + USER_CLASS_LIST + "')")
    public Result<PageVO<ClassInfoVO>> listClassesByPage(
            @Parameter(description = "分页查询参数") @Valid @RequestBody PageDTO<ClassInfoQueryDTO> pageDTO) {
        return Result.success(classInfoService.listClassesByPage(pageDTO));
    }

    /**
     * 根据学院ID获取班级列表
     *
     * @param collegeId 学院ID
     * @return 班级列表
     */
    @GetMapping("/college/{collegeId}")
    @Operation(summary = "根据学院ID查询班级列表", description = "获取指定学院下的所有班级")
    @SiaeAuthorize("hasAuthority('" + USER_CLASS_LIST + "')")
    public Result<List<ClassInfoVO>> listClassesByCollegeId(
            @Parameter(description = "学院ID") @PathVariable("collegeId") @NotNull Long collegeId) {
        return Result.success(classInfoService.listClassesByCollegeId(collegeId));
    }

    /**
     * 根据专业ID获取班级列表
     *
     * @param majorId 专业ID
     * @return 班级列表
     */
    @GetMapping("/major/{majorId}")
    @Operation(summary = "根据专业ID查询班级列表", description = "获取指定专业下的所有班级")
    @SiaeAuthorize("hasAuthority('" + USER_CLASS_LIST + "')")
    public Result<List<ClassInfoVO>> listClassesByMajorId(
            @Parameter(description = "专业ID") @PathVariable("majorId") @NotNull Long majorId) {
        return Result.success(classInfoService.listClassesByMajorId(majorId));
    }

    /**
     * 根据入学年份获取班级列表
     *
     * @param year 入学年份
     * @return 班级列表
     */
    @GetMapping("/year/{year}")
    @Operation(summary = "根据入学年份查询班级列表", description = "获取指定入学年份的所有班级")
    @SiaeAuthorize("hasAuthority('" + USER_CLASS_LIST + "')")
    public Result<List<ClassInfoVO>> listClassesByYear(
            @Parameter(description = "入学年份") @PathVariable("year") @NotNull Integer year) {
        return Result.success(classInfoService.listClassesByYear(year));
    }

    /**
     * 根据ID删除班级
     *
     * @param id 班级ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "根据ID删除班级", description = "删除指定ID的班级信息")
    @SiaeAuthorize("hasAuthority('" + USER_CLASS_DELETE + "')")
    public Result<Boolean> deleteClass(
            @Parameter(description = "班级ID") @PathVariable("id") @NotNull Long id) {
        return Result.success(classInfoService.deleteClass(id));
    }
}