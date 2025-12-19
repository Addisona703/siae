package com.hngy.siae.auth.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.api.user.client.UserFeignClient;
import com.hngy.siae.api.user.dto.request.UserCreateDTO;
import com.hngy.siae.api.user.dto.response.UserLoginVO;
import com.hngy.siae.api.user.dto.response.UserVO;
import com.hngy.siae.auth.dto.request.ChangePasswordDTO;
import com.hngy.siae.auth.dto.request.LoginDTO;
import com.hngy.siae.auth.dto.request.RegisterDTO;
import com.hngy.siae.auth.dto.request.TokenRefreshDTO;
import com.hngy.siae.auth.dto.response.CurrentUserVO;
import com.hngy.siae.auth.dto.response.LoginVO;
import com.hngy.siae.auth.dto.response.RegisterVO;
import com.hngy.siae.auth.dto.response.TokenRefreshVO;
import com.hngy.siae.auth.entity.Role;
import com.hngy.siae.auth.entity.UserAuth;
import com.hngy.siae.auth.entity.UserRole;

import com.hngy.siae.auth.mapper.RoleMapper;
import com.hngy.siae.auth.mapper.UserAuthMapper;
import com.hngy.siae.auth.mapper.UserPermissionMapper;
import com.hngy.siae.auth.mapper.UserRoleMapper;
import com.hngy.siae.auth.service.LogService;
import com.hngy.siae.auth.service.AuthService;
import com.hngy.siae.security.service.SecurityCacheService;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.AuthResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.core.exception.ServiceException;
import com.hngy.siae.core.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 * <p>
 * 提供用户登录、注册、令牌刷新和登出等认证功能，
 * 负责JWT令牌管理和权限缓存处理。
 *
 * @author KEYKB
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl
        extends ServiceImpl<UserAuthMapper, UserAuth>
        implements AuthService {

    // TODO: 后面抽成外观模式
    private final UserFeignClient userClient;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final LogService logService;
    private final RoleMapper roleMapper;
    private final UserPermissionMapper userPermissionMapper;
    private final UserRoleMapper userRoleMapper;
    private final SecurityCacheService securityCacheService;
    
    /**
     * 用户登录认证
     * <p>
     * 验证用户身份并生成JWT令牌，同时缓存权限信息到Redis。
     *
     * @param loginDTO 登录请求参数
     * @param clientIp 客户端IP地址
     * @param browser 浏览器信息
     * @param os 操作系统信息
     * @return 登录响应信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO login(LoginDTO loginDTO, String clientIp, String browser, String os) {
        try {
            // 远程调用userClient接口获取用户信息
            UserLoginVO user = userClient.getUserByUsername(loginDTO.getUsername());
            
            // 验证用户存在性/用户状态/验证密码并记录失败日志
            assertUserExists(user, loginDTO.getUsername(), clientIp, browser, os);
            assertUserEnabled(user, clientIp, browser, os);
            assertPasswordMatches(loginDTO.getPassword(), user, clientIp, browser, os);

            // 查询用户权限和角色
            List<String> permissions = userPermissionMapper.selectPermissionCodesByUserId(user.getId());
            List<String> roles = userRoleMapper.selectRoleCodesByUserId(user.getId());

            // 生成优化的JWT令牌（不包含权限信息）
            String accessToken = jwtUtils.createAccessToken(user.getId(), user.getUsername());
            String refreshToken = jwtUtils.createRefreshToken(user.getId(), user.getUsername());
            Date expirationDate = jwtUtils.getExpirationDate(accessToken);
            long tokenExpireSeconds = (expirationDate.getTime() - System.currentTimeMillis()) / 1000;

            // TODO:后续需要邮箱验证码确认用户身份，消息队列

            // 7. 将用户权限和角色分别缓存到Redis，TTL与JWT过期时间一致
            log.info("【权限缓存调试】登录时缓存权限，用户ID: {}, 过期时间: {}秒 (约{}小时)", 
                    user.getId(), tokenExpireSeconds, tokenExpireSeconds / 3600);
            cacheUserPermissionsAndRolesToRedis(user.getId(), permissions, roles, tokenExpireSeconds);

            // 8. 同端互斥：踢掉同类型设备的旧登录
            String deviceType = loginDTO.getDeviceType() != null ? loginDTO.getDeviceType() : "web";
            log.info("【登录】用户 {} 使用设备类型: {} 登录", user.getUsername(), deviceType);
            
            // 8.1 清理该用户该设备类型的旧token（从Redis和数据库）
            cleanupOldTokensForDevice(user.getId(), deviceType);
            
            // 8.2 存储新的设备token映射
            securityCacheService.storeUserDeviceToken(user.getId(), deviceType, accessToken, tokenExpireSeconds);
            
            // 9. 将token存储到Redis，实现统一的token验证机制
            storeTokenToRedis(accessToken, user, tokenExpireSeconds);
            storeTokenToRedis(refreshToken, user, tokenExpireSeconds);

            // 10. 保存认证信息到数据库
            UserAuth userAuth = new UserAuth();
            userAuth.setUserId(user.getId());
            userAuth.setDeviceType(deviceType);
            userAuth.setAccessToken(accessToken);
            userAuth.setRefreshToken(refreshToken);
            userAuth.setTokenType("Bearer");
            userAuth.setExpiresAt(LocalDateTime.ofInstant(expirationDate.toInstant(), ZoneId.systemDefault()));
            save(userAuth);

            // 10. 记录登录成功日志
            logService.saveLoginLogAsync(user.getId(), user.getUsername(), clientIp, browser, os, 1, "登录成功");

            // 11. 构建响应
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
            AssertUtils.fail("登录失败: " + e.getMessage());
            return null; // 永远不会执行到这里
        }
    }
    
    /**
     * 用户注册
     * <p>
     * 创建新用户账户并分配默认角色，生成JWT令牌。
     *
     * @param registerDTO 注册请求参数
     * @param clientIp 客户端IP地址
     * @param browser 浏览器信息
     * @param os 操作系统信息
     * @return 注册响应信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public RegisterVO register(RegisterDTO registerDTO, String clientIp, String browser, String os) {
        try {
            // 1. 验证两次密码一致
            AssertUtils.isTrue(registerDTO.getPassword().equals(registerDTO.getConfirmPassword()),
                    AuthResultCodeEnum.PASSWORD_MISMATCH);

            // 2. 检查用户名是否已存在，user服务插入时已经验证
            // 3. 构建用户DTO，使用BeanConvertUtil转换
            UserCreateDTO userDTO = BeanConvertUtil.to(registerDTO, UserCreateDTO.class);
            userDTO.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
            userDTO.setStatus(1); // 默认启用
            
            // 4. 调用用户服务创建用户
            UserVO createdUser = userClient.register(userDTO);
            AssertUtils.notNull(createdUser, AuthResultCodeEnum.USER_CREATION_FAILED);

            // 5. 分配默认角色到数据库
            assignDefaultRoleToUser(createdUser.getId());

            // 6. 初始化默认权限和角色列表 - 新用户默认只有基本权限
            List<String> permissions = new ArrayList<>();
            permissions.add("user:basic");
            List<String> roles = new ArrayList<>();
            roles.add("ROLE_USER"); // 新用户默认角色为普通用户

            // 7. 生成优化的JWT令牌（不包含权限信息）
            String accessToken = jwtUtils.createAccessToken(createdUser.getId(), createdUser.getUsername());
            String refreshToken = jwtUtils.createRefreshToken(createdUser.getId(), createdUser.getUsername());
            Date expirationDate = jwtUtils.getExpirationDate(accessToken);
            long currentTimeMillis  = System.currentTimeMillis();
            // 计算剩余过期时间（向上取整，避免0秒）
            long tokenExpireSeconds = (expirationDate.getTime() - currentTimeMillis + 999) / 1000;

            // 8. 将用户权限和角色缓存到Redis
            cacheUserPermissionsAndRolesToRedis(createdUser.getId(), permissions, roles, tokenExpireSeconds);

            // 9. 保存认证信息到数据库（注册默认为web设备）
            UserAuth userAuth = new UserAuth();
            userAuth.setUserId(createdUser.getId());
            userAuth.setDeviceType("web");
            userAuth.setAccessToken(accessToken);
            userAuth.setRefreshToken(refreshToken);
            userAuth.setTokenType("Bearer");
            userAuth.setExpiresAt(LocalDateTime.ofInstant(expirationDate.toInstant(), ZoneId.systemDefault()));
            save(userAuth);
            
            // 存储设备token映射
            securityCacheService.storeUserDeviceToken(createdUser.getId(), "web", accessToken, tokenExpireSeconds);
            
            // 存储token到Redis
            storeTokenToRedis(accessToken, BeanConvertUtil.to(createdUser, UserLoginVO.class), tokenExpireSeconds);
            storeTokenToRedis(refreshToken, BeanConvertUtil.to(createdUser, UserLoginVO.class), tokenExpireSeconds);

            // 10. 记录注册成功日志
            logService.saveLoginLogAsync(createdUser.getId(), createdUser.getUsername(), clientIp, browser, os, 1, "注册成功");

            // 构建响应
            RegisterVO response = new RegisterVO();
            response.setUserId(createdUser.getId());
            response.setUsername(createdUser.getUsername());
            response.setAccessToken(accessToken);
            response.setRefreshToken(refreshToken);
            response.setTokenType("Bearer");
            response.setExpiresIn(tokenExpireSeconds);

            return response;
        } catch (Exception e) {
            log.error("注册异常", e);
            if (e instanceof ServiceException) {
                throw e;
            }
            AssertUtils.fail(AuthResultCodeEnum.REGISTER_FAILED);
            return null; // 永远不会执行到这里
        }
    }
    
    /**
     * 刷新JWT令牌
     * <p>
     * 使用刷新令牌获取新的访问令牌，同时更新权限缓存。
     *
     * @param request 令牌刷新请求
     * @return 新的令牌信息
     */
    @Override
    public TokenRefreshVO refreshToken(TokenRefreshDTO request) {
        String refreshToken = request.getRefreshToken();
        
        // 验证刷新令牌是否有效
        AssertUtils.isTrue(jwtUtils.validateToken(refreshToken), "刷新令牌已过期或无效");
        
        // 从刷新令牌中获取用户ID和用户名
        Long userId = jwtUtils.getUserId(refreshToken);
        String username = jwtUtils.getUsername(refreshToken);
        
        // 查询数据库中的认证信息
        UserAuth userAuth = getOne(
                new LambdaQueryWrapper<UserAuth>()
                        .eq(UserAuth::getUserId, userId)
                        .eq(UserAuth::getRefreshToken, refreshToken)
        );
        
        AssertUtils.notNull(userAuth, "刷新令牌不存在");
        
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
    
    /**
     * 用户登出
     * <p>
     * 清理用户认证信息和权限缓存，使令牌失效。
     *
     * @param userId 用户ID（由 Controller 从 Security 上下文获取）
     */
    @Override
    public void logout(Long userId) {
        AssertUtils.notNull(userId, AuthResultCodeEnum.TOKEN_INVALID);

        // 1. 从数据库中删除该用户的所有认证信息
        remove(
                new LambdaQueryWrapper<UserAuth>()
                        .eq(UserAuth::getUserId, userId)
        );

        // 2. 清除 Redis 中的用户权限和角色缓存
        clearUserCacheFromRedis(userId);

        // 3. 清理 Spring Security 上下文
        SecurityContextHolder.clearContext();

        log.info("用户登出成功，用户ID: {}", userId);
    }

    /**
     * 获取当前登录用户信息
     *
     * @param userId 用户ID（由 Controller 从 Security 上下文获取并传入）
     * @return 当前用户信息
     */
    @Override
    public CurrentUserVO getCurrentUser(Long userId) {
        // 1. 获取用户详细信息（包含头像URL）
        UserVO userDetail = userClient.getUserById(userId);
        AssertUtils.notNull(userDetail, AuthResultCodeEnum.USER_NOT_FOUND);
        AssertUtils.isTrue(userDetail.getStatus() == null || userDetail.getStatus() == 1, AuthResultCodeEnum.ACCOUNT_DISABLED);

        // 2. 从 Redis 获取权限和角色
        List<String> permissions = securityCacheService.getUserPermissions(userId);
        List<String> roleCodes = securityCacheService.getUserRoles(userId);

        // 3. 构建返回对象
        return CurrentUserVO.builder()
                .userId(userDetail.getId())
                .username(userDetail.getUsername())
                .nickname(userDetail.getNickname())
                .email(userDetail.getEmail())
                .avatar(userDetail.getAvatarUrl())
                .roles(roleCodes != null ? roleCodes : new ArrayList<>())
                .permissions(permissions != null ? permissions : new ArrayList<>())
                .build();
    }

    /**
     * 修改密码
     *
     * @param userId 用户ID（由 Controller 从 Security 上下文获取）
     * @param changePasswordDTO 修改密码请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, ChangePasswordDTO changePasswordDTO) {
        // 1. 验证新密码与确认密码是否一致
        AssertUtils.isTrue(changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword()),
                AuthResultCodeEnum.PASSWORD_MISMATCH);

        // 2. 获取用户信息
        UserVO user = userClient.getUserById(userId);
        AssertUtils.notNull(user, AuthResultCodeEnum.USER_NOT_FOUND);

        // 3. 获取用户登录信息（包含密码）
        UserLoginVO userLogin = userClient.getUserByUsername(user.getUsername());
        AssertUtils.notNull(userLogin, AuthResultCodeEnum.USER_NOT_FOUND);

        // 4. 验证旧密码是否正确
        AssertUtils.isTrue(passwordEncoder.matches(changePasswordDTO.getOldPassword(), userLogin.getPassword()),
                AuthResultCodeEnum.PASSWORD_ERROR);

        // 5. 验证新密码不能与旧密码相同
        AssertUtils.isTrue(!passwordEncoder.matches(changePasswordDTO.getNewPassword(), userLogin.getPassword()),
                "新密码不能与旧密码相同");

        // 6. 调用用户服务更新密码
        String encodedNewPassword = passwordEncoder.encode(changePasswordDTO.getNewPassword());
        userClient.updatePassword(userId, encodedNewPassword);

        log.info("用户密码修改成功，用户ID: {}", userId);
    }


    /**
     * 将用户权限和角色分别缓存到Redis
     *
     * <p>将用户的权限列表和角色列表分别缓存到Redis中，设置与JWT令牌相同的过期时间。
     * 这样可以确保缓存与令牌的生命周期保持一致，避免权限信息过期问题。</p>
     *
     * <p>缓存策略：</p>
     * <ul>
     *   <li>权限和角色分别存储，便于独立管理</li>
     *   <li>过期时间与JWT令牌保持一致</li>
     *   <li>缓存失败不影响主业务流程</li>
     *   <li>提供详细的调试日志记录</li>
     * </ul>
     *
     * @param userId 用户ID，作为缓存的键值标识
     * @param permissions 用户权限列表，包含所有权限编码
     * @param roles 用户角色列表，包含所有角色编码
     * @param expireSeconds 缓存过期时间（秒），与JWT令牌过期时间一致
     */
    private void cacheUserPermissionsAndRolesToRedis(Long userId, List<String> permissions, List<String> roles, long expireSeconds) {
        try {
            // 分别缓存用户权限和角色，TTL与JWT过期时间一致
            securityCacheService.cacheUserPermissions(userId, permissions, expireSeconds, TimeUnit.SECONDS);
            securityCacheService.cacheUserRoles(userId, roles, expireSeconds, TimeUnit.SECONDS);

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
     * <p>在用户登出时清除Redis中存储的权限和角色缓存信息。
     * 该方法会分别尝试清除权限缓存和角色缓存，即使其中一个操作失败也不会影响另一个。</p>
     *
     * <p>清除策略：</p>
     * <ul>
     *   <li>分别清除权限和角色缓存，提高容错性</li>
     *   <li>记录每个操作的成功或失败状态</li>
     *   <li>提供详细的日志记录便于问题排查</li>
     *   <li>异常不会向上传播，避免影响登出流程</li>
     * </ul>
     *
     * @param userId 用户ID，用于定位需要清除的缓存
     */
    private void clearUserCacheFromRedis(Long userId) {
        // 分别清除权限和角色缓存，确保即使一个失败也不影响另一个
        boolean permissionCleared = false;
        boolean roleCleared = false;

        // 清除用户权限缓存
        try {
            securityCacheService.clearUserPermissions(userId);
            permissionCleared = true;
            log.debug("已清除用户权限缓存，用户ID: {}", userId);
        } catch (Exception e) {
            log.error("清除用户权限缓存失败，用户ID: {}", userId, e);
        }

        // 清除用户角色缓存
        try {
            securityCacheService.clearUserRoles(userId);
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
     *
     * <p>验证用户是否存在，如果用户不存在则记录登录失败日志并抛出用户名未找到异常。
     * 这是登录流程中的第一个验证步骤。</p>
     *
     * @param user 用户信息对象，可能为null
     * @param username 用户名，用于日志记录
     * @param clientIp 客户端IP地址，用于审计日志
     * @param browser 浏览器信息，用于审计日志
     * @param os 操作系统信息，用于审计日志
     * @throws UsernameNotFoundException 当用户不存在时抛出
     */
    private void assertUserExists(UserLoginVO user, String username, String clientIp, String browser, String os) {
        if (user == null) {
            logService.saveLoginLogAsync(null, username, clientIp, browser, os, 0, AuthResultCodeEnum.USER_NOT_FOUND.getMessage());
            AssertUtils.fail(AuthResultCodeEnum.USER_NOT_FOUND);
        }
    }

    /**
     * 断言用户已启用，如果被禁用则记录日志并抛出异常
     *
     * <p>验证用户账户状态是否为启用状态，如果账户被禁用则记录登录失败日志并抛出业务异常。
     * 这是登录流程中的第二个验证步骤。</p>
     *
     * @param user 用户信息对象，包含用户状态
     * @param clientIp 客户端IP地址，用于审计日志
     * @param browser 浏览器信息，用于审计日志
     * @param os 操作系统信息，用于审计日志
     * @throws ServiceException 当用户账户被禁用时抛出
     */
    private void assertUserEnabled(UserLoginVO user, String clientIp, String browser, String os) {
        if (user.getStatus() != null && user.getStatus() == 0) {
            logService.saveLoginLogAsync(user.getId(), user.getUsername(), clientIp, browser, os, 0, AuthResultCodeEnum.ACCOUNT_DISABLED.getMessage());
            AssertUtils.fail(AuthResultCodeEnum.ACCOUNT_DISABLED);
        }
    }

    /**
     * 断言密码匹配，如果不匹配则记录日志并抛出异常
     *
     * <p>验证用户输入的密码是否与数据库中存储的加密密码匹配，如果密码错误则记录登录失败日志
     * 并抛出凭据错误异常。这是登录流程中的第三个验证步骤。</p>
     *
     * @param inputPassword 用户输入的原始密码
     * @param user 用户信息对象，包含加密后的密码
     * @param clientIp 客户端IP地址，用于审计日志
     * @param browser 浏览器信息，用于审计日志
     * @param os 操作系统信息，用于审计日志
     * @throws BadCredentialsException 当密码不匹配时抛出
     */
    private void assertPasswordMatches(String inputPassword, UserLoginVO user, String clientIp, String browser, String os) {
        if (!passwordEncoder.matches(inputPassword, user.getPassword())) {
            logService.saveLoginLogAsync(user.getId(), user.getUsername(), clientIp, browser, os, 0, AuthResultCodeEnum.PASSWORD_ERROR.getMessage());
            AssertUtils.fail(AuthResultCodeEnum.PASSWORD_ERROR);
        }
    }

    /**
     * 为新用户分配默认角色
     *
     * <p>在用户注册成功后，自动为新用户分配默认的ROLE_USER角色。
     * 该操作确保新用户具有基本的系统访问权限。</p>
     *
     * <p>分配流程：</p>
     * <ol>
     *   <li>查找系统中的ROLE_USER默认角色</li>
     *   <li>验证默认角色是否存在且处于启用状态</li>
     *   <li>创建用户与角色的关联记录</li>
     *   <li>将关联记录保存到数据库</li>
     * </ol>
     *
     * <p>注意：该方法的异常不会影响注册流程，只会记录错误日志。</p>
     *
     * @param userId 新注册用户的ID
     */
    private void assignDefaultRoleToUser(Long userId) {
        try {
            // 1. 查找ROLE_USER角色
            Role defaultRole = roleMapper.selectOne(
                    new LambdaQueryWrapper<Role>()
                            .eq(Role::getCode, "ROLE_USER")
                            .eq(Role::getStatus, 1)
            );

            // 2. 验证角色存在
            if (defaultRole == null) {
                log.error("默认角色ROLE_USER不存在，无法为用户分配角色，用户ID: {}", userId);
                return;
            }

            // 3. 创建用户角色关联记录
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(defaultRole.getId());
            userRole.setCreatedAt(LocalDateTime.now());

            // 4. 插入到数据库
            userRoleMapper.insert(userRole);

            log.info("成功为用户分配默认角色，用户ID: {}, 角色: {}", userId, defaultRole.getCode());

        } catch (Exception e) {
            log.error("为用户分配默认角色失败，用户ID: {}", userId, e);
            // 不抛出异常，避免影响注册流程
        }
    }

    /**
     * 将token存储到Redis
     *
     * @param token JWT token
     * @param user 用户信息
     * @param expireSeconds 过期时间（秒）
     */
    private void storeTokenToRedis(String token, UserLoginVO user, long expireSeconds) {
        try {
            // 存储用户基本信息到Redis
            String userInfo = String.format("{\"userId\":%d,\"username\":\"%s\",\"status\":%d}",
                                          user.getId(), user.getUsername(), user.getStatus());

            securityCacheService.storeToken(token, userInfo, expireSeconds);

            log.debug("Token已通过securityCacheService存储到Redis: expireSeconds={}", expireSeconds);

        } catch (Exception e) {
            log.error("存储token到Redis失败: {}", e.getMessage(), e);
            // 不抛出异常，避免影响登录流程
        }
    }

    /**
     * 从Redis中删除token
     *
     * @param token JWT token
     */
    private void removeTokenFromRedis(String token) {
        try {
            securityCacheService.removeToken(token);
            log.debug("Token已通过securityCacheService从Redis中删除");
        } catch (Exception e) {
            log.error("从Redis删除token失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 清理用户指定设备类型的旧token
     * <p>
     * 在用户登录时调用，实现同端互斥：
     * 1. 从数据库查询该用户该设备类型的所有旧认证记录
     * 2. 从Redis中删除这些旧token
     * 3. 从数据库中删除这些旧认证记录
     *
     * @param userId 用户ID
     * @param deviceType 设备类型
     */
    private void cleanupOldTokensForDevice(Long userId, String deviceType) {
        try {
            // 1. 查询该用户该设备类型的所有旧认证记录
            List<UserAuth> oldAuths = list(new LambdaQueryWrapper<UserAuth>()
                    .eq(UserAuth::getUserId, userId)
                    .eq(UserAuth::getDeviceType, deviceType));
            
            if (oldAuths.isEmpty()) {
                log.debug("【登录清理】用户 {} 设备 {} 没有旧的认证记录", userId, deviceType);
                return;
            }
            
            log.info("【登录清理】用户 {} 设备 {} 发现 {} 条旧认证记录，开始清理", 
                    userId, deviceType, oldAuths.size());
            
            // 2. 从Redis中删除这些旧token
            for (UserAuth oldAuth : oldAuths) {
                if (oldAuth.getAccessToken() != null) {
                    removeTokenFromRedis(oldAuth.getAccessToken());
                }
                if (oldAuth.getRefreshToken() != null) {
                    removeTokenFromRedis(oldAuth.getRefreshToken());
                }
            }
            
            // 3. 从数据库中删除这些旧认证记录
            List<Long> oldAuthIds = oldAuths.stream().map(UserAuth::getId).toList();
            removeByIds(oldAuthIds);
            
            log.info("【登录清理】用户 {} 设备 {} 清理完成，删除了 {} 条旧记录", 
                    userId, deviceType, oldAuthIds.size());
            
        } catch (Exception e) {
            log.error("清理用户旧token失败，userId: {}, deviceType: {}", userId, deviceType, e);
            // 不抛出异常，避免影响登录流程
        }
    }
}
