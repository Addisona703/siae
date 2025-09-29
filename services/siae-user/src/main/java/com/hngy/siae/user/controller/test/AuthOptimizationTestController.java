package com.hngy.siae.user.controller;

import com.hngy.siae.core.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证优化测试控制器
 *
 * @author KEYKB
 */
@RestController
@RequestMapping("/test/auth")
@Tag(name = "认证优化测试", description = "用于测试认证优化功能")
@Slf4j
public class AuthOptimizationTestController {

    @GetMapping("/user-info")
    @Operation(summary = "获取当前用户信息", description = "测试用户信息传递是否正常")
    public Result<Map<String, Object>> getCurrentUserInfo(HttpServletRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> result = new HashMap<>();
        result.put("authenticated", authentication != null && authentication.isAuthenticated());

        if (authentication != null) {
            result.put("username", authentication.getName());
            result.put("authorities", authentication.getAuthorities());
            result.put("details", authentication.getDetails());
        }

        // 请求头信息
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Gateway-Auth", request.getHeader("X-Gateway-Auth"));
        headers.put("X-Internal-Service-Call", request.getHeader("X-Internal-Service-Call"));
        headers.put("X-Caller-Service", request.getHeader("X-Caller-Service"));

        result.put("requestHeaders", headers);
        result.put("requestSource", identifyRequestSource(request));

        return Result.success(result);
    }

    @GetMapping("/performance")
    @Operation(summary = "认证性能测试", description = "测试认证处理的性能")
    public Result<Map<String, Object>> performanceTest() {
        long startTime = System.currentTimeMillis();

        // 模拟一些业务处理
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated();

        long endTime = System.currentTimeMillis();

        Map<String, Object> result = new HashMap<>();
        result.put("authenticated", isAuthenticated);
        result.put("processingTime", endTime - startTime);
        result.put("timestamp", System.currentTimeMillis());

        return Result.success(result);
    }

    private String identifyRequestSource(HttpServletRequest request) {
        if ("true".equals(request.getHeader("X-Gateway-Auth"))) {
            return "GATEWAY";
        } else if (request.getHeader("X-Internal-Service-Call") != null) {
            return "INTERNAL_SERVICE";
        } else {
            return "DIRECT_ACCESS";
        }
    }
}