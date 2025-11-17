package com.hngy.siae.auth.util;

import com.hngy.siae.auth.config.OAuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * OAuth State参数管理工具类
 * 用于生成、验证和删除state参数，防止CSRF攻击
 * 
 * @author KEYKB
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StateManager {
    
    private static final String STATE_KEY_PREFIX = "oauth:state:";
    
    private final StringRedisTemplate stringRedisTemplate;
    private final OAuthProperties oAuthProperties;
    
    /**
     * 生成state参数并存储到Redis
     * 
     * @param provider 第三方平台标识（qq/wx/github）
     * @return 生成的state参数
     */
    public String generateState(String provider) {
        String state = UUID.randomUUID().toString().replace("-", "");
        String key = STATE_KEY_PREFIX + state;
        
        int expireSeconds = oAuthProperties.getState() != null 
            ? oAuthProperties.getState().getExpireSeconds() 
            : 300;
        
        stringRedisTemplate.opsForValue().set(key, provider, expireSeconds, TimeUnit.SECONDS);
        
        log.debug("生成OAuth state参数: state={}, provider={}, ttl={}秒", state, provider, expireSeconds);
        
        return state;
    }
    
    /**
     * 验证state参数的有效性
     * 
     * @param state state参数
     * @param provider 期望的第三方平台标识
     * @return 验证是否通过
     */
    public boolean validateState(String state, String provider) {
        if (state == null || provider == null) {
            log.warn("State验证失败: state或provider为空");
            return false;
        }
        
        String key = STATE_KEY_PREFIX + state;
        String storedProvider = stringRedisTemplate.opsForValue().get(key);
        
        if (storedProvider == null) {
            log.warn("State验证失败: state不存在或已过期, state={}", state);
            return false;
        }
        
        if (!storedProvider.equals(provider)) {
            log.warn("State验证失败: provider不匹配, expected={}, actual={}", storedProvider, provider);
            return false;
        }
        
        log.debug("State验证成功: state={}, provider={}", state, provider);
        return true;
    }
    
    /**
     * 删除state参数
     * 
     * @param state state参数
     */
    public void removeState(String state) {
        if (state == null) {
            return;
        }
        
        String key = STATE_KEY_PREFIX + state;
        Boolean deleted = stringRedisTemplate.delete(key);
        
        log.debug("删除OAuth state参数: state={}, deleted={}", state, deleted);
    }
}
