package com.hngy.siae.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hngy.siae.auth.dto.request.LoginDTO;
import com.hngy.siae.auth.entity.Permission;
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
import com.hngy.siae.auth.mapper.PermissionMapper;
import com.hngy.siae.auth.mapper.UserAuthMapper;
import com.hngy.siae.auth.service.AuthService;
import com.hngy.siae.auth.service.RedisPermissionCacheService;
import com.hngy.siae.core.asserts.AssertUtils;
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
import java.util.stream.Collectors;

/**
 * 认证服务实现类
 * 
 * @author KEYKB
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    
    private final UserClient userClient;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder; // <-- 核心修复：将类型改为接口
    private final UserAuthMapper userAuthMapper;
    private final LoginLogMapper loginLogMapper;
    private final PermissionMapper permissionMapper;
    private final RedisPermissionCacheService redisPermissionCacheService;
    
    @Override
    public LoginVO login(LoginDTO loginDTO, String clientIp, String browser, String os) {
        try {
            // 调用用户服务获取用户信息
            UserVO user = userClient.getUserByUsername(loginDTO.getUsername());
            if (user == null) {
                // 记录登录失败日志
                saveLoginLog(null, loginDTO.getUsername(), clientIp, browser, os, 0, "用户不存在");
                throw new UsernameNotFoundException("用户不存在");
            }
            
            // 验证用户状态
            if (user.getStatus() != null && user.getStatus() == 0) {
                saveLoginLog(user.getId(), user.getUsername(), clientIp, browser, os, 0, "用户已禁用");
                throw new ServiceException("用户已禁用");
            }
            
//            // 验证密码
//            if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
//                saveLoginLog(user.getId(), user.getUsername(), clientIp, browser, os, 0, "密码错误");
//                throw new BadCredentialsException("密码错误");
//            }
            
            // 查询用户权限
            List<String> permissions = permissionMapper.selectByUserId(user.getId())
                    .stream()
                    .map(Permission::getCode)
                    .collect(Collectors.toList());

            // 生成优化的JWT令牌（不包含权限信息）
            String accessToken = jwtUtils.createAccessToken(user.getId(), user.getUsername());
            String refreshToken = jwtUtils.createRefreshToken(user.getId(), user.getUsername());
            Date expirationDate = jwtUtils.getExpirationDate(accessToken);

            // 将用户权限缓存到Redis，TTL与JWT过期时间一致
            long tokenExpireSeconds = (expirationDate.getTime() - System.currentTimeMillis()) / 1000;
            cacheUserPermissionsToRedis(user.getId(), permissions, tokenExpireSeconds);
            
            // 保存认证信息到数据库
            UserAuth userAuth = new UserAuth();
            userAuth.setUserId(user.getId());
            userAuth.setAccessToken(accessToken);
            userAuth.setRefreshToken(refreshToken);
            userAuth.setTokenType("Bearer");
            userAuth.setExpiresAt(LocalDateTime.ofInstant(expirationDate.toInstant(), ZoneId.systemDefault()));
            userAuthMapper.insert(userAuth);
            
            // 记录登录成功日志
            saveLoginLog(user.getId(), user.getUsername(), clientIp, browser, os, 1, "登录成功");
            
            // 构建响应
            LoginVO response = new LoginVO();
            response.setUserId(user.getId());
            response.setUsername(user.getUsername());
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setTokenType("Bearer");
            response.setExpiresIn((expirationDate.getTime() - System.currentTimeMillis()) / 1000);
            
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
            
            // 初始化默认权限列表 - 新用户默认只有基本权限
            List<String> permissions = new ArrayList<>();
            permissions.add("user:basic");
            
            // 生成优化的JWT令牌（不包含权限信息）
            String accessToken = jwtUtils.createAccessToken(createdUser.getId(), createdUser.getUsername());
            String refreshToken = jwtUtils.createRefreshToken(createdUser.getId(), createdUser.getUsername());
            Date expirationDate = jwtUtils.getExpirationDate(accessToken);

            // 将用户权限缓存到Redis
            long tokenExpireSeconds = (expirationDate.getTime() - System.currentTimeMillis()) / 1000;
            cacheUserPermissionsToRedis(createdUser.getId(), permissions, tokenExpireSeconds);
            
            // 保存认证信息到数据库
            UserAuth userAuth = new UserAuth();
            userAuth.setUserId(createdUser.getId());
            userAuth.setAccessToken(accessToken);
            userAuth.setRefreshToken(refreshToken);
            userAuth.setTokenType("Bearer");
            userAuth.setExpiresAt(LocalDateTime.ofInstant(expirationDate.toInstant(), ZoneId.systemDefault()));
            userAuthMapper.insert(userAuth);
            
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
        UserAuth userAuth = userAuthMapper.selectOne(
                new LambdaQueryWrapper<UserAuth>()
                        .eq(UserAuth::getUserId, userId)
                        .eq(UserAuth::getRefreshToken, refreshToken)
        );
        
        if (userAuth == null) {
            throw new ServiceException("刷新令牌不存在");
        }
        
        // 查询用户权限
        List<String> permissions = permissionMapper.selectByUserId(userId)
                .stream()
                .map(p -> p.getCode())
                .collect(Collectors.toList());
        System.out.println(permissions.get(0));
        
        // 生成新的优化JWT令牌（不包含权限信息）
        String newAccessToken = jwtUtils.createAccessToken(userId, username);
        String newRefreshToken = jwtUtils.createRefreshToken(userId, username);
        Date expirationDate = jwtUtils.getExpirationDate(newAccessToken);

        // 将用户权限缓存到Redis
        long tokenExpireSeconds = (expirationDate.getTime() - System.currentTimeMillis()) / 1000;
        cacheUserPermissionsToRedis(userId, permissions, tokenExpireSeconds);
        
        // 更新数据库中的认证信息
        userAuth.setAccessToken(newAccessToken);
        userAuth.setRefreshToken(newRefreshToken);
        userAuth.setExpiresAt(LocalDateTime.ofInstant(expirationDate.toInstant(), ZoneId.systemDefault()));
        userAuth.setUpdatedAt(LocalDateTime.now());
        userAuthMapper.updateById(userAuth);
        
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
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        // 验证令牌是否有效
        if (!jwtUtils.validateToken(token)) {
            throw new ServiceException("访问令牌已过期或无效");
        }

        // 获取用户ID用于清除Redis缓存
        Long userId = jwtUtils.getUserId(token);

        // 从数据库中删除认证信息
        userAuthMapper.delete(
                new LambdaQueryWrapper<UserAuth>()
                        .eq(UserAuth::getAccessToken, token)
        );

        // 清除Redis中的用户权限缓存
        if (userId != null) {
            clearUserCacheFromRedis(userId);
        }
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
     * 将用户权限缓存到Redis
     *
     * @param userId 用户ID
     * @param permissions 权限列表
     * @param expireSeconds 过期时间（秒）
     */
    private void cacheUserPermissionsToRedis(Long userId, List<String> permissions, long expireSeconds) {
        try {
            // 缓存用户权限，TTL与JWT过期时间一致
            redisPermissionCacheService.cacheUserPermissions(userId, permissions, expireSeconds, TimeUnit.SECONDS);

            // TODO: 如果需要，也可以缓存用户角色
            // List<String> roles = roleMapper.selectByUserId(userId);
            // redisPermissionCacheService.cacheUserRoles(userId, roles, expireSeconds, TimeUnit.SECONDS);

            log.debug("用户权限已缓存到Redis，用户ID: {}, 权限数量: {}, 过期时间: {}秒",
                     userId, permissions.size(), expireSeconds);
        } catch (Exception e) {
            log.error("缓存用户权限到Redis失败，用户ID: {}", userId, e);
            // 不抛出异常，避免影响登录流程
        }
    }

    /**
     * 清除Redis中的用户权限缓存
     *
     * @param userId 用户ID
     */
    private void clearUserCacheFromRedis(Long userId) {
        try {
            redisPermissionCacheService.clearUserCache(userId);
            log.debug("已清除用户权限缓存，用户ID: {}", userId);
        } catch (Exception e) {
            log.error("清除用户权限缓存失败，用户ID: {}", userId, e);
            // 不抛出异常，避免影响登出流程
        }
    }
}