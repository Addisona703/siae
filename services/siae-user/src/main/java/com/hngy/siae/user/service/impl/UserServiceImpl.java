package com.hngy.siae.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.UserResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.web.utils.PageConvertUtil;
import com.hngy.siae.user.dto.request.UserDTO;
import com.hngy.siae.user.dto.request.UserQueryDTO;
import com.hngy.siae.user.dto.request.UserUpdateDTO;
import com.hngy.siae.user.dto.response.UserVO;
import com.hngy.siae.user.entity.User;
import com.hngy.siae.user.mapper.UserMapper;
import com.hngy.siae.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;

/**
 * 用户服务实现类
 *
 * @author KEYKB
 */
@Service
@RequiredArgsConstructor
public class UserServiceImpl
        extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Override
    public UserVO createUser(UserDTO userDTO) {
        // 检查用户名是否已存在
        boolean exists = lambdaQuery()
                .eq(User::getUsername, userDTO.getUsername())
                .exists();
        AssertUtils.isFalse(exists, UserResultCodeEnum.USERNAME_ALREADY_EXISTS);

        // 构建用户实体
        User user = BeanConvertUtil.to(userDTO, User.class);
        
        // 设置默认值
        user.setStatus(1); // 默认启用
        user.setIsDeleted(0); // 默认未删除
        
        // 保存用户
        save(user);
        
        // 转换为视图对象并返回
        return BeanConvertUtil.to(user, UserVO.class);
    }

    @Override
    public UserVO updateUser(UserUpdateDTO userDTO) {
        // 检查用户是否存在
        User user = getById(userDTO.getId());
        AssertUtils.notNull(user, UserResultCodeEnum.USER_NOT_FOUND);
        
        // 如果更新用户名，需要检查是否与其他用户冲突
        if (StringUtils.hasText(userDTO.getUsername()) && !userDTO.getUsername().equals(user.getUsername())) {
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

    @Override
    public UserVO getUserById(Long id) {
        User user = getById(id);
        AssertUtils.notNull(user, UserResultCodeEnum.USER_NOT_FOUND);
        return BeanConvertUtil.to(user, UserVO.class);
    }

    @Override
    public UserVO getUserByUsername(String username) {
        User user = lambdaQuery()
                .eq(User::getUsername, username)
                .eq(User::getIsDeleted, 0)
                .one();
//        AssertUtils.notNull(user, UserResultCodeEnum.USER_NOT_FOUND);
        return BeanConvertUtil.to(user, UserVO.class);
    }

    @Override
    public PageVO<UserVO> listUsersByPage(PageDTO<UserQueryDTO> pageDTO) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();

        Optional.ofNullable(pageDTO.getParams()).ifPresent(param -> {
            wrapper.like(StringUtils.hasText(param.getUsername()), User::getUsername, param.getUsername())
                    .eq(param.getStatus() != null, User::getStatus, param.getStatus());
        });

        // 排除已删除记录，并按创建时间倒序
        wrapper.eq(User::getIsDeleted, 0)
                .orderByDesc(User::getCreatedAt);

        Page<User> resultPage = page(PageConvertUtil.toPage(pageDTO), wrapper);
        return PageConvertUtil.convert(resultPage, UserVO.class);
    }


    @Override
    public boolean deleteUser(Long id) {
        User user = getById(id);
        AssertUtils.notNull(user, UserResultCodeEnum.USER_NOT_FOUND);
        
        // 逻辑删除用户
        user.setIsDeleted(1);
        return updateById(user);
    }

    @Override
    public void assertUserExists(Long userId) {
        boolean exists = lambdaQuery().eq(User::getId, userId).exists();
        AssertUtils.isTrue(exists, UserResultCodeEnum.USER_NOT_FOUND);
    }
} 