package com.hngy.siae.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.auth.dto.request.LoginDTO;
import com.hngy.siae.auth.feign.dto.request.RegisterDTO;
import com.hngy.siae.auth.dto.response.LoginVO;
import com.hngy.siae.auth.feign.dto.response.RegisterVO;
import com.hngy.siae.auth.dto.request.TokenRefreshDTO;
import com.hngy.siae.auth.dto.response.TokenRefreshVO;
import com.hngy.siae.auth.entity.LoginLog;
import com.hngy.siae.auth.entity.UserAuth;
import com.hngy.siae.auth.feign.UserClient;
import com.hngy.siae.auth.feign.dto.request.UserDTO;
import com.hngy.siae.auth.feign.dto.response.UserVO;
import com.hngy.siae.auth.mapper.LoginLogMapper;
import com.hngy.siae.auth.mapper.UserAuthMapper;
import com.hngy.siae.auth.mapper.UserPermissionMapper;
import com.hngy.siae.auth.mapper.UserRoleMapper;
import com.hngy.siae.auth.service.AuthService;
import com.hngy.siae.auth.service.RedisPermissionCacheService;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.AuthResultCodeEnum;
import com.hngy.siae.core.utils.JwtUtils;
import com.hngy.siae.core.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 * 
 * @author KEYKB
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl
        extends ServiceImpl<UserAuthMapper, UserAuth>
        implements AuthService {
    
    private final UserClient userClient;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final LoginLogMapper loginLogMapper;
    private final UserPermissionMapper userPermissionMapper;
    private final UserRoleMapper userRoleMapper;
    private final RedisPermissionCacheService redisPermissionCacheService;
    
    @Override
    public LoginVO login(LoginDTO loginDTO, String clientIp, String browser, String os) {
        try {
            // 1. 远程调用userClient接口获取用户信息
            UserVO user = userClient.getUserByUsername(loginDTO.getUsername());

            // 验证用户存在性并记录失败日志
            assertUserExists(user, loginDTO.getUsername(), clientIp, browser, os);

            // 2. 验证用户状态并记录失败日志
            assertUserEnabled(user, clientIp, browser, os);

            // 3. 验证密码并记录失败日志
            assertPasswordMatches(loginDTO.getPassword(), user, clientIp, browser, os);

            // 4. 查询用户权限（使用JOIN查询直接获取权限代码）
            List<String> permissions = userPermissionMapper.selectPermissionCodesByUserId(user.getId());

            // 5. 查询用户角色（使用JOIN查询直接获取角色代码）
            List<String> roles = userRoleMapper.selectRoleCodesByUserId(user.getId());

            // 6. 生成优化的JWT令牌（不包含权限信息）
            String accessToken = jwtUtils.createAccessToken(user.getId(), user.getUsername());
            String refreshToken = jwtUtils.createRefreshToken(user.getId(), user.getUsername());
            Date expirationDate = jwtUtils.getExpirationDate(accessToken);
            long currentTime = System.currentTimeMillis();
            long tokenExpireSeconds = (expirationDate.getTime() - currentTime) / 1000;

            // 7. 将用户权限和角色分别缓存到Redis，TTL与JWT过期时间一致
            cacheUserPermissionsAndRolesToRedis(user.getId(), permissions, roles, tokenExpireSeconds);

            // 8. 保存认证信息到数据库
            UserAuth userAuth = new UserAuth();
            userAuth.setUserId(user.getId());
            userAuth.setAccessToken(accessToken);
            userAuth.setRefreshToken(refreshToken);
            userAuth.setTokenType("Bearer");
            userAuth.setExpiresAt(LocalDateTime.ofInstant(expirationDate.toInstant(), ZoneId.systemDefault()));
            save(userAuth);

            // 9. 记录登录成功日志
            saveLoginLog(user.getId(), user.getUsername(), clientIp, browser, os, 1, "登录成功");

            // 10. 构建响应
            LoginVO response = new LoginVO();
            response.setUserId(user.getId());
            response.setUsername(user.getUsername());
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setTokenType("Bearer");
            response.setExpiresIn(tokenExpireSeconds);

            return response;
        } catch (Exception e) {
            log.error("登录异常", e);
            if (e instanceof ServiceException || e instanceof UsernameNotFoundException || e instanceof BadCredentialsException) {
                throw e;
            }
            throw new ServiceException("登录失败: " + e.getMessage());
        }
    }
    
    @Override
    public RegisterVO register(RegisterDTO request, String clientIp, String browser, String os) {
        try {
            // 验证两次密码是否一致
            AssertUtils.isTrue(request.getPassword().equals(request.getConfirmPassword()), "两次输入的密码不一致");
            
            // 检查用户名是否已存在
            UserVO existingUser = userClient.getUserByUsername(request.getUsername());
            AssertUtils.isNull(existingUser, "用户名已被使用");
            
            // 构建用户DTO
            UserDTO userDTO = new UserDTO();
            userDTO.setUsername(request.getUsername());
            userDTO.setPassword(passwordEncoder.encode(request.getPassword()));
            userDTO.setEmail(request.getEmail());
            userDTO.setPhone(request.getPhone());
            userDTO.setNickname(request.getNickname());
            userDTO.setStatus(1); // 默认启用
            
            // 调用用户服务创建用户
            UserVO createdUser = userClient.createUser(userDTO);
            AssertUtils.notNull(createdUser, "用户创建失败");
            
            // 初始化默认权限和角色列表 - 新用户默认只有基本权限
            List<String> permissions = new ArrayList<>();
            permissions.add("user:basic");
            List<String> roles = new ArrayList<>(); // 新用户默认无角色

            // 生成优化的JWT令牌（不包含权限信息）
            String accessToken = jwtUtils.createAccessToken(createdUser.getId(), createdUser.getUsername());
            String refreshToken = jwtUtils.createRefreshToken(createdUser.getId(), createdUser.getUsername());
            Date expirationDate = jwtUtils.getExpirationDate(accessToken);

            // 将用户权限和角色缓存到Redis
            long tokenExpireSeconds = (expirationDate.getTime() - System.currentTimeMillis()) / 1000;
            cacheUserPermissionsAndRolesToRedis(createdUser.getId(), permissions, roles, tokenExpireSeconds);
            
            // 保存认证信息到数据库
            UserAuth userAuth = new UserAuth();
            userAuth.setUserId(createdUser.getId());
            userAuth.setAccessToken(accessToken);
            userAuth.setRefreshToken(refreshToken);
            userAuth.setTokenType("Bearer");
            userAuth.setExpiresAt(LocalDateTime.ofInstant(expirationDate.toInstant(), ZoneId.systemDefault()));
            save(userAuth); // 使用MyBatis-Plus的save方法
            
            // 记录登录成功日志
            saveLoginLog(createdUser.getId(), createdUser.getUsername(), clientIp, browser, os, 1, "注册成功并登录");
            
            // 构建响应
            RegisterVO response = new RegisterVO();
            response.setUserId(createdUser.getId());
            response.setUsername(createdUser.getUsername());
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setTokenType("Bearer");
            response.setExpiresIn((expirationDate.getTime() - System.currentTimeMillis()) / 1000);
            
            return response;
        } catch (Exception e) {
            log.error("注册异常", e);
            if (e instanceof ServiceException) {
                throw e;
            }
            throw new ServiceException("注册失败: " + e.getMessage());
        }
    }
    
    @Override
    public TokenRefreshVO refreshToken(TokenRefreshDTO request) {
        String refreshToken = request.getRefreshToken();
        
        // 验证刷新令牌是否有效
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new ServiceException("刷新令牌已过期或无效");
        }
        
        // 从刷新令牌中获取用户ID和用户名
        Long userId = jwtUtils.getUserId(refreshToken);
        String username = jwtUtils.getUsername(refreshToken);
        
        // 查询数据库中的认证信息
        UserAuth userAuth = getOne(
                new LambdaQueryWrapper<UserAuth>()
                        .eq(UserAuth::getUserId, userId)
                        .eq(UserAuth::getRefreshToken, refreshToken)
        );
        
        if (userAuth == null) {
            throw new ServiceException("刷新令牌不存在");
        }
        
        // 查询用户权限和角色
        List<String> permissions = userPermissionMapper.selectPermissionCodesByUserId(userId);
        List<String> roles = userRoleMapper.selectRoleCodesByUserId(userId);
        
        // 生成新的优化JWT令牌（不包含权限信息）
        String newAccessToken = jwtUtils.createAccessToken(userId, username);
        String newRefreshToken = jwtUtils.createRefreshToken(userId, username);
        Date expirationDate = jwtUtils.getExpirationDate(newAccessToken);

        // 将用户权限和角色缓存到Redis
        long tokenExpireSeconds = (expirationDate.getTime() - System.currentTimeMillis()) / 1000;
        cacheUserPermissionsAndRolesToRedis(userId, permissions, roles, tokenExpireSeconds);
        
        // 更新数据库中的认证信息
        userAuth.setAccessToken(newAccessToken);
        userAuth.setRefreshToken(newRefreshToken);
        userAuth.setExpiresAt(LocalDateTime.ofInstant(expirationDate.toInstant(), ZoneId.systemDefault()));
        userAuth.setUpdatedAt(LocalDateTime.now());
        updateById(userAuth); // 使用MyBatis-Plus的updateById方法
        
        // 构建响应
        TokenRefreshVO response = new TokenRefreshVO();
        response.setAccessToken(newAccessToken);
        response.setRefreshToken(newRefreshToken);
        response.setTokenType("Bearer");
        response.setExpiresIn((expirationDate.getTime() - System.currentTimeMillis()) / 1000);
        
        return response;
    }
    
    @Override
    public void logout(String token) {
        // 1. 验证token参数不为空
        AssertUtils.notEmpty(token, AuthResultCodeEnum.TOKEN_INVALID);

        // 2. 处理Bearer前缀
        String actualToken = token;
        if (token.startsWith("Bearer ")) {
            actualToken = token.substring(7);
        }

        // 3. 验证token格式不为空
        AssertUtils.notEmpty(actualToken, AuthResultCodeEnum.TOKEN_INVALID);

        // 4. 验证JWT令牌有效性
        AssertUtils.isTrue(jwtUtils.validateToken(actualToken), AuthResultCodeEnum.TOKEN_EXPIRED);

        // 5. 获取用户ID用于清除Redis缓存
        Long userId = jwtUtils.getUserId(actualToken);
        AssertUtils.notNull(userId, AuthResultCodeEnum.TOKEN_INVALID);
        // 6. 从数据库中删除认证信息
        remove(
                new LambdaQueryWrapper<UserAuth>()
                        .eq(UserAuth::getAccessToken, actualToken)
        );

        // 7. 清除Redis中的用户权限和角色缓存
        clearUserCacheFromRedis(userId);

        log.info("用户登出成功，用户ID: {}", userId);
    }
    
    /**
     * 保存登录日志
     */
    @Async
    protected void saveLoginLog(Long userId, String username, String loginIp, String browser, String os, Integer status, String msg) {
        try {
            LoginLog loginLog = new LoginLog();
            loginLog.setUserId(userId);
            loginLog.setUsername(username);
            loginLog.setLoginIp(loginIp);
            loginLog.setBrowser(browser);
            loginLog.setOs(os);
            loginLog.setStatus(status);
            loginLog.setMsg(msg);
            loginLog.setLoginTime(LocalDateTime.now());
            loginLogMapper.insert(loginLog);
        } catch (Exception e) {
            log.error("保存登录日志失败", e);
        }
    }

    /**
     * 将用户权限和角色分别缓存到Redis
     *
     * @param userId 用户ID
     * @param permissions 权限列表
     * @param roles 角色列表
     * @param expireSeconds 过期时间（秒）
     */
    private void cacheUserPermissionsAndRolesToRedis(Long userId, List<String> permissions, List<String> roles, long expireSeconds) {
        try {
            // 分别缓存用户权限和角色，TTL与JWT过期时间一致
            redisPermissionCacheService.cacheUserPermissions(userId, permissions, expireSeconds, TimeUnit.SECONDS);
            redisPermissionCacheService.cacheUserRoles(userId, roles, expireSeconds, TimeUnit.SECONDS);

            log.debug("用户权限和角色已缓存到Redis，用户ID: {}, 权限数量: {}, 角色数量: {}, 过期时间: {}秒",
                     userId, permissions.size(), roles.size(), expireSeconds);
        } catch (Exception e) {
            log.error("缓存用户权限和角色到Redis失败，用户ID: {}", userId, e);
            // 不抛出异常，避免影响登录流程
        }
    }

    /**
     * 清除Redis中的用户权限和角色缓存
     *
     * @param userId 用户ID
     */
    private void clearUserCacheFromRedis(Long userId) {
        // 分别清除权限和角色缓存，确保即使一个失败也不影响另一个
        boolean permissionCleared = false;
        boolean roleCleared = false;

        // 清除用户权限缓存
        try {
            redisPermissionCacheService.clearUserPermissions(userId);
            permissionCleared = true;
            log.debug("已清除用户权限缓存，用户ID: {}", userId);
        } catch (Exception e) {
            log.error("清除用户权限缓存失败，用户ID: {}", userId, e);
        }

        // 清除用户角色缓存
        try {
            redisPermissionCacheService.clearUserRoles(userId);
            roleCleared = true;
            log.debug("已清除用户角色缓存，用户ID: {}", userId);
        } catch (Exception e) {
            log.error("清除用户角色缓存失败，用户ID: {}", userId, e);
        }

        // 记录清除结果
        if (permissionCleared && roleCleared) {
            log.info("用户权限和角色缓存清除成功，用户ID: {}", userId);
        } else if (permissionCleared) {
            log.warn("仅权限缓存清除成功，角色缓存清除失败，用户ID: {}", userId);
        } else if (roleCleared) {
            log.warn("仅角色缓存清除成功，权限缓存清除失败，用户ID: {}", userId);
        } else {
            log.error("权限和角色缓存清除均失败，用户ID: {}", userId);
        }
    }

    /**
     * 断言用户存在，如果不存在则记录日志并抛出异常
     */
    private void assertUserExists(UserVO user, String username, String clientIp, String browser, String os) {
        if (user == null) {
            saveLoginLog(null, username, clientIp, browser, os, 0, AuthResultCodeEnum.USER_NOT_FOUND.getMessage());
            throw new UsernameNotFoundException(AuthResultCodeEnum.USER_NOT_FOUND.getMessage());
        }
    }

    /**
     * 断言用户已启用，如果被禁用则记录日志并抛出异常
     */
    private void assertUserEnabled(UserVO user, String clientIp, String browser, String os) {
        if (user.getStatus() != null && user.getStatus() == 0) {
            saveLoginLog(user.getId(), user.getUsername(), clientIp, browser, os, 0, AuthResultCodeEnum.ACCOUNT_DISABLED.getMessage());
            throw new ServiceException(AuthResultCodeEnum.ACCOUNT_DISABLED);
        }
    }

    /**
     * 断言密码匹配，如果不匹配则记录日志并抛出异常
     */
    private void assertPasswordMatches(String inputPassword, UserVO user, String clientIp, String browser, String os) {
        if (!passwordEncoder.matches(inputPassword, user.getPassword())) {
            saveLoginLog(user.getId(), user.getUsername(), clientIp, browser, os, 0, AuthResultCodeEnum.PASSWORD_ERROR.getMessage());
            throw new BadCredentialsException(AuthResultCodeEnum.PASSWORD_ERROR.getMessage());
        }
    }
}