package com.hngy.siae.user.controller;

import com.hngy.siae.common.dto.request.PageDTO;
import com.hngy.siae.common.dto.response.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.common.validation.CreateGroup;
import com.hngy.siae.common.validation.UpdateGroup;
import com.hngy.siae.user.dto.request.AwardTypeDTO;
import com.hngy.siae.user.dto.response.AwardTypeVO;
import com.hngy.siae.user.service.AwardTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.hngy.siae.core.permissions.UserPermissions.*;

import java.util.List;

/**
 * 奖项类型字典控制器
 *
 * @author KEYKB
 */
@RestController
@RequestMapping("/award-types")
@RequiredArgsConstructor
@Tag(name = "奖项类型字典管理", description = "奖项类型相关接口")
public class AwardTypeController {

    private final AwardTypeService awardTypeService;

    /**
     * 创建奖项类型
     *
     * @param awardTypeDTO 奖项类型创建请求DTO
     * @return 创建的奖项类型信息
     */
    @Operation(summary = "创建奖项类型", description = "创建一个新的奖项类型")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "创建成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = AwardTypeVO.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误或奖项类型已存在",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "未授权访问",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "权限不足，需要奖项类型创建权限",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "服务器内部错误",
            content = @Content(mediaType = "application/json"))
    })
    @PostMapping
    @PreAuthorize("hasAuthority('" + USER_AWARD_TYPE_CREATE + "')")
    public Result<AwardTypeVO> createAwardType(
            @Parameter(description = "奖项类型创建请求数据，包含类型名称、描述等信息", required = true)
            @RequestBody @Validated(CreateGroup.class) AwardTypeDTO awardTypeDTO) {
        return Result.success(awardTypeService.createAwardType(awardTypeDTO));
    }

    /**
     * 更新奖项类型
     *
     * @param awardTypeDTO 奖项类型更新请求DTO
     * @return 更新后的奖项类型信息
     */
    @Operation(summary = "更新奖项类型", description = "更新一个已存在的奖项类型")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "更新成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = AwardTypeVO.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误或奖项类型ID无效",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "未授权访问",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "权限不足，需要奖项类型更新权限",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "奖项类型不存在",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "服务器内部错误",
            content = @Content(mediaType = "application/json"))
    })
    @PutMapping
    @PreAuthorize("hasAuthority('" + USER_AWARD_TYPE_UPDATE + "')")
    public Result<AwardTypeVO> updateAwardType(
            @Parameter(description = "奖项类型更新请求数据，必须包含类型ID和要修改的字段", required = true)
            @RequestBody @Validated(UpdateGroup.class) AwardTypeDTO awardTypeDTO) {
        return Result.success(awardTypeService.updateAwardType(awardTypeDTO));
    }

    /**
     * 根据ID获取奖项类型
     *
     * @param id 奖项类型ID
     * @return 奖项类型详细信息
     */
    @Operation(summary = "根据ID获取奖项类型", description = "获取指定ID的奖项类型详细信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = AwardTypeVO.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误，奖项类型ID无效",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "未授权访问",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "权限不足，需要奖项类型查询权限",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "奖项类型不存在",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "服务器内部错误",
            content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + USER_AWARD_TYPE_VIEW + "')")
    public Result<AwardTypeVO> getAwardTypeById(
            @Parameter(name = "id", description = "奖项类型ID", required = true, example = "1", in = ParameterIn.PATH)
            @PathVariable Long id) {
        return Result.success(awardTypeService.getAwardTypeById(id));
    }

    /**
     * 根据名称获取奖项类型
     *
     * @param name 奖项类型名称
     * @return 奖项类型详细信息
     */
    @Operation(summary = "根据名称获取奖项类型", description = "通过奖项类型名称查询详细信息")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = AwardTypeVO.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "未授权访问",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "权限不足，需要奖项类型查询权限",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "奖项类型不存在",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "服务器内部错误",
            content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/name/{name}")
    @PreAuthorize("hasAuthority('" + USER_AWARD_TYPE_VIEW + "')")
    public Result<AwardTypeVO> getAwardTypeByName(
            @Parameter(name = "name", description = "奖项类型名称", required = true, example = "学科竞赛", in = ParameterIn.PATH)
            @PathVariable String name) {
        return Result.success(awardTypeService.getAwardTypeByName(name));
    }

    /**
     * 获取所有奖项类型
     *
     * @return 所有奖项类型列表
     */
    @Operation(summary = "获取所有奖项类型", description = "获取系统中所有奖项类型列表")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = AwardTypeVO.class)))),
        @ApiResponse(responseCode = "401", description = "未授权访问",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "权限不足，需要奖项类型列表查询权限",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "服务器内部错误",
            content = @Content(mediaType = "application/json"))
    })
    @GetMapping
    @PreAuthorize("hasAuthority('" + USER_AWARD_TYPE_LIST + "')")
    public Result<List<AwardTypeVO>> listAllAwardTypes() {
        return Result.success(awardTypeService.listAllAwardTypes());
    }

    /**
     * 分页查询奖项类型
     *
     * @param pageDTO 分页查询请求DTO
     * @return 分页奖项类型列表
     */
    @Operation(summary = "分页查询奖项类型", description = "根据条件分页获取奖项类型列表")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = PageVO.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "未授权访问",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "权限不足，需要奖项类型列表查询权限",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "服务器内部错误",
            content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/page")
    @PreAuthorize("hasAuthority('" + USER_AWARD_TYPE_LIST + "')")
    public Result<PageVO<AwardTypeVO>> listAwardTypesByPage(
            @Parameter(description = "分页查询请求数据，包含分页参数和查询条件", required = true)
            @RequestBody PageDTO<AwardTypeDTO> pageDTO) {
        return Result.success(awardTypeService.listAwardTypesByPage(pageDTO));
    }

    /**
     * 根据ID删除奖项类型
     *
     * @param id 奖项类型ID
     * @return 删除结果
     */
    @Operation(summary = "根据ID删除奖项类型", description = "删除指定ID的奖项类型")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "删除成功",
            content = @Content(mediaType = "application/json",
                schema = @Schema(implementation = Boolean.class))),
        @ApiResponse(responseCode = "400", description = "请求参数错误，奖项类型ID无效",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "401", description = "未授权访问",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "403", description = "权限不足，需要奖项类型删除权限",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "奖项类型不存在",
            content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "500", description = "服务器内部错误",
            content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + USER_AWARD_TYPE_DELETE + "')")
    public Result<Boolean> deleteAwardType(
            @Parameter(name = "id", description = "奖项类型ID", required = true, example = "1", in = ParameterIn.PATH)
            @PathVariable Long id) {
        return Result.success(awardTypeService.deleteAwardType(id));
    }
}