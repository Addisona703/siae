package com.hngy.siae.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hngy.siae.auth.common.BusinessException;
import com.hngy.siae.auth.dto.LoginRequest;
import com.hngy.siae.auth.dto.LoginResponse;
import com.hngy.siae.auth.dto.TokenRefreshRequest;
import com.hngy.siae.auth.dto.TokenRefreshResponse;
import com.hngy.siae.auth.entity.LoginLog;
import com.hngy.siae.auth.entity.UserAuth;
import com.hngy.siae.auth.feign.UserClient;
import com.hngy.siae.auth.feign.dto.UserDTO;
import com.hngy.siae.auth.mapper.LoginLogMapper;
import com.hngy.siae.auth.mapper.PermissionMapper;
import com.hngy.siae.auth.mapper.UserAuthMapper;
import com.hngy.siae.auth.service.AuthService;
import com.hngy.siae.auth.util.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 认证服务实现类
 * 
 * @author KEYKB
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {
    
    private final UserClient userClient;
    private final JwtUtils jwtUtils;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserAuthMapper userAuthMapper;
    private final LoginLogMapper loginLogMapper;
    private final PermissionMapper permissionMapper;
    
    /**
     * 构造函数
     *
     * @param userClient      用户客户端
     * @param jwtUtils        JWT工具类
     * @param passwordEncoder 密码编码器
     * @param userAuthMapper  用户认证Mapper
     * @param loginLogMapper  登录日志Mapper
     * @param permissionMapper 权限Mapper
     */
    public AuthServiceImpl(UserClient userClient, JwtUtils jwtUtils, BCryptPasswordEncoder passwordEncoder,
                          UserAuthMapper userAuthMapper, LoginLogMapper loginLogMapper, 
                          PermissionMapper permissionMapper) {
        this.userClient = userClient;
        this.jwtUtils = jwtUtils;
        this.passwordEncoder = passwordEncoder;
        this.userAuthMapper = userAuthMapper;
        this.loginLogMapper = loginLogMapper;
        this.permissionMapper = permissionMapper;
    }
    
    @Override
    public LoginResponse login(LoginRequest request, String clientIp, String browser, String os) {
        try {
            // 调用用户服务获取用户信息
            UserDTO user = userClient.getUserByUsername(request.getUsername()).getData();
            if (user == null) {
                // 记录登录失败日志
                saveLoginLog(null, request.getUsername(), clientIp, browser, os, 0, "用户不存在");
                throw new UsernameNotFoundException("用户不存在");
            }
            
            // 验证用户状态
            if (user.getStatus() != null && user.getStatus() == 0) {
                saveLoginLog(user.getId(), user.getUsername(), clientIp, browser, os, 0, "用户已禁用");
                throw new BusinessException("用户已禁用");
            }
            
            // 验证密码
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                saveLoginLog(user.getId(), user.getUsername(), clientIp, browser, os, 0, "密码错误");
                throw new BadCredentialsException("密码错误");
            }
            
            // 查询用户权限
            List<String> permissions = permissionMapper.selectByUserId(user.getId())
                    .stream()
                    .map(p -> p.getCode())
                    .collect(Collectors.toList());
            
            // 生成令牌
            String accessToken = jwtUtils.createAccessToken(user.getId(), user.getUsername(), permissions);
            String refreshToken = jwtUtils.createRefreshToken(user.getId(), user.getUsername());
            Date expirationDate = jwtUtils.getExpirationDate(accessToken);
            
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
            LoginResponse response = new LoginResponse();
            response.setUserId(user.getId());
            response.setUsername(user.getUsername());
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setTokenType("Bearer");
            response.setExpiresIn((expirationDate.getTime() - System.currentTimeMillis()) / 1000);
            
            return response;
        } catch (Exception e) {
            log.error("登录异常", e);
            if (e instanceof BusinessException || e instanceof UsernameNotFoundException || e instanceof BadCredentialsException) {
                throw e;
            }
            throw new BusinessException("登录失败: " + e.getMessage());
        }
    }
    
    @Override
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        String refreshToken = request.getRefreshToken();
        
        // 验证刷新令牌是否有效
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new BusinessException("刷新令牌已过期或无效");
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
            throw new BusinessException("刷新令牌不存在");
        }
        
        // 查询用户权限
        List<String> permissions = permissionMapper.selectByUserId(userId)
                .stream()
                .map(p -> p.getCode())
                .collect(Collectors.toList());
        
        // 生成新的令牌
        String newAccessToken = jwtUtils.createAccessToken(userId, username, permissions);
        String newRefreshToken = jwtUtils.createRefreshToken(userId, username);
        Date expirationDate = jwtUtils.getExpirationDate(newAccessToken);
        
        // 更新数据库中的认证信息
        userAuth.setAccessToken(newAccessToken);
        userAuth.setRefreshToken(newRefreshToken);
        userAuth.setExpiresAt(LocalDateTime.ofInstant(expirationDate.toInstant(), ZoneId.systemDefault()));
        userAuth.setUpdatedAt(LocalDateTime.now());
        userAuthMapper.updateById(userAuth);
        
        // 构建响应
        TokenRefreshResponse response = new TokenRefreshResponse();
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
            throw new BusinessException("访问令牌已过期或无效");
        }
        
        // 从数据库中删除认证信息
        userAuthMapper.delete(
                new LambdaQueryWrapper<UserAuth>()
                        .eq(UserAuth::getAccessToken, token)
        );
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
} 