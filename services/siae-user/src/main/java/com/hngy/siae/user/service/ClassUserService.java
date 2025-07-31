package com.hngy.siae.user.service;

import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.user.dto.request.ClassUserCreateDTO;
import com.hngy.siae.user.dto.request.ClassUserQueryDTO;
import com.hngy.siae.user.dto.request.ClassUserUpdateDTO;
import com.hngy.siae.user.dto.response.ClassUserVO;

import java.util.List;

/**
 * 班级用户关联服务接口
 * <p>
 * 提供班级用户关联的增删改查功能，包括添加用户到班级、更新关联信息、查询和删除关联关系。
 * 支持分页查询和条件查询，支持按班级、成员类型、用户状态等条件筛选。
 *
 * @author KEYKB
 */
public interface ClassUserService {

    /**
     * 添加用户到班级
     *
     * @param classUserCreateDTO 班级用户关联创建参数
     * @return 班级用户关联信息
     */
    ClassUserVO addUserToClass(ClassUserCreateDTO classUserCreateDTO);

    /**
     * 更新用户班级关联信息
     *
     * @param classUserUpdateDTO 班级用户关联更新参数
     * @return 更新后的班级用户关联信息
     */
    ClassUserVO updateClassUser(ClassUserUpdateDTO classUserUpdateDTO);

    /**
     * 根据ID获取班级用户关联信息
     *
     * @param id 关联记录ID
     * @return 班级用户关联详细信息
     */
    ClassUserVO getClassUserById(Long id);

    /**
     * 分页查询班级用户关联列表
     *
     * @param pageDTO 分页查询参数
     * @return 分页班级用户关联列表
     */
    PageVO<ClassUserVO> listClassUsersByPage(PageDTO<ClassUserQueryDTO> pageDTO);

    /**
     * 根据班级ID获取用户列表
     *
     * @param classId 班级ID
     * @return 班级下的用户列表
     */
    List<ClassUserVO> listUsersByClassId(Long classId);

    /**
     * 从班级移除用户
     *
     * @param classId 班级ID
     * @param userId 用户ID
     * @return 移除结果，true表示移除成功，false表示移除失败
     */
    boolean removeUserFromClass(Long classId, Long userId);

    /**
     * 根据ID删除班级用户关联（逻辑删除）
     *
     * @param id 关联记录ID
     * @return 删除结果，true表示删除成功，false表示删除失败
     */
    boolean deleteClassUser(Long id);
}
