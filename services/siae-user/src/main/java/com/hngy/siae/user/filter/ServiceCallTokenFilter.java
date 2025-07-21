package com.hngy.siae.user.filter;

import com.hngy.siae.core.utils.JwtUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ServiceCallTokenFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    public ServiceCallTokenFilter(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // 只校验用户查询接口
        if (path.startsWith("/api/v1/users")) {
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                reject(response);
                return;
            }

            String token = authHeader.substring(7);
            if (!jwtUtils.validateToken(token)) {
                reject(response);
                return;
            }

            // 校验token中声明的服务标识，确保是授权的服务调用
            String serviceName = jwtUtils.parseToken(token).get("service", String.class);
            if (!"auth-to-user".equals(serviceName)) {
                reject(response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":403,\"message\":\"Forbidden: invalid service call token\"}");
    }
}
