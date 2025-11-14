package com.hngy.siae.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.UserResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.user.dto.request.DepartmentCreateDTO;
import com.hngy.siae.user.dto.response.DepartmentVO;
import com.hngy.siae.user.entity.Department;
import com.hngy.siae.user.mapper.DepartmentMapper;
import com.hngy.siae.user.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 部门服务实现类
 *
 * @author KEYKB
 */
@Service
@RequiredArgsConstructor
public class DepartmentServiceImpl
        extends ServiceImpl<DepartmentMapper, Department>
        implements DepartmentService {

    /**
     * 创建部门
     *
     * @param createDTO 部门创建参数
     * @return 创建成功的部门信息
     */
    @Override
    public DepartmentVO createDepartment(DepartmentCreateDTO createDTO) {
        // 检查部门名称是否已存在
        boolean exists = lambdaQuery()
                .eq(Department::getName, createDTO.getName())
                .exists();
        AssertUtils.isFalse(exists, UserResultCodeEnum.DEPARTMENT_ALREADY_EXISTS);

        // 创建部门
        Department department = BeanConvertUtil.to(createDTO, Department.class);
        save(department);

        return BeanConvertUtil.to(department, DepartmentVO.class);
    }

    /**
     * 更新部门
     *
     * @param id 部门ID
     * @param name 部门名称
     * @return 更新后的部门信息
     */
    @Override
    public DepartmentVO updateDepartment(Long id, String name) {
        // 检查部门是否存在
        Department department = getById(id);
        AssertUtils.notNull(department, UserResultCodeEnum.DEPARTMENT_NOT_FOUND);

        // 如果更新名称，检查是否与其他部门冲突
        if (StrUtil.isNotBlank(name) && !name.equals(department.getName())) {
            boolean exists = lambdaQuery()
                    .eq(Department::getName, name)
                    .ne(Department::getId, id)
                    .exists();
            AssertUtils.isFalse(exists, UserResultCodeEnum.DEPARTMENT_ALREADY_EXISTS);
            department.setName(name);
        }

        updateById(department);
        return BeanConvertUtil.to(department, DepartmentVO.class);
    }

    /**
     * 根据ID查询部门
     *
     * @param id 部门ID
     * @return 部门信息
     */
    @Override
    public DepartmentVO getDepartmentById(Long id) {
        Department department = getById(id);
        AssertUtils.notNull(department, UserResultCodeEnum.DEPARTMENT_NOT_FOUND);
        return BeanConvertUtil.to(department, DepartmentVO.class);
    }

    /**
     * 查询所有部门
     *
     * @return 部门列表
     */
    @Override
    public List<DepartmentVO> listAllDepartments() {
        List<Department> departments = list();
        return BeanConvertUtil.toList(departments, DepartmentVO.class);
    }

    /**
     * 删除部门
     *
     * @param id 部门ID
     * @return 删除结果
     */
    @Override
    public Boolean deleteDepartment(Long id) {
        // 检查部门是否存在
        Department department = getById(id);
        AssertUtils.notNull(department, UserResultCodeEnum.DEPARTMENT_NOT_FOUND);

        return removeById(id);
    }
}
