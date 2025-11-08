package com.hngy.siae.auth.util;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

/**
 * @deprecated Token 统一通过 Authorization 头传递，目前不再使用 Cookie。
 */
@Deprecated
@Component
public class AuthCookieUtils {

    private static final String ACCESS_TOKEN_COOKIE = "ACCESS_TOKEN";
    private static final String REFRESH_TOKEN_COOKIE = "REFRESH_TOKEN";
    private static final int DEFAULT_REFRESH_TOKEN_MAX_AGE = 30 * 24 * 60 * 60; // 30天

    private static final String COOKIE_PATH = "/";
    private static final boolean IS_PROD = !"dev".equals(System.getProperty("spring.profiles.active"));

    public void writeTokenCookies(String accessToken, Long accessTokenTtlSeconds,
                                  String refreshToken, HttpServletResponse response) {
        if (StrUtil.isNotBlank(accessToken) && accessTokenTtlSeconds != null) {
            addCookie(response, ACCESS_TOKEN_COOKIE, accessToken, accessTokenTtlSeconds);
        }
        if (StrUtil.isNotBlank(refreshToken)) {
            addCookie(response, REFRESH_TOKEN_COOKIE, refreshToken, DEFAULT_REFRESH_TOKEN_MAX_AGE);
        }
    }

    public void clearCookies(HttpServletResponse response) {
        deleteCookie(response, ACCESS_TOKEN_COOKIE);
        deleteCookie(response, REFRESH_TOKEN_COOKIE);
    }

    private void addCookie(HttpServletResponse response, String name, String value, long maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(IS_PROD)
                .path(COOKIE_PATH)
                .sameSite("None")
                .maxAge(maxAgeSeconds)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void deleteCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(IS_PROD)
                .path(COOKIE_PATH)
                .sameSite("None")
                .maxAge(0)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
