package com.hngy.siae.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.UserResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.user.dto.request.UserProfileCreateDTO;
import com.hngy.siae.user.dto.request.UserProfileQueryDTO;
import com.hngy.siae.user.dto.request.UserProfileUpdateDTO;
import com.hngy.siae.user.dto.response.UserProfileVO;
import com.hngy.siae.user.entity.User;
import com.hngy.siae.user.entity.UserProfile;
import com.hngy.siae.user.mapper.UserMapper;
import com.hngy.siae.user.mapper.UserProfileMapper;
import com.hngy.siae.user.service.UserProfileService;
import com.hngy.siae.web.utils.PageConvertUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 用户详情服务实现类
 * <p>
 * 提供用户详情的增删改查功能，包括创建、更新、查询和删除用户详情信息。
 * 支持分页查询和条件查询，支持按用户基本信息查询用户详情。
 *
 * @author KEYKB
 */
@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl
        extends ServiceImpl<UserProfileMapper, UserProfile>
        implements UserProfileService {

    private final UserMapper userMapper;

    /**
     * 创建用户详情
     *
     * @param userProfileCreateDTO 用户详情创建参数
     * @return 创建成功的用户详情信息
     */
    @Override
    public UserProfileVO createUserProfile(UserProfileCreateDTO userProfileCreateDTO) {
        // 检查用户是否存在
        assertUserExists(userProfileCreateDTO.getUserId());

        // 检查用户详情是否已存在
        boolean exists = lambdaQuery().eq(UserProfile::getUserId, userProfileCreateDTO.getUserId()).exists();
        AssertUtils.isFalse(exists, UserResultCodeEnum.USER_PROFILE_ALREADY_EXISTS);

        // 检查邮箱是否已被使用
        if (StrUtil.isNotBlank(userProfileCreateDTO.getEmail())) {
            boolean emailExists = lambdaQuery()
                .eq(UserProfile::getEmail, userProfileCreateDTO.getEmail())
                .exists();
            AssertUtils.isFalse(emailExists, UserResultCodeEnum.EMAIL_ALREADY_EXISTS);
        }

        // 检查手机号是否已被使用
        if (StrUtil.isNotBlank(userProfileCreateDTO.getPhone())) {
            boolean phoneExists = lambdaQuery()
                .eq(UserProfile::getPhone, userProfileCreateDTO.getPhone())
                .exists();
            AssertUtils.isFalse(phoneExists, UserResultCodeEnum.PHONE_ALREADY_EXISTS);
        }

        // 构建用户详情实体
        UserProfile userProfile = BeanConvertUtil.to(userProfileCreateDTO, UserProfile.class);

        // 保存用户详情
        save(userProfile);

        // 转换为视图对象并返回
        return BeanConvertUtil.to(userProfile, UserProfileVO.class);
    }

    /**
     * 断言用户存在
     * <p>
     * 检查指定ID的用户是否存在，如果不存在则抛出异常
     *
     * @param userId 用户ID
     * @throws RuntimeException 如果用户不存在
     */
    public void assertUserExists(Long userId) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getId, userId)
        );
        AssertUtils.isTrue(count != null && count > 0, UserResultCodeEnum.USER_NOT_FOUND);
    }

    /**
     * 更新用户详情信息
     *
     * @param userProfileUpdateDTO 用户详情更新参数
     * @return 更新后的用户详情信息
     */
    @Override
    public UserProfileVO updateUserProfile(UserProfileUpdateDTO userProfileUpdateDTO) {
        Long userId = userProfileUpdateDTO.getUserId();

        // 检查用户是否存在
        assertUserExists(userId);

        // 检查用户详情是否存在
        UserProfile userProfile = getById(userId);
        AssertUtils.notNull(userProfile, UserResultCodeEnum.USER_PROFILE_NOT_FOUND);

        // 检查邮箱是否已被其他用户使用
        if (StrUtil.isNotBlank(userProfileUpdateDTO.getEmail()) &&
                !userProfileUpdateDTO.getEmail().equals(userProfile.getEmail())) {
            boolean emailExists = lambdaQuery()
                .eq(UserProfile::getEmail, userProfileUpdateDTO.getEmail())
                .ne(UserProfile::getUserId, userId)
                .exists();
            AssertUtils.isFalse(emailExists, UserResultCodeEnum.EMAIL_ALREADY_EXISTS);
        }

        // 检查手机号是否已被其他用户使用
        if (StrUtil.isNotBlank(userProfileUpdateDTO.getPhone()) &&
                !userProfileUpdateDTO.getPhone().equals(userProfile.getPhone())) {
            boolean phoneExists = lambdaQuery()
                .eq(UserProfile::getPhone, userProfileUpdateDTO.getPhone())
                .ne(UserProfile::getUserId, userId)
                .exists();
            AssertUtils.isFalse(phoneExists, UserResultCodeEnum.PHONE_ALREADY_EXISTS);
        }

        // 更新用户详情信息
        BeanConvertUtil.to(userProfileUpdateDTO, userProfile, "userId", "createdAt");

        // 更新用户详情
        updateById(userProfile);

        // 转换为视图对象并返回
        return BeanConvertUtil.to(userProfile, UserProfileVO.class);
    }

    /**
     * 根据用户ID获取用户详情信息
     *
     * @param userId 用户ID
     * @return 用户详情信息
     */
    @Override
    public UserProfileVO getUserProfileByUserId(Long userId) {
        UserProfile userProfile = getById(userId);
        AssertUtils.notNull(userProfile, UserResultCodeEnum.USER_PROFILE_NOT_FOUND);
        return BeanConvertUtil.to(userProfile, UserProfileVO.class);
    }

    /**
     * 根据邮箱获取用户详情信息
     *
     * @param email 邮箱
     * @return 用户详情信息
     */
    @Override
    public UserProfileVO getUserProfileByEmail(String email) {
        UserProfile userProfile = lambdaQuery()
            .eq(UserProfile::getEmail, email)
            .one();
        AssertUtils.notNull(userProfile, UserResultCodeEnum.USER_PROFILE_NOT_FOUND);
        return BeanConvertUtil.to(userProfile, UserProfileVO.class);
    }

    /**
     * 根据手机号获取用户详情信息
     *
     * @param phone 手机号
     * @return 用户详情信息
     */
    @Override
    public UserProfileVO getUserProfileByPhone(String phone) {
        UserProfile userProfile = lambdaQuery()
            .eq(UserProfile::getPhone, phone)
            .one();
        AssertUtils.notNull(userProfile, UserResultCodeEnum.USER_PROFILE_NOT_FOUND);
        return BeanConvertUtil.to(userProfile, UserProfileVO.class);
    }

    /**
     * 分页查询用户详情列表
     *
     * @param pageDTO 分页查询参数，包含分页信息和查询条件
     * @return 分页用户详情列表
     */
    @Override
    public PageVO<UserProfileVO> listUserProfilesByPage(PageDTO<UserProfileQueryDTO> pageDTO) {
        UserProfileQueryDTO queryDTO = pageDTO.getParams();
        LambdaQueryWrapper<UserProfile> wrapper = new LambdaQueryWrapper<>();

        if (queryDTO != null) {
            wrapper.eq(queryDTO.getUserId() != null, UserProfile::getUserId, queryDTO.getUserId())
                    .like(StrUtil.isNotBlank(queryDTO.getNickname()), UserProfile::getNickname, queryDTO.getNickname())
                    .like(StrUtil.isNotBlank(queryDTO.getRealName()), UserProfile::getRealName, queryDTO.getRealName())
                    .like(StrUtil.isNotBlank(queryDTO.getEmail()), UserProfile::getEmail, queryDTO.getEmail())
                    .eq(StrUtil.isNotBlank(queryDTO.getPhone()), UserProfile::getPhone, queryDTO.getPhone())
                    .eq(StrUtil.isNotBlank(queryDTO.getQq()), UserProfile::getQq, queryDTO.getQq())
                    .eq(StrUtil.isNotBlank(queryDTO.getWechat()), UserProfile::getWechat, queryDTO.getWechat())
                    .eq(StrUtil.isNotBlank(queryDTO.getIdCard()), UserProfile::getIdCard, queryDTO.getIdCard())
                    .eq(queryDTO.getGender() != null, UserProfile::getGender, queryDTO.getGender())
                    .ge(queryDTO.getBirthdayStart() != null, UserProfile::getBirthday, queryDTO.getBirthdayStart())
                    .le(queryDTO.getBirthdayEnd() != null, UserProfile::getBirthday, queryDTO.getBirthdayEnd());
        }

        wrapper.orderByDesc(UserProfile::getCreatedAt);

        Page<UserProfile> page = PageConvertUtil.toPage(pageDTO);
        Page<UserProfile> resultPage = page(page, wrapper);

        return PageConvertUtil.convert(resultPage, UserProfileVO.class);
    }
} 