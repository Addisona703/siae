package com.hngy.siae.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.core.result.UserResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.web.utils.PageConvertUtil;
import com.hngy.siae.user.dto.request.UserCreateDTO;
import com.hngy.siae.user.dto.request.UserQueryDTO;
import com.hngy.siae.user.dto.request.UserUpdateDTO;
import com.hngy.siae.user.dto.response.UserDetailVO;
import com.hngy.siae.user.dto.response.UserVO;
import com.hngy.siae.user.entity.MajorClassEnrollment;
import com.hngy.siae.user.entity.User;
import com.hngy.siae.user.entity.UserProfile;
import com.hngy.siae.user.enums.ClassUserStatusEnum;
import com.hngy.siae.user.enums.MemberTypeEnum;
import com.hngy.siae.user.feign.MediaFeignClient;
import com.hngy.siae.user.feign.dto.BatchUrlRequest;
import com.hngy.siae.user.feign.dto.BatchUrlResponse;
import com.hngy.siae.user.mapper.MajorClassEnrollmentMapper;
import com.hngy.siae.user.mapper.UserMapper;
import com.hngy.siae.user.mapper.UserProfileMapper;
import com.hngy.siae.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.hutool.core.util.StrUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 * <p>
 * 提供用户的增删改查功能，包括创建、更新、查询和删除用户信息。
 * 支持分页查询和条件查询，支持按用户名、邮箱、手机号等条件筛选。
 *
 * @author KEYKB
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl
        extends ServiceImpl<UserMapper, User>
        implements UserService {

    private final UserProfileMapper userProfileMapper;
    private final MajorClassEnrollmentMapper majorClassEnrollmentMapper;
    private final MediaFeignClient mediaFeignClient;

    /**
     * 创建用户（一体化创建流程）
     * <p>
     * 实现用户、用户详情、班级关联的三阶段一体化创建流程：
     * 1. 第一阶段：创建用户基本信息（user表）
     * 2. 第二阶段：创建用户详情信息（user_profile表）
     * 3. 第三阶段：创建班级关联信息（major_class_enrollment表，可选）
     *
     * @param userCreateDTO 用户创建参数
     * @return 创建成功的用户信息
     * @throws RuntimeException 当用户名已存在或学号已存在时
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO createUser(UserCreateDTO userCreateDTO) {
        // ==================== 第一阶段：创建用户基本信息 ====================
        
        // 校验用户名唯一性
        AssertUtils.isFalse(isUsernameExists(userCreateDTO.getUsername()), 
                UserResultCodeEnum.USERNAME_ALREADY_EXISTS);

        // 校验学号唯一性（如果提供了学号）
        if (StrUtil.isNotBlank(userCreateDTO.getStudentId())) {
            AssertUtils.isFalse(isStudentIdExists(userCreateDTO.getStudentId()), 
                    UserResultCodeEnum.STUDENT_ID_ALREADY_EXISTS);
        }

        // 创建用户基本信息
        User user = BeanConvertUtil.to(userCreateDTO, User.class);
        user.setStatus(userCreateDTO.getStatus() != null ? userCreateDTO.getStatus() : 1);
        user.setIsDeleted(0);
        save(user);

        // ==================== 第二阶段：创建用户详情信息 ====================
        
        UserProfile userProfile = BeanConvertUtil.to(userCreateDTO, UserProfile.class);
        userProfile.setUserId(user.getId());
        userProfileMapper.insert(userProfile);

        // ==================== 第三阶段：创建班级关联信息（可选） ====================
        
        // 如果提供了专业ID、入学年份和班号，则创建班级关联
        if (userCreateDTO.getMajorId() != null 
                && userCreateDTO.getEntryYear() != null 
                && userCreateDTO.getClassNo() != null) {
            
            MajorClassEnrollment enrollment = BeanConvertUtil.to(userCreateDTO, MajorClassEnrollment.class);
            enrollment.setUserId(user.getId());
            enrollment.setMemberType(userCreateDTO.getMemberType() != null ? userCreateDTO.getMemberType() : MemberTypeEnum.NON_MEMBER);
            enrollment.setStatus(ClassUserStatusEnum.ENROLLED);
            enrollment.setIsDeleted(0);
            majorClassEnrollmentMapper.insert(enrollment);
        }

        // 转换为视图对象并返回
        return BeanConvertUtil.to(user, UserVO.class);
    }

    /**
     * 更新用户信息（一体化更新流程）
     * <p>
     * 实现用户、用户详情、班级关联的三阶段一体化更新流程：
     * 1. 第一阶段：更新用户基本信息（user表）
     * 2. 第二阶段：更新用户详情信息（user_profile表）
     * 3. 第三阶段：更新班级关联信息（major_class_enrollment表，可选）
     *
     * @param userDTO 用户更新参数
     * @return 更新后的用户信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO updateUser(UserUpdateDTO userDTO) {
        // ==================== 第一阶段：更新用户基本信息 ====================
        
        // 检查用户是否存在
        User user = getById(userDTO.getId());
        AssertUtils.notNull(user, UserResultCodeEnum.USER_NOT_FOUND);

        // 如果更新用户名，需要检查是否与其他用户冲突
        if (StrUtil.isNotBlank(userDTO.getUsername()) && !userDTO.getUsername().equals(user.getUsername())) {
            boolean exists = lambdaQuery()
                    .eq(User::getUsername, userDTO.getUsername())
                    .ne(User::getId, user.getId())
                    .exists();
            AssertUtils.isFalse(exists, UserResultCodeEnum.USERNAME_ALREADY_EXISTS);
        }

        // 更新用户基本信息（排除不应修改的字段）
        BeanConvertUtil.to(userDTO, user, "id", "password", "isDeleted", "createdAt");
        updateById(user);

        // ==================== 第二阶段：更新用户详情信息 ====================
        
        // 查询用户详情
        LambdaQueryWrapper<UserProfile> profileWrapper = new LambdaQueryWrapper<>();
        profileWrapper.eq(UserProfile::getUserId, userDTO.getId());
        UserProfile userProfile = userProfileMapper.selectOne(profileWrapper);

        if (userProfile != null) {
            // 更新已存在的用户详情
            BeanConvertUtil.to(userDTO, userProfile, "id", "userId", "createdAt");
            userProfileMapper.updateById(userProfile);
        } else {
            // 如果用户详情不存在，则创建
            userProfile = BeanConvertUtil.to(userDTO, UserProfile.class);
            userProfile.setUserId(userDTO.getId());
            userProfileMapper.insert(userProfile);
        }

        // ==================== 第三阶段：更新班级关联信息（可选） ====================
        
        // 如果提供了专业ID、入学年份和班号，则更新或创建班级关联
        if (userDTO.getMajorId() != null 
                && userDTO.getEntryYear() != null 
                && userDTO.getClassNo() != null) {
            
            LambdaQueryWrapper<MajorClassEnrollment> enrollmentWrapper = new LambdaQueryWrapper<>();
            enrollmentWrapper.eq(MajorClassEnrollment::getUserId, userDTO.getId())
                    .eq(MajorClassEnrollment::getIsDeleted, 0);
            MajorClassEnrollment enrollment = majorClassEnrollmentMapper.selectOne(enrollmentWrapper);

            if (enrollment != null) {
                // 更新已存在的班级关联
                BeanConvertUtil.to(userDTO, enrollment, "id", "userId", "isDeleted", "createdAt");
                majorClassEnrollmentMapper.updateById(enrollment);
            } else {
                // 如果班级关联不存在，则创建
                enrollment = BeanConvertUtil.to(userDTO, MajorClassEnrollment.class);
                enrollment.setUserId(userDTO.getId());
                enrollment.setStatus(ClassUserStatusEnum.ENROLLED);
                enrollment.setIsDeleted(0);
                majorClassEnrollmentMapper.insert(enrollment);
            }
        }

        // 转换为视图对象并返回
        return BeanConvertUtil.to(user, UserVO.class);
    }

    /**
     * 根据ID获取用户信息
     *
     * @param id 用户ID
     * @return 用户详细信息
     */
    @Override
    public UserVO getUserById(Long id) {
        User user = getById(id);
        AssertUtils.notNull(user, UserResultCodeEnum.USER_NOT_FOUND);
        UserVO userVO = BeanConvertUtil.to(user, UserVO.class);
        
        // 获取头像URL
        enrichUserWithAvatarUrl(userVO);
        
        return userVO;
    }

    /**
     * 根据ID获取用户详细信息（包含用户基本信息、详情信息和班级关联信息）
     * <p>
     * 通过三表联查（user + user_profile + major_class_enrollment + major）
     * 获取用户的完整信息
     *
     * @param id 用户ID
     * @return 用户详细信息
     */
    @Override
    public UserDetailVO getUserDetailById(Long id) {
        UserDetailVO userDetail = baseMapper.selectUserDetailById(id);
        AssertUtils.notNull(userDetail, UserResultCodeEnum.USER_NOT_FOUND);
        
        // 获取头像和背景图URL
        enrichUserDetailWithMediaUrls(userDetail);
        
        return userDetail;
    }

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户详细信息，如果不存在则返回null
     */
    @Override
    public UserVO getUserByUsername(String username) {
        User user = lambdaQuery()
                .eq(User::getUsername, username)
                .eq(User::getIsDeleted, 0)
                .one();
        log.info(user.toString());
        return BeanConvertUtil.to(user, UserVO.class);
    }

    /**
     * 分页查询用户列表
     *
     * @param pageDTO 分页查询参数，包含分页信息和查询条件
     * @return 分页用户列表
     */
    @Override
    public PageVO<UserVO> listUsersByPage(PageDTO<UserQueryDTO> pageDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        Optional.ofNullable(pageDTO.getParams()).ifPresent(param -> wrapper.like(StrUtil.isNotBlank(param.getUsername()), User::getUsername, param.getUsername())
                .eq(param.getStatus() != null, User::getStatus, param.getStatus()));

        // 排除已删除记录，并按创建时间倒序
        wrapper.eq(User::getIsDeleted, 0)
                .orderByDesc(User::getCreatedAt);

        Page<User> resultPage = page(PageConvertUtil.toPage(pageDTO), wrapper);
        PageVO<UserVO> pageVO = PageConvertUtil.convert(resultPage, UserVO.class);
        
        // 批量获取头像URL
        enrichUsersWithAvatarUrls(pageVO.getRecords());
        
        return pageVO;
    }

    /**
     * 根据ID删除用户（逻辑删除）
     *
     * @param id 用户ID
     * @return 删除结果，true表示删除成功，false表示删除失败
     */
    @Override
    public boolean deleteUser(Long id) {
        User user = getById(id);
        AssertUtils.notNull(user, UserResultCodeEnum.USER_NOT_FOUND);

        // 逻辑删除用户
        user.setIsDeleted(1);
        return updateById(user);
    }

    /**
     * 断言用户存在
     * <p>
     * 检查指定ID的用户是否存在，如果不存在则抛出异常
     *
     * @param userId 用户ID
     * @throws RuntimeException 如果用户不存在
     */
    @Override
    public void assertUserExists(Long userId) {
        boolean exists = lambdaQuery().eq(User::getId, userId).exists();
        AssertUtils.isTrue(exists, UserResultCodeEnum.USER_NOT_FOUND);
    }

    @Override
    public UserVO getUserByStudentId(String studentId) {
        if (StrUtil.isBlank(studentId)) {
            return null;
        }

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getStudentId, studentId)
                .eq(User::getIsDeleted, 0);

        User user = getOne(wrapper);
        return user != null ? BeanConvertUtil.to(user, UserVO.class) : null;
    }

    @Override
    public boolean isStudentIdExists(String studentId) {
        if (StrUtil.isBlank(studentId)) {
            return false;
        }

        return lambdaQuery()
                .eq(User::getStudentId, studentId)
                .eq(User::getIsDeleted, 0)
                .exists();
    }

    @Override
    public boolean isUsernameExists(String username) {
        if (StrUtil.isBlank(username)) {
            return false;
        }

        return lambdaQuery()
                .eq(User::getUsername, username)
                .eq(User::getIsDeleted, 0)
                .exists();
    }

    /**
     * 批量断言用户存在
     * <p>
     * 检查指定ID列表中的所有用户是否存在，如果有任何用户不存在则抛出异常
     *
     * @param userIds 用户ID列表
     * @throws RuntimeException 如果有用户不存在
     */
    @Override
    public void assertUsersExist(List<Long> userIds) {
        AssertUtils.notEmpty(userIds, "用户ID列表不能为空");

        // 查询存在的用户数量
        long existCount = lambdaQuery()
                .in(User::getId, userIds)
                .eq(User::getIsDeleted, 0)
                .count();

        // 检查是否所有用户都存在
        if (existCount != userIds.size()) {
            // 查询存在的用户ID集合
            Set<Long> existingUserIds = lambdaQuery()
                    .in(User::getId, userIds)
                    .eq(User::getIsDeleted, 0)
                    .select(User::getId)
                    .list()
                    .stream()
                    .map(User::getId)
                    .collect(Collectors.toSet());

            // 找出不存在的用户ID
            List<Long> notExistIds = userIds.stream()
                    .filter(id -> !existingUserIds.contains(id))
                    .toList();
            AssertUtils.fail("用户不存在，ID: " + notExistIds);
        }
    }

    // ==================== 私有辅助方法：Media服务集成 ====================

    /**
     * 为单个用户填充头像URL
     */
    private void enrichUserWithAvatarUrl(UserVO userVO) {
        if (StrUtil.isBlank(userVO.getAvatarFileId())) {
            return;
        }

        try {
            // 单个用户查询使用单个接口更简洁
            Result<String> result = mediaFeignClient.getFileUrl(userVO.getAvatarFileId(), 86400);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                userVO.setAvatarUrl(result.getData());
            }
        } catch (Exception e) {
            log.warn("Failed to get avatar URL for user: {}, error: {}", userVO.getId(), e.getMessage());
        }
    }

    /**
     * 为用户列表批量填充头像URL
     */
    private void enrichUsersWithAvatarUrls(List<UserVO> users) {
        if (users == null || users.isEmpty()) {
            return;
        }

        // 收集所有头像ID
        List<String> avatarIds = users.stream()
                .map(UserVO::getAvatarFileId)
                .filter(StrUtil::isNotBlank)
                .distinct()
                .collect(Collectors.toList());

        if (avatarIds.isEmpty()) {
            return;
        }

        try {
            // 批量获取URL
            Map<String, String> urls = batchGetMediaUrls(avatarIds);

            // 填充到用户对象
            users.forEach(user -> {
                if (StrUtil.isNotBlank(user.getAvatarFileId())) {
                    user.setAvatarUrl(urls.get(user.getAvatarFileId()));
                }
            });

            log.info("Enriched {} users with avatar URLs, success: {}/{}", 
                    users.size(), urls.size(), avatarIds.size());
        } catch (Exception e) {
            log.error("Failed to batch get avatar URLs", e);
        }
    }

    /**
     * 为用户详情填充媒体URL（头像和背景图）
     */
    private void enrichUserDetailWithMediaUrls(UserDetailVO userDetail) {
        List<String> fileIds = new ArrayList<>();
        
        if (StrUtil.isNotBlank(userDetail.getAvatarFileId())) {
            fileIds.add(userDetail.getAvatarFileId());
        }
        if (StrUtil.isNotBlank(userDetail.getBackgroundFileId())) {
            fileIds.add(userDetail.getBackgroundFileId());
        }

        if (fileIds.isEmpty()) {
            return;
        }

        try {
            Map<String, String> urls = batchGetMediaUrls(fileIds);
            
            if (StrUtil.isNotBlank(userDetail.getAvatarFileId())) {
                userDetail.setAvatarUrl(urls.get(userDetail.getAvatarFileId()));
            }
            if (StrUtil.isNotBlank(userDetail.getBackgroundFileId())) {
                userDetail.setBackgroundUrl(urls.get(userDetail.getBackgroundFileId()));
            }
        } catch (Exception e) {
            log.warn("Failed to get media URLs for user detail: {}, error: {}", 
                    userDetail.getId(), e.getMessage());
        }
    }

    /**
     * 批量获取媒体文件URL
     */
    private Map<String, String> batchGetMediaUrls(List<String> fileIds) {
        if (fileIds == null || fileIds.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            BatchUrlRequest request = BatchUrlRequest.builder()
                    .fileIds(fileIds)
                    .expirySeconds(86400) // 24小时
                    .build();

            Result<BatchUrlResponse> result = mediaFeignClient.batchGetFileUrls(request);
            
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                return result.getData().getUrls();
            } else {
                log.warn("Media service returned unsuccessful result: {}", result);
                return Collections.emptyMap();
            }
        } catch (Exception e) {
            log.error("Failed to call media service for batch URLs", e);
            return Collections.emptyMap();
        }
    }
}
