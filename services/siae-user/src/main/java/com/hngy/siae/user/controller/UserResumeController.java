package com.hngy.siae.user.controller;

import com.hngy.siae.core.result.Result;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import com.hngy.siae.user.dto.request.UserResumeCreateDTO;

import static com.hngy.siae.user.permissions.UserPermissions.*;
import com.hngy.siae.user.dto.request.UserResumeUpdateDTO;
import com.hngy.siae.user.dto.response.UserResumeVO;
import com.hngy.siae.user.service.UserResumeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户简历控制器
 * <p>
 * 提供用户简历管理相关的REST API接口，包括简历的增删改查操作。
 * 每个用户只能有一份简历，所有操作都基于当前登录用户。
 *
 * @author KEYKB
 */
@RestController
@RequestMapping("/resume")
@RequiredArgsConstructor
@Validated
@Tag(name = "用户简历管理", description = "用户简历相关API")
public class UserResumeController {

    private final UserResumeService userResumeService;

    /**
     * 创建简历
     * <p>
     * 为当前登录用户创建简历，如果用户已有简历则返回错误
     *
     * @param dto 简历创建参数
     * @return 创建成功的简历信息
     */
    @PostMapping
    @Operation(summary = "创建简历", description = "为当前用户创建简历，每个用户只能有一份简历")
    @SiaeAuthorize("hasAuthority('" + USER_RESUME_CREATE + "')")
    public Result<UserResumeVO> createResume(@Valid @RequestBody UserResumeCreateDTO dto) {
        UserResumeVO result = userResumeService.createResume(dto);
        return Result.success(result);
    }

    /**
     * 获取当前用户简历
     * <p>
     * 返回当前登录用户的简历信息
     *
     * @return 用户简历信息，不存在时返回null
     */
    @GetMapping
    @Operation(summary = "获取当前用户简历", description = "获取当前登录用户的简历信息")
    @SiaeAuthorize("hasAuthority('" + USER_RESUME_VIEW + "')")
    public Result<UserResumeVO> getMyResume() {
        UserResumeVO result = userResumeService.getMyResume();
        return Result.success(result);
    }

    /**
     * 更新简历
     * <p>
     * 更新当前登录用户的简历信息
     *
     * @param dto 简历更新参数
     * @return 更新后的简历信息
     */
    @PutMapping
    @Operation(summary = "更新简历", description = "更新当前用户的简历信息")
    @SiaeAuthorize("hasAuthority('" + USER_RESUME_UPDATE + "')")
    public Result<UserResumeVO> updateResume(@Valid @RequestBody UserResumeUpdateDTO dto) {
        UserResumeVO result = userResumeService.updateResume(dto);
        return Result.success(result);
    }

    /**
     * 删除简历
     * <p>
     * 逻辑删除当前登录用户的简历
     *
     * @return 删除结果
     */
    @DeleteMapping
    @Operation(summary = "删除简历", description = "删除当前用户的简历（逻辑删除）")
    @SiaeAuthorize("hasAuthority('" + USER_RESUME_DELETE + "')")
    public Result<Boolean> deleteResume() {
        boolean result = userResumeService.deleteResume();
        return Result.success(result);
    }
}
