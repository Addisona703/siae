package com.hngy.siae.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.common.asserts.AssertUtils;
import com.hngy.siae.common.result.UserResultCodeEnum;
import com.hngy.siae.common.utils.BeanConvertUtil;
import com.hngy.siae.user.dto.request.UserProfileDTO;
import com.hngy.siae.user.dto.response.UserProfileVO;
import com.hngy.siae.user.entity.UserProfile;
import com.hngy.siae.user.mapper.UserProfileMapper;
import com.hngy.siae.user.service.UserProfileService;
import com.hngy.siae.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 用户详情服务实现类
 *
 * @author KEYKB
 */
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl
        extends ServiceImpl<UserProfileMapper, UserProfile>
        implements UserProfileService {

    private final UserService userService;

    @Override
    public UserProfileVO createUserProfile(UserProfileDTO userProfileDTO) {
        // 检查用户是否存在
        userService.assertUserExists(userProfileDTO.getUserId());

        // 检查用户详情是否已存在
        boolean exists = lambdaQuery().eq(UserProfile::getUserId, userProfileDTO.getUserId()).exists();
        AssertUtils.isFalse(exists, UserResultCodeEnum.USER_PROFILE_ALREADY_EXISTS);

        // 检查邮箱是否已被使用
        if (StringUtils.hasText(userProfileDTO.getEmail())) {
            boolean emailExists = lambdaQuery()
                .eq(UserProfile::getEmail, userProfileDTO.getEmail())
                .exists();
            AssertUtils.isFalse(emailExists, UserResultCodeEnum.EMAIL_ALREADY_EXISTS);
        }

        // 检查手机号是否已被使用
        if (StringUtils.hasText(userProfileDTO.getPhone())) {
            boolean phoneExists = lambdaQuery()
                .eq(UserProfile::getPhone, userProfileDTO.getPhone())
                .exists();
            AssertUtils.isFalse(phoneExists, UserResultCodeEnum.PHONE_ALREADY_EXISTS);
        }

        // 构建用户详情实体
        UserProfile userProfile = BeanConvertUtil.to(userProfileDTO, UserProfile.class);
        
        // 保存用户详情
        save(userProfile);
        
        // 转换为视图对象并返回
        return BeanConvertUtil.to(userProfile, UserProfileVO.class);
    }

    @Override
    public UserProfileVO updateUserProfile(Long userId, UserProfileDTO userProfileDTO) {
        // 检查用户是否存在
        userService.assertUserExists(userId);
        
        // 检查用户详情是否存在
        UserProfile userProfile = getById(userId);
        AssertUtils.notNull(userProfile, UserResultCodeEnum.USER_PROFILE_NOT_FOUND);
        
        // 检查邮箱是否已被其他用户使用
        if (StringUtils.hasText(userProfileDTO.getEmail()) && 
                !userProfileDTO.getEmail().equals(userProfile.getEmail())) {
            boolean emailExists = lambdaQuery()
                .eq(UserProfile::getEmail, userProfileDTO.getEmail())
                .ne(UserProfile::getUserId, userId)
                .exists();
            AssertUtils.isFalse(emailExists, UserResultCodeEnum.EMAIL_ALREADY_EXISTS);
        }
        
        // 检查手机号是否已被其他用户使用
        if (StringUtils.hasText(userProfileDTO.getPhone()) && 
                !userProfileDTO.getPhone().equals(userProfile.getPhone())) {
            boolean phoneExists = lambdaQuery()
                .eq(UserProfile::getPhone, userProfileDTO.getPhone())
                .ne(UserProfile::getUserId, userId)
                .exists();
            AssertUtils.isFalse(phoneExists, UserResultCodeEnum.PHONE_ALREADY_EXISTS);
        }
        
        // 更新用户详情信息
        BeanConvertUtil.to(userProfileDTO, userProfile, "userId", "createdAt");
        
        // 更新用户详情
        updateById(userProfile);
        
        // 转换为视图对象并返回
        return BeanConvertUtil.to(userProfile, UserProfileVO.class);
    }

    @Override
    public UserProfileVO getUserProfileByUserId(Long userId) {
        UserProfile userProfile = getById(userId);
        AssertUtils.notNull(userProfile, UserResultCodeEnum.USER_PROFILE_NOT_FOUND);
        return BeanConvertUtil.to(userProfile, UserProfileVO.class);
    }

    @Override
    public UserProfileVO getUserProfileByEmail(String email) {
        UserProfile userProfile = lambdaQuery()
            .eq(UserProfile::getEmail, email)
            .one();
        AssertUtils.notNull(userProfile, UserResultCodeEnum.USER_PROFILE_NOT_FOUND);
        return BeanConvertUtil.to(userProfile, UserProfileVO.class);
    }

    @Override
    public UserProfileVO getUserProfileByPhone(String phone) {
        UserProfile userProfile = lambdaQuery()
            .eq(UserProfile::getPhone, phone)
            .one();
        AssertUtils.notNull(userProfile, UserResultCodeEnum.USER_PROFILE_NOT_FOUND);
        return BeanConvertUtil.to(userProfile, UserProfileVO.class);
    }
} 