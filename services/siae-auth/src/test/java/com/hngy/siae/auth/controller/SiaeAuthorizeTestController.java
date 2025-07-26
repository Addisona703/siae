package com.hngy.siae.auth.controller;

import com.hngy.siae.core.result.Result;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.hngy.siae.core.permissions.AuthPermissions.*;

/**
 * SiaeAuthorize注解测试控制器
 * 
 * 用于测试@SiaeAuthorize注解和SiaeAuthorizeAspect切面的功能
 * 
 * @author SIAE开发团队
 */
@Slf4j
@Tag(name = "SiaeAuthorize测试", description = "测试@SiaeAuthorize注解的权限控制功能")
@RestController
@RequestMapping("/test/siae-authorize")
public class SiaeAuthorizeTestController {

    /**
     * 无权限要求的测试接口
     */
    @GetMapping("/public")
    @Operation(summary = "公开接口", description = "无权限要求的测试接口")
    public Result<String> publicEndpoint() {
        return Result.success("公开接口访问成功");
    }

    /**
     * 测试超级管理员权限
     */
    @GetMapping("/super-admin")
    @Operation(summary = "超级管理员测试", description = "测试超级管理员是否能直接放行")
    @SiaeAuthorize("hasAuthority('IMPOSSIBLE_PERMISSION')")
    public Result<String> superAdminTest() {
        return Result.success("超级管理员测试通过 - 即使没有IMPOSSIBLE_PERMISSION权限也能访问");
    }

    /**
     * 测试具体权限检查
     */
    @GetMapping("/auth-log-query")
    @Operation(summary = "日志查询权限测试", description = "测试AUTH_LOG_QUERY权限")
    @SiaeAuthorize("hasAuthority('" + AUTH_LOG_QUERY + "')")
    public Result<String> authLogQueryTest() {
        return Result.success("AUTH_LOG_QUERY权限测试通过");
    }

    /**
     * 测试角色检查
     */
    @GetMapping("/admin-role")
    @Operation(summary = "管理员角色测试", description = "测试ADMIN角色")
    @SiaeAuthorize("hasRole('ADMIN')")
    public Result<String> adminRoleTest() {
        return Result.success("ADMIN角色测试通过");
    }

    /**
     * 测试复合权限表达式 - OR逻辑
     */
    @GetMapping("/or-expression")
    @Operation(summary = "OR逻辑测试", description = "测试OR逻辑的复合权限表达式")
    @SiaeAuthorize("hasRole('ADMIN') or hasAuthority('" + AUTH_LOG_QUERY + "')")
    public Result<String> orExpressionTest() {
        return Result.success("OR逻辑权限表达式测试通过");
    }

    /**
     * 测试复合权限表达式 - AND逻辑
     */
    @GetMapping("/and-expression")
    @Operation(summary = "AND逻辑测试", description = "测试AND逻辑的复合权限表达式")
    @SiaeAuthorize("hasRole('ADMIN') and hasAuthority('" + AUTH_LOG_QUERY + "')")
    public Result<String> andExpressionTest() {
        return Result.success("AND逻辑权限表达式测试通过");
    }

    /**
     * 测试权限不足的情况
     */
    @GetMapping("/permission-denied")
    @Operation(summary = "权限不足测试", description = "测试权限不足时的异常处理")
    @SiaeAuthorize("hasAuthority('NON_EXISTENT_PERMISSION')")
    public Result<String> permissionDeniedTest() {
        return Result.success("这个接口应该返回权限不足异常");
    }

    /**
     * 测试空权限表达式
     */
    @GetMapping("/empty-expression")
    @Operation(summary = "空表达式测试", description = "测试空权限表达式的处理")
    @SiaeAuthorize("")
    public Result<String> emptyExpressionTest() {
        return Result.success("这个接口应该返回未授权访问异常");
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current-user")
    @Operation(summary = "当前用户信息", description = "获取当前认证用户的详细信息")
    public Result<Map<String, Object>> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> userInfo = new HashMap<>();
        if (auth != null) {
            userInfo.put("username", auth.getName());
            userInfo.put("isAuthenticated", auth.isAuthenticated());
            userInfo.put("authorities", auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .collect(Collectors.toList()));
            userInfo.put("principal", auth.getPrincipal().toString());
            userInfo.put("details", auth.getDetails());
            
            // 检查是否为超级管理员
            boolean isSuperAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ROOT".equals(a.getAuthority()));
            userInfo.put("isSuperAdmin", isSuperAdmin);
        } else {
            userInfo.put("message", "用户未认证");
        }
        
        return Result.success(userInfo);
    }

    /**
     * 测试POST请求的权限控制
     */
    @PostMapping("/post-test")
    @Operation(summary = "POST请求权限测试", description = "测试POST请求的权限控制")
    @SiaeAuthorize("hasAuthority('" + AUTH_LOG_QUERY + "')")
    public Result<String> postTest(@RequestBody(required = false) Map<String, Object> requestData) {
        log.info("POST请求测试，请求数据: {}", requestData);
        return Result.success("POST请求权限测试通过");
    }

    /**
     * 测试多个权限的复杂表达式
     */
    @GetMapping("/complex-expression")
    @Operation(summary = "复杂权限表达式测试", description = "测试复杂的权限表达式")
    @SiaeAuthorize("(hasRole('ADMIN') and hasAuthority('" + AUTH_LOG_QUERY + "')) or hasRole('SUPER_ADMIN')")
    public Result<String> complexExpressionTest() {
        return Result.success("复杂权限表达式测试通过");
    }

    /**
     * 测试权限检查的性能
     */
    @GetMapping("/performance-test")
    @Operation(summary = "性能测试", description = "测试权限检查的性能")
    @SiaeAuthorize("hasAuthority('" + AUTH_LOG_QUERY + "')")
    public Result<Map<String, Object>> performanceTest() {
        long startTime = System.currentTimeMillis();
        
        // 模拟一些业务逻辑
        try {
            Thread.sleep(10); // 模拟10ms的业务处理时间
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        long endTime = System.currentTimeMillis();
        
        Map<String, Object> result = new HashMap<>();
        result.put("message", "性能测试完成");
        result.put("executionTime", endTime - startTime + "ms");
        result.put("timestamp", System.currentTimeMillis());
        
        return Result.success(result);
    }

    /**
     * 测试异常处理
     */
    @GetMapping("/exception-test")
    @Operation(summary = "异常处理测试", description = "测试权限异常的处理")
    @SiaeAuthorize("hasAuthority('DEFINITELY_NON_EXISTENT_PERMISSION')")
    public Result<String> exceptionTest() {
        return Result.success("如果看到这个消息，说明权限检查有问题");
    }

    /**
     * 批量权限测试
     */
    @GetMapping("/batch-test")
    @Operation(summary = "批量权限测试", description = "返回多个权限测试的结果")
    public Result<Map<String, String>> batchTest() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Map<String, String> results = new HashMap<>();
        
        if (auth == null || !auth.isAuthenticated()) {
            results.put("status", "未认证");
            return Result.success(results);
        }
        
        // 检查各种权限
        boolean isSuperAdmin = auth.getAuthorities().stream()
            .anyMatch(a -> "ROLE_ROOT".equals(a.getAuthority()));
        results.put("isSuperAdmin", String.valueOf(isSuperAdmin));
        
        boolean hasAdminRole = auth.getAuthorities().stream()
            .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        results.put("hasAdminRole", String.valueOf(hasAdminRole));
        
        boolean hasLogQueryAuth = auth.getAuthorities().stream()
            .anyMatch(a -> AUTH_LOG_QUERY.equals(a.getAuthority()));
        results.put("hasLogQueryAuth", String.valueOf(hasLogQueryAuth));
        
        results.put("username", auth.getName());
        results.put("authoritiesCount", String.valueOf(auth.getAuthorities().size()));
        
        return Result.success(results);
    }
}
