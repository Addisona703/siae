package com.hngy.siae.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hngy.siae.core.result.CommonResultCodeEnum;
import com.hngy.siae.core.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

/**
 * 将未认证异常转换为统一 JSON 响应的入口点。
 *
 * @author KEYKB
 */
@Slf4j
@RequiredArgsConstructor
public class JsonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        Result<Object> body = Result.error(CommonResultCodeEnum.UNAUTHORIZED);
        writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, body);
        log.warn("Unauthorized request blocked: path={}, reason={}", request.getRequestURI(), authException.getMessage());
    }

    private void writeJson(HttpServletResponse response, int status, Result<?> body) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
        response.getWriter().flush();
    }
}
