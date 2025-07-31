package com.hngy.siae.user.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.user.dto.request.UserProfileCreateDTO;
import com.hngy.siae.user.dto.request.UserProfileQueryDTO;
import com.hngy.siae.user.dto.request.UserProfileUpdateDTO;
import com.hngy.siae.user.dto.response.UserProfileVO;
import com.hngy.siae.user.entity.UserProfile;

/**
 * 用户详情服务接口
 * <p>
 * 提供用户详情的增删改查功能，包括创建、更新、查询和删除用户详情信息。
 * 支持分页查询和条件查询，支持按用户基本信息查询用户详情。
 *
 * @author KEYKB
 */
public interface UserProfileService extends IService<UserProfile> {

    /**
     * 创建用户详情
     *
     * @param userProfileCreateDTO 用户详情创建参数
     * @return 用户详情视图对象
     */
    UserProfileVO createUserProfile(UserProfileCreateDTO userProfileCreateDTO);

    /**
     * 更新用户详情
     *
     * @param userProfileUpdateDTO 用户详情更新参数
     * @return 用户详情视图对象
     */
    UserProfileVO updateUserProfile(UserProfileUpdateDTO userProfileUpdateDTO);

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

    /**
     * 分页查询用户详情列表
     *
     * @param pageDTO 分页查询参数
     * @return 分页用户详情列表
     */
    PageVO<UserProfileVO> listUserProfilesByPage(PageDTO<UserProfileQueryDTO> pageDTO);
}