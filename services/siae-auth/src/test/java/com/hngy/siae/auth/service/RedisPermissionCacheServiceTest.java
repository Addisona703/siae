//package com.hngy.siae.auth.service;
//
//import com.hngy.siae.auth.service.impl.RedisPermissionCacheServiceImpl;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.data.redis.core.ValueOperations;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.concurrent.TimeUnit;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
///**
// * Redis权限缓存服务测试类
// *
// * @author KEYKB
// */
//@ExtendWith(MockitoExtension.class)
//class RedisPermissionCacheServiceTest {
//
//    @Mock
//    private StringRedisTemplate redisTemplate;
//
//    @Mock
//    private ValueOperations<String, String> valueOperations;
//
//    private RedisPermissionCacheServiceImpl redisPermissionCacheService;
//
//    @BeforeEach
//    void setUp() {
//        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
//        redisPermissionCacheService = new RedisPermissionCacheServiceImpl(redisTemplate);
//    }
//
//    @Test
//    void testCacheUserPermissions() {
//        // Given
//        Long userId = 1L;
//        List<String> permissions = Arrays.asList("CONTENT_CREATE", "CONTENT_READ", "CONTENT_UPDATE");
//        long expireTime = 3600L;
//        TimeUnit timeUnit = TimeUnit.SECONDS;
//
//        // When
//        redisPermissionCacheService.cacheUserPermissions(userId, permissions, expireTime, timeUnit);
//
//        // Then
//        String expectedKey = "auth:perms:1";
//        String expectedValue = "CONTENT_CREATE,CONTENT_READ,CONTENT_UPDATE";
//        verify(valueOperations).set(expectedKey, expectedValue, expireTime, timeUnit);
//    }
//
//    @Test
//    void testCacheUserRoles() {
//        // Given
//        Long userId = 1L;
//        List<String> roles = Arrays.asList("USER", "ADMIN");
//        long expireTime = 3600L;
//        TimeUnit timeUnit = TimeUnit.SECONDS;
//
//        // When
//        redisPermissionCacheService.cacheUserRoles(userId, roles, expireTime, timeUnit);
//
//        // Then
//        String expectedKey = "auth:roles:1";
//        String expectedValue = "USER,ADMIN";
//        verify(valueOperations).set(expectedKey, expectedValue, expireTime, timeUnit);
//    }
//
//    @Test
//    void testGetUserPermissions_Success() {
//        // Given
//        Long userId = 1L;
//        String key = "auth:perms:1";
//        String cachedValue = "CONTENT_CREATE,CONTENT_READ,CONTENT_UPDATE";
//        when(valueOperations.get(key)).thenReturn(cachedValue);
//
//        // When
//        List<String> result = redisPermissionCacheService.getUserPermissions(userId);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(3, result.size());
//        assertTrue(result.contains("CONTENT_CREATE"));
//        assertTrue(result.contains("CONTENT_READ"));
//        assertTrue(result.contains("CONTENT_UPDATE"));
//    }
//
//    @Test
//    void testGetUserPermissions_NotFound() {
//        // Given
//        Long userId = 1L;
//        String key = "auth:perms:1";
//        when(valueOperations.get(key)).thenReturn(null);
//
//        // When
//        List<String> result = redisPermissionCacheService.getUserPermissions(userId);
//
//        // Then
//        assertNull(result);
//    }
//
//    @Test
//    void testGetUserPermissions_Empty() {
//        // Given
//        Long userId = 1L;
//        String key = "auth:perms:1";
//        when(valueOperations.get(key)).thenReturn("");
//
//        // When
//        List<String> result = redisPermissionCacheService.getUserPermissions(userId);
//
//        // Then
//        assertNotNull(result);
//        assertTrue(result.isEmpty());
//    }
//
//    @Test
//    void testGetUserRoles_Success() {
//        // Given
//        Long userId = 1L;
//        String key = "auth:roles:1";
//        String cachedValue = "USER,ADMIN";
//        when(valueOperations.get(key)).thenReturn(cachedValue);
//
//        // When
//        List<String> result = redisPermissionCacheService.getUserRoles(userId);
//
//        // Then
//        assertNotNull(result);
//        assertEquals(2, result.size());
//        assertTrue(result.contains("USER"));
//        assertTrue(result.contains("ADMIN"));
//    }
//
//    @Test
//    void testClearUserCache() {
//        // Given
//        Long userId = 1L;
//
//        // When
//        redisPermissionCacheService.clearUserCache(userId);
//
//        // Then
//        verify(redisTemplate).delete("auth:perms:1");
//        verify(redisTemplate).delete("auth:roles:1");
//    }
//
//    @Test
//    void testHasUserPermissionsCache() {
//        // Given
//        Long userId = 1L;
//        String key = "auth:perms:1";
//        when(redisTemplate.hasKey(key)).thenReturn(true);
//
//        // When
//        boolean result = redisPermissionCacheService.hasUserPermissionsCache(userId);
//
//        // Then
//        assertTrue(result);
//    }
//
//    @Test
//    void testRefreshUserPermissionsCache() {
//        // Given
//        Long userId = 1L;
//        String key = "auth:perms:1";
//        long expireTime = 7200L;
//        TimeUnit timeUnit = TimeUnit.SECONDS;
//
//        // When
//        redisPermissionCacheService.refreshUserPermissionsCache(userId, expireTime, timeUnit);
//
//        // Then
//        verify(redisTemplate).expire(key, expireTime, timeUnit);
//    }
//
//    @Test
//    void testCacheUserPermissions_WithException() {
//        // Given
//        Long userId = 1L;
//        List<String> permissions = Arrays.asList("CONTENT_CREATE");
//        when(valueOperations.set(anyString(), anyString(), anyLong(), any(TimeUnit.class)))
//                .thenThrow(new RuntimeException("Redis connection failed"));
//
//        // When & Then - should not throw exception
//        assertDoesNotThrow(() -> {
//            redisPermissionCacheService.cacheUserPermissions(userId, permissions, 3600L, TimeUnit.SECONDS);
//        });
//    }
//}
