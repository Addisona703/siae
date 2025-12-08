package com.hngy.siae.user.controller;

import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.security.permissions.RoleConstants;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import com.hngy.siae.user.dto.request.MembershipCreateDTO;
import com.hngy.siae.user.dto.request.MembershipPromoteDTO;
import com.hngy.siae.user.dto.request.MembershipQueryDTO;
import com.hngy.siae.user.dto.request.MembershipUpdateDTO;
import com.hngy.siae.user.dto.response.MembershipDetailVO;
import com.hngy.siae.user.dto.response.MembershipVO;
import com.hngy.siae.user.service.MembershipService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 成员统一管理控制器
 * 合并了原 MemberController 和 MemberCandidateController 的功能
 *
 * @author KEYKB
 */
@Slf4j
@RestController
@RequestMapping("/memberships")
@RequiredArgsConstructor
@Tag(name = "成员管理", description = "成员统一管理接口（候选/正式）")
@Validated
public class MembershipController {

    private final MembershipService membershipService;

    @PostMapping("/apply")
    @Operation(summary = "申请成为候选成员", description = "用户申请加入协会，创建候选成员记录")
    @SiaeAuthorize(RoleConstants.MEMBER_LEVEL)
    public Result<Long> applyForMembership(@Valid @RequestBody MembershipCreateDTO createDTO) {
        log.info("用户申请成为候选成员，用户ID: {}", createDTO.getUserId());
        Long membershipId = membershipService.createCandidate(createDTO);
        return Result.success(membershipId);
    }

    @PutMapping("/{id}/approve")
    @Operation(summary = "审核通过", description = "管理员审核通过，将待审核成员转为候选成员")
    @SiaeAuthorize(RoleConstants.ADMIN_LEVEL)
    public Result<Boolean> approveCandidate(
            @Parameter(description = "成员ID") @PathVariable Long id) {
        log.info("审核通过，成员ID: {}", id);
        boolean success = membershipService.approveCandidate(id);
        return Result.success(success);
    }

    @PutMapping("/{id}/reject")
    @Operation(summary = "审核拒绝", description = "管理员审核拒绝，将待审核成员标记为已拒绝")
    @SiaeAuthorize(RoleConstants.ADMIN_LEVEL)
    public Result<Boolean> rejectCandidate(
            @Parameter(description = "成员ID") @PathVariable Long id) {
        log.info("审核拒绝，成员ID: {}", id);
        boolean success = membershipService.rejectCandidate(id);
        return Result.success(success);
    }

    @PutMapping("/{id}/promote")
    @Operation(summary = "候选成员转正", description = "管理员将候选成员转为正式成员")
    @SiaeAuthorize(RoleConstants.ADMIN_LEVEL)
    public Result<Boolean> promoteToOfficial(
            @Parameter(description = "成员ID") @PathVariable Long id,
            @Valid @RequestBody MembershipPromoteDTO promoteDTO) {
        log.info("候选成员转正，成员ID: {}", id);
        promoteDTO.setId(id);
        boolean success = membershipService.promoteToOfficial(promoteDTO);
        return Result.success(success);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新成员信息", description = "更新成员的基本信息")
    @SiaeAuthorize(RoleConstants.ADMIN_LEVEL)
    public Result<Boolean> updateMembership(
            @Parameter(description = "成员ID") @PathVariable Long id,
            @Valid @RequestBody MembershipUpdateDTO updateDTO) {
        log.info("更新成员信息，成员ID: {}", id);
        updateDTO.setId(id);
        boolean success = membershipService.updateMembership(updateDTO);
        return Result.success(success);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除成员", description = "逻辑删除成员记录")
    @SiaeAuthorize(RoleConstants.ADMIN_LEVEL)
    public Result<Boolean> deleteMembership(
            @Parameter(description = "成员ID") @PathVariable Long id) {
        log.info("删除成员，成员ID: {}", id);
        boolean success = membershipService.deleteMembership(id);
        return Result.success(success);
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询成员信息", description = "根据成员ID查询成员详细信息（含用户、部门、职位）")
    @SiaeAuthorize(RoleConstants.MEMBER_LEVEL)
    public Result<MembershipDetailVO> getMembershipById(
            @Parameter(description = "成员ID") @PathVariable Long id) {
        MembershipDetailVO membership = membershipService.getMembershipById(id);
        return Result.success(membership);
    }

    @GetMapping("/by-user/{userId}")
    @Operation(summary = "根据用户ID查询成员", description = "根据用户ID查询成员详细信息（含用户、部门、职位）")
    @SiaeAuthorize(RoleConstants.MEMBER_LEVEL)
    public Result<MembershipDetailVO> getMembershipByUserId(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        MembershipDetailVO membership = membershipService.getMembershipByUserId(userId);
        return Result.success(membership);
    }

    @GetMapping("/{id}/detail")
    @Operation(summary = "查询成员详情", description = "查询成员详细信息（含用户、部门、职位）")
    @SiaeAuthorize(RoleConstants.MEMBER_LEVEL)
    public Result<MembershipDetailVO> getMembershipDetail(
            @Parameter(description = "成员ID") @PathVariable Long id) {
        MembershipDetailVO detail = membershipService.getMembershipDetailById(id);
        return Result.success(detail);
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询成员", description = "支持分页、筛选、关键字搜索")
    @SiaeAuthorize(RoleConstants.MEMBER_LEVEL)
    public Result<PageVO<MembershipVO>> pageMemberships(
            @Parameter(description = "分页查询参数") @Valid @RequestBody PageDTO<MembershipQueryDTO> pageDTO) {
        PageVO<MembershipVO> result = membershipService.pageMemberships(pageDTO);
        return Result.success(result);
    }

    @GetMapping("/check/{userId}")
    @Operation(summary = "检查用户成员身份", description = "判断用户是否为成员（候选或正式）")
    @SiaeAuthorize(RoleConstants.ADMIN_LEVEL)
    public Result<Boolean> isMember(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        boolean isMember = membershipService.isMember(userId);
        return Result.success(isMember);
    }

    @GetMapping("/check/{userId}/candidate")
    @Operation(summary = "检查是否为候选成员", description = "判断用户是否为候选成员")
    @SiaeAuthorize(RoleConstants.ADMIN_LEVEL)
    public Result<Boolean> isCandidate(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        boolean isCandidate = membershipService.isCandidate(userId);
        return Result.success(isCandidate);
    }

    @GetMapping("/check/{userId}/official")
    @Operation(summary = "检查是否为正式成员", description = "判断用户是否为正式成员")
    @SiaeAuthorize(RoleConstants.ADMIN_LEVEL)
    public Result<Boolean> isOfficialMember(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        boolean isOfficial = membershipService.isOfficialMember(userId);
        return Result.success(isOfficial);
    }
}
