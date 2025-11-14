package com.hngy.siae.user.controller;

import com.hngy.siae.core.permissions.RoleConstants;
import com.hngy.siae.core.result.Result;
import jakarta.validation.Valid;
import com.hngy.siae.user.dto.request.AwardLevelCreateDTO;
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

    @PostMapping
    @Operation(summary = "创建奖项等级", description = "创建新的奖项等级")
    @SiaeAuthorize("hasAuthority('" + USER_AWARD_LEVEL_CREATE + "')")
    public Result<AwardLevelVO> createAwardLevel(
            @Parameter(description = "奖项等级创建参数") @Valid @RequestBody AwardLevelCreateDTO awardLevelCreateDTO) {
        return Result.success(awardLevelService.createAwardLevel(awardLevelCreateDTO));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新奖项等级", description = "更新奖项等级信息")
    @SiaeAuthorize("hasAuthority('" + USER_AWARD_LEVEL_UPDATE + "')")
    public Result<AwardLevelVO> updateAwardLevel(
            @Parameter(description = "奖项等级ID") @PathVariable("id") @NotNull Long id,
            @Parameter(description = "奖项等级名称") @RequestParam @NotBlank String name,
            @Parameter(description = "排序ID") @RequestParam(required = false) Integer orderId) {
        return Result.success(awardLevelService.updateAwardLevel(id, name, orderId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取奖项等级", description = "根据ID查询奖项等级详细信息")
    @SiaeAuthorize(RoleConstants.ANY_AUTHENTICATED)
    public Result<AwardLevelVO> getAwardLevelById(
            @Parameter(description = "奖项等级ID") @PathVariable("id") @NotNull Long id) {
        return Result.success(awardLevelService.getAwardLevelById(id));
    }

    @GetMapping
    @Operation(summary = "查询奖项等级列表", description = "查询所有奖项等级（字典数据，按orderId排序）")
    @SiaeAuthorize(RoleConstants.ANY_AUTHENTICATED)
    public Result<List<AwardLevelVO>> listAllAwardLevels() {
        return Result.success(awardLevelService.listAllAwardLevels());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "根据ID删除奖项等级", description = "删除指定ID的奖项等级")
    @SiaeAuthorize("hasAuthority('" + USER_AWARD_LEVEL_DELETE + "')")
    public Result<Boolean> deleteAwardLevel(
            @Parameter(description = "奖项等级ID") @PathVariable("id") @NotNull Long id) {
        return Result.success(awardLevelService.deleteAwardLevel(id));
    }
}