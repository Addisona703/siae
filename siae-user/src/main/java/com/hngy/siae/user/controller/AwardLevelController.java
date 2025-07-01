package com.hngy.siae.user.controller;

import com.hngy.siae.common.dto.request.PageDTO;
import com.hngy.siae.common.dto.response.PageVO;
import com.hngy.siae.common.result.Result;
import com.hngy.siae.common.validation.UpdateGroup;
import com.hngy.siae.user.dto.request.AwardLevelDTO;
import com.hngy.siae.user.dto.response.AwardLevelVO;
import com.hngy.siae.user.service.AwardLevelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 奖项等级字典控制器
 *
 * @author KEYKB
 */
@RestController
@RequestMapping("/api/award-levels")
@RequiredArgsConstructor
@Tag(name = "奖项等级字典管理", description = "用于管理奖项等级信息的接口")
@Validated
public class AwardLevelController {

    private final AwardLevelService awardLevelService;

    @PostMapping
    @Operation(summary = "创建奖项等级")
    @ApiResponse(responseCode = "200", description = "创建成功", content = @Content(schema = @Schema(implementation = AwardLevelVO.class)))
    public Result<AwardLevelVO> createAwardLevel(@Validated @RequestBody AwardLevelDTO awardLevelDTO) {
        return Result.success(awardLevelService.createAwardLevel(awardLevelDTO));
    }

    @PutMapping
    @Operation(summary = "更新奖项等级")
    @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = AwardLevelVO.class)))
    public Result<AwardLevelVO> updateAwardLevel(@Validated(UpdateGroup.class) @RequestBody AwardLevelDTO awardLevelDTO) {
        return Result.success(awardLevelService.updateAwardLevel(awardLevelDTO));
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取奖项等级")
    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = AwardLevelVO.class)))
    public Result<AwardLevelVO> getAwardLevelById(
        @Parameter(description = "奖项等级ID", required = true, example = "1", in = ParameterIn.PATH) @PathVariable Long id) {
        return Result.success(awardLevelService.getAwardLevelById(id));
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "根据名称获取奖项等级")
    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = AwardLevelVO.class)))
    public Result<AwardLevelVO> getAwardLevelByName(
        @Parameter(description = "奖项等级名称", required = true, example = "国家级", in = ParameterIn.PATH) @PathVariable String name) {
        return Result.success(awardLevelService.getAwardLevelByName(name));
    }

    @GetMapping
    @Operation(summary = "获取所有奖项等级")
    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = List.class)))
    public Result<List<AwardLevelVO>> listAllAwardLevels() {
        return Result.success(awardLevelService.listAllAwardLevels());
    }

    @PostMapping("/page")
    @Operation(summary = "分页查询奖项等级")
    @ApiResponse(responseCode = "200", description = "查询成功", content = @Content(schema = @Schema(implementation = PageVO.class)))
    public Result<PageVO<AwardLevelVO>> listAwardLevelsByPage(@RequestBody PageDTO<AwardLevelDTO> pageDTO) {
        return Result.success(awardLevelService.listAwardLevelsByPage(pageDTO));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "根据ID删除奖项等级")
    @ApiResponse(responseCode = "200", description = "删除成功", content = @Content(schema = @Schema(implementation = Boolean.class)))
    public Result<Boolean> deleteAwardLevel(
        @Parameter(description = "奖项等级ID", required = true, example = "1", in = ParameterIn.PATH) @PathVariable Long id) {
        return Result.success(awardLevelService.deleteAwardLevel(id));
    }
}