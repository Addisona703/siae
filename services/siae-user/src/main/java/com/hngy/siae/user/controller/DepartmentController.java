package com.hngy.siae.user.controller;

import com.hngy.siae.core.permissions.RoleConstants;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import com.hngy.siae.user.dto.request.DepartmentCreateDTO;
import com.hngy.siae.user.dto.response.DepartmentVO;
import com.hngy.siae.user.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.hngy.siae.core.permissions.UserPermissions.*;

/**
 * 部门字典控制器
 *
 * @author KEYKB
 */
@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
@Validated
@Tag(name = "部门字典管理", description = "部门相关接口")
public class DepartmentController {

    private final DepartmentService departmentService;

    @PostMapping
    @Operation(summary = "创建部门", description = "创建新的部门")
    @SiaeAuthorize("hasAuthority('" + USER_DEPARTMENT_CREATE + "')")
    public Result<DepartmentVO> createDepartment(
            @Parameter(description = "部门创建参数") @Valid @RequestBody DepartmentCreateDTO createDTO) {
        return Result.success(departmentService.createDepartment(createDTO));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新部门", description = "更新部门信息")
    @SiaeAuthorize("hasAuthority('" + USER_DEPARTMENT_UPDATE + "')")
    public Result<DepartmentVO> updateDepartment(
            @Parameter(description = "部门ID") @PathVariable @NotNull Long id,
            @Parameter(description = "部门名称") @RequestParam(required = false) String name) {
        return Result.success(departmentService.updateDepartment(id, name));
    }

    @GetMapping("/{id}")
    @Operation(summary = "查询部门", description = "根据ID查询部门信息")
    @SiaeAuthorize(RoleConstants.ANY_AUTHENTICATED)
    public Result<DepartmentVO> getDepartmentById(
            @Parameter(description = "部门ID") @PathVariable @NotNull Long id) {
        return Result.success(departmentService.getDepartmentById(id));
    }

    @GetMapping
    @Operation(summary = "查询部门列表", description = "查询所有部门（字典数据，按orderId排序）")
    @SiaeAuthorize(RoleConstants.ANY_AUTHENTICATED)
    public Result<List<DepartmentVO>> listAllDepartments() {
        return Result.success(departmentService.listAllDepartments());
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除部门", description = "根据ID删除部门")
    @SiaeAuthorize("hasAuthority('" + USER_DEPARTMENT_DELETE + "')")
    public Result<Boolean> deleteDepartment(
            @Parameter(description = "部门ID") @PathVariable @NotNull Long id) {
        return Result.success(departmentService.deleteDepartment(id));
    }
}
