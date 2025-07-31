package com.hngy.siae.user.controller;

import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import jakarta.validation.Valid;
import com.hngy.siae.user.dto.request.AwardLevelCreateDTO;
import com.hngy.siae.user.dto.request.AwardLevelQueryDTO;
import com.hngy.siae.user.dto.request.AwardLevelUpdateDTO;
import com.hngy.siae.user.dto.response.AwardLevelVO;
import com.hngy.siae.user.service.AwardLevelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import static com.hngy.siae.core.permissions.UserPermissions.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 奖项等级字典控制器
 * <p>
 * 提供奖项等级管理相关的REST API接口，包括奖项等级的增删改查操作。
 * 所有接口都需要相应的权限才能访问。
 *
 * @author KEYKB
 */
@RestController
@RequestMapping("/award-levels")
@RequiredArgsConstructor
@Tag(name = "奖项等级字典管理", description = "用于管理奖项等级信息的接口")
@Validated
public class AwardLevelController {

    private final AwardLevelService awardLevelService;

    /**
     * 创建奖项等级
     *
     * @param awardLevelCreateDTO 奖项等级创建参数
     * @return 创建成功的奖项等级信息
     */
    @PostMapping
    @Operation(summary = "创建奖项等级", description = "创建新的奖项等级")
    @SiaeAuthorize("hasAuthority('" + USER_AWARD_LEVEL_CREATE + "')")
    public Result<AwardLevelVO> createAwardLevel(
            @Parameter(description = "奖项等级创建参数") @Valid @RequestBody AwardLevelCreateDTO awardLevelCreateDTO) {
        return Result.success(awardLevelService.createAwardLevel(awardLevelCreateDTO));
    }

    /**
     * 更新奖项等级信息
     *
     * @param awardLevelUpdateDTO 奖项等级更新参数
     * @return 更新后的奖项等级信息
     */
    @PutMapping
    @Operation(summary = "更新奖项等级", description = "更新奖项等级信息")
    @SiaeAuthorize("hasAuthority('" + USER_AWARD_LEVEL_UPDATE + "')")
    public Result<AwardLevelVO> updateAwardLevel(
            @Parameter(description = "奖项等级更新参数") @Valid @RequestBody AwardLevelUpdateDTO awardLevelUpdateDTO) {
        return Result.success(awardLevelService.updateAwardLevel(awardLevelUpdateDTO));
    }

    /**
     * 根据ID获取奖项等级信息
     *
     * @param id 奖项等级ID
     * @return 奖项等级详细信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取奖项等级", description = "根据ID查询奖项等级详细信息")
    @SiaeAuthorize("hasAuthority('" + USER_AWARD_LEVEL_VIEW + "')")
    public Result<AwardLevelVO> getAwardLevelById(
            @Parameter(description = "奖项等级ID") @PathVariable("id") @NotNull Long id) {
        return Result.success(awardLevelService.getAwardLevelById(id));
    }

    /**
     * 根据名称获取奖项等级信息
     *
     * @param name 奖项等级名称
     * @return 奖项等级详细信息
     */
    @GetMapping("/name/{name}")
    @Operation(summary = "根据名称获取奖项等级", description = "根据名称查询奖项等级详细信息")
    @SiaeAuthorize("hasAuthority('" + USER_AWARD_LEVEL_VIEW + "')")
    public Result<AwardLevelVO> getAwardLevelByName(
            @Parameter(description = "奖项等级名称") @PathVariable("name") @NotBlank String name) {
        return Result.success(awardLevelService.getAwardLevelByName(name));
    }

    /**
     * 获取所有奖项等级列表
     *
     * @return 所有奖项等级列表
     */
    @GetMapping
    @Operation(summary = "获取所有奖项等级", description = "获取系统中所有奖项等级列表")
    @SiaeAuthorize("hasAuthority('" + USER_AWARD_LEVEL_LIST + "')")
    public Result<List<AwardLevelVO>> listAllAwardLevels() {
        return Result.success(awardLevelService.listAllAwardLevels());
    }

    /**
     * 分页查询奖项等级列表
     *
     * @param pageDTO 分页查询参数
     * @return 分页奖项等级列表
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询奖项等级", description = "根据条件分页查询奖项等级列表")
    @SiaeAuthorize("hasAuthority('" + USER_AWARD_LEVEL_LIST + "')")
    public Result<PageVO<AwardLevelVO>> listAwardLevelsByPage(
            @Parameter(description = "分页查询参数") @Valid @RequestBody PageDTO<AwardLevelQueryDTO> pageDTO) {
        return Result.success(awardLevelService.listAwardLevelsByPage(pageDTO));
    }

    /**
     * 根据ID删除奖项等级
     *
     * @param id 奖项等级ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "根据ID删除奖项等级", description = "删除指定ID的奖项等级")
    @SiaeAuthorize("hasAuthority('" + USER_AWARD_LEVEL_DELETE + "')")
    public Result<Boolean> deleteAwardLevel(
            @Parameter(description = "奖项等级ID") @PathVariable("id") @NotNull Long id) {
        return Result.success(awardLevelService.deleteAwardLevel(id));
    }
}