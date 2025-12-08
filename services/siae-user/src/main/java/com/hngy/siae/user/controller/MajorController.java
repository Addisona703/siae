package com.hngy.siae.user.controller;

import com.hngy.siae.security.permissions.RoleConstants;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import com.hngy.siae.user.dto.request.MajorCreateDTO;
import com.hngy.siae.user.dto.response.MajorVO;
import com.hngy.siae.user.service.MajorService;
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
 * 专业字典控制器
 *
 * @author KEYKB
 */
@RestController
@RequestMapping("/majors")
@RequiredArgsConstructor
@Validated
@Tag(name = "专业字典管理", description = "专业相关接口")
public class MajorController {

    private final MajorService majorService;

    @PostMapping
    @Operation(summary = "创建专业", description = "创建新的专业")
    @SiaeAuthorize("hasAuthority('" + USER_MAJOR_CREATE + "')")
    public Result<MajorVO> createMajor(
            @Parameter(description = "专业创建参数") @Valid @RequestBody MajorCreateDTO createDTO) {
        return Result.success(majorService.createMajor(createDTO));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新专业", description = "更新专业信息")
    @SiaeAuthorize("hasAuthority('" + USER_MAJOR_UPDATE + "')")
    public Result<MajorVO> updateMajor(
            @Parameter(description = "专业ID") @PathVariable @NotNull Long id,
            @Parameter(description = "专业名称") @RequestParam(required = false) String name,
            @Parameter(description = "专业代码") @RequestParam(required = false) String code,
            @Parameter(description = "专业简称") @RequestParam(required = false) String abbr,
            @Parameter(description = "所属学院") @RequestParam(required = false) String collegeName) {
        return Result.success(majorService.updateMajor(id, name, code, abbr, collegeName));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询专业", description = "根据ID查询专业信息")
    @SiaeAuthorize(RoleConstants.ANY_AUTHENTICATED)
    public Result<MajorVO> getMajorById(
            @Parameter(description = "专业ID") @PathVariable @NotNull Long id) {
        return Result.success(majorService.getMajorById(id));
    }

    @GetMapping
    @Operation(summary = "查询专业列表", description = "查询所有专业（字典数据）")
    @SiaeAuthorize(RoleConstants.ANY_AUTHENTICATED)
    public Result<List<MajorVO>> listAllMajors() {
        return Result.success(majorService.listAllMajors());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除专业", description = "根据ID删除专业")
    @SiaeAuthorize("hasAuthority('" + USER_MAJOR_DELETE + "')")
    public Result<Boolean> deleteMajor(
            @Parameter(description = "专业ID") @PathVariable @NotNull Long id) {
        return Result.success(majorService.deleteMajor(id));
    }
}
