package com.hngy.siae.user.controller;

import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.core.validation.CreateGroup;
import com.hngy.siae.core.validation.UpdateGroup;
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
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 奖项类型字典控制器
 *
 * @author KEYKB
 */
@RestController
@RequestMapping("/api/award-types")
@RequiredArgsConstructor
@Tag(name = "奖项类型字典管理", description = "奖项类型相关接口")
public class AwardTypeController {

    private final AwardTypeService awardTypeService;

    @PostMapping
    @Operation(summary = "创建奖项类型", description = "创建一个新的奖项类型")
    @ApiResponse(responseCode = "200", description = "创建成功",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AwardTypeVO.class))
    )
    public Result<AwardTypeVO> createAwardType(@RequestBody @Validated(CreateGroup.class) AwardTypeDTO awardTypeDTO) {
        return Result.success(awardTypeService.createAwardType(awardTypeDTO));
    }

    @PutMapping
    @Operation(summary = "更新奖项类型", description = "更新一个已存在的奖项类型")
    @ApiResponse(responseCode = "200", description = "更新成功",
        content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = AwardTypeVO.class))
    )
    public Result<AwardTypeVO> updateAwardType(@RequestBody @Validated(UpdateGroup.class) AwardTypeDTO awardTypeDTO) {
        return Result.success(awardTypeService.updateAwardType(awardTypeDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取奖项类型", description = "获取指定ID的奖项类型详细信息")
    @Parameter(name = "id", description = "奖项类型ID", required = true, example = "1", in = ParameterIn.PATH)
    @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = AwardTypeVO.class))
    )
    public Result<AwardTypeVO> getAwardTypeById(@PathVariable("id") Long id) {
        return Result.success(awardTypeService.getAwardTypeById(id));
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "根据名称获取奖项类型", description = "通过奖项类型名称查询详细信息")
    @Parameter(name = "name", description = "奖项类型名称", required = true, example = "学科竞赛", in = ParameterIn.PATH)
    @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = AwardTypeVO.class))
    )
    public Result<AwardTypeVO> getAwardTypeByName(@PathVariable("name") String name) {
        return Result.success(awardTypeService.getAwardTypeByName(name));
    }

    @GetMapping
    @Operation(summary = "获取所有奖项类型", description = "获取系统中所有奖项类型列表")
    @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = AwardTypeVO.class)))
    )
    public Result<List<AwardTypeVO>> listAllAwardTypes() {
        return Result.success(awardTypeService.listAllAwardTypes());
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询奖项类型", description = "根据条件分页获取奖项类型列表")
    @ApiResponse(responseCode = "200", description = "查询成功",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = PageVO.class))
            // TODO: OpenAPI 3 不支持精确定义 PageVO<AwardTypeVO> 这样的泛型响应，此为通用分页响应结构
    )
    public Result<PageVO<AwardTypeVO>> listAwardTypesByPage(@RequestBody PageDTO<AwardTypeDTO> pageDTO) {
        return Result.success(awardTypeService.listAwardTypesByPage(pageDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "根据ID删除奖项类型", description = "删除指定ID的奖项类型")
    @Parameter(name = "id", description = "奖项类型ID", required = true, example = "1", in = ParameterIn.PATH)
    @ApiResponse(responseCode = "200", description = "删除成功",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Boolean.class))
    )
    public Result<Boolean> deleteAwardType(@PathVariable("id") Long id) {
        return Result.success(awardTypeService.deleteAwardType(id));
    }
}