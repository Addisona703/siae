package com.hngy.siae.common.service.impl;

import com.hngy.siae.common.service.RedisPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Redis权限服务实现类
 * 
 * 从Redis缓存中获取用户权限和角色信息
 * 只有在Redis相关类存在时才会被创建
 * 
 * @author KEYKB
 */
@Slf4j
@Service
@ConditionalOnClass(StringRedisTemplate.class)
public class RedisPermissionServiceImpl implements RedisPermissionService {
    
    private final StringRedisTemplate redisTemplate;
    
    /**
     * Redis键前缀常量
     */
    private static final String PERMISSION_KEY_PREFIX = "auth:perms:";
    private static final String ROLE_KEY_PREFIX = "auth:roles:";
    private static final String DELIMITER = ",";
    
    public RedisPermissionServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    @Override
    public List<String> getUserPermissions(Long userId) {
        try {
            String key = PERMISSION_KEY_PREFIX + userId;
            String value = redisTemplate.opsForValue().get(key);
            
            if (value == null || value.isEmpty()) {
                log.debug("用户权限缓存不存在或为空，用户ID: {}", userId);
                return Collections.emptyList();
            }
            
            List<String> permissions = Arrays.asList(value.split(DELIMITER));
            log.debug("从缓存获取用户权限成功，用户ID: {}, 权限数量: {}", userId, permissions.size());
            return permissions;
        } catch (Exception e) {
            log.error("从缓存获取用户权限失败，用户ID: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<String> getUserRoles(Long userId) {
        try {
            String key = ROLE_KEY_PREFIX + userId;
            String value = redisTemplate.opsForValue().get(key);
            
            if (value == null || value.isEmpty()) {
                log.debug("用户角色缓存不存在或为空，用户ID: {}", userId);
                return Collections.emptyList();
            }
            
            List<String> roles = Arrays.asList(value.split(DELIMITER));
            log.debug("从缓存获取用户角色成功，用户ID: {}, 角色数量: {}", userId, roles.size());
            return roles;
        } catch (Exception e) {
            log.error("从缓存获取用户角色失败，用户ID: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public List<String> getAllUserAuthorities(Long userId) {
        try {
            // 获取用户权限
            List<String> permissions = getUserPermissions(userId);
            
            // 获取用户角色（添加ROLE_前缀以符合Spring Security规范）
            List<String> roles = getUserRoles(userId).stream()
                    .map(role -> role.startsWith("ROLE_") ? role : "ROLE_" + role)
                    .collect(Collectors.toList());
            
            // 合并权限和角色
            List<String> allAuthorities = Stream.concat(permissions.stream(), roles.stream())
                    .distinct()
                    .collect(Collectors.toList());
            
            log.debug("获取用户所有权限成功，用户ID: {}, 权限总数: {}", userId, allAuthorities.size());
            return allAuthorities;
        } catch (Exception e) {
            log.error("获取用户所有权限失败，用户ID: {}", userId, e);
            return Collections.emptyList();
        }
    }
}
