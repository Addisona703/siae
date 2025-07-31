package com.hngy.siae.user.controller;

import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.user.dto.request.MemberCreateDTO;
import com.hngy.siae.user.dto.request.MemberQueryDTO;
import com.hngy.siae.user.dto.request.MemberUpdateDTO;
import com.hngy.siae.user.dto.response.MemberVO;
import com.hngy.siae.user.service.MemberService;
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
 * 正式成员控制器
 * <p>
 * 提供正式成员管理相关的REST API接口，包括成员的增删改查操作。
 * 所有接口都需要相应的权限才能访问。
 *
 * @author KEYKB
 */
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
@Validated
@Tag(name = "正式成员管理", description = "正式成员相关接口")
public class MemberController {

    private final MemberService memberService;

    /**
     * 创建正式成员
     *
     * @param memberCreateDTO 正式成员创建参数
     * @return 创建成功的正式成员信息
     */
    @PostMapping
    @Operation(summary = "创建正式成员", description = "创建新的正式成员")
    @SiaeAuthorize("hasAuthority('" + USER_MEMBER_CREATE + "')")
    public Result<MemberVO> createMember(
            @Parameter(description = "正式成员创建参数") @Valid @RequestBody MemberCreateDTO memberCreateDTO) {
        return Result.success(memberService.createMember(memberCreateDTO));
    }

    /**
     * 从候选成员添加为正式成员
     *
     * @param candidateId 候选成员ID
     * @param positionId 职位ID
     * @return 新增的正式成员信息
     */
    @PostMapping("/from-candidate")
    @Operation(summary = "从候选成员添加为正式成员", description = "将候选成员升级为正式成员并分配职位")
    @SiaeAuthorize("hasAuthority('" + USER_MEMBER_UPDATE + "')")
    public Result<MemberVO> addMemberFromCandidate(
            @Parameter(description = "候选成员ID") @RequestParam @NotNull Long candidateId,
            @Parameter(description = "职位ID") @RequestParam @NotNull Long positionId) {
        return Result.success(memberService.addMemberFromCandidate(candidateId, positionId));
    }

    /**
     * 更新正式成员信息
     *
     * @param memberUpdateDTO 正式成员更新参数
     * @return 更新后的正式成员信息
     */
    @PutMapping
    @Operation(summary = "更新正式成员信息", description = "更新正式成员的基本信息")
    @SiaeAuthorize("hasAuthority('" + USER_MEMBER_UPDATE + "')")
    public Result<MemberVO> updateMember(
            @Parameter(description = "正式成员更新参数") @Valid @RequestBody MemberUpdateDTO memberUpdateDTO) {
        return Result.success(memberService.updateMember(memberUpdateDTO));
    }

    /**
     * 根据ID获取正式成员信息
     *
     * @param id 正式成员ID
     * @return 正式成员详细信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询正式成员信息", description = "根据成员ID查询详细信息")
    @SiaeAuthorize("hasAuthority('" + USER_MEMBER_VIEW + "')")
    public Result<MemberVO> getMemberById(
            @Parameter(description = "正式成员ID") @PathVariable("id") @NotNull Long id) {
        return Result.success(memberService.getMemberById(id));
    }

    /**
     * 根据用户ID获取正式成员信息
     *
     * @param userId 用户ID
     * @return 正式成员详细信息
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "根据用户ID查询正式成员信息", description = "根据关联的用户ID查询成员详细信息")
    @SiaeAuthorize("hasAuthority('" + USER_MEMBER_VIEW + "')")
    public Result<MemberVO> getMemberByUserId(
            @Parameter(description = "用户ID") @PathVariable("userId") @NotNull Long userId) {
        return Result.success(memberService.getMemberByUserId(userId));
    }

    /**
     * 动态条件查询正式成员列表
     *
     * @param queryDTO 查询条件
     * @return 符合条件的正式成员列表
     */
    @PostMapping("/list")
    @Operation(summary = "动态条件查询正式成员列表", description = "根据提供的条件查询符合要求的成员列表")
    @SiaeAuthorize("hasAuthority('" + USER_MEMBER_LIST + "')")
    public Result<List<MemberVO>> listMembers(
            @Parameter(description = "查询条件") @Valid @RequestBody MemberQueryDTO queryDTO) {
        return Result.success(memberService.listMembers(queryDTO));
    }

    /**
     * 分页查询正式成员列表
     *
     * @param pageDTO 分页查询参数
     * @return 分页正式成员列表
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询正式成员列表", description = "分页查询成员信息")
    @SiaeAuthorize("hasAuthority('" + USER_MEMBER_LIST + "')")
    public Result<PageVO<MemberVO>> listMembersByPage(
            @Parameter(description = "分页查询参数") @Valid @RequestBody PageDTO<MemberQueryDTO> pageDTO) {
        return Result.success(memberService.listMembersByPage(pageDTO));
    }

    /**
     * 根据ID删除正式成员
     *
     * @param id 正式成员ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除正式成员", description = "根据ID删除正式成员")
    @SiaeAuthorize("hasAuthority('" + USER_MEMBER_DELETE + "')")
    public Result<Boolean> deleteMember(
            @Parameter(description = "正式成员ID") @PathVariable("id") @NotNull Long id) {
        return Result.success(memberService.deleteMember(id));
    }
}