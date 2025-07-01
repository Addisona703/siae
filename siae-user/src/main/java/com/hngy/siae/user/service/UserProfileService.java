package com.hngy.siae.user.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.user.dto.request.UserProfileDTO;
import com.hngy.siae.user.dto.response.UserProfileVO;
import com.hngy.siae.user.entity.UserProfile;

/**
 * 用户详情服务接口
 *
 * @author KEYKB
 */
public interface UserProfileService extends IService<UserProfile> {

    /**
     * 创建用户详情
     *
     * @param userProfileDTO 用户详情数据传输对象
     * @return 用户详情视图对象
     */
    UserProfileVO createUserProfile(UserProfileDTO userProfileDTO);

    /**
     * 更新用户详情
     *
     * @param userId 用户ID
     * @param userProfileDTO 用户详情数据传输对象
     * @return 用户详情视图对象
     */
    UserProfileVO updateUserProfile(Long userId, UserProfileDTO userProfileDTO);

    /**
     * 根据用户ID获取用户详情
     *
     * @param userId 用户ID
     * @return 用户详情视图对象
     */
    UserProfileVO getUserProfileByUserId(Long userId);

    /**
     * 根据邮箱获取用户详情
     *
     * @param email 邮箱
     * @return 用户详情视图对象
     */
    UserProfileVO getUserProfileByEmail(String email);

    /**
     * 根据手机号获取用户详情
     *
     * @param phone 手机号
     * @return 用户详情视图对象
     */
    UserProfileVO getUserProfileByPhone(String phone);
} 