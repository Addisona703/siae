package com.hngy.siae.user.service;

import com.hngy.siae.user.dto.request.DepartmentCreateDTO;
import com.hngy.siae.user.dto.response.DepartmentVO;

import java.util.List;

/**
 * 部门服务接口
 *
 * @author KEYKB
 */
public interface DepartmentService {

    /**
     * 创建部门
     */
    DepartmentVO createDepartment(DepartmentCreateDTO createDTO);

    /**
     * 更新部门
     */
    DepartmentVO updateDepartment(Long id, String name);

    /**
     * 根据ID查询部门
     */
    DepartmentVO getDepartmentById(Long id);

    /**
     * 查询所有部门
     */
    List<DepartmentVO> listAllDepartments();

    /**
     * 删除部门
     */
    Boolean deleteDepartment(Long id);
}
