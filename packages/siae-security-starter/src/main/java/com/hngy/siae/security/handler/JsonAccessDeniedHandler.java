package com.hngy.siae.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hngy.siae.core.result.CommonResultCodeEnum;
import com.hngy.siae.core.result.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;

/**
 * 统一处理 403 响应，返回标准 JSON 结构。
 *
 * @author KEYKB
 */
@Slf4j
public record JsonAccessDeniedHandler(ObjectMapper objectMapper) implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        Result<Object> body = Result.error(CommonResultCodeEnum.FORBIDDEN);
        writeJson(response, HttpServletResponse.SC_FORBIDDEN, body);
        log.warn("Forbidden request blocked: path={}, reason={}", request.getRequestURI(), accessDeniedException.getMessage());
    }

    private void writeJson(HttpServletResponse response, int status, Result<?> body) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
        response.getWriter().flush();
    }
}
