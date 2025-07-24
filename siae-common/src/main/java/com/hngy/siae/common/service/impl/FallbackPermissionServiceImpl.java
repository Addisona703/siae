package com.hngy.siae.common.service.impl;

import com.hngy.siae.common.service.RedisPermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 权限服务的回退实现
 * 
 * 当Redis不可用时使用此实现，返回空权限列表
 * 这样可以保证系统在Redis不可用时仍能正常运行
 * 
 * @author KEYKB
 */
@Slf4j
@Service
@ConditionalOnMissingClass("org.springframework.data.redis.core.StringRedisTemplate")
public class FallbackPermissionServiceImpl implements RedisPermissionService {
    
    @Override
    public List<String> getUserPermissions(Long userId) {
        log.warn("Redis不可用，返回空权限列表，用户ID: {}", userId);
        return Collections.emptyList();
    }
    
    @Override
    public List<String> getUserRoles(Long userId) {
        log.warn("Redis不可用，返回空角色列表，用户ID: {}", userId);
        return Collections.emptyList();
    }
    
    @Override
    public List<String> getAllUserAuthorities(Long userId) {
        log.warn("Redis不可用，返回空权限列表，用户ID: {}", userId);
        return Collections.emptyList();
    }
}
