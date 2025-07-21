package com.hngy.siae.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.user.entity.UserThirdPartyAuth;
import com.hngy.siae.user.mapper.UserThirdPartyAuthMapper;
import com.hngy.siae.user.service.UserThirdPartyAuthService;
import org.springframework.stereotype.Service;

/**
 * 用户第三方认证服务实现类
 *
 * @author KEYKB
 */
@Service
public class UserThirdPartyAuthServiceImpl extends ServiceImpl<UserThirdPartyAuthMapper, UserThirdPartyAuth> implements UserThirdPartyAuthService {

//    @Autowired
//    private UserMapper userMapper;
//
//    @Autowired
//    private UserService userService;
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public UserThirdPartyAuthVO createAuth(UserThirdPartyAuthDTO authDTO) {
//        // 参数校验
//        AssertUtils.notNull(authDTO, ResultCodeEnum.PARAM_ERROR, "认证信息不能为空");
//        AssertUtils.notNull(authDTO.getUserId(), ResultCodeEnum.PARAM_ERROR, "用户ID不能为空");
//        AssertUtils.hasText(authDTO.getProvider(), ResultCodeEnum.PARAM_ERROR, "提供商不能为空");
//        AssertUtils.hasText(authDTO.getProviderUserId(), ResultCodeEnum.PARAM_ERROR, "提供商用户ID不能为空");
//
//        // 检查用户是否存在
//        userService.assertUserExists(authDTO.getUserId());
//
//        // 检查是否已经存在相同的第三方认证
//        boolean existsProviderAuth = lambdaQuery()
//            .eq(UserThirdPartyAuth::getProvider, authDTO.getProvider())
//            .eq(UserThirdPartyAuth::getProviderUserId, authDTO.getProviderUserId())
//            .exists(); // 使用ServiceImpl提供的链式调用
//
//        AssertUtils.isFalse(existsProviderAuth, ResultCodeEnum.BUSINESS_ERROR, "该第三方账号已被绑定");
//
//        // 检查用户是否已绑定相同提供商的账号
//        boolean existsUserProviderAuth = lambdaQuery()
//            .eq(UserThirdPartyAuth::getUserId, authDTO.getUserId())
//            .eq(UserThirdPartyAuth::getProvider, authDTO.getProvider())
//            .exists(); // 使用ServiceImpl提供的链式调用
//
//        AssertUtils.isFalse(existsUserProviderAuth, ResultCodeEnum.BUSINESS_ERROR, "用户已绑定该类型的第三方账号");
//
//        // 构建认证实体
//        UserThirdPartyAuth auth = new UserThirdPartyAuth();
//        BeanUtils.copyProperties(authDTO, auth);
//
//        // 保存认证信息
//        save(auth); // 使用ServiceImpl提供的方法
//
//        // 转换为视图对象并返回
//        return convertToVO(auth);
//    }
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public UserThirdPartyAuthVO updateAuth(Long id, UserThirdPartyAuthDTO authDTO) {
//        // 参数校验
//        AssertUtils.notNull(id, ResultCodeEnum.PARAM_ERROR, "认证ID不能为空");
//        AssertUtils.notNull(authDTO, ResultCodeEnum.PARAM_ERROR, "认证信息不能为空");
//
//        // 检查认证是否存在
//        UserThirdPartyAuth auth = getById(id); // 使用ServiceImpl提供的方法
//        AssertUtils.notNull(auth, ResultCodeEnum.DATA_NOT_FOUND, "认证信息不存在");
//
//        // 不允许修改关键信息
//        authDTO.setUserId(auth.getUserId());
//        authDTO.setProvider(auth.getProvider());
//        authDTO.setProviderUserId(auth.getProviderUserId());
//
//        // 更新认证信息
//        BeanUtils.copyProperties(authDTO, auth, "id", "userId", "provider", "providerUserId", "createdAt");
//
//        // 更新认证
//        updateById(auth); // 使用ServiceImpl提供的方法
//
//        // 转换为视图对象并返回
//        return convertToVO(auth);
//    }
//
//    @Override
//    public UserThirdPartyAuthVO getAuthById(Long id) {
//        // 参数校验
//        AssertUtils.notNull(id, ResultCodeEnum.PARAM_ERROR, "认证ID不能为空");
//
//        // 查询认证
//        UserThirdPartyAuth auth = getById(id); // 使用ServiceImpl提供的方法
//        AssertUtils.notNull(auth, ResultCodeEnum.DATA_NOT_FOUND, "认证信息不存在");
//
//        // 转换为视图对象并返回
//        return convertToVO(auth);
//    }
//
//    @Override
//    public List<UserThirdPartyAuthVO> getAuthsByUserId(Long userId) {
//        // 参数校验
//        AssertUtils.notNull(userId, ResultCodeEnum.PARAM_ERROR, "用户ID不能为空");
//
//        // 查询用户所有第三方认证
//        List<UserThirdPartyAuth> auths = lambdaQuery()
//            .eq(UserThirdPartyAuth::getUserId, userId)
//            .list(); // 使用ServiceImpl提供的链式调用
//
//        // 转换为视图对象并返回
//        return auths.stream().map(this::convertToVO).collect(Collectors.toList());
//    }
//
//    @Override
//    public UserThirdPartyAuthVO getAuthByProviderAndProviderUserId(String provider, String providerUserId) {
//        // 参数校验
//        AssertUtils.hasText(provider, ResultCodeEnum.PARAM_ERROR, "提供商不能为空");
//        AssertUtils.hasText(providerUserId, ResultCodeEnum.PARAM_ERROR, "提供商用户ID不能为空");
//
//        // 查询认证
//        UserThirdPartyAuth auth = lambdaQuery()
//            .eq(UserThirdPartyAuth::getProvider, provider)
//            .eq(UserThirdPartyAuth::getProviderUserId, providerUserId)
//            .one(); // 使用ServiceImpl提供的链式调用
//
//        // 转换为视图对象并返回
//        return convertToVO(auth);
//    }
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public boolean unbindAuth(Long id) {
//        // 参数校验
//        AssertUtils.notNull(id, ResultCodeEnum.PARAM_ERROR, "认证ID不能为空");
//
//        // 检查认证是否存在
//        UserThirdPartyAuth auth = getById(id); // 使用ServiceImpl提供的方法
//        AssertUtils.notNull(auth, ResultCodeEnum.DATA_NOT_FOUND, "认证信息不存在");
//
//        // 删除认证
//        return removeById(id); // 使用ServiceImpl提供的方法
//    }
//
//    @Override
//    @Transactional(rollbackFor = Exception.class)
//    public boolean unbindAuthByUserIdAndProvider(Long userId, String provider) {
//        // 参数校验
//        AssertUtils.notNull(userId, ResultCodeEnum.PARAM_ERROR, "用户ID不能为空");
//        AssertUtils.hasText(provider, ResultCodeEnum.PARAM_ERROR, "提供商不能为空");
//
//        // 查询认证
//        UserThirdPartyAuth auth = lambdaQuery()
//            .eq(UserThirdPartyAuth::getUserId, userId)
//            .eq(UserThirdPartyAuth::getProvider, provider)
//            .one(); // 使用ServiceImpl提供的链式调用
//
//        // 如果不存在，直接返回成功
//        if (auth == null) {
//            return true;
//        }
//
//        // 删除认证
//        return remove(lambdaQuery()
//            .eq(UserThirdPartyAuth::getUserId, userId)
//            .eq(UserThirdPartyAuth::getProvider, provider)); // 使用ServiceImpl提供的链式调用
//    }
//
//    /**
//     * 将实体对象转换为视图对象
//     *
//     * @param auth 用户第三方认证实体
//     * @return 用户第三方认证视图对象
//     */
//    private UserThirdPartyAuthVO convertToVO(UserThirdPartyAuth auth) {
//        if (auth == null) {
//            return null;
//        }
//        UserThirdPartyAuthVO authVO = new UserThirdPartyAuthVO();
//        BeanUtils.copyProperties(auth, authVO);
//        // 不返回敏感信息
//        authVO.setAccessToken(null);
//        return authVO;
//    }
} 