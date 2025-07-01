package com.hngy.siae.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.common.dto.request.PageDTO;
import com.hngy.siae.common.dto.response.PageVO;
import com.hngy.siae.user.dto.request.LoginDTO;
import com.hngy.siae.user.dto.request.UserDTO;
import com.hngy.siae.user.dto.request.UserQueryDTO;
import com.hngy.siae.user.dto.request.UserUpdateDTO;
import com.hngy.siae.user.dto.response.UserVO;
import com.hngy.siae.user.entity.User;

/**
 * 用户服务接口
 *
 * @author KEYKB
 */
public interface UserService extends IService<User> {

    // 登录
    UserVO login(LoginDTO loginDTO);

    /**
     * 创建用户
     *
     * @param userDTO 用户数据传输对象
     * @return 用户视图对象
     */
    UserVO register(UserDTO userDTO);

    /**
     * 更新用户
     *
     * @param userDTO 用户数据传输对象
     * @return 用户视图对象
     */
    UserVO updateUser(UserUpdateDTO userDTO);

    /**
     * 根据ID获取用户
     *
     * @param id 用户ID
     * @return 用户视图对象
     */
    UserVO getUserById(Long id);

    /**
     * 根据用户名获取用户
     *
     * @param username 用户名
     * @return 用户视图对象
     */
    UserVO getUserByUsername(String username);

    /**
     * 分页查询用户列表
     *
     * @param pageDTO 分页对象
     * @return 分页用户视图对象
     */
    PageVO<UserVO> listUsersByPage(PageDTO<UserQueryDTO>  pageDTO);

    /**
     * 根据ID删除用户（逻辑删除）
     *
     * @param id 用户ID
     * @return 是否删除成功
     */
    boolean deleteUser(Long id);

    /**
     * 批量删除用户（逻辑删除）
     *
     * @param ids 用户ID列表
     * @return 是否删除成功
     */
//    boolean batchDeleteUsers(List<Long> ids);

    /**
     * 校验用户是否存在，若不存在则抛出业务异常
     * @param userId 用户ID
     */
    void assertUserExists(Long userId);
} 