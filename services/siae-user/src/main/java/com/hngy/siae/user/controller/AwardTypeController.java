package com.hngy.siae.user.controller;

import com.hngy.siae.security.permissions.RoleConstants;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.user.dto.request.AwardTypeCreateDTO;
import com.hngy.siae.user.dto.response.AwardTypeVO;
import com.hngy.siae.user.service.AwardTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import static com.hngy.siae.user.permissions.UserPermissions.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 奖项类型字典控制器
 * <p>
 * 提供奖项类型管理相关的REST API接口，包括奖项类型的增删改查操作。
 * 所有接口都需要相应的权限才能访问。
 *
 * @author KEYKB
 */
@RestController
@RequestMapping("/award-types")
@RequiredArgsConstructor
@Validated
@Tag(name = "奖项类型字典管理", description = "奖项类型相关接口")
public class AwardTypeController {

    private final AwardTypeService awardTypeService;

    /**
     * 创建奖项类型
     *
     * @param awardTypeCreateDTO 奖项类型创建参数
     * @return 创建成功的奖项类型信息
     */
    @PostMapping
    @Operation(summary = "创建奖项类型", description = "创建一个新的奖项类型")
    @SiaeAuthorize("hasAuthority('" + USER_AWARD_TYPE_CREATE + "')")
    public Result<AwardTypeVO> createAwardType(
            @Parameter(description = "奖项类型创建参数") @Valid @RequestBody AwardTypeCreateDTO awardTypeCreateDTO) {
        return Result.success(awardTypeService.createAwardType(awardTypeCreateDTO));
    }

    /**
     * 更新奖项类型信息
     *
     * @param id 奖项类型ID
     * @param name 奖项类型名称
     * @param orderId 排序ID
     * @return 更新后的奖项类型信息
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新奖项类型", description = "更新一个已存在的奖项类型")
    @SiaeAuthorize("hasAuthority('" + USER_AWARD_TYPE_UPDATE + "')")
    public Result<AwardTypeVO> updateAwardType(
            @Parameter(description = "奖项类型ID") @PathVariable("id") @NotNull Long id,
            @Parameter(description = "奖项类型名称") @RequestParam @NotBlank String name,
            @Parameter(description = "排序ID") @RequestParam(required = false) Integer orderId) {
        return Result.success(awardTypeService.updateAwardType(id, name, orderId));
    }

    /**
     * 根据ID获取奖项类型信息
     *
     * @param id 奖项类型ID
     * @return 奖项类型详细信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取奖项类型", description = "获取指定ID的奖项类型详细信息")
    @SiaeAuthorize(RoleConstants.ANY_AUTHENTICATED)
    public Result<AwardTypeVO> getAwardTypeById(
            @Parameter(description = "奖项类型ID") @PathVariable("id") @NotNull Long id) {
        return Result.success(awardTypeService.getAwardTypeById(id));
    }

    /**
     * 查询奖项类型列表（字典数据）
     *
     * @return 所有奖项类型列表，按orderId排序
     */
    @GetMapping
    @Operation(summary = "查询奖项类型列表", description = "查询所有奖项类型（字典数据，按orderId排序）")
    @SiaeAuthorize(RoleConstants.ANY_AUTHENTICATED)
    public Result<List<AwardTypeVO>> listAllAwardTypes() {
        return Result.success(awardTypeService.listAllAwardTypes());
    }

    /**
     * 根据ID删除奖项类型
     *
     * @param id 奖项类型ID
     * @return 删除结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "根据ID删除奖项类型", description = "删除指定ID的奖项类型")
    @SiaeAuthorize("hasAuthority('" + USER_AWARD_TYPE_DELETE + "')")
    public Result<Boolean> deleteAwardType(
            @Parameter(description = "奖项类型ID") @PathVariable("id") @NotNull Long id) {
        return Result.success(awardTypeService.deleteAwardType(id));
    }
}