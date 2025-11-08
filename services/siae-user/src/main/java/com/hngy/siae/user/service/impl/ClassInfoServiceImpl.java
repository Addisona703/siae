package com.hngy.siae.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.UserResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.user.dto.request.ClassInfoCreateDTO;
import com.hngy.siae.user.dto.request.ClassInfoQueryDTO;
import com.hngy.siae.user.dto.request.ClassInfoUpdateDTO;
import com.hngy.siae.user.dto.response.ClassInfoVO;
import com.hngy.siae.user.entity.ClassInfo;
import com.hngy.siae.user.entity.College;
import com.hngy.siae.user.entity.Major;
import com.hngy.siae.user.mapper.ClassInfoMapper;
import com.hngy.siae.user.mapper.CollegeMapper;
import com.hngy.siae.user.mapper.MajorMapper;
import com.hngy.siae.user.service.ClassInfoService;
import com.hngy.siae.web.utils.PageConvertUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import cn.hutool.core.util.StrUtil;

import java.util.List;
import java.util.Optional;

/**
 * 班级服务实现类
 * <p>
 * 提供班级信息的增删改查功能，包括创建、更新、查询和删除班级信息。
 * 支持分页查询和条件查询，支持按学院、专业、年份查询班级列表。
 *
 * @author KEYKB
 */
@Service
@RequiredArgsConstructor
public class ClassInfoServiceImpl
        extends ServiceImpl<ClassInfoMapper, ClassInfo>
        implements ClassInfoService {

    private final CollegeMapper collegeMapper;
    private final MajorMapper majorMapper;

    /**
     * 创建班级
     *
     * @param classInfoCreateDTO 班级创建参数
     * @return 创建成功的班级信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassInfoVO createClass(ClassInfoCreateDTO classInfoCreateDTO) {
        College college = collegeMapper.selectById(classInfoCreateDTO.getCollegeId());
        AssertUtils.notNull(college, UserResultCodeEnum.COLLEGE_NOT_FOUND);
        Major major = majorMapper.selectById(classInfoCreateDTO.getMajorId());
        AssertUtils.notNull(major, UserResultCodeEnum.MAJOR_NOT_FOUND);

        boolean exists = lambdaQuery()
                .eq(ClassInfo::getCollegeId, classInfoCreateDTO.getCollegeId())
                .eq(ClassInfo::getMajorId, classInfoCreateDTO.getMajorId())
                .eq(ClassInfo::getYear, classInfoCreateDTO.getYear())
                .eq(ClassInfo::getClassNo, classInfoCreateDTO.getClassNo())
                .exists();
        AssertUtils.isFalse(exists, UserResultCodeEnum.CLASS_ALREADY_EXISTS);

        ClassInfo classInfo = BeanConvertUtil.to(classInfoCreateDTO, ClassInfo.class);
        classInfo.setIsDeleted(0);
        save(classInfo);

        ClassInfoVO classInfoVO = BeanConvertUtil.to(classInfo, ClassInfoVO.class);
        classInfoVO.setCollegeName(college.getName());
        classInfoVO.setMajorName(major.getName());
        classInfoVO.setMajorAbbr(major.getAbbr());
        setClassName(classInfoVO);

        return classInfoVO;
    }

    /**
     * 更新班级信息
     *
     * @param classInfoUpdateDTO 班级更新参数
     * @return 更新后的班级信息
     */
    @Override
    public ClassInfoVO updateClass(ClassInfoUpdateDTO classInfoUpdateDTO) {
        ClassInfo classInfo = getById(classInfoUpdateDTO.getId());
        AssertUtils.notNull(classInfo, UserResultCodeEnum.CLASS_NOT_FOUND);

        College college = collegeMapper.selectById(classInfoUpdateDTO.getCollegeId());
        AssertUtils.notNull(college, UserResultCodeEnum.COLLEGE_NOT_FOUND);
        Major major = majorMapper.selectById(classInfoUpdateDTO.getMajorId());
        AssertUtils.notNull(major, UserResultCodeEnum.MAJOR_NOT_FOUND);

        // 检查班级唯一性（排除当前记录）
        boolean exists = lambdaQuery()
                .eq(ClassInfo::getCollegeId, classInfoUpdateDTO.getCollegeId())
                .eq(ClassInfo::getMajorId, classInfoUpdateDTO.getMajorId())
                .eq(ClassInfo::getYear, classInfoUpdateDTO.getYear())
                .eq(ClassInfo::getClassNo, classInfoUpdateDTO.getClassNo())
                .ne(ClassInfo::getId, classInfoUpdateDTO.getId())
                .exists();
        AssertUtils.isFalse(exists, UserResultCodeEnum.CLASS_ALREADY_EXISTS);

        BeanConvertUtil.to(classInfoUpdateDTO, classInfo, "id", "isDeleted");
        updateById(classInfo);

        ClassInfoVO classInfoVO = BeanConvertUtil.to(classInfo, ClassInfoVO.class);
        classInfoVO.setCollegeName(college.getName());
        classInfoVO.setMajorName(major.getName());
        classInfoVO.setMajorAbbr(major.getAbbr());
        setClassName(classInfoVO);

        return classInfoVO;
    }

    /**
     * 根据ID获取班级信息
     *
     * @param id 班级ID
     * @return 班级详细信息
     */
    @Override
    public ClassInfoVO getClassById(Long id) {
        ClassInfoVO classInfoVO = baseMapper.selectClassDetailById(id);
        AssertUtils.notNull(classInfoVO, UserResultCodeEnum.CLASS_NOT_FOUND);
        setClassName(classInfoVO);

        return classInfoVO;
    }

    /**
     * 分页查询班级列表
     *
     * @param pageDTO 分页查询参数，包含分页信息和查询条件
     * @return 分页班级列表
     */
    @Override
    public PageVO<ClassInfoVO> listClassesByPage(PageDTO<ClassInfoQueryDTO> pageDTO) {
        LambdaQueryWrapper<ClassInfo> queryWrapper = new LambdaQueryWrapper<>();
        Optional.ofNullable(pageDTO.getParams()).ifPresent(param ->
                queryWrapper.eq(param.getId() != null, ClassInfo::getId, param.getId())
                .eq(param.getCollegeId() != null, ClassInfo::getCollegeId, param.getCollegeId())
                .eq(param.getMajorId() != null, ClassInfo::getMajorId, param.getMajorId())
                .eq(param.getYear() != null, ClassInfo::getYear, param.getYear())
                .eq(param.getClassNo() != null, ClassInfo::getClassNo, param.getClassNo()));

        queryWrapper.eq(ClassInfo::getIsDeleted, 0)
                .orderByDesc(ClassInfo::getYear)
                .orderByAsc(ClassInfo::getClassNo);

        Page<ClassInfo> page = PageConvertUtil.toPage(pageDTO);
        Page<ClassInfo> resultPage = page(page, queryWrapper);

        PageVO<ClassInfoVO> pageVO = PageConvertUtil.convert(resultPage, ClassInfoVO.class);
        pageVO.getRecords().forEach(this::fillMajorInfoAndSetClassName);

        return pageVO;
    }

    /**
     * 根据学院ID获取班级列表
     *
     * @param collegeId 学院ID
     * @return 班级列表，按年份降序、班号升序排列
     */
    @Override
    public List<ClassInfoVO> listClassesByCollegeId(Long collegeId) {
        AssertUtils.notNull(collegeMapper.selectById(collegeId), UserResultCodeEnum.COLLEGE_NOT_FOUND);

        return getClassInfoVOS(lambdaQuery()
                .eq(ClassInfo::getCollegeId, collegeId));
    }

    /**
     * 获取班级信息列表的通用方法
     *
     * @param queryChain 查询链
     * @return 班级信息列表
     */
    private List<ClassInfoVO> getClassInfoVOS(LambdaQueryChainWrapper<ClassInfo> queryChain) {
        List<ClassInfo> classInfoList = queryChain
                .eq(ClassInfo::getIsDeleted, 0)
                .orderByDesc(ClassInfo::getYear)
                .orderByAsc(ClassInfo::getClassNo)
                .list();

        List<ClassInfoVO> classInfoVOList = BeanConvertUtil.toList(classInfoList, ClassInfoVO.class);
        classInfoVOList.forEach(this::fillMajorInfoAndSetClassName);

        return classInfoVOList;
    }

    /**
     * 根据专业ID获取班级列表
     *
     * @param majorId 专业ID
     * @return 班级列表，按年份降序、班号升序排列
     */
    @Override
    public List<ClassInfoVO> listClassesByMajorId(Long majorId) {
        AssertUtils.notNull(majorMapper.selectById(majorId), UserResultCodeEnum.MAJOR_NOT_FOUND);

        return getClassInfoVOS(lambdaQuery()
                .eq(ClassInfo::getMajorId, majorId));
    }

    /**
     * 根据入学年份获取班级列表
     *
     * @param year 入学年份
     * @return 班级列表，按学院、专业、班号升序排列
     */
    @Override
    public List<ClassInfoVO> listClassesByYear(Integer year) {
        List<ClassInfo> classInfoList = lambdaQuery()
                .eq(ClassInfo::getYear, year)
                .eq(ClassInfo::getIsDeleted, 0)
                .orderByAsc(ClassInfo::getCollegeId)
                .orderByAsc(ClassInfo::getMajorId)
                .orderByAsc(ClassInfo::getClassNo)
                .list();

        List<ClassInfoVO> classInfoVOList = BeanConvertUtil.toList(classInfoList, ClassInfoVO.class);
        classInfoVOList.forEach(this::fillMajorInfoAndSetClassName);

        return classInfoVOList;
    }

    /**
     * 根据ID删除班级（逻辑删除）
     *
     * @param id 班级ID
     * @return 删除结果，true表示删除成功，false表示删除失败
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteClass(Long id) {
        ClassInfo classInfo = getById(id);
        AssertUtils.notNull(classInfo, UserResultCodeEnum.CLASS_NOT_FOUND);

        // TODO: 检查是否有学生关联该班级，如有则不允许删除

        classInfo.setIsDeleted(1);
        return updateById(classInfo);
    }

    /**
     * 设置班级名称，格式：专业简称 + 年份后两位 + "-" + 班号
     * 例如：移应23-1
     */
    private void fillMajorInfoAndSetClassName(ClassInfoVO classInfoVO) {
        fillMajorInfoIfNeeded(classInfoVO);
        setClassName(classInfoVO);
    }

    private void fillMajorInfoIfNeeded(ClassInfoVO classInfoVO) {
        if (classInfoVO == null || classInfoVO.getMajorId() == null) {
            return;
        }

        boolean hasName = StrUtil.isNotBlank(classInfoVO.getMajorName());
        boolean hasAbbr = StrUtil.isNotBlank(classInfoVO.getMajorAbbr());

        if (hasName && hasAbbr) {
            return;
        }

        Major major = majorMapper.selectById(classInfoVO.getMajorId());
        if (major == null) {
            return;
        }

        if (!hasName) {
            classInfoVO.setMajorName(major.getName());
        }
        if (!hasAbbr && StrUtil.isNotBlank(major.getAbbr())) {
            classInfoVO.setMajorAbbr(major.getAbbr());
        }
    }

    private void setClassName(ClassInfoVO classInfoVO) {
        if (classInfoVO == null) {
            return;
        }

        String yearSuffix = "";
        if (classInfoVO.getYear() != null) {
            String yearStr = String.valueOf(classInfoVO.getYear());
            yearSuffix = yearStr.length() > 2 ? yearStr.substring(yearStr.length() - 2) : yearStr;
        }

        String className = classInfoVO.getMajorAbbr() + yearSuffix + "-" + classInfoVO.getClassNo();
        classInfoVO.setClassName(className);
    }
}
