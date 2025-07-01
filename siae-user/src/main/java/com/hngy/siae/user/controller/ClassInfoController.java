package com.hngy.siae.user.controller;

import com.hngy.siae.common.dto.request.PageDTO;
import com.hngy.siae.common.dto.response.PageVO;
import com.hngy.siae.common.result.Result;
import com.hngy.siae.common.validation.CreateGroup;
import com.hngy.siae.common.validation.UpdateGroup;
import com.hngy.siae.user.dto.request.ClassInfoDTO;
import com.hngy.siae.user.dto.response.ClassInfoVO;
import com.hngy.siae.user.service.ClassInfoService;
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
 * 班级控制器
 *
 * @author KEYKB
 */
@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
@Tag(name = "班级管理", description = "班级信息相关接口")
public class ClassInfoController {

    private final ClassInfoService classInfoService;

    @PostMapping
    @Operation(summary = "创建班级", description = "创建新的班级信息")
    @ApiResponse(responseCode = "200", description = "创建成功", content = @Content(schema = @Schema(implementation = ClassInfoVO.class)))
    public Result<ClassInfoVO> createClass(@RequestBody @Validated(CreateGroup.class) ClassInfoDTO classInfoDTO) {
        return Result.success(classInfoService.createClass(classInfoDTO));
    }

    @PutMapping
    @Validated(UpdateGroup.class)
    @Operation(summary = "更新班级", description = "更新班级信息")
    @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = ClassInfoVO.class)))
    public Result<ClassInfoVO> updateClass(@RequestBody ClassInfoDTO classInfoDTO) {
        return Result.success(classInfoService.updateClass(classInfoDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取班级", description = "根据班级ID获取班级详细信息")
    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = ClassInfoVO.class)))
    public Result<ClassInfoVO> getClassById(
            @Parameter(description = "班级ID", example = "1", required = true, in = ParameterIn.PATH) @PathVariable Long id) {
        return Result.success(classInfoService.getClassById(id));
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询班级列表", description = "根据条件分页查询班级列表")
    // TODO: OpenAPI 3.0 对于泛型嵌套响应的 schema 定义不够精确，这里暂时使用 PageVO.class，前端需要了解实际返回类型
    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = PageVO.class)))
    public Result<PageVO<ClassInfoVO>> listClassesByPage(@RequestBody PageDTO<ClassInfoDTO> pageDTO) {
        return Result.success(classInfoService.listClassesByPage(pageDTO));
    }

    @GetMapping("/college/{collegeId}")
    @Operation(summary = "根据学院ID查询班级列表", description = "获取指定学院下的所有班级")
    // TODO: OpenAPI 3.0 对于泛型嵌套响应的 schema 定义不够精确，这里暂时使用 List.class，前端需要了解实际返回类型
    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = List.class)))
    public Result<List<ClassInfoVO>> listClassesByCollegeId(
            @Parameter(description = "学院ID", example = "1", required = true, in = ParameterIn.PATH) @PathVariable Long collegeId) {
        return Result.success(classInfoService.listClassesByCollegeId(collegeId));
    }

    @GetMapping("/major/{majorId}")
    @Operation(summary = "根据专业ID查询班级列表", description = "获取指定专业下的所有班级")
    // TODO: OpenAPI 3.0 对于泛型嵌套响应的 schema 定义不够精确，这里暂时使用 List.class，前端需要了解实际返回类型
    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = List.class)))
    public Result<List<ClassInfoVO>> listClassesByMajorId(
            @Parameter(description = "专业ID", example = "1", required = true, in = ParameterIn.PATH) @PathVariable Long majorId) {
        return Result.success(classInfoService.listClassesByMajorId(majorId));
    }

    @GetMapping("/year/{year}")
    @Operation(summary = "根据入学年份查询班级列表", description = "获取指定入学年份的所有班级")
    // TODO: OpenAPI 3.0 对于泛型嵌套响应的 schema 定义不够精确，这里暂时使用 List.class，前端需要了解实际返回类型
    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = List.class)))
    public Result<List<ClassInfoVO>> listClassesByYear(
            @Parameter(description = "入学年份", example = "2023", required = true, in = ParameterIn.PATH) @PathVariable Integer year) {
        return Result.success(classInfoService.listClassesByYear(year));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "根据ID删除班级", description = "删除指定ID的班级信息")
    @ApiResponse(responseCode = "200", description = "删除成功", content = @Content(schema = @Schema(implementation = Boolean.class)))
    public Result<Boolean> deleteClass(
            @Parameter(description = "班级ID", example = "1", required = true, in = ParameterIn.PATH) @PathVariable Long id) {
        return Result.success(classInfoService.deleteClass(id));
    }
}