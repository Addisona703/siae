package com.hngy.siae.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * Web工具类
 * 
 * @author KEYKB
 */
public class WebUtils {
    
    private static final String UNKNOWN = "unknown";
    
    /**
     * 获取客户端IP地址
     *
     * @param request HTTP请求
     * @return 客户端IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.hasText(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (!StringUtils.hasText(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (!StringUtils.hasText(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (!StringUtils.hasText(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (!StringUtils.hasText(ip) || UNKNOWN.equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
    
    /**
     * 获取客户端浏览器类型
     *
     * @param request HTTP请求
     * @return 浏览器类型
     */
    public static String getBrowser(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (!StringUtils.hasText(userAgent)) {
            return "未知浏览器";
        }
        
        if (userAgent.contains("MSIE") || userAgent.contains("Trident")) {
            return "IE浏览器";
        } else if (userAgent.contains("Firefox")) {
            return "Firefox浏览器";
        } else if (userAgent.contains("Chrome")) {
            return "Chrome浏览器";
        } else if (userAgent.contains("Safari")) {
            return "Safari浏览器";
        } else if (userAgent.contains("Opera")) {
            return "Opera浏览器";
        } else {
            return "其他浏览器";
        }
    }
    
    /**
     * 获取客户端操作系统
     *
     * @param request HTTP请求
     * @return 操作系统
     */
    public static String getOs(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (!StringUtils.hasText(userAgent)) {
            return "未知操作系统";
        }
        
        if (userAgent.toLowerCase().contains("windows")) {
            return "Windows";
        } else if (userAgent.toLowerCase().contains("mac")) {
            return "Mac OS";
        } else if (userAgent.toLowerCase().contains("linux")) {
            return "Linux";
        } else if (userAgent.toLowerCase().contains("android")) {
            return "Android";
        } else if (userAgent.toLowerCase().contains("iphone") || userAgent.toLowerCase().contains("ipad")) {
            return "iOS";
        } else {
            return "其他操作系统";
        }
    }
} 