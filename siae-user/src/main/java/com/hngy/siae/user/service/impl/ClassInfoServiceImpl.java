package com.hngy.siae.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.common.asserts.AssertUtils;
import com.hngy.siae.common.dto.request.PageDTO;
import com.hngy.siae.common.dto.response.PageVO;
import com.hngy.siae.common.result.UserResultCodeEnum;
import com.hngy.siae.common.utils.BeanConvertUtil;
import com.hngy.siae.common.utils.PageConvertUtil;
import com.hngy.siae.user.dto.request.ClassInfoDTO;
import com.hngy.siae.user.dto.response.ClassInfoVO;
import com.hngy.siae.user.entity.ClassInfo;
import com.hngy.siae.user.entity.Major;
import com.hngy.siae.user.mapper.ClassInfoMapper;
import com.hngy.siae.user.mapper.CollegeMapper;
import com.hngy.siae.user.mapper.MajorMapper;
import com.hngy.siae.user.service.ClassInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

/**
 * 班级服务实现类
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ClassInfoVO createClass(ClassInfoDTO classInfoDTO) {
        AssertUtils.notNull(collegeMapper.selectById(classInfoDTO.getCollegeId()),
                UserResultCodeEnum.COLLEGE_NOT_FOUND);
        AssertUtils.notNull(majorMapper.selectById(classInfoDTO.getMajorId()),
                UserResultCodeEnum.MAJOR_NOT_FOUND);

        boolean exists = lambdaQuery()
                .eq(ClassInfo::getCollegeId, classInfoDTO.getCollegeId())
                .eq(ClassInfo::getMajorId, classInfoDTO.getMajorId())
                .eq(ClassInfo::getYear, classInfoDTO.getYear())
                .eq(ClassInfo::getClassNo, classInfoDTO.getClassNo())
                .exists();
        AssertUtils.isFalse(exists, UserResultCodeEnum.CLASS_ALREADY_EXISTS);

        ClassInfo classInfo = BeanConvertUtil.to(classInfoDTO, ClassInfo.class);
        classInfo.setIsDeleted(0);
        save(classInfo);

        ClassInfoVO classInfoVO = BeanConvertUtil.to(classInfo, ClassInfoVO.class);
        setClassName(classInfoVO);

        return classInfoVO;
    }

    @Override
    public ClassInfoVO updateClass(ClassInfoDTO classInfoDTO) {
        ClassInfo classInfo = getById(classInfoDTO.getId());
        AssertUtils.notNull(classInfo, UserResultCodeEnum.CLASS_NOT_FOUND);

        if (classInfoDTO.getCollegeId() != null && !classInfoDTO.getCollegeId().equals(classInfo.getCollegeId())) {
            AssertUtils.notNull(collegeMapper.selectById(classInfoDTO.getCollegeId()),
                    UserResultCodeEnum.COLLEGE_NOT_FOUND);
        }

        if (classInfoDTO.getMajorId() != null && !classInfoDTO.getMajorId().equals(classInfo.getMajorId())) {
            AssertUtils.notNull(majorMapper.selectById(classInfoDTO.getMajorId()),
                    UserResultCodeEnum.MAJOR_NOT_FOUND);
        }

        if (classInfoDTO.getCollegeId() != null && classInfoDTO.getMajorId() != null
                && classInfoDTO.getYear() != null && classInfoDTO.getClassNo() != null) {
            boolean exists = lambdaQuery()
                    .eq(ClassInfo::getCollegeId, classInfoDTO.getCollegeId())
                    .eq(ClassInfo::getMajorId, classInfoDTO.getMajorId())
                    .eq(ClassInfo::getYear, classInfoDTO.getYear())
                    .eq(ClassInfo::getClassNo, classInfoDTO.getClassNo())
                    .ne(ClassInfo::getId, classInfoDTO.getId())
                    .exists();
            AssertUtils.isFalse(exists, UserResultCodeEnum.CLASS_ALREADY_EXISTS);
        }

        BeanConvertUtil.to(classInfoDTO, classInfo, "id", "isDeleted");
        updateById(classInfo);

        ClassInfoVO classInfoVO = BeanConvertUtil.to(classInfo, ClassInfoVO.class);
        setClassName(classInfoVO);

        return classInfoVO;
    }

    @Override
    public ClassInfoVO getClassById(Long id) {
        ClassInfo classInfo = getById(id);
        AssertUtils.notNull(classInfo, UserResultCodeEnum.CLASS_NOT_FOUND);

        ClassInfoVO classInfoVO = BeanConvertUtil.to(classInfo, ClassInfoVO.class);
        setClassName(classInfoVO);

        return classInfoVO;
    }

    @Override
    public PageVO<ClassInfoVO> listClassesByPage(PageDTO<ClassInfoDTO> pageDTO) {
        LambdaQueryWrapper<ClassInfo> queryWrapper = new LambdaQueryWrapper<>();
        Optional.ofNullable(pageDTO.getParams()).ifPresent(param -> {
            queryWrapper.eq(param.getCollegeId() != null, ClassInfo::getCollegeId, param.getCollegeId())
                    .eq(param.getMajorId() != null, ClassInfo::getMajorId, param.getMajorId())
                    .eq(param.getYear() != null, ClassInfo::getYear, param.getYear())
                    .eq(param.getClassNo() != null, ClassInfo::getClassNo, param.getClassNo());
        });

        queryWrapper.eq(ClassInfo::getIsDeleted, 0)
                .orderByDesc(ClassInfo::getYear)
                .orderByAsc(ClassInfo::getClassNo);

        Page<ClassInfo> page = pageDTO.toPage();
        Page<ClassInfo> resultPage = page(page, queryWrapper);

        PageVO<ClassInfoVO> pageVO = PageConvertUtil.convert(resultPage, ClassInfoVO.class);
        pageVO.getRecords().forEach(this::setClassName);

        return pageVO;
    }

    @Override
    public List<ClassInfoVO> listClassesByCollegeId(Long collegeId) {
        AssertUtils.notNull(collegeMapper.selectById(collegeId), UserResultCodeEnum.COLLEGE_NOT_FOUND);

        return getClassInfoVOS(lambdaQuery()
                .eq(ClassInfo::getCollegeId, collegeId));
    }

    private List<ClassInfoVO> getClassInfoVOS(LambdaQueryChainWrapper<ClassInfo> eq) {
        List<ClassInfo> classInfoList = eq
                .eq(ClassInfo::getIsDeleted, 0)
                .orderByDesc(ClassInfo::getYear)
                .orderByAsc(ClassInfo::getClassNo)
                .list();

        List<ClassInfoVO> classInfoVOList = BeanConvertUtil.toList(classInfoList, ClassInfoVO.class);
        classInfoVOList.forEach(this::setClassName);

        return classInfoVOList;
    }

    @Override
    public List<ClassInfoVO> listClassesByMajorId(Long majorId) {
        AssertUtils.notNull(majorMapper.selectById(majorId), UserResultCodeEnum.MAJOR_NOT_FOUND);

        return getClassInfoVOS(lambdaQuery()
                .eq(ClassInfo::getMajorId, majorId));
    }

    @Override
    public List<ClassInfoVO> listClassesByYear(Integer year) {
        List<ClassInfo> classInfoList = lambdaQuery()
                .eq(ClassInfo::getYear, year)
                .eq(ClassInfo::getIsDeleted, 0)
                // fixME: orderByAsc只支持单参数，拆成链式调用
                .orderByAsc(ClassInfo::getCollegeId)
                .orderByAsc(ClassInfo::getMajorId)
                .orderByAsc(ClassInfo::getClassNo)
                .list();

        List<ClassInfoVO> classInfoVOList = BeanConvertUtil.toList(classInfoList, ClassInfoVO.class);
        classInfoVOList.forEach(this::setClassName);

        return classInfoVOList;
    }

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
    private void setClassName(ClassInfoVO classInfoVO) {
        if (classInfoVO == null) {
            return;
        }

        Major major = majorMapper.selectById(classInfoVO.getMajorId());
        if (major == null) {
            return;
        }

        String majorAbbr = major.getAbbr();
        if (!StringUtils.hasText(majorAbbr)) {
            majorAbbr = major.getName();
        }

        String yearSuffix = "";
        if (classInfoVO.getYear() != null) {
            String yearStr = String.valueOf(classInfoVO.getYear());
            yearSuffix = yearStr.length() > 2 ? yearStr.substring(yearStr.length() - 2) : yearStr;
        }

        String className = majorAbbr + yearSuffix + "-" + classInfoVO.getClassNo();
        classInfoVO.setClassName(className);
    }
}
