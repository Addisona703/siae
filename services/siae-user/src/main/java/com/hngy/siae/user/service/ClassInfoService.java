package com.hngy.siae.user.service;

import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.user.dto.request.ClassInfoDTO;
import com.hngy.siae.user.dto.response.ClassInfoVO;


import java.util.List;

/**
 * 班级服务接口
 *
 * @author KEYKB
 */
public interface ClassInfoService {

    /**
     * 创建班级
     *
     * @param classInfoDTO 班级数据传输对象
     * @return 班级视图对象
     */
    ClassInfoVO createClass(ClassInfoDTO classInfoDTO);

    /**
     * 更新班级
     *
     * @param classInfoDTO 班级数据传输对象
     * @return 班级视图对象
     */
    ClassInfoVO updateClass(ClassInfoDTO classInfoDTO);

    /**
     * 根据ID获取班级
     *
     * @param id 班级ID
     * @return 班级视图对象
     */
    ClassInfoVO getClassById(Long id);

    /**
     * 分页查询班级列表
     *
     * @param pageDTO 分页参数
     * @return 分页班级视图对象
     */
    PageVO<ClassInfoVO> listClassesByPage(PageDTO<ClassInfoDTO> pageDTO);

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
     * 根据ID删除班级（逻辑删除）
     *
     * @param id 班级ID
     * @return 是否删除成功
     */
    boolean deleteClass(Long id);
} 