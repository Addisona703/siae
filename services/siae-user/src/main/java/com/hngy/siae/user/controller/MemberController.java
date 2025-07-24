package com.hngy.siae.user.controller;

import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.core.validation.UpdateGroup;
import com.hngy.siae.user.dto.request.MemberDTO;
import com.hngy.siae.user.dto.response.MemberVO;
import com.hngy.siae.user.service.MemberService;
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
 * 正式成员控制器
 *
 * @author KEYKB
 */
@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "正式成员管理", description = "正式成员相关接口")
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/from-candidate")
    @Operation(summary = "从候选成员添加为正式成员", description = "将候选成员升级为正式成员并分配职位")
    @ApiResponse(responseCode = "200", description = "操作成功", content = @Content(schema = @Schema(implementation = MemberVO.class)))
    public Result<MemberVO> addMemberFromCandidate(
        @Parameter(description = "候选成员ID", required = true, example = "1", in = ParameterIn.QUERY) @RequestParam Long candidateId,
        @Parameter(description = "职位ID", required = true, example = "2", in = ParameterIn.QUERY) @RequestParam Long positionId) {
        return Result.success(memberService.addMemberFromCandidate(candidateId, positionId));
    }

    @PutMapping
    @Operation(summary = "更新正式成员信息", description = "更新正式成员的基本信息")
    @ApiResponse(responseCode = "200", description = "操作成功", content = @Content(schema = @Schema(implementation = MemberVO.class)))
    public Result<MemberVO> updateMember(@RequestBody @Validated(UpdateGroup.class) MemberDTO memberDTO) {
        return Result.success(memberService.updateMember(memberDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询正式成员信息", description = "根据成员ID查询详细信息")
    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = MemberVO.class)))
    public Result<MemberVO> getMemberById(@Parameter(description = "正式成员ID", required = true, example = "1", in = ParameterIn.PATH) @PathVariable("id") Long id) {
        return Result.success(memberService.getMemberById(id));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "根据用户ID查询正式成员信息", description = "根据关联的用户ID查询成员详细信息")
    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = MemberVO.class)))
    public Result<MemberVO> getMemberByUserId(@Parameter(description = "用户ID", required = true, example = "101", in = ParameterIn.PATH) @PathVariable("userId") Long userId) {
        return Result.success(memberService.getMemberByUserId(userId));
    }

    @PostMapping("/list")
    @Operation(summary = "动态条件查询正式成员列表", description = "根据提供的条件查询符合要求的成员列表")
    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = List.class)))
    public Result<List<MemberVO>> listMembers(@RequestBody MemberDTO queryDTO) {
        return Result.success(memberService.listMembers(queryDTO));
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询正式成员列表", description = "分页查询成员信息")
    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = PageVO.class)))
    public Result<PageVO<MemberVO>> pageMember(@RequestBody PageDTO<MemberDTO> pageDTO) {
        return Result.success(memberService.pageMember(pageDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除正式成员", description = "根据ID删除正式成员")
    @ApiResponse(responseCode = "200", description = "删除成功", content = @Content(schema = @Schema(implementation = Boolean.class)))
    public Result<Boolean> deleteMember(@Parameter(description = "正式成员ID", required = true, example = "1", in = ParameterIn.PATH) @PathVariable("id") Long id) {
        return Result.success(memberService.deleteMember(id));
    }
}