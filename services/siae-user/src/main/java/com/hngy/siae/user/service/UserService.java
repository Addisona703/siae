package com.hngy.siae.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.user.dto.request.UserCreateDTO;

import com.hngy.siae.user.dto.request.UserQueryDTO;
import com.hngy.siae.user.dto.request.UserUpdateDTO;
import com.hngy.siae.user.dto.response.UserBasicVO;
import com.hngy.siae.user.dto.response.UserVO;
import com.hngy.siae.user.entity.User;

import java.util.Map;
import java.util.Set;

/**
 * 用户服务接口
 * <p>
 * 提供用户的增删改查功能，包括创建、更新、查询和删除用户信息。
 * 支持分页查询和条件查询，支持按用户名、邮箱、手机号等条件筛选。
 *
 * @author KEYKB
 */
public interface UserService extends IService<User> {

    /**
     * 创建用户（一体化创建流程）
     * <p>
     * 实现用户、用户详情、班级关联的三阶段一体化创建流程：
     * 1. 第一阶段：创建用户基本信息（user表）
     * 2. 第二阶段：创建用户详情信息（user_profile表）
     * 3. 第三阶段：创建班级关联信息（class_user表，可选）
     *
     * @param userCreateDTO 用户创建数据传输对象，包含用户基本信息、详情信息和班级关联信息
     * @return 用户视图对象
     * @throws RuntimeException 当用户名已存在、邮箱已存在、手机号已存在、班级不存在或用户重复加入班级时
     */
    UserVO createUser(UserCreateDTO userCreateDTO);

    /**
     * 更新用户信息
     *
     * @param userDTO 用户更新数据传输对象
     * @return 更新后的用户视图对象
     */
    UserVO updateUser(UserUpdateDTO userDTO);

    /**
     * 根据ID获取用户信息
     *
     * @param id 用户ID
     * @return 用户详细信息
     */
    UserVO getUserById(Long id);

    /**
     * 根据用户名获取用户信息
     *
     * @param username 用户名
     * @return 用户详细信息，如果不存在则返回null
     */
    UserVO getUserByUsername(String username);

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
    UserBasicVO getUserByUsernameClient(String username);

    /**
     * 分页查询用户列表
     *
     * @param pageDTO 分页查询参数，包含分页信息和查询条件
     * @return 分页用户列表
     */
    PageVO<UserVO> listUsersByPage(PageDTO<UserQueryDTO> pageDTO);

    /**
     * 根据ID删除用户（逻辑删除）
     *
     * @param id 用户ID
     * @return 删除结果，true表示删除成功，false表示删除失败
     */
    boolean deleteUser(Long id);

    /**
     * 断言用户存在
     * <p>
     * 检查指定ID的用户是否存在，如果不存在则抛出异常
     *
     * @param userId 用户ID
     * @throws RuntimeException 如果用户不存在
     */
    void assertUserExists(Long userId);

    /**
     * 根据用户ID集合获取用户ID到用户名的映射
     *
     * @param userIds 用户ID集合
     * @return 用户ID到用户名的映射Map，key为用户ID，value为用户名
     */
    Map<Long, String> getUserMapByIds(Set<Long> userIds);

    /**
     * 检查用户是否存在
     *
     * @param userId 用户ID
     * @return true表示用户存在，false表示用户不存在或已删除
     */
    Boolean checkUserExists(Long userId);
} 