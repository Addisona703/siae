package com.hngy.siae.user.service;

import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.user.dto.request.UserAwardCreateDTO;
import com.hngy.siae.user.dto.request.UserAwardQueryDTO;
import com.hngy.siae.user.dto.request.UserAwardUpdateDTO;
import com.hngy.siae.user.dto.response.UserAwardVO;

/**
 * 用户获奖记录服务接口
 * <p>
 * 提供用户获奖记录的增删改查功能，包括创建、更新、查询和删除用户获奖记录信息。
 * 支持分页查询和条件查询，支持按用户ID查询获奖记录列表。
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
     * @param userAwardUpdateDTO 用户获奖记录数据传输对象（不含id）
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
     * 根据用户ID分页获取用户获奖记录列表
     *
     * @param userId 用户ID
     * @param pageDTO 分页参数
     * @return 分页用户获奖记录视图对象
     */
    PageVO<UserAwardVO> pageUserAwardsByUserId(Long userId, PageDTO<Void> pageDTO);

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