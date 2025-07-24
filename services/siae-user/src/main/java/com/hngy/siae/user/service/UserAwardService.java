package com.hngy.siae.user.service;

import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.user.dto.request.UserAwardCreateDTO;
import com.hngy.siae.user.dto.request.UserAwardQueryDTO;
import com.hngy.siae.user.dto.request.UserAwardUpdateDTO;
import com.hngy.siae.user.dto.response.UserAwardVO;

import java.util.List;

/**
 * 用户获奖记录服务接口
 *
 * @author KEYKB
 */
public interface UserAwardService {

    /**
     * 创建用户获奖记录
     *
     * @param userAwardCreateDTO 用户获奖记录数据传输对象
     * @return 用户获奖记录视图对象
     */
    UserAwardVO createUserAward(UserAwardCreateDTO userAwardCreateDTO);

    /**
     * 更新用户获奖记录
     *
     * @param userAwardUpdateDTO 用户获奖记录数据传输对象
     * @return 用户获奖记录视图对象
     */
    UserAwardVO updateUserAward(UserAwardUpdateDTO userAwardUpdateDTO);

    /**
     * 根据ID获取用户获奖记录
     *
     * @param id 获奖记录ID
     * @return 用户获奖记录视图对象
     */
    UserAwardVO getUserAwardById(Long id);

    /**
     * 根据用户ID获取用户获奖记录列表
     *
     * @param userId 用户ID
     * @return 用户获奖记录视图对象列表
     */
    List<UserAwardVO> listUserAwardsByUserId(Long userId);

    /**
     * 分页查询用户获奖记录
     *
     * @param pageDTO<UserAwardDTO> 分页参数
     * @return 分页用户获奖记录视图对象
     */
    PageVO<UserAwardVO> listUserAwardsByPage(PageDTO<UserAwardQueryDTO> pageDTO);

    /**
     * 根据ID删除用户获奖记录
     *
     * @param id 获奖记录ID
     * @return 是否删除成功
     */
    boolean deleteUserAward(Long id);
} 