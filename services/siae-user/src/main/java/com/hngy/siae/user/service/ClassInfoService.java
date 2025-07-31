package com.hngy.siae.user.service;

import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.user.dto.request.ClassInfoCreateDTO;
import com.hngy.siae.user.dto.request.ClassInfoQueryDTO;
import com.hngy.siae.user.dto.request.ClassInfoUpdateDTO;
import com.hngy.siae.user.dto.response.ClassInfoVO;


import java.util.List;

/**
 * 班级服务接口
 * <p>
 * 提供班级信息的增删改查功能，包括创建、更新、查询和删除班级信息。
 * 支持分页查询和条件查询，支持按学院、专业、年份查询班级列表。
 *
 * @author KEYKB
 */
public interface ClassInfoService {

    /**
     * 创建班级
     *
     * @param classInfoCreateDTO 班级创建参数
     * @return 班级视图对象
     */
    ClassInfoVO createClass(ClassInfoCreateDTO classInfoCreateDTO);

    /**
     * 更新班级
     *
     * @param classInfoUpdateDTO 班级更新参数
     * @return 班级视图对象
     */
    ClassInfoVO updateClass(ClassInfoUpdateDTO classInfoUpdateDTO);

    /**
     * 根据ID获取班级
     *
     * @param id 班级ID
     * @return 班级视图对象
     */
    ClassInfoVO getClassById(Long id);

    /**
     * 根据学院ID查询班级列表
     *
     * @param collegeId 学院ID
     * @return 班级视图对象列表
     */
    List<ClassInfoVO> listClassesByCollegeId(Long collegeId);

    /**
     * 根据专业ID查询班级列表
     *
     * @param majorId 专业ID
     * @return 班级视图对象列表
     */
    List<ClassInfoVO> listClassesByMajorId(Long majorId);

    /**
     * 根据入学年份查询班级列表
     *
     * @param year 入学年份
     * @return 班级视图对象列表
     */
    List<ClassInfoVO> listClassesByYear(Integer year);

    /**
     * 分页查询班级列表
     *
     * @param pageDTO 分页查询参数
     * @return 分页班级列表
     */
    PageVO<ClassInfoVO> listClassesByPage(PageDTO<ClassInfoQueryDTO> pageDTO);

    /**
     * 根据ID删除班级（逻辑删除）
     *
     * @param id 班级ID
     * @return 是否删除成功
     */
    boolean deleteClass(Long id);
}