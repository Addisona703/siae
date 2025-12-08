package com.hngy.siae.security.service.impl;

import com.hngy.siae.core.utils.JwtUtils;
import com.hngy.siae.security.properties.SecurityProperties;
import com.hngy.siae.security.service.SecurityCacheService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 安全缓存服务实现类
 * 统一管理用户权限、角色、Token的Redis缓存操作
 * 
 * @author KEYKB
 */
@Slf4j
@Service
@AllArgsConstructor
@ConditionalOnClass(StringRedisTemplate.class)
@ConditionalOnProperty(prefix = "siae.security.permission", name = "redis-enabled", havingValue = "true", matchIfMissing = true)
public class SecurityCacheServiceImpl implements SecurityCacheService {

//    public SecurityCacheServiceImpl(StringRedisTemplate redisTemplate,
//                                    RedisTemplate<String, Object> objectRedisTemplate,
//                                    SecurityProperties securityProperties,
//                                    JwtUtils jwtUtils) {
//        this.redisTemplate = redisTemplate;
//        this.objectRedisTemplate = objectRedisTemplate;
//        this.securityProperties = securityProperties;
//        this.jwtUtils = jwtUtils;
//        log.info("SecurityCacheService已初始化，支持权限和token验证，集成JWT工具类");
//    }
    
    private final StringRedisTemplate redisTemplate;
    private final RedisTemplate<String, Object> objectRedisTemplate;
    private final SecurityProperties securityProperties;
    private final JwtUtils jwtUtils;

    /**
     * Redis键前缀常量
     */
    private static final String PERMISSION_SUFFIX = "permissions";
    private static final String ROLE_SUFFIX = "roles";
    private static final String TOKEN_KEY_PREFIX = "auth:token:";
    private static final String DELIMITER = ",";
    
    @Override
    public List<String> getUserPermissions(Long userId) {
        try {
            String key = buildPermissionKey(userId);
            String value = redisTemplate.opsForValue().get(key);
            
            // 获取TTL用于调试
            Long ttl = redisTemplate.getExpire(key);
            
            if (value == null || value.isEmpty()) {
                log.warn("【权限缓存】用户权限缓存不存在或为空，用户ID: {}, key: {}, TTL: {}", userId, key, ttl);
                return Collections.emptyList();
            }
            
            List<String> permissions = Arrays.stream(value.split(DELIMITER))
                    .filter(perm -> !perm.trim().isEmpty())
                    .collect(Collectors.toList());
            
            log.debug("【权限缓存】获取权限成功，用户ID: {}, 权限数量: {}, 剩余TTL: {}秒", userId, permissions.size(), ttl);
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
            
            // 获取TTL用于调试
            Long ttl = redisTemplate.getExpire(key);
            
            if (value == null || value.isEmpty()) {
                log.warn("【角色缓存】用户角色缓存不存在或为空，用户ID: {}, key: {}, TTL: {}", userId, key, ttl);
                return Collections.emptyList();
            }
            
            List<String> roles = Arrays.stream(value.split(DELIMITER))
                    .filter(role -> !role.trim().isEmpty())
                    .collect(Collectors.toList());
            
            log.debug("【角色缓存】获取角色成功，用户ID: {}, 角色数量: {}, 剩余TTL: {}秒", userId, roles.size(), ttl);
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

            // 始终打印缓存信息，便于调试
            log.info("【权限缓存】设置权限缓存，key: {}, 权限数量: {}, 过期时间: {} {}",
                    key, permissions != null ? permissions.size() : 0, expireTime, timeUnit);
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

            // 始终打印缓存信息，便于调试
            log.info("【角色缓存】设置角色缓存，key: {}, 角色数量: {}, 过期时间: {} {}",
                    key, roles != null ? roles.size() : 0, expireTime, timeUnit);
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

    // ==================== Token验证方法实现 ====================

    @Override
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.debug("Token为空，验证失败");
            return false;
        }

        try {
            // 第一步：验证JWT格式和有效性
            boolean jwtValid = jwtUtils.validateToken(token);
            if (!jwtValid) {
                log.debug("JWT格式验证失败或token已过期");
                return false;
            }

            // 第二步：检查token在Redis中是否存在
            String tokenKey = TOKEN_KEY_PREFIX + token;
            boolean redisValid = objectRedisTemplate.hasKey(tokenKey);

            if (!redisValid) {
                log.debug("Token在Redis中不存在，用户可能已登出");
                return false;
            }

            log.debug("Token验证通过: JWT格式有效且在Redis中存在");
            return true;

        } catch (Exception e) {
            log.warn("验证token时发生异常: {}, 验证失败", e.getMessage());
            // JWT验证异常时返回false，不采用宽松模式
            return false;
        }
    }

    @Override
    public void storeToken(String token, Object userInfo, long expireSeconds) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("Token为空，无法存储");
            return;
        }

        try {
            // 验证JWT格式是否正确
            if (!jwtUtils.validateToken(token)) {
                log.warn("JWT格式无效，拒绝存储token");
                return;
            }

            // 尝试从JWT中获取过期时间，确保一致性
            try {
                Date jwtExpiration = jwtUtils.getExpirationDate(token);
                long jwtExpireSeconds = (jwtExpiration.getTime() - System.currentTimeMillis()) / 1000;

                // 使用JWT中的过期时间，但不超过传入的过期时间
                long actualExpireSeconds = Math.min(expireSeconds, Math.max(jwtExpireSeconds, 0));

                String tokenKey = TOKEN_KEY_PREFIX + token;
                objectRedisTemplate.opsForValue().set(tokenKey, userInfo, actualExpireSeconds, TimeUnit.SECONDS);

                log.debug("Token已存储到Redis: key={}, expireSeconds={} (JWT过期时间: {}秒)",
                         tokenKey, actualExpireSeconds, jwtExpireSeconds);

            } catch (Exception jwtException) {
                // JWT解析失败时使用传入的过期时间
                log.debug("无法解析JWT过期时间，使用默认过期时间: {}", jwtException.getMessage());

                String tokenKey = TOKEN_KEY_PREFIX + token;
                objectRedisTemplate.opsForValue().set(tokenKey, userInfo, expireSeconds, TimeUnit.SECONDS);

                log.debug("Token已存储到Redis: key={}, expireSeconds={}", tokenKey, expireSeconds);
            }

        } catch (Exception e) {
            log.error("存储token到Redis失败: {}", e.getMessage(), e);
            // 不抛出异常，避免影响业务流程
        }
    }

    @Override
    public void removeToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("Token为空，无法删除");
            return;
        }

        try {
            String tokenKey = TOKEN_KEY_PREFIX + token;
            objectRedisTemplate.delete(tokenKey);

            log.debug("Token已从Redis中删除: key={}", tokenKey);

        } catch (Exception e) {
            log.error("从Redis删除token失败: {}", e.getMessage(), e);
        }
    }

    // ==================== Token解析功能 ====================

    /**
     * 从token中提取用户ID
     *
     * @param token JWT token
     * @return 用户ID，解析失败时返回null
     */
    public Long getUserIdFromToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        try {
            return jwtUtils.getUserId(token);
        } catch (Exception e) {
            log.debug("从token中提取用户ID失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从token中提取用户名
     *
     * @param token JWT token
     * @return 用户名，解析失败时返回null
     */
    public String getUsernameFromToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        try {
            return jwtUtils.getUsername(token);
        } catch (Exception e) {
            log.debug("从token中提取用户名失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从token中提取过期时间
     *
     * @param token JWT token
     * @return 过期时间，解析失败时返回null
     */
    public Date getExpirationFromToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        try {
            return jwtUtils.getExpirationDate(token);
        } catch (Exception e) {
            log.debug("从token中提取过期时间失败: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查token是否即将过期（在指定分钟内过期）
     *
     * @param token JWT token
     * @param minutesBeforeExpiry 过期前的分钟数
     * @return true表示即将过期，false表示还有足够时间
     */
    public boolean isTokenExpiringSoon(String token, int minutesBeforeExpiry) {
        Date expiration = getExpirationFromToken(token);
        if (expiration == null) {
            return false;
        }

        long currentTime = System.currentTimeMillis();
        long expirationTime = expiration.getTime();
        long timeUntilExpiry = expirationTime - currentTime;
        long minutesUntilExpiry = timeUntilExpiry / (60 * 1000);

        return minutesUntilExpiry <= minutesBeforeExpiry;
    }

    // ==================== 单点登录（同端互斥）方法 ====================

    private static final String USER_DEVICE_TOKEN_PREFIX = "auth:user:device:";

    @Override
    public void storeUserDeviceToken(Long userId, String deviceType, String token, long expireSeconds) {
        if (userId == null || deviceType == null || token == null) {
            log.warn("参数为空，无法存储用户设备token");
            return;
        }

        try {
            String key = USER_DEVICE_TOKEN_PREFIX + userId + ":" + deviceType;
            objectRedisTemplate.opsForValue().set(key, token, expireSeconds, TimeUnit.SECONDS);
            log.debug("用户设备token已存储: userId={}, deviceType={}", userId, deviceType);
        } catch (Exception e) {
            log.error("存储用户设备token失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public String getUserDeviceToken(Long userId, String deviceType) {
        if (userId == null || deviceType == null) {
            return null;
        }

        try {
            String key = USER_DEVICE_TOKEN_PREFIX + userId + ":" + deviceType;
            Object value = objectRedisTemplate.opsForValue().get(key);
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.error("获取用户设备token失败: {}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void removeUserDeviceToken(Long userId, String deviceType) {
        if (userId == null || deviceType == null) {
            return;
        }

        try {
            String key = USER_DEVICE_TOKEN_PREFIX + userId + ":" + deviceType;
            objectRedisTemplate.delete(key);
            log.debug("用户设备token已删除: userId={}, deviceType={}", userId, deviceType);
        } catch (Exception e) {
            log.error("删除用户设备token失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public void kickSameDeviceAndStoreNewToken(Long userId, String deviceType, String newToken, long expireSeconds) {
        if (userId == null || deviceType == null || newToken == null) {
            log.warn("参数为空，无法执行同端互斥");
            return;
        }

        try {
            // 1. 获取该设备类型的旧token
            String oldToken = getUserDeviceToken(userId, deviceType);

            // 2. 如果存在旧token，删除它（使旧token失效）
            if (oldToken != null && !oldToken.isEmpty()) {
                removeToken(oldToken);
                log.info("同端互斥：已踢掉用户 {} 的 {} 设备旧登录", userId, deviceType);
            }

            // 3. 存储新token到设备映射
            storeUserDeviceToken(userId, deviceType, newToken, expireSeconds);

        } catch (Exception e) {
            log.error("同端互斥处理失败: {}", e.getMessage(), e);
        }
    }
}
