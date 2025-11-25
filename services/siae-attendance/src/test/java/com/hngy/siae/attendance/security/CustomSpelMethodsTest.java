package com.hngy.siae.attendance.security;

import com.hngy.siae.security.expression.SiaeSecurityExpressionRoot;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 自定义 SpEL 方法测试
 * 
 * <p>演示如何使用自定义的权限检查方法</p>
 * 
 * @author SIAE Team
 */
class CustomSpelMethodsTest {

    @Test
    void testIsOwner() {
        // 创建认证对象，用户ID为123
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "123",
            null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        // 创建自定义表达式根对象
        SiaeSecurityExpressionRoot root = new SiaeSecurityExpressionRoot(auth);
        
        // 测试所有者检查
        assertTrue(root.isOwner(123L), "用户应该是记录123的所有者");
        assertFalse(root.isOwner(456L), "用户不应该是记录456的所有者");
        assertFalse(root.isOwner(null), "null记录ID应该返回false");
    }

    @Test
    void testHasPermissionOrOwner() {
        // 创建有权限的用户
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "123",
            null,
            Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ATTENDANCE_VIEW")
            )
        );
        
        SiaeSecurityExpressionRoot root = new SiaeSecurityExpressionRoot(auth);
        
        // 测试：有权限的情况
        assertTrue(root.hasPermissionOrOwner("ATTENDANCE_VIEW", 456L),
            "有权限应该返回true，即使不是所有者");
        
        // 测试：是所有者的情况
        assertTrue(root.hasPermissionOrOwner("ATTENDANCE_EXPORT", 123L),
            "是所有者应该返回true，即使没有权限");
        
        // 测试：既没有权限也不是所有者
        assertFalse(root.hasPermissionOrOwner("ATTENDANCE_EXPORT", 456L),
            "既没有权限也不是所有者应该返回false");
    }

    @Test
    void testIsSuperAdminOrHasPermission() {
        // 测试超级管理员
        Authentication adminAuth = new UsernamePasswordAuthenticationToken(
            "1",
            null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ROOT"))
        );
        
        SiaeSecurityExpressionRoot adminRoot = new SiaeSecurityExpressionRoot(adminAuth);
        
        assertTrue(adminRoot.isSuperAdminOrHasPermission("ANY_PERMISSION"),
            "超级管理员应该通过任何权限检查");
        
        // 测试普通用户有权限
        Authentication userAuth = new UsernamePasswordAuthenticationToken(
            "123",
            null,
            Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ATTENDANCE_EXPORT")
            )
        );
        
        SiaeSecurityExpressionRoot userRoot = new SiaeSecurityExpressionRoot(userAuth);
        
        assertTrue(userRoot.isSuperAdminOrHasPermission("ATTENDANCE_EXPORT"),
            "有权限的用户应该通过检查");
        
        assertFalse(userRoot.isSuperAdminOrHasPermission("ATTENDANCE_DELETE"),
            "没有权限的用户应该不通过检查");
    }

    @Test
    void testCanApproveLeave() {
        // 测试超级管理员
        Authentication adminAuth = new UsernamePasswordAuthenticationToken(
            "1",
            null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );
        
        SiaeSecurityExpressionRoot adminRoot = new SiaeSecurityExpressionRoot(adminAuth);
        
        assertTrue(adminRoot.canApproveLeave(123L),
            "超级管理员应该可以审批任何请假");
        
        // 测试有审批权限的用户
        Authentication approverAuth = new UsernamePasswordAuthenticationToken(
            "123",
            null,
            Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("LEAVE_APPROVE")
            )
        );
        
        SiaeSecurityExpressionRoot approverRoot = new SiaeSecurityExpressionRoot(approverAuth);
        
        assertTrue(approverRoot.canApproveLeave(456L),
            "有审批权限的用户应该可以审批");
        
        // 测试普通用户
        Authentication userAuth = new UsernamePasswordAuthenticationToken(
            "123",
            null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
        
        SiaeSecurityExpressionRoot userRoot = new SiaeSecurityExpressionRoot(userAuth);
        
        assertFalse(userRoot.canApproveLeave(456L),
            "普通用户不应该可以审批");
    }

    @Test
    void testCombinedWithStandardMethods() {
        // 测试自定义方法与标准方法的组合使用
        Authentication auth = new UsernamePasswordAuthenticationToken(
            "123",
            null,
            Arrays.asList(
                new SimpleGrantedAuthority("ROLE_USER"),
                new SimpleGrantedAuthority("ATTENDANCE_VIEW")
            )
        );
        
        SiaeSecurityExpressionRoot root = new SiaeSecurityExpressionRoot(auth);
        
        // 测试标准方法仍然可用
        assertTrue(root.isAuthenticated(), "用户应该是已认证的");
        assertTrue(root.hasAuthority("ATTENDANCE_VIEW"), "用户应该有ATTENDANCE_VIEW权限");
        assertTrue(root.hasRole("USER"), "用户应该有USER角色");
        
        // 测试自定义方法
        assertTrue(root.isOwner(123L), "用户应该是所有者");
        
        // 测试组合逻辑
        boolean canAccess = root.hasAuthority("ATTENDANCE_VIEW") || root.isOwner(123L);
        assertTrue(canAccess, "有权限或是所有者都应该可以访问");
    }
}
