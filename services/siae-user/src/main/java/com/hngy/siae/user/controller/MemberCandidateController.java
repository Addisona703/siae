package com.hngy.siae.user.controller;

import com.hngy.siae.common.dto.request.PageDTO;
import com.hngy.siae.common.dto.response.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.common.validation.CreateGroup;
import com.hngy.siae.common.validation.UpdateGroup;
import com.hngy.siae.user.dto.request.MemberCandidateDTO;
import com.hngy.siae.user.dto.response.MemberCandidateVO;
import com.hngy.siae.user.service.MemberCandidateService;
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
 * 候选成员控制器
 *
 * @author KEYKB
 */
@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
@Tag(name = "候选成员管理", description = "候选成员相关接口")
public class MemberCandidateController {

    private final MemberCandidateService memberCandidateService;

    @PostMapping
    @Operation(summary = "添加候选成员", description = "添加新的候选成员信息")
    @ApiResponse(responseCode = "200", description = "操作成功", content = @Content(schema = @Schema(implementation = MemberCandidateVO.class)))
    public Result<MemberCandidateVO> addCandidate(@Validated(CreateGroup.class) @RequestBody MemberCandidateDTO candidateDTO) {
        return Result.success(memberCandidateService.addCandidate(candidateDTO));
    }

    @PutMapping
    @Operation(summary = "更新候选成员信息", description = "更新候选成员的基本信息")
    @ApiResponse(responseCode = "200", description = "操作成功", content = @Content(schema = @Schema(implementation = MemberCandidateVO.class)))
    public Result<MemberCandidateVO> updateCandidate(@Validated(UpdateGroup.class) @RequestBody MemberCandidateDTO candidateDTO) {
        return Result.success(memberCandidateService.updateCandidate(candidateDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询候选成员信息", description = "根据候选成员ID查询详细信息")
    @ApiResponse(responseCode = "200", description = "操作成功", content = @Content(schema = @Schema(implementation = MemberCandidateVO.class)))
    public Result<MemberCandidateVO> getCandidateById(@Parameter(description = "候选成员ID", required = true, example = "1", in = ParameterIn.PATH) @PathVariable Long id) {
        return Result.success(memberCandidateService.getCandidateById(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "根据用户ID查询候选成员信息", description = "根据关联的用户ID查询候选成员详细信息")
    @ApiResponse(responseCode = "200", description = "操作成功", content = @Content(schema = @Schema(implementation = MemberCandidateVO.class)))
    public Result<MemberCandidateVO> getCandidateByUserId(@Parameter(description = "用户ID", required = true, example = "101", in = ParameterIn.PATH) @PathVariable Long userId) {
        return Result.success(memberCandidateService.getCandidateByUserId(userId));
    }

    @GetMapping("/student/{studentId}")
    @Operation(summary = "根据学号查询候选成员信息", description = "根据学号查询候选成员详细信息")
    @ApiResponse(responseCode = "200", description = "操作成功", content = @Content(schema = @Schema(implementation = MemberCandidateVO.class)))
    public Result<MemberCandidateVO> getCandidateByStudentId(@Parameter(description = "学号", required = true, example = "20230001", in = ParameterIn.PATH) @PathVariable String studentId) {
        return Result.success(memberCandidateService.getCandidateByStudentId(studentId));
    }

    @GetMapping("/department/{departmentId}")
    @Operation(summary = "根据部门ID查询候选成员列表", description = "查询指定部门下的所有候选成员")
    @ApiResponse(responseCode = "200", description = "操作成功", content = @Content(schema = @Schema(implementation = List.class)))
    public Result<List<MemberCandidateVO>> listCandidatesByDepartment(@Parameter(description = "部门ID", required = true, example = "1", in = ParameterIn.PATH) @PathVariable Long departmentId) {
        return Result.success(memberCandidateService.listCandidatesByDepartment(departmentId));
    }

    @PostMapping("/list")
    @Operation(summary = "动态条件查询候选成员列表", description = "根据提供的条件查询符合要求的候选成员列表")
    @ApiResponse(responseCode = "200", description = "操作成功", content = @Content(schema = @Schema(implementation = List.class)))
    public Result<List<MemberCandidateVO>> listCandidates(@RequestBody MemberCandidateDTO queryDTO) {
        return Result.success(memberCandidateService.listCandidates(queryDTO));
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询候选成员列表", description = "分页查询候选成员信息")
    @ApiResponse(responseCode = "200", description = "操作成功", content = @Content(schema = @Schema(implementation = PageVO.class)))
    public Result<PageVO<MemberCandidateVO>> pageCandidate(@RequestBody PageDTO<MemberCandidateDTO> pageDTO) {
        return Result.success(memberCandidateService.pageCandidate(pageDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除候选成员", description = "根据ID删除候选成员")
    @ApiResponse(responseCode = "200", description = "操作成功", content = @Content(schema = @Schema(implementation = Boolean.class)))
    public Result<Boolean> deleteCandidate(@Parameter(description = "候选成员ID", required = true, example = "1", in = ParameterIn.PATH) @PathVariable Long id) {
        return Result.success(memberCandidateService.deleteCandidate(id));
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除候选成员", description = "批量删除多个候选成员")
    @ApiResponse(responseCode = "200", description = "操作成功", content = @Content(schema = @Schema(implementation = Boolean.class)))
    public Result<Boolean> batchDeleteCandidates(@RequestBody List<Long> ids) {
        return Result.success(memberCandidateService.batchDeleteCandidates(ids));
    }
}