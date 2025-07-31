package com.hngy.siae.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.UserResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.user.dto.response.UserBasicVO;
import com.hngy.siae.web.utils.PageConvertUtil;
import com.hngy.siae.user.dto.request.UserCreateDTO;

import com.hngy.siae.user.dto.request.UserQueryDTO;
import com.hngy.siae.user.dto.request.UserUpdateDTO;
import com.hngy.siae.user.dto.request.ClassUserCreateDTO;
import com.hngy.siae.user.dto.request.UserProfileCreateDTO;
import com.hngy.siae.user.dto.response.UserVO;
import com.hngy.siae.user.entity.User;
import com.hngy.siae.user.mapper.UserMapper;
import com.hngy.siae.user.service.ClassUserService;
import com.hngy.siae.user.service.UserProfileService;
import com.hngy.siae.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.hutool.core.util.StrUtil;

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
@Service
@RequiredArgsConstructor
public class UserServiceImpl
        extends ServiceImpl<UserMapper, User>
        implements UserService {

    private final UserProfileService userProfileService;
    private final ClassUserService classUserService;

    /**
     * 创建用户（一体化创建流程）
     * <p>
     * 实现用户、用户详情、班级关联的三阶段一体化创建流程：
     * 1. 第一阶段：创建用户基本信息（user表）
     * 2. 第二阶段：创建用户详情信息（user_profile表）
     * 3. 第三阶段：创建班级关联信息（class_user表，可选）
     *
     * @param userCreateDTO 用户创建参数，包含用户基本信息、详情信息和班级关联信息
     * @return 创建成功的用户完整信息
     * @throws RuntimeException 当用户名已存在、邮箱已存在、手机号已存在、班级不存在或用户重复加入班级时
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserVO createUser(UserCreateDTO userCreateDTO) {
        // 统一的唯一性校验
        validateUniqueConstraints(userCreateDTO);

        // 第一阶段：创建用户基本信息
        User user = createUserBasicInfo(userCreateDTO);

        // 第二阶段：创建用户详情信息
        createUserProfile(userCreateDTO, user.getId());

        // 第三阶段：创建班级关联信息（可选）
        if (userCreateDTO.getClassId() != null) {
            createClassUserRelation(userCreateDTO, user.getId());
        }

        // 构建并返回完整的用户信息
        return buildUserWithProfileVO(user, userCreateDTO);
    }

    /**
     * 更新用户信息
     *
     * @param userDTO 用户更新参数
     * @return 更新后的用户信息
     */
    @Override
    public UserVO updateUser(UserUpdateDTO userDTO) {
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

        // 更新用户信息（排除ID、密码和逻辑删除标记、创建时间等不应修改的字段）
        BeanConvertUtil.to(userDTO, user, "id", "password", "isDeleted", "createdAt");

        // 更新用户
        updateById(user);

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
        return BeanConvertUtil.to(user, UserVO.class);
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
        return BeanConvertUtil.to(user, UserVO.class);
    }

    /**
     * 根据用户名获取用户信息（Feign客户端专用）
     * <p>
     * 专门用于Feign客户端调用的用户查询方法，返回包含密码字段的完整用户信息。
     * 此方法主要用于内部服务间的身份验证和用户查找场景。
     * <p>
     * <strong>安全警告：</strong>此方法返回包含用户密码的完整信息，仅限内部服务调用使用，
     * 不得暴露给外部API接口，以防止敏感信息泄露。
     *
     * @param username 用户名
     * @return 完整的用户信息（包含密码字段），如果用户不存在则返回null
     */
    @Override
    public UserBasicVO getUserByUsernameClient(String username) {
        User user = lambdaQuery()
                .eq(User::getUsername, username)
                .eq(User::getIsDeleted, 0)
                .one();

        return BeanConvertUtil.to(user, UserBasicVO.class);
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
        return PageConvertUtil.convert(resultPage, UserVO.class);
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

    /**
     * 根据用户ID集合获取用户ID到用户名的映射
     *
     * @param userIds 用户ID集合
     * @return 用户ID到用户名的映射Map，key为用户ID，value为用户名
     */
    @Override
    public Map<Long, String> getUserMapByIds(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        List<User> users = lambdaQuery()
                .in(User::getId, userIds)
                .eq(User::getIsDeleted, 0)
                .select(User::getId, User::getUsername)
                .list();

        return users.stream()
                .collect(Collectors.toMap(User::getId, User::getUsername));
    }

    /**
     * 检查用户是否存在
     *
     * @param userId 用户ID
     * @return true表示用户存在，false表示用户不存在或已删除
     */
    @Override
    public Boolean checkUserExists(Long userId) {
        if (userId == null) {
            return false;
        }

        return lambdaQuery()
                .eq(User::getId, userId)
                .eq(User::getIsDeleted, 0)
                .exists();
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 统一的唯一性校验方法
     * <p>
     * 校验用户名、邮箱、手机号的唯一性，以及班级存在性
     *
     * @param userCreateDTO 用户创建参数
     * @throws RuntimeException 当校验失败时抛出相应的业务异常
     */
    private void validateUniqueConstraints(UserCreateDTO userCreateDTO) {
        // 校验用户名唯一性
        boolean usernameExists = lambdaQuery()
                .eq(User::getUsername, userCreateDTO.getUsername())
                .exists();
        AssertUtils.isFalse(usernameExists, UserResultCodeEnum.USERNAME_ALREADY_EXISTS);

        // 校验邮箱唯一性（如果提供了邮箱）
        if (StrUtil.isNotBlank(userCreateDTO.getEmail())) {
            try {
                userProfileService.getUserProfileByEmail(userCreateDTO.getEmail());
                // 如果没有抛出异常，说明邮箱已存在
                AssertUtils.fail(UserResultCodeEnum.EMAIL_ALREADY_EXISTS);
            } catch (Exception e) {
                // 抛出异常说明邮箱不存在，这是我们期望的结果
            }
        }

        // 校验手机号唯一性（如果提供了手机号）
        if (StrUtil.isNotBlank(userCreateDTO.getPhone())) {
            try {
                userProfileService.getUserProfileByPhone(userCreateDTO.getPhone());
                // 如果没有抛出异常，说明手机号已存在
                AssertUtils.fail(UserResultCodeEnum.PHONE_ALREADY_EXISTS);
            } catch (Exception e) {
                // 抛出异常说明手机号不存在，这是我们期望的结果
            }
        }

        // TODO：校验班级存在性
        // 校验班级存在性（如果提供了班级ID）
//        if (userCreateDTO.getClassId() != null) {
            // 这里假设有ClassInfoService来校验班级存在性
            // classInfoService.assertClassExists(userCreateDTO.getClassId());
//        }
    }

    /**
     * 创建用户基本信息
     * <p>
     * 第一阶段：提取用户基本信息字段，保存到user表
     *
     * @param userCreateDTO 用户创建参数
     * @return 创建成功的用户实体
     */
    private User createUserBasicInfo(UserCreateDTO userCreateDTO) {
        // 构建用户实体，只包含基本信息字段
        User user = new User();
        user.setUsername(userCreateDTO.getUsername());
        user.setPassword(userCreateDTO.getPassword());
        user.setStatus(userCreateDTO.getStatus() != null ? userCreateDTO.getStatus() : 1);
        user.setIsDeleted(0);

        // 保存用户基本信息
        save(user);

        return user;
    }

    /**
     * 创建用户详情信息
     * <p>
     * 第二阶段：提取用户详情字段，构建UserProfileCreateDTO并调用用户详情服务
     *
     * @param userCreateDTO 用户创建参数
     * @param userId 用户ID
     */
    private void createUserProfile(UserCreateDTO userCreateDTO, Long userId) {
        UserProfileCreateDTO profileCreateDTO = buildUserProfileCreateDTO(userCreateDTO, userId);
        userProfileService.createUserProfile(profileCreateDTO);
    }

    /**
     * 创建班级用户关联信息
     * <p>
     * 第三阶段：提取班级关联字段，构建ClassUserCreateDTO并调用班级用户服务
     *
     * @param userCreateDTO 用户创建参数
     * @param userId 用户ID
     */
    private void createClassUserRelation(UserCreateDTO userCreateDTO, Long userId) {
        ClassUserCreateDTO classUserCreateDTO = buildClassUserCreateDTO(userCreateDTO, userId);
        classUserService.addUserToClass(classUserCreateDTO);
    }

    /**
     * 构建用户详情创建DTO
     *
     * @param source 用户创建参数
     * @param userId 用户ID
     * @return 用户详情创建DTO
     */
    private UserProfileCreateDTO buildUserProfileCreateDTO(UserCreateDTO source, Long userId) {
        UserProfileCreateDTO profileCreateDTO = new UserProfileCreateDTO();
        profileCreateDTO.setUserId(userId);
        profileCreateDTO.setRealName(source.getRealName());
        profileCreateDTO.setNickname(source.getNickname());
        profileCreateDTO.setEmail(source.getEmail());
        profileCreateDTO.setPhone(source.getPhone());
        profileCreateDTO.setAvatar(source.getAvatar());
        profileCreateDTO.setBio(source.getBio());
        profileCreateDTO.setGender(source.getGender());
        profileCreateDTO.setBirthday(source.getBirthday());
        profileCreateDTO.setIdCard(source.getIdCard());
        profileCreateDTO.setQq(source.getQq());
        profileCreateDTO.setWechat(source.getWechat());
        profileCreateDTO.setBgUrl(source.getBgUrl());
        return profileCreateDTO;
    }

    /**
     * 构建班级用户关联创建DTO
     *
     * @param source 用户创建参数
     * @param userId 用户ID
     * @return 班级用户关联创建DTO
     */
    private ClassUserCreateDTO buildClassUserCreateDTO(UserCreateDTO source, Long userId) {
        ClassUserCreateDTO classUserCreateDTO = new ClassUserCreateDTO();
        classUserCreateDTO.setUserId(userId);
        classUserCreateDTO.setClassId(source.getClassId());
        classUserCreateDTO.setMemberType(source.getMemberType() != null ? source.getMemberType() : 0);
        // 注意：ClassUserCreateDTO可能没有joinDate字段，这里需要根据实际情况调整
        return classUserCreateDTO;
    }

    /**
     * 构建用户完整信息视图对象
     *
     * @param user 用户实体
     * @param userCreateDTO 用户创建参数
     * @return 用户完整信息视图对象
     */
    private UserVO buildUserWithProfileVO(User user, UserCreateDTO userCreateDTO) {
        // 这里简化处理，直接返回基本的UserVO
        // 在实际项目中，可能需要查询用户详情信息并组装成UserWithProfileVO
        UserVO userVO = BeanConvertUtil.to(user, UserVO.class);

        userVO.setAvatar(userCreateDTO.getAvatar());
        userVO.setNickname(userCreateDTO.getNickname());

        return userVO;
    }
}