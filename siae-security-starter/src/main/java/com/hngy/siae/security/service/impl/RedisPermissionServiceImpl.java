package com.hngy.siae.security.service.impl;

import com.hngy.siae.security.properties.SecurityProperties;
import com.hngy.siae.security.service.PermissionService;
import com.hngy.siae.security.service.RedisPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Redis权限服务实现类
 * 从Redis缓存中获取用户权限和角色信息，支持缓存过期和刷新
 * 
 * @author SIAE开发团队
 */
@Slf4j
@Service
@ConditionalOnClass(StringRedisTemplate.class)
@ConditionalOnProperty(prefix = "siae.security.permission", name = "redis-enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class RedisPermissionServiceImpl implements PermissionService, RedisPermissionService {
    
    private final StringRedisTemplate redisTemplate;
    private final SecurityProperties securityProperties;
    
    /**
     * Redis键前缀常量
     */
    private static final String PERMISSION_SUFFIX = "permissions";
    private static final String ROLE_SUFFIX = "roles";
    private static final String DELIMITER = ",";
    
    @Override
    public List<String> getUserPermissions(Long userId) {
        try {
            String key = buildPermissionKey(userId);
            String value = redisTemplate.opsForValue().get(key);
            
            if (value == null || value.isEmpty()) {
                if (securityProperties.getPermission().isLogEnabled()) {
                    log.debug("用户权限缓存不存在或为空，用户ID: {}", userId);
                }
                return Collections.emptyList();
            }
            
            List<String> permissions = Arrays.stream(value.split(DELIMITER))
                    .filter(perm -> !perm.trim().isEmpty())
                    .collect(Collectors.toList());
            
            if (securityProperties.getPermission().isLogEnabled()) {
                log.debug("从缓存获取用户权限成功，用户ID: {}, 权限数量: {}", userId, permissions.size());
            }
            return permissions;
        } catch (Exception e) {
            log.error("从缓存获取用户权限失败，用户ID: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<String> getUserRoles(Long userId) {
        try {
            String key = buildRoleKey(userId);
            String value = redisTemplate.opsForValue().get(key);
            
            if (value == null || value.isEmpty()) {
                if (securityProperties.getPermission().isLogEnabled()) {
                    log.debug("用户角色缓存不存在或为空，用户ID: {}", userId);
                }
                return Collections.emptyList();
            }
            
            List<String> roles = Arrays.stream(value.split(DELIMITER))
                    .filter(role -> !role.trim().isEmpty())
                    .collect(Collectors.toList());
            
            if (securityProperties.getPermission().isLogEnabled()) {
                log.debug("从缓存获取用户角色成功，用户ID: {}, 角色数量: {}", userId, roles.size());
            }
            return roles;
        } catch (Exception e) {
            log.error("从缓存获取用户角色失败，用户ID: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<String> getAllUserAuthorities(Long userId) {
        log.info("开始获取用户所有权限，用户ID: {}", userId);

        try {
            // 获取用户权限
            List<String> permissions = getUserPermissions(userId);
            log.info("获取用户权限完成，用户ID: {}, 权限列表: {}", userId, permissions);

            // 获取用户角色（添加ROLE_前缀以符合Spring Security规范）
            List<String> rawRoles = getUserRoles(userId);
            log.info("获取用户角色完成，用户ID: {}, 原始角色列表: {}", userId, rawRoles);

            List<String> roles = rawRoles.stream()
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    .toList();
            log.info("角色前缀处理完成，用户ID: {}, 处理后角色列表: {}", userId, roles);

            // 合并权限和角色
            List<String> allAuthorities = Stream.concat(permissions.stream(), roles.stream())
                    .distinct()
                    .collect(Collectors.toList());

            log.info("权限合并完成，用户ID: {}, 最终权限列表: {}", userId, allAuthorities);

            if (securityProperties.getPermission().isLogEnabled()) {
                log.debug("获取用户所有权限成功，用户ID: {}, 权限总数: {}", userId, allAuthorities.size());
            }
            return allAuthorities;
        } catch (Exception e) {
            log.error("获取用户所有权限失败，用户ID: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public boolean hasPermission(Long userId, String permission) {
        return getUserPermissions(userId).contains(permission);
    }
    
    @Override
    public boolean hasRole(Long userId, String role) {
        List<String> userRoles = getUserRoles(userId);
        return userRoles.contains(role) || userRoles.contains("ROLE_" + role);
    }
    
    @Override
    public boolean hasAnyPermission(Long userId, String... permissions) {
        List<String> userPermissions = getUserPermissions(userId);
        return Arrays.stream(permissions).anyMatch(userPermissions::contains);
    }
    
    @Override
    public boolean hasAllPermissions(Long userId, String... permissions) {
        List<String> userPermissions = getUserPermissions(userId);
        return Arrays.stream(permissions).allMatch(userPermissions::contains);
    }
    
    @Override
    public void refreshUserPermissions(Long userId) {
        // 这里可以实现从数据库重新加载权限的逻辑
        // 暂时只是清除缓存，让下次访问时重新加载
        clearUserPermissions(userId);
        log.info("用户权限缓存已刷新，用户ID: {}", userId);
    }
    
    @Override
    public void clearUserPermissions(Long userId) {
        try {
            String permissionKey = buildPermissionKey(userId);
            redisTemplate.delete(permissionKey);

            log.info("用户权限缓存已清除，用户ID: {}", userId);
        } catch (Exception e) {
            log.error("清除用户权限缓存失败，用户ID: {}", userId, e);
        }
    }

    @Override
    public void clearUserRoles(Long userId) {
        try {
            String roleKey = buildRoleKey(userId);
            redisTemplate.delete(roleKey);

            log.info("用户角色缓存已清除，用户ID: {}", userId);
        } catch (Exception e) {
            log.error("清除用户角色缓存失败，用户ID: {}", userId, e);
        }
    }
    
    /**
     * 构建权限缓存键
     */
    private String buildPermissionKey(Long userId) {
        return securityProperties.getPermission().getCacheKeyPrefix() + userId + ":" + PERMISSION_SUFFIX;
    }
    
    /**
     * 构建角色缓存键
     */
    private String buildRoleKey(Long userId) {
        return securityProperties.getPermission().getCacheKeyPrefix() + userId + ":" + ROLE_SUFFIX;
    }

    // ==================== 新增的缓存管理方法 ====================

    @Override
    public void cacheUserPermissions(Long userId, List<String> permissions, long expireTime, TimeUnit timeUnit) {
        try {
            String key = buildPermissionKey(userId);
            String value = permissions != null && !permissions.isEmpty()
                ? String.join(DELIMITER, permissions)
                : "";

            redisTemplate.opsForValue().set(key, value, expireTime, timeUnit);

            if (securityProperties.getPermission().isLogEnabled()) {
                log.debug("缓存用户权限成功，用户ID: {}, 权限数量: {}, 过期时间: {}{}",
                        userId, permissions != null ? permissions.size() : 0, expireTime, timeUnit);
            }
        } catch (Exception e) {
            log.error("缓存用户权限失败，用户ID: {}", userId, e);
            // 不抛出异常，避免影响主业务流程
        }
    }

    @Override
    public void cacheUserRoles(Long userId, List<String> roles, long expireTime, TimeUnit timeUnit) {
        try {
            String key = buildRoleKey(userId);
            String value = roles != null && !roles.isEmpty()
                ? String.join(DELIMITER, roles)
                : "";

            redisTemplate.opsForValue().set(key, value, expireTime, timeUnit);

            if (securityProperties.getPermission().isLogEnabled()) {
                log.debug("缓存用户角色成功，用户ID: {}, 角色数量: {}, 过期时间: {}{}",
                        userId, roles != null ? roles.size() : 0, expireTime, timeUnit);
            }
        } catch (Exception e) {
            log.error("缓存用户角色失败，用户ID: {}", userId, e);
            // 不抛出异常，避免影响主业务流程
        }
    }

    @Override
    public void clearUserCache(Long userId) {
        clearUserPermissions(userId);
        clearUserRoles(userId);
    }

    @Override
    public boolean hasUserPermissionsCache(Long userId) {
        try {
            String key = buildPermissionKey(userId);
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("检查用户权限缓存失败，用户ID: {}", userId, e);
            return false;
        }
    }

    @Override
    public boolean hasUserRolesCache(Long userId) {
        try {
            String key = buildRoleKey(userId);
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("检查用户角色缓存失败，用户ID: {}", userId, e);
            return false;
        }
    }

    @Override
    public void refreshUserPermissionsCache(Long userId, long expireTime, TimeUnit timeUnit) {
        try {
            String key = buildPermissionKey(userId);
            if (redisTemplate.hasKey(key)) {
                redisTemplate.expire(key, expireTime, timeUnit);

                if (securityProperties.getPermission().isLogEnabled()) {
                    log.debug("刷新用户权限缓存过期时间成功，用户ID: {}, 新过期时间: {}{}",
                            userId, expireTime, timeUnit);
                }
            } else {
                if (securityProperties.getPermission().isLogEnabled()) {
                    log.debug("用户权限缓存不存在，无法刷新，用户ID: {}", userId);
                }
            }
        } catch (Exception e) {
            log.error("刷新用户权限缓存失败，用户ID: {}", userId, e);
        }
    }

    @Override
    public void refreshUserRolesCache(Long userId, long expireTime, TimeUnit timeUnit) {
        try {
            String key = buildRoleKey(userId);
            if (redisTemplate.hasKey(key)) {
                redisTemplate.expire(key, expireTime, timeUnit);

                if (securityProperties.getPermission().isLogEnabled()) {
                    log.debug("刷新用户角色缓存过期时间成功，用户ID: {}, 新过期时间: {}{}",
                            userId, expireTime, timeUnit);
                }
            } else {
                if (securityProperties.getPermission().isLogEnabled()) {
                    log.debug("用户角色缓存不存在，无法刷新，用户ID: {}", userId);
                }
            }
        } catch (Exception e) {
            log.error("刷新用户角色缓存失败，用户ID: {}", userId, e);
        }
    }
}
