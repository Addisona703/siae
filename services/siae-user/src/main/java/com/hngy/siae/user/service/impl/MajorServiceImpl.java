package com.hngy.siae.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.UserResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.user.dto.request.MajorCreateDTO;
import com.hngy.siae.user.dto.response.MajorVO;
import com.hngy.siae.user.entity.Major;
import com.hngy.siae.user.mapper.MajorMapper;
import com.hngy.siae.user.service.MajorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 专业服务实现类
 *
 * @author KEYKB
 */
@Service
@RequiredArgsConstructor
public class MajorServiceImpl extends ServiceImpl<MajorMapper, Major>
        implements MajorService {

    /**
     * 创建专业
     *
     * @param createDTO 专业创建参数
     * @return 创建成功的专业信息
     */
    @Override
    public MajorVO createMajor(MajorCreateDTO createDTO) {
        // 检查专业名称是否已存在
        boolean nameExists = lambdaQuery()
                .eq(Major::getName, createDTO.getName())
                .exists();
        AssertUtils.isFalse(nameExists, UserResultCodeEnum.MAJOR_ALREADY_EXISTS);

        // 检查专业编码是否已存在（如果提供了编码）
        if (StrUtil.isNotBlank(createDTO.getCode())) {
            boolean codeExists = lambdaQuery()
                    .eq(Major::getCode, createDTO.getCode())
                    .exists();
            AssertUtils.isFalse(codeExists, UserResultCodeEnum.MAJOR_CODE_ALREADY_EXISTS);
        }

        // 创建专业
        Major major = BeanConvertUtil.to(createDTO, Major.class);
        save(major);

        return BeanConvertUtil.to(major, MajorVO.class);
    }

    /**
     * 更新专业
     *
     * @param id 专业ID
     * @param name 专业名称
     * @param code 专业编码
     * @param abbr 专业简称
     * @param collegeName 所属学院名称
     * @return 更新后的专业信息
     */
    @Override
    public MajorVO updateMajor(Long id, String name, String code, String abbr, String collegeName) {
        // 检查专业是否存在
        Major major = getById(id);
        AssertUtils.notNull(major, UserResultCodeEnum.MAJOR_NOT_FOUND);

        // 如果更新名称，检查是否与其他专业冲突
        if (StrUtil.isNotBlank(name) && !name.equals(major.getName())) {
            boolean exists = lambdaQuery()
                    .eq(Major::getName, name)
                    .ne(Major::getId, id)
                    .exists();
            AssertUtils.isFalse(exists, UserResultCodeEnum.MAJOR_ALREADY_EXISTS);
            major.setName(name);
        }

        // 如果更新编码，检查是否与其他专业冲突
        if (StrUtil.isNotBlank(code) && !code.equals(major.getCode())) {
            boolean exists = lambdaQuery()
                    .eq(Major::getCode, code)
                    .ne(Major::getId, id)
                    .exists();
            AssertUtils.isFalse(exists, UserResultCodeEnum.MAJOR_CODE_ALREADY_EXISTS);
            major.setCode(code);
        }

        // 更新其他字段
        if (StrUtil.isNotBlank(abbr)) {
            major.setAbbr(abbr);
        }
        if (StrUtil.isNotBlank(collegeName)) {
            major.setCollegeName(collegeName);
        }

        updateById(major);
        return BeanConvertUtil.to(major, MajorVO.class);
    }

    /**
     * 根据ID查询专业
     *
     * @param id 专业ID
     * @return 专业信息
     */
    @Override
    public MajorVO getMajorById(Long id) {
        Major major = getById(id);
        AssertUtils.notNull(major, UserResultCodeEnum.MAJOR_NOT_FOUND);
        return BeanConvertUtil.to(major, MajorVO.class);
    }

    /**
     * 查询所有专业
     *
     * @return 专业列表
     */
    @Override
    public List<MajorVO> listAllMajors() {
        // 添加查询条件以通过 IllegalSQLInnerInterceptor
        LambdaQueryWrapper<Major> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.isNotNull(Major::getId);  // 添加一个永真条件
        List<Major> majors = list(queryWrapper);
        return BeanConvertUtil.toList(majors, MajorVO.class);
    }

    /**
     * 删除专业
     *
     * @param id 专业ID
     * @return 删除结果
     */
    @Override
    public Boolean deleteMajor(Long id) {
        // 检查专业是否存在
        Major major = getById(id);
        AssertUtils.notNull(major, UserResultCodeEnum.MAJOR_NOT_FOUND);

        return removeById(id);
    }
}
