package com.hngy.siae.web.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.util.StringUtils;

/**
 * Web工具类
 * 提供HTTP请求相关的工具方法
 * 
 * @author SIAE开发团队
 */
public class WebUtils {
    
    private static final String UNKNOWN = "unknown";
    
    /**
     * 获取客户端IP地址
     * 支持代理服务器和负载均衡器的IP获取
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
        
        // 处理多个IP的情况，取第一个非unknown的IP
        if (StringUtils.hasText(ip) && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
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
        
        userAgent = userAgent.toLowerCase();
        
        if (userAgent.contains("edg")) {
            return "Microsoft Edge";
        } else if (userAgent.contains("chrome")) {
            return "Chrome浏览器";
        } else if (userAgent.contains("firefox")) {
            return "Firefox浏览器";
        } else if (userAgent.contains("safari") && !userAgent.contains("chrome")) {
            return "Safari浏览器";
        } else if (userAgent.contains("opera") || userAgent.contains("opr")) {
            return "Opera浏览器";
        } else if (userAgent.contains("msie") || userAgent.contains("trident")) {
            return "IE浏览器";
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
        
        userAgent = userAgent.toLowerCase();
        
        if (userAgent.contains("windows nt 10")) {
            return "Windows 10";
        } else if (userAgent.contains("windows nt 6.3")) {
            return "Windows 8.1";
        } else if (userAgent.contains("windows nt 6.2")) {
            return "Windows 8";
        } else if (userAgent.contains("windows nt 6.1")) {
            return "Windows 7";
        } else if (userAgent.contains("windows")) {
            return "Windows";
        } else if (userAgent.contains("mac os x")) {
            return "Mac OS X";
        } else if (userAgent.contains("mac")) {
            return "Mac OS";
        } else if (userAgent.contains("linux")) {
            return "Linux";
        } else if (userAgent.contains("android")) {
            return "Android";
        } else if (userAgent.contains("iphone")) {
            return "iPhone";
        } else if (userAgent.contains("ipad")) {
            return "iPad";
        } else {
            return "其他操作系统";
        }
    }
    
    /**
     * 获取请求的完整URL
     *
     * @param request HTTP请求
     * @return 完整URL
     */
    public static String getFullRequestUrl(HttpServletRequest request) {
        StringBuffer requestURL = request.getRequestURL();
        String queryString = request.getQueryString();
        
        if (StringUtils.hasText(queryString)) {
            requestURL.append("?").append(queryString);
        }
        
        return requestURL.toString();
    }
    
    /**
     * 判断是否为Ajax请求
     *
     * @param request HTTP请求
     * @return 是否为Ajax请求
     */
    public static boolean isAjaxRequest(HttpServletRequest request) {
        String requestedWith = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(requestedWith);
    }
}
