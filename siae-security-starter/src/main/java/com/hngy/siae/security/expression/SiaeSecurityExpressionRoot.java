//package com.hngy.siae.security.expression;
//
//import org.springframework.security.access.expression.SecurityExpressionRoot;
//import org.springframework.security.core.Authentication;
//
//public class SiaeSecurityExpressionRoot extends SecurityExpressionRoot {
//
//    public SiaeSecurityExpressionRoot(Authentication authentication) {
//        super(authentication); // 直接调用父类构造函数，传递认证对象
//    }
//
//    // 你还可以添加自定义权限判断方法
//    public boolean isRoot() {
//        Authentication auth = getAuthentication();
//        return auth != null && auth.getAuthorities().stream()
//            .anyMatch(a -> "ROLE_ROOT".equals(a.getAuthority()));
//    }
//
//    // 这里可以写更多自定义权限表达式...
//}
