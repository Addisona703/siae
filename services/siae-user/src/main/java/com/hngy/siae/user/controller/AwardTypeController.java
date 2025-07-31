package com.hngy.siae.user.controller;

import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.user.dto.request.AwardTypeCreateDTO;
import com.hngy.siae.user.dto.request.AwardTypeQueryDTO;
import com.hngy.siae.user.dto.request.AwardTypeUpdateDTO;
import com.hngy.siae.user.dto.response.AwardTypeVO;
import com.hngy.siae.user.service.AwardTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import static com.hngy.siae.core.permissions.UserPermissions.*;
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
     * @param awardTypeUpdateDTO 奖项类型更新参数
     * @return 更新后的奖项类型信息
     */
    @PutMapping
    @Operation(summary = "更新奖项类型", description = "更新一个已存在的奖项类型")
    @SiaeAuthorize("hasAuthority('" + USER_AWARD_TYPE_UPDATE + "')")
    public Result<AwardTypeVO> updateAwardType(
            @Parameter(description = "奖项类型更新参数") @Valid @RequestBody AwardTypeUpdateDTO awardTypeUpdateDTO) {
        return Result.success(awardTypeService.updateAwardType(awardTypeUpdateDTO));
    }

    /**
     * 根据ID获取奖项类型信息
     *
     * @param id 奖项类型ID
     * @return 奖项类型详细信息
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取奖项类型", description = "获取指定ID的奖项类型详细信息")
    @SiaeAuthorize("hasAuthority('" + USER_AWARD_TYPE_VIEW + "')")
    public Result<AwardTypeVO> getAwardTypeById(
            @Parameter(description = "奖项类型ID") @PathVariable("id") @NotNull Long id) {
        return Result.success(awardTypeService.getAwardTypeById(id));
    }

    /**
     * 根据名称获取奖项类型信息
     *
     * @param name 奖项类型名称
     * @return 奖项类型详细信息
     */
    @GetMapping("/name/{name}")
    @Operation(summary = "根据名称获取奖项类型", description = "通过奖项类型名称查询详细信息")
    @SiaeAuthorize("hasAuthority('" + USER_AWARD_TYPE_VIEW + "')")
    public Result<AwardTypeVO> getAwardTypeByName(
            @Parameter(description = "奖项类型名称") @PathVariable("name") @NotBlank String name) {
        return Result.success(awardTypeService.getAwardTypeByName(name));
    }

    /**
     * 获取所有奖项类型列表
     *
     * @return 所有奖项类型列表
     */
    @GetMapping
    @Operation(summary = "获取所有奖项类型", description = "获取系统中所有奖项类型列表")
    @SiaeAuthorize("hasAuthority('" + USER_AWARD_TYPE_LIST + "')")
    public Result<List<AwardTypeVO>> listAllAwardTypes() {
        return Result.success(awardTypeService.listAllAwardTypes());
    }

    /**
     * 分页查询奖项类型列表
     *
     * @param pageDTO 分页查询参数
     * @return 分页奖项类型列表
     */
    @PostMapping("/page")
    @Operation(summary = "分页查询奖项类型", description = "根据条件分页获取奖项类型列表")
    @SiaeAuthorize("hasAuthority('" + USER_AWARD_TYPE_LIST + "')")
    public Result<PageVO<AwardTypeVO>> listAwardTypesByPage(
            @Parameter(description = "分页查询参数") @Valid @RequestBody PageDTO<AwardTypeQueryDTO> pageDTO) {
        return Result.success(awardTypeService.listAwardTypesByPage(pageDTO));
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