package com.hngy.siae.auth.service.impl;

import com.hngy.siae.auth.service.RedisPermissionCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Redis权限缓存服务实现类
 * 
 * 使用Redis存储用户权限和角色信息，提供高效的权限查询功能
 * 
 * 缓存键模式：
 * - 用户权限：auth:perms:{userId}
 * - 用户角色：auth:roles:{userId}
 * 
 * @author KEYKB
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisPermissionCacheServiceImpl implements RedisPermissionCacheService {
    
    private final StringRedisTemplate redisTemplate;
    
    /**
     * Redis键前缀常量
     */
    private static final String PERMISSION_KEY_PREFIX = "auth:perms:";
    private static final String ROLE_KEY_PREFIX = "auth:roles:";
    private static final String DELIMITER = ",";
    
    @Override
    public void cacheUserPermissions(Long userId, List<String> permissions, long expireTime, TimeUnit timeUnit) {
        try {
            String key = PERMISSION_KEY_PREFIX + userId;
            String value = permissions != null && !permissions.isEmpty() 
                ? String.join(DELIMITER, permissions) 
                : "";
            
            redisTemplate.opsForValue().set(key, value, expireTime, timeUnit);
            log.debug("缓存用户权限成功，用户ID: {}, 权限数量: {}", userId, permissions != null ? permissions.size() : 0);
        } catch (Exception e) {
            log.error("缓存用户权限失败，用户ID: {}", userId, e);
            // 不抛出异常，避免影响主业务流程
        }
    }
    
    @Override
    public void cacheUserRoles(Long userId, List<String> roles, long expireTime, TimeUnit timeUnit) {
        try {
            String key = ROLE_KEY_PREFIX + userId;
            String value = roles != null && !roles.isEmpty() 
                ? String.join(DELIMITER, roles) 
                : "";
            
            redisTemplate.opsForValue().set(key, value, expireTime, timeUnit);
            log.debug("缓存用户角色成功，用户ID: {}, 角色数量: {}", userId, roles != null ? roles.size() : 0);
        } catch (Exception e) {
            log.error("缓存用户角色失败，用户ID: {}", userId, e);
            // 不抛出异常，避免影响主业务流程
        }
    }
    
    @Override
    public List<String> getUserPermissions(Long userId) {
        try {
            String key = PERMISSION_KEY_PREFIX + userId;
            String value = redisTemplate.opsForValue().get(key);
            
            if (value == null) {
                log.debug("用户权限缓存不存在，用户ID: {}", userId);
                return null;
            }
            
            if (value.isEmpty()) {
                log.debug("用户权限为空，用户ID: {}", userId);
                return Collections.emptyList();
            }
            
            List<String> permissions = Arrays.asList(value.split(DELIMITER));
            log.debug("从缓存获取用户权限成功，用户ID: {}, 权限数量: {}", userId, permissions.size());
            return permissions;
        } catch (Exception e) {
            log.error("从缓存获取用户权限失败，用户ID: {}", userId, e);
            return null; // 返回null表示缓存获取失败，需要从数据库查询
        }
    }
    
    @Override
    public List<String> getUserRoles(Long userId) {
        try {
            String key = ROLE_KEY_PREFIX + userId;
            String value = redisTemplate.opsForValue().get(key);
            
            if (value == null) {
                log.debug("用户角色缓存不存在，用户ID: {}", userId);
                return null;
            }
            
            if (value.isEmpty()) {
                log.debug("用户角色为空，用户ID: {}", userId);
                return Collections.emptyList();
            }
            
            List<String> roles = Arrays.asList(value.split(DELIMITER));
            log.debug("从缓存获取用户角色成功，用户ID: {}, 角色数量: {}", userId, roles.size());
            return roles;
        } catch (Exception e) {
            log.error("从缓存获取用户角色失败，用户ID: {}", userId, e);
            return null; // 返回null表示缓存获取失败，需要从数据库查询
        }
    }
    
    @Override
    public void clearUserCache(Long userId) {
        try {
            String permissionKey = PERMISSION_KEY_PREFIX + userId;
            String roleKey = ROLE_KEY_PREFIX + userId;

            redisTemplate.delete(permissionKey);
            redisTemplate.delete(roleKey);

            log.debug("清除用户缓存成功，用户ID: {}", userId);
        } catch (Exception e) {
            log.error("清除用户缓存失败，用户ID: {}", userId, e);
            // 不抛出异常，避免影响主业务流程
        }
    }

    @Override
    public void clearUserPermissions(Long userId) {
        try {
            String permissionKey = PERMISSION_KEY_PREFIX + userId;
            redisTemplate.delete(permissionKey);
            log.debug("清除用户权限缓存成功，用户ID: {}", userId);
        } catch (Exception e) {
            log.error("清除用户权限缓存失败，用户ID: {}", userId, e);
            // 不抛出异常，避免影响主业务流程
        }
    }

    @Override
    public void clearUserRoles(Long userId) {
        try {
            String roleKey = ROLE_KEY_PREFIX + userId;
            redisTemplate.delete(roleKey);
            log.debug("清除用户角色缓存成功，用户ID: {}", userId);
        } catch (Exception e) {
            log.error("清除用户角色缓存失败，用户ID: {}", userId, e);
            // 不抛出异常，避免影响主业务流程
        }
    }
    
    @Override
    public boolean hasUserPermissionsCache(Long userId) {
        try {
            String key = PERMISSION_KEY_PREFIX + userId;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("检查用户权限缓存失败，用户ID: {}", userId, e);
            return false;
        }
    }
    
    @Override
    public boolean hasUserRolesCache(Long userId) {
        try {
            String key = ROLE_KEY_PREFIX + userId;
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("检查用户角色缓存失败，用户ID: {}", userId, e);
            return false;
        }
    }
    
    @Override
    public void refreshUserPermissionsCache(Long userId, long expireTime, TimeUnit timeUnit) {
        try {
            String key = PERMISSION_KEY_PREFIX + userId;
            redisTemplate.expire(key, expireTime, timeUnit);
            log.debug("刷新用户权限缓存过期时间成功，用户ID: {}", userId);
        } catch (Exception e) {
            log.error("刷新用户权限缓存过期时间失败，用户ID: {}", userId, e);
        }
    }
    
    @Override
    public void refreshUserRolesCache(Long userId, long expireTime, TimeUnit timeUnit) {
        try {
            String key = ROLE_KEY_PREFIX + userId;
            redisTemplate.expire(key, expireTime, timeUnit);
            log.debug("刷新用户角色缓存过期时间成功，用户ID: {}", userId);
        } catch (Exception e) {
            log.error("刷新用户角色缓存过期时间失败，用户ID: {}", userId, e);
        }
    }
}
