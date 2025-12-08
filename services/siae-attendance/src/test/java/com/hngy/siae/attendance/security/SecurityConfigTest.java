//package com.hngy.siae.attendance.security;
//
//import com.hngy.siae.security.utils.SecurityUtil;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.test.context.TestPropertySource;
//
//import java.util.Arrays;
//import java.util.Collections;
//
//import static org.junit.jupiter.api.Assertions.*;
//
///**
// * 安全配置测试
// *
// * @author SIAE Team
// */
//@SpringBootTest
//@TestPropertySource(properties = {
//    "siae.security.enabled=false"  // 测试环境禁用安全配置
//})
//class SecurityConfigTest {
//
//    @Test
//    void testSecurityUtilGetCurrentUserId() {
//        // 模拟认证用户
//        Authentication auth = new UsernamePasswordAuthenticationToken(
//            "123",  // 用户ID
//            null,
//            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
//        );
//        SecurityContextHolder.getContext().setAuthentication(auth);
//
//        // 测试获取当前用户ID
//        Long userId = SecurityUtil.getCurrentUserId();
//        assertEquals(123L, userId);
//
//        // 清理
//        SecurityContextHolder.clearContext();
//    }
//
//    @Test
//    void testSecurityUtilGetCurrentUserIdOrNull() {
//        // 未认证情况
//        SecurityContextHolder.clearContext();
//        Long userId = SecurityUtil.getCurrentUserIdOrNull();
//        assertNull(userId);
//    }
//
//    @Test
//    void testSecurityUtilHasPermission() {
//        // 模拟有权限的用户
//        Authentication auth = new UsernamePasswordAuthenticationToken(
//            "123",
//            null,
//            Arrays.asList(
//                new SimpleGrantedAuthority("ATTENDANCE_VIEW"),
//                new SimpleGrantedAuthority("ATTENDANCE_LIST")
//            )
//        );
//        SecurityContextHolder.getContext().setAuthentication(auth);
//
//        // 测试权限检查
//        assertTrue(SecurityUtil.hasPermission("ATTENDANCE_VIEW"));
//        assertTrue(SecurityUtil.hasPermission("ATTENDANCE_LIST"));
//        assertFalse(SecurityUtil.hasPermission("ATTENDANCE_EXPORT"));
//
//        // 清理
//        SecurityContextHolder.clearContext();
//    }
//
//    @Test
//    void testSecurityUtilIsSuperAdmin() {
//        // 模拟超级管理员
//        Authentication auth = new UsernamePasswordAuthenticationToken(
//            "1",
//            null,
//            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ROOT"))
//        );
//        SecurityContextHolder.getContext().setAuthentication(auth);
//
//        // 测试超级管理员检查
//        assertTrue(SecurityUtil.isSuperAdmin());
//
//        // 清理
//        SecurityContextHolder.clearContext();
//    }
//
//    @Test
//    void testSecurityUtilIsOwner() {
//        // 模拟认证用户
//        Authentication auth = new UsernamePasswordAuthenticationToken(
//            "123",
//            null,
//            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
//        );
//        SecurityContextHolder.getContext().setAuthentication(auth);
//
//        // 测试所有者检查
//        assertTrue(SecurityUtil.isOwner(123L));
//        assertFalse(SecurityUtil.isOwner(456L));
//
//        // 清理
//        SecurityContextHolder.clearContext();
//    }
//
//    @Test
//    void testSecurityUtilHasAnyPermission() {
//        // 模拟有部分权限的用户
//        Authentication auth = new UsernamePasswordAuthenticationToken(
//            "123",
//            null,
//            Collections.singletonList(new SimpleGrantedAuthority("ATTENDANCE_VIEW"))
//        );
//        SecurityContextHolder.getContext().setAuthentication(auth);
//
//        // 测试任意权限检查
//        assertTrue(SecurityUtil.hasAnyPermission("ATTENDANCE_VIEW", "ATTENDANCE_LIST"));
//        assertTrue(SecurityUtil.hasAnyPermission("ATTENDANCE_EXPORT", "ATTENDANCE_VIEW"));
//        assertFalse(SecurityUtil.hasAnyPermission("ATTENDANCE_EXPORT", "ATTENDANCE_DELETE"));
//
//        // 清理
//        SecurityContextHolder.clearContext();
//    }
//
//    @Test
//    void testSecurityUtilHasAllPermissions() {
//        // 模拟有多个权限的用户
//        Authentication auth = new UsernamePasswordAuthenticationToken(
//            "123",
//            null,
//            Arrays.asList(
//                new SimpleGrantedAuthority("ATTENDANCE_VIEW"),
//                new SimpleGrantedAuthority("ATTENDANCE_LIST")
//            )
//        );
//        SecurityContextHolder.getContext().setAuthentication(auth);
//
//        // 测试所有权限检查
//        assertTrue(SecurityUtil.hasAllPermissions("ATTENDANCE_VIEW", "ATTENDANCE_LIST"));
//        assertFalse(SecurityUtil.hasAllPermissions("ATTENDANCE_VIEW", "ATTENDANCE_EXPORT"));
//
//        // 清理
//        SecurityContextHolder.clearContext();
//    }
//
//    @Test
//    void testSuperAdminBypassesPermissionCheck() {
//        // 模拟超级管理员
//        Authentication auth = new UsernamePasswordAuthenticationToken(
//            "1",
//            null,
//            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ROOT"))
//        );
//        SecurityContextHolder.getContext().setAuthentication(auth);
//
//        // 超级管理员应该通过所有权限检查
//        assertTrue(SecurityUtil.hasPermission("ANY_PERMISSION"));
//        assertTrue(SecurityUtil.hasAnyPermission("ANY_PERMISSION"));
//        assertTrue(SecurityUtil.hasAllPermissions("ANY_PERMISSION", "ANOTHER_PERMISSION"));
//
//        // 清理
//        SecurityContextHolder.clearContext();
//    }
//}
