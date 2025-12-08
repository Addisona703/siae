package com.hngy.siae.user.controller;

import com.hngy.siae.security.permissions.RoleConstants;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import com.hngy.siae.user.dto.request.PositionCreateDTO;
import com.hngy.siae.user.dto.response.PositionVO;
import com.hngy.siae.user.service.PositionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.hngy.siae.user.permissions.UserPermissions.*;

/**
 * 职位字典控制器
 *
 * @author KEYKB
 */
@RestController
@RequestMapping("/positions")
@RequiredArgsConstructor
@Validated
@Tag(name = "职位字典管理", description = "职位相关接口")
public class PositionController {

    private final PositionService positionService;

    @PostMapping
    @Operation(summary = "创建职位", description = "创建新的职位")
    @SiaeAuthorize("hasAuthority('" + USER_POSITION_CREATE + "')")
    public Result<PositionVO> createPosition(
            @Parameter(description = "职位创建参数") @Valid @RequestBody PositionCreateDTO createDTO) {
        return Result.success(positionService.createPosition(createDTO));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新职位", description = "更新职位信息")
    @SiaeAuthorize("hasAuthority('" + USER_POSITION_UPDATE + "')")
    public Result<PositionVO> updatePosition(
            @Parameter(description = "职位ID") @PathVariable @NotNull Long id,
            @Parameter(description = "职位名称") @RequestParam(required = false) String name) {
        return Result.success(positionService.updatePosition(id, name));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询职位", description = "根据ID查询职位信息")
    @SiaeAuthorize(RoleConstants.ANY_AUTHENTICATED)
    public Result<PositionVO> getPositionById(
            @Parameter(description = "职位ID") @PathVariable @NotNull Long id) {
        return Result.success(positionService.getPositionById(id));
    }

    @GetMapping
    @Operation(summary = "查询职位列表", description = "查询所有职位（字典数据，按orderId排序）")
    @SiaeAuthorize(RoleConstants.ANY_AUTHENTICATED)
    public Result<List<PositionVO>> listAllPositions() {
        return Result.success(positionService.listAllPositions());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除职位", description = "根据ID删除职位")
    @SiaeAuthorize("hasAuthority('" + USER_POSITION_DELETE + "')")
    public Result<Boolean> deletePosition(
            @Parameter(description = "职位ID") @PathVariable @NotNull Long id) {
        return Result.success(positionService.deletePosition(id));
    }
}
