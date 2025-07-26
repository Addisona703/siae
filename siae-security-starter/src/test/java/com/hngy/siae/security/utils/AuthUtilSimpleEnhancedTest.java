package com.hngy.siae.security.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * AuthUtil简化增强功能单元测试
 * 
 * @author SIAE开发团队
 */
@ExtendWith(MockitoExtension.class)
class AuthUtilSimpleEnhancedTest {

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    private AuthUtil authUtil;

    @BeforeEach
    void setUp() {
        authUtil = new AuthUtil();
    }

    @Test
    void testIsSuperAdmin_WithSuperAdminRole_ShouldReturnTrue() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ROOT"))
        );

        // When
        boolean result = authUtil.isSuperAdmin(authentication);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsSuperAdmin_WithoutSuperAdminRole_ShouldReturnFalse() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // When
        boolean result = authUtil.isSuperAdmin(authentication);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsSuperAdmin_WithInvalidAuthentication_ShouldReturnFalse() {
        // Given
        when(authentication.isAuthenticated()).thenReturn(false);

        // When
        boolean result = authUtil.isSuperAdmin(authentication);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsSuperAdmin_CurrentUser_WithSuperAdminRole_ShouldReturnTrue() {
        // Given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getAuthorities()).thenReturn(
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ROOT"))
        );

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            boolean result = authUtil.isSuperAdmin();

            // Then
            assertTrue(result);
        }
    }

    @Test
    void testHasPermissionOrSuperAdmin_WithSuperAdmin_ShouldReturnTrue() {
        // Given
        String permission = "USER_VIEW";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("superadmin");
        when(authentication.getAuthorities()).thenReturn(
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ROOT"))
        );

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            boolean result = authUtil.hasPermissionOrSuperAdmin(permission);

            // Then
            assertTrue(result);
        }
    }

    @Test
    void testHasPermissionOrSuperAdmin_WithPermission_ShouldReturnTrue() {
        // Given
        String permission = "USER_VIEW";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getAuthorities()).thenReturn(
            Collections.singletonList(new SimpleGrantedAuthority("USER_VIEW"))
        );

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            boolean result = authUtil.hasPermissionOrSuperAdmin(permission);

            // Then
            assertTrue(result);
        }
    }

    @Test
    void testHasPermissionOrSuperAdmin_WithoutPermission_ShouldReturnFalse() {
        // Given
        String permission = "USER_VIEW";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getAuthorities()).thenReturn(
            Collections.singletonList(new SimpleGrantedAuthority("USER_EDIT"))
        );

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            boolean result = authUtil.hasPermissionOrSuperAdmin(permission);

            // Then
            assertFalse(result);
        }
    }

    @Test
    void testHasRoleOrSuperAdmin_WithSuperAdmin_ShouldReturnTrue() {
        // Given
        String role = "ADMIN";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("superadmin");
        when(authentication.getAuthorities()).thenReturn(
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ROOT"))
        );

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            boolean result = authUtil.hasRoleOrSuperAdmin(role);

            // Then
            assertTrue(result);
        }
    }

    @Test
    void testHasRoleOrSuperAdmin_WithRole_ShouldReturnTrue() {
        // Given
        String role = "ADMIN";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getAuthorities()).thenReturn(
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            boolean result = authUtil.hasRoleOrSuperAdmin(role);

            // Then
            assertTrue(result);
        }
    }

    @Test
    void testHasRoleOrSuperAdmin_WithRolePrefix_ShouldReturnTrue() {
        // Given
        String role = "ROLE_ADMIN";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getAuthorities()).thenReturn(
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            boolean result = authUtil.hasRoleOrSuperAdmin(role);

            // Then
            assertTrue(result);
        }
    }

    @Test
    void testHasRoleOrSuperAdmin_WithoutRole_ShouldReturnFalse() {
        // Given
        String role = "ADMIN";
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("testuser");
        when(authentication.getAuthorities()).thenReturn(
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // When
            boolean result = authUtil.hasRoleOrSuperAdmin(role);

            // Then
            assertFalse(result);
        }
    }
}
