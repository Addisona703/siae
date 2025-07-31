package com.hngy.siae.user.controller;

import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.user.dto.request.MemberCandidateCreateDTO;
import com.hngy.siae.user.dto.request.MemberCandidateQueryDTO;
import com.hngy.siae.user.dto.request.MemberCandidateUpdateDTO;
import com.hngy.siae.user.dto.response.MemberCandidateVO;
import com.hngy.siae.user.service.MemberCandidateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import static com.hngy.siae.core.permissions.UserPermissions.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 候选成员控制器
 * <p>
 * 提供候选成员管理相关的REST API接口，包括候选成员的增删改查操作。
 * 所有接口都需要相应的权限才能访问。
 *
 * @author KEYKB
 */
@RestController
@RequestMapping("/candidates")
@RequiredArgsConstructor
@Validated
@Tag(name = "候选成员管理", description = "候选成员相关接口")
public class MemberCandidateController {

    private final MemberCandidateService memberCandidateService;

    /**
     * 创建候选成员
     *
     * @param memberCandidateCreateDTO 候选成员创建参数
     * @return 创建成功的候选成员信息
     */
    @PostMapping
    @Operation(summary = "创建候选成员", description = "创建新的候选成员")
    @SiaeAuthorize("hasAuthority('" + USER_CANDIDATE_CREATE + "')")
    public Result<MemberCandidateVO> createMemberCandidate(
            @Parameter(description = "候选成员创建参数") @Valid @RequestBody MemberCandidateCreateDTO memberCandidateCreateDTO) {
        return Result.success(memberCandidateService.createMemberCandidate(memberCandidateCreateDTO));
    }

    /**
     * 添加候选成员
     *
     * @param candidateDTO 候选成员创建参数
     * @return 新增的候选成员信息
     */
    @PostMapping("/add")
    @Operation(summary = "添加候选成员", description = "添加新的候选成员信息")
    @SiaeAuthorize("hasAuthority('" + USER_CANDIDATE_CREATE + "')")
    public Result<MemberCandidateVO> addCandidate(
            @Parameter(description = "候选成员创建参数") @Valid @RequestBody MemberCandidateCreateDTO candidateDTO) {
        return Result.success(memberCandidateService.addCandidate(candidateDTO));
    }

    /**
     * 更新候选成员信息
     *
     * @param candidateDTO 候选成员更新参数
     * @return 更新后的候选成员信息
     */
    @PutMapping
    @Operation(summary = "更新候选成员信息", description = "更新候选成员的基本信息")
    @SiaeAuthorize("hasAuthority('" + USER_CANDIDATE_UPDATE + "')")
    public Result<MemberCandidateVO> updateCandidate(
            @Parameter(description = "候选成员更新参数") @Valid @RequestBody MemberCandidateUpdateDTO candidateDTO) {
        return Result.success(memberCandidateService.updateCandidate(candidateDTO));
    }

    /**
     * 根据ID获取候选成员信息
     *
     * @param id 候选成员ID
     * @return 候选成员详细信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询候选成员信息", description = "根据候选成员ID查询详细信息")
    @SiaeAuthorize("hasAuthority('" + USER_CANDIDATE_VIEW + "')")
    public Result<MemberCandidateVO> getCandidateById(
            @Parameter(description = "候选成员ID") @PathVariable("id") @NotNull Long id) {
        return Result.success(memberCandidateService.getCandidateById(id));
    }

    /**
     * 根据用户ID获取候选成员信息
     *
     * @param userId 用户ID
     * @return 候选成员详细信息
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "根据用户ID查询候选成员信息", description = "根据关联的用户ID查询候选成员详细信息")
    @SiaeAuthorize("hasAuthority('" + USER_CANDIDATE_VIEW + "')")
    public Result<MemberCandidateVO> getCandidateByUserId(
            @Parameter(description = "用户ID") @PathVariable("userId") @NotNull Long userId) {
        return Result.success(memberCandidateService.getCandidateByUserId(userId));
    }

    /**
     * 根据学号获取候选成员信息
     *
     * @param studentId 学号
     * @return 候选成员详细信息
     */
    @GetMapping("/student/{studentId}")
    @Operation(summary = "根据学号查询候选成员信息", description = "根据学号查询候选成员详细信息")
    @SiaeAuthorize("hasAuthority('" + USER_CANDIDATE_VIEW + "')")
    public Result<MemberCandidateVO> getCandidateByStudentId(
            @Parameter(description = "学号") @PathVariable("studentId") @NotBlank String studentId) {
        return Result.success(memberCandidateService.getCandidateByStudentId(studentId));
    }

    /**
     * 根据部门ID获取候选成员列表
     *
     * @param departmentId 部门ID
     * @return 候选成员列表
     */
    @GetMapping("/department/{departmentId}")
    @Operation(summary = "根据部门ID查询候选成员列表", description = "查询指定部门下的所有候选成员")
    @SiaeAuthorize("hasAuthority('" + USER_CANDIDATE_LIST + "')")
    public Result<List<MemberCandidateVO>> listCandidatesByDepartment(
            @Parameter(description = "部门ID") @PathVariable("departmentId") @NotNull Long departmentId) {
        return Result.success(memberCandidateService.listCandidatesByDepartment(departmentId));
    }

    /**
     * 动态条件查询候选成员列表
     *
     * @param queryDTO 查询条件
     * @return 符合条件的候选成员列表
     */
    @PostMapping("/list")
    @Operation(summary = "动态条件查询候选成员列表", description = "根据提供的条件查询符合要求的候选成员列表")
    @SiaeAuthorize("hasAuthority('" + USER_CANDIDATE_LIST + "')")
    public Result<List<MemberCandidateVO>> listCandidates(
            @Parameter(description = "查询条件") @Valid @RequestBody MemberCandidateQueryDTO queryDTO) {
        return Result.success(memberCandidateService.listCandidates(queryDTO));
    }

    /**
     * 分页查询候选成员列表
     *
     * @param pageDTO 分页查询参数
     * @return 分页候选成员列表
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询候选成员列表", description = "分页查询候选成员信息")
    @SiaeAuthorize("hasAuthority('" + USER_CANDIDATE_LIST + "')")
    public Result<PageVO<MemberCandidateVO>> listCandidatesByPage(
            @Parameter(description = "分页查询参数") @Valid @RequestBody PageDTO<MemberCandidateQueryDTO> pageDTO) {
        return Result.success(memberCandidateService.listCandidatesByPage(pageDTO));
    }

    /**
     * 根据ID删除候选成员
     *
     * @param id 候选成员ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除候选成员", description = "根据ID删除候选成员")
    @SiaeAuthorize("hasAuthority('" + USER_CANDIDATE_DELETE + "')")
    public Result<Boolean> deleteCandidate(
            @Parameter(description = "候选成员ID") @PathVariable("id") @NotNull Long id) {
        return Result.success(memberCandidateService.deleteCandidate(id));
    }

    /**
     * 批量删除候选成员
     *
     * @param ids 候选成员ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除候选成员", description = "批量删除多个候选成员")
    @SiaeAuthorize("hasAuthority('" + USER_CANDIDATE_DELETE + "')")
    public Result<Boolean> batchDeleteCandidates(
            @Parameter(description = "候选成员ID列表") @Valid @RequestBody List<Long> ids) {
        return Result.success(memberCandidateService.batchDeleteCandidates(ids));
    }
}