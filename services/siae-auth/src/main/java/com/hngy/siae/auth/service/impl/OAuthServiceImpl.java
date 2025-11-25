package com.hngy.siae.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hngy.siae.auth.dto.response.LoginVO;
import com.hngy.siae.auth.dto.response.OAuthAccountVO;
import com.hngy.siae.auth.entity.OAuthAccount;
import com.hngy.siae.auth.entity.Role;
import com.hngy.siae.auth.entity.UserAuth;
import com.hngy.siae.auth.entity.UserRole;
import com.hngy.siae.auth.feign.UserClient;
import com.hngy.siae.auth.feign.dto.request.UserCreateDTO;
import com.hngy.siae.auth.feign.dto.response.UserVO;
import com.hngy.siae.auth.mapper.OAuthAccountMapper;
import com.hngy.siae.auth.mapper.RoleMapper;
import com.hngy.siae.auth.mapper.UserAuthMapper;
import com.hngy.siae.auth.mapper.UserPermissionMapper;
import com.hngy.siae.auth.mapper.UserRoleMapper;
import com.hngy.siae.auth.service.LogService;
import com.hngy.siae.auth.service.OAuthService;
import com.hngy.siae.auth.service.oauth.GithubOAuthService;
import com.hngy.siae.auth.service.oauth.QQOAuthService;
import com.hngy.siae.auth.service.oauth.GiteeOAuthService;
import com.hngy.siae.auth.util.StateManager;
import com.hngy.siae.core.exception.ServiceException;
import com.hngy.siae.core.result.AuthResultCodeEnum;
import com.hngy.siae.core.utils.JwtUtils;
import com.hngy.siae.security.service.RedisPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * OAuth第三方登录服务实现类
 * 
 * @author SIAE
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OAuthServiceImpl implements OAuthService {
    
    private final OAuthAccountMapper oauthAccountMapper;
    private final QQOAuthService qqOAuthService;
    private final GiteeOAuthService giteeOAuthService;
    private final GithubOAuthService githubOAuthService;
    private final StateManager stateManager;
    private final JwtUtils jwtUtils;
    private final LogService logService;
    private final UserClient userClient;
    private final RoleMapper roleMapper;
    private final UserRoleMapper userRoleMapper;
    private final UserPermissionMapper userPermissionMapper;
    private final UserAuthMapper userAuthMapper;
    private final RedisPermissionService redisPermissionService;
    private final ObjectMapper objectMapper;
    
    @Override
    public String generateAuthUrl(String provider) {
        try {
            // 生成state参数
            String state = stateManager.generateState(provider);
            
            // 根据provider调用对应的第三方服务生成授权URL
            String authUrl = switch (provider.toLowerCase()) {
                case "qq" -> qqOAuthService.generateAuthUrl(state);
                case "gitee" -> giteeOAuthService.generateAuthUrl(state);
                case "github" -> githubOAuthService.generateAuthUrl(state);
                default -> throw new ServiceException(AuthResultCodeEnum.OAUTH_PROVIDER_NOT_SUPPORTED);
            };
            
            log.info("生成第三方授权URL成功: provider={}, state={}", provider, state);
            return authUrl;
            
        } catch (Exception e) {
            log.error("生成第三方授权URL失败: provider={}, error={}", provider, e.getMessage(), e);
            if (e instanceof ServiceException) {
                throw e;
            }
            throw new ServiceException("生成授权URL失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO handleCallback(String provider, String code, String state, 
                                  String clientIp, String browser, String os) {
        try {
            // 1. 验证state参数
            if (!stateManager.validateState(state, provider)) {
                log.warn("State验证失败: provider={}, state={}", provider, state);
                throw new ServiceException(AuthResultCodeEnum.OAUTH_STATE_INVALID);
            }
            
            // 2. 删除已使用的state
            stateManager.removeState(state);
            
            // 3. 换取access_token并获取用户信息
            Map<String, Object> userInfo = getUserInfoFromProvider(provider, code);
            String providerUserId = (String) userInfo.get("provider_user_id");
            String nickname = (String) userInfo.get("nickname");
            String avatar = (String) userInfo.get("avatar");
            String accessToken = (String) userInfo.get("access_token");
            
            // 4. 查询oauth_account表
            OAuthAccount oauthAccount = oauthAccountMapper.selectByProviderAndUserId(provider, providerUserId);
            
            Long userId;
            String username;
            
            if (oauthAccount == null) {
                // 5. 第三方账号未绑定，创建新用户
                UserVO newUser = createUserFromOAuth(nickname, avatar);
                userId = newUser.getId();
                username = newUser.getUsername();
                
                // 6. 创建绑定记录
                oauthAccount = new OAuthAccount();
                oauthAccount.setUserId(userId);
                oauthAccount.setProvider(provider);
                oauthAccount.setProviderUserId(providerUserId);
                oauthAccount.setNickname(nickname);
                oauthAccount.setAvatar(avatar);
                oauthAccount.setAccessToken(accessToken);
                oauthAccount.setRawJson(convertToJson(userInfo));
                oauthAccount.setCreatedAt(LocalDateTime.now());
                oauthAccountMapper.insert(oauthAccount);
                
                log.info("创建新用户并绑定第三方账号: userId={}, provider={}, providerUserId={}", 
                        userId, provider, providerUserId);
            } else {
                // 7. 第三方账号已绑定，直接使用已有用户
                userId = oauthAccount.getUserId();
                
                // 通过userId获取用户信息
                UserVO user = userClient.getUserById(userId);
                if (user == null) {
                    log.error("用户不存在: userId={}", userId);
                    throw new ServiceException(AuthResultCodeEnum.USER_NOT_FOUND);
                }
                
                username = user.getUsername();
                
                if (username == null) {
                    log.error("无法获取用户名: userId={}", userId);
                    throw new ServiceException(AuthResultCodeEnum.USER_NOT_FOUND);
                }
                
                // 更新access_token
                oauthAccount.setAccessToken(accessToken);
                oauthAccount.setRawJson(convertToJson(userInfo));
                oauthAccountMapper.updateById(oauthAccount);
                
                log.info("使用已绑定账号登录: userId={}, provider={}, providerUserId={}", 
                        userId, provider, providerUserId);
            }
            
            // 8. 查询用户权限和角色
            List<String> permissions = userPermissionMapper.selectPermissionCodesByUserId(userId);
            List<String> roles = userRoleMapper.selectRoleCodesByUserId(userId);
            
            // 9. 生成JWT令牌
            String jwtAccessToken = jwtUtils.createAccessToken(userId, username);
            String refreshToken = jwtUtils.createRefreshToken(userId, username);
            Date expirationDate = jwtUtils.getExpirationDate(jwtAccessToken);
            long tokenExpireSeconds = (expirationDate.getTime() - System.currentTimeMillis()) / 1000;
            
            // 10. 缓存用户权限和角色到Redis
            cacheUserPermissionsAndRolesToRedis(userId, permissions, roles, tokenExpireSeconds);
            
            // 11. 保存认证信息到数据库
            UserAuth userAuth = new UserAuth();
            userAuth.setUserId(userId);
            userAuth.setAccessToken(jwtAccessToken);
            userAuth.setRefreshToken(refreshToken);
            userAuth.setTokenType("Bearer");
            userAuth.setExpiresAt(LocalDateTime.ofInstant(expirationDate.toInstant(), ZoneId.systemDefault()));
            userAuthMapper.insert(userAuth);
            
            // 12. 记录登录日志
            logService.saveLoginLogAsync(userId, username, clientIp, browser, os, 1, 
                    provider.toUpperCase() + "登录成功");
            
            // 13. 构建响应
            LoginVO response = new LoginVO();
            response.setUserId(userId);
            response.setUsername(username);
            response.setAccessToken(jwtAccessToken);
            response.setRefreshToken(refreshToken);
            response.setTokenType("Bearer");
            response.setExpiresIn(tokenExpireSeconds);
            
            return response;
            
        } catch (Exception e) {
            log.error("处理第三方登录回调失败: provider={}, error={}", provider, e.getMessage(), e);
            if (e instanceof ServiceException) {
                throw e;
            }
            throw new ServiceException("第三方登录失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindAccount(Long userId, String provider, String code, String state) {
        try {
            // 1. 验证state参数
            if (!stateManager.validateState(state, provider)) {
                log.warn("State验证失败: provider={}, state={}", provider, state);
                throw new ServiceException(AuthResultCodeEnum.OAUTH_STATE_INVALID);
            }
            
            // 2. 删除已使用的state
            stateManager.removeState(state);
            
            // 3. 换取access_token并获取用户信息
            Map<String, Object> userInfo = getUserInfoFromProvider(provider, code);
            String providerUserId = (String) userInfo.get("provider_user_id");
            String nickname = (String) userInfo.get("nickname");
            String avatar = (String) userInfo.get("avatar");
            String accessToken = (String) userInfo.get("access_token");
            
            // 4. 验证第三方账号未被其他用户绑定
            OAuthAccount existingAccount = oauthAccountMapper.selectByProviderAndUserId(provider, providerUserId);
            if (existingAccount != null) {
                log.warn("第三方账号已被绑定: provider={}, providerUserId={}, boundUserId={}", 
                        provider, providerUserId, existingAccount.getUserId());
                throw new ServiceException(AuthResultCodeEnum.OAUTH_ACCOUNT_ALREADY_BOUND);
            }
            
            // 5. 创建绑定记录
            OAuthAccount oauthAccount = new OAuthAccount();
            oauthAccount.setUserId(userId);
            oauthAccount.setProvider(provider);
            oauthAccount.setProviderUserId(providerUserId);
            oauthAccount.setNickname(nickname);
            oauthAccount.setAvatar(avatar);
            oauthAccount.setAccessToken(accessToken);
            oauthAccount.setRawJson(convertToJson(userInfo));
            oauthAccount.setCreatedAt(LocalDateTime.now());
            oauthAccountMapper.insert(oauthAccount);
            
            log.info("绑定第三方账号成功: userId={}, provider={}, providerUserId={}", 
                    userId, provider, providerUserId);
            
        } catch (Exception e) {
            log.error("绑定第三方账号失败: userId={}, provider={}, error={}", 
                    userId, provider, e.getMessage(), e);
            if (e instanceof ServiceException) {
                throw e;
            }
            throw new ServiceException("绑定第三方账号失败: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbindAccount(Long userId, String provider) {
        try {
            // 1. 验证用户至少有一种登录方式
            int accountCount = oauthAccountMapper.countByUserId(userId);
            
            // 检查用户是否有密码登录方式（通过检查user表）
            Boolean hasPasswordLogin = userClient.checkUserIdExists(userId);
            
            // 如果只有一个第三方账号且没有密码登录，不允许解绑
            if (accountCount == 1 && (hasPasswordLogin == null || !hasPasswordLogin)) {
                log.warn("无法解绑最后一个登录方式: userId={}, provider={}", userId, provider);
                throw new ServiceException(AuthResultCodeEnum.OAUTH_UNBIND_LAST_ACCOUNT);
            }
            
            // 2. 查询绑定记录
            OAuthAccount oauthAccount = oauthAccountMapper.selectByUserIdAndProvider(userId, provider);
            if (oauthAccount == null) {
                log.warn("未找到绑定的第三方账号: userId={}, provider={}", userId, provider);
                throw new ServiceException(AuthResultCodeEnum.OAUTH_ACCOUNT_NOT_FOUND);
            }
            
            // 3. 删除绑定记录
            oauthAccountMapper.deleteById(oauthAccount.getId());
            
            log.info("解绑第三方账号成功: userId={}, provider={}", userId, provider);
            
        } catch (Exception e) {
            log.error("解绑第三方账号失败: userId={}, provider={}, error={}", 
                    userId, provider, e.getMessage(), e);
            if (e instanceof ServiceException) {
                throw e;
            }
            throw new ServiceException("解绑第三方账号失败: " + e.getMessage());
        }
    }
    
    @Override
    public List<OAuthAccountVO> getUserAccounts(Long userId) {
        try {
            List<OAuthAccount> accounts = oauthAccountMapper.selectByUserId(userId);
            
            List<OAuthAccountVO> result = new ArrayList<>();
            for (OAuthAccount account : accounts) {
                OAuthAccountVO vo = new OAuthAccountVO();
                vo.setProvider(account.getProvider());
                vo.setNickname(account.getNickname());
                vo.setAvatar(account.getAvatar());
                vo.setCreatedAt(account.getCreatedAt());
                result.add(vo);
            }
            
            log.debug("查询用户绑定账号列表: userId={}, count={}", userId, result.size());
            return result;
            
        } catch (Exception e) {
            log.error("查询用户绑定账号列表失败: userId={}, error={}", userId, e.getMessage(), e);
            throw new ServiceException("查询绑定账号列表失败: " + e.getMessage());
        }
    }
    
    /**
     * 从第三方平台获取用户信息
     */
    private Map<String, Object> getUserInfoFromProvider(String provider, String code) {
        Map<String, Object> result = new HashMap<>();
        
        switch (provider.toLowerCase()) {
            case "qq" -> {
                String accessToken = qqOAuthService.getAccessToken(code);
                String openId = qqOAuthService.getOpenId(accessToken);
                Map<String, Object> userInfo = qqOAuthService.getUserInfo(accessToken, openId);
                
                result.put("provider_user_id", openId);
                result.put("nickname", userInfo.get("nickname"));
                result.put("avatar", userInfo.get("avatar"));
                result.put("access_token", accessToken);
                result.putAll(userInfo);
            }
            case "gitee" -> {
                Map<String, Object> userInfo = giteeOAuthService.getUserInfo(code);
                
                result.put("provider_user_id", userInfo.get("provider_user_id"));
                result.put("nickname", userInfo.get("nickname"));
                result.put("avatar", userInfo.get("avatar"));
                result.put("access_token", userInfo.get("access_token"));
                result.putAll(userInfo);
            }
            case "github" -> {
                String accessToken = githubOAuthService.getAccessToken(code);
                Map<String, Object> userInfo = githubOAuthService.getUserInfo(accessToken);
                
                result.put("provider_user_id", userInfo.get("id"));
                result.put("nickname", userInfo.get("nickname"));
                result.put("avatar", userInfo.get("avatar"));
                result.put("access_token", accessToken);
                result.putAll(userInfo);
            }
            default -> throw new ServiceException(AuthResultCodeEnum.OAUTH_PROVIDER_NOT_SUPPORTED);
        }
        
        return result;
    }
    
    /**
     * 从第三方账号信息创建新用户
     */
    private UserVO createUserFromOAuth(String nickname, String avatar) {
        try {
            // 生成唯一用户名（使用时间戳+随机数）
            String username = "user_" + System.currentTimeMillis() + "_" + 
                    new Random().nextInt(1000);
            
            // 生成随机密码（用户可以后续修改）
            String randomPassword = UUID.randomUUID().toString().substring(0, 16);
            
            UserCreateDTO userDTO = new UserCreateDTO();
            userDTO.setUsername(username);
            userDTO.setPassword(randomPassword);
            userDTO.setNickname(nickname);
            userDTO.setAvatar(avatar);
            userDTO.setStatus(1);
            
            // 调用用户服务创建用户
            UserVO createdUser = userClient.registerUser(userDTO);
            
            if (createdUser == null) {
                throw new ServiceException(AuthResultCodeEnum.USER_CREATION_FAILED);
            }
            
            // 分配默认角色
            assignDefaultRoleToUser(createdUser.getId());
            
            log.info("从第三方账号创建新用户成功: userId={}, username={}", 
                    createdUser.getId(), createdUser.getUsername());
            
            return createdUser;
            
        } catch (Exception e) {
            log.error("从第三方账号创建用户失败: nickname={}, error={}", nickname, e.getMessage(), e);
            throw new ServiceException("创建用户失败: " + e.getMessage());
        }
    }
    
    /**
     * 为新用户分配默认角色
     */
    private void assignDefaultRoleToUser(Long userId) {
        try {
            Role defaultRole = roleMapper.selectOne(
                    new LambdaQueryWrapper<Role>()
                            .eq(Role::getCode, "ROLE_USER")
                            .eq(Role::getStatus, 1)
            );
            
            if (defaultRole == null) {
                log.error("默认角色ROLE_USER不存在，无法为用户分配角色，用户ID: {}", userId);
                return;
            }
            
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(defaultRole.getId());
            userRole.setCreatedAt(LocalDateTime.now());
            userRoleMapper.insert(userRole);
            
            log.info("成功为用户分配默认角色，用户ID: {}, 角色: {}", userId, defaultRole.getCode());
            
        } catch (Exception e) {
            log.error("为用户分配默认角色失败，用户ID: {}", userId, e);
        }
    }
    
    /**
     * 缓存用户权限和角色到Redis
     */
    private void cacheUserPermissionsAndRolesToRedis(Long userId, List<String> permissions, 
                                                     List<String> roles, long expireSeconds) {
        try {
            redisPermissionService.cacheUserPermissions(userId, permissions, expireSeconds, TimeUnit.SECONDS);
            redisPermissionService.cacheUserRoles(userId, roles, expireSeconds, TimeUnit.SECONDS);
            
            log.debug("用户权限和角色已缓存到Redis，用户ID: {}, 权限数量: {}, 角色数量: {}, 过期时间: {}秒",
                    userId, permissions.size(), roles.size(), expireSeconds);
        } catch (Exception e) {
            log.error("缓存用户权限和角色到Redis失败，用户ID: {}", userId, e);
        }
    }
    
    /**
     * 将Map转换为JSON字符串
     */
    private String convertToJson(Map<String, Object> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            log.error("转换JSON失败", e);
            return "{}";
        }
    }
}
