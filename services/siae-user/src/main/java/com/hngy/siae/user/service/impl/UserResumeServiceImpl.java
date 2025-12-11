package com.hngy.siae.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.UserResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.security.utils.SecurityUtil;
import com.hngy.siae.user.dto.request.UserResumeCreateDTO;
import com.hngy.siae.user.dto.request.UserResumeUpdateDTO;
import com.hngy.siae.user.dto.response.UserResumeVO;
import com.hngy.siae.user.entity.UserResume;
import com.hngy.siae.user.mapper.UserResumeMapper;
import com.hngy.siae.user.service.UserResumeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 用户简历服务实现类
 * <p>
 * 提供用户简历的增删改查功能实现。每个用户只能有一份简历。
 *
 * @author KEYKB
 */
@Service
@RequiredArgsConstructor
public class UserResumeServiceImpl
        extends ServiceImpl<UserResumeMapper, UserResume>
        implements UserResumeService {

    private final SecurityUtil securityUtil;

    /**
     * 创建简历
     * <p>
     * 为当前登录用户创建简历，如果用户已有简历则抛出异常
     *
     * @param dto 简历创建参数
     * @return 创建成功的简历信息
     */
    @Override
    public UserResumeVO createResume(UserResumeCreateDTO dto) {
        Long userId = securityUtil.getCurrentUserId();

        // 检查用户是否已有简历
        LambdaQueryWrapper<UserResume> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserResume::getUserId, userId);
        UserResume existingResume = getOne(queryWrapper);
        AssertUtils.isNull(existingResume, UserResultCodeEnum.RESUME_ALREADY_EXISTS);

        // 创建简历
        UserResume resume = BeanConvertUtil.to(dto, UserResume.class);
        resume.setUserId(userId);
        resume.setCreatedAt(LocalDateTime.now());
        resume.setUpdatedAt(LocalDateTime.now());
        save(resume);

        return BeanConvertUtil.to(resume, UserResumeVO.class);
    }

    /**
     * 获取当前用户简历
     * <p>
     * 返回当前登录用户的简历信息，如果不存在则返回null
     *
     * @return 用户简历信息，不存在时返回null
     */
    @Override
    public UserResumeVO getMyResume() {
        Long userId = securityUtil.getCurrentUserId();

        LambdaQueryWrapper<UserResume> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserResume::getUserId, userId);
        UserResume resume = getOne(queryWrapper);

        if (resume == null) {
            return null;
        }

        return BeanConvertUtil.to(resume, UserResumeVO.class);
    }

    /**
     * 更新简历
     * <p>
     * 更新当前登录用户的简历信息，如果简历不存在则抛出异常
     *
     * @param dto 简历更新参数
     * @return 更新后的简历信息
     */
    @Override
    public UserResumeVO updateResume(UserResumeUpdateDTO dto) {
        Long userId = securityUtil.getCurrentUserId();

        // 查询当前用户的简历
        LambdaQueryWrapper<UserResume> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserResume::getUserId, userId);
        UserResume existingResume = getOne(queryWrapper);
        AssertUtils.notNull(existingResume, UserResultCodeEnum.RESUME_NOT_FOUND);

        // 更新简历字段（只更新非空字段）
        if (dto.getAvatar() != null) {
            existingResume.setAvatar(dto.getAvatar());
        }
        if (dto.getName() != null) {
            existingResume.setName(dto.getName());
        }
        if (dto.getGender() != null) {
            existingResume.setGender(dto.getGender());
        }
        if (dto.getAge() != null) {
            existingResume.setAge(dto.getAge());
        }
        if (dto.getWorkStatus() != null) {
            existingResume.setWorkStatus(dto.getWorkStatus());
        }
        if (dto.getPhone() != null) {
            existingResume.setPhone(dto.getPhone());
        }
        if (dto.getWechat() != null) {
            existingResume.setWechat(dto.getWechat());
        }
        if (dto.getJobStatus() != null) {
            existingResume.setJobStatus(dto.getJobStatus());
        }
        if (dto.getGraduationYear() != null) {
            existingResume.setGraduationYear(dto.getGraduationYear());
        }
        if (dto.getExpectedJobs() != null) {
            existingResume.setExpectedJobs(dto.getExpectedJobs());
        }
        if (dto.getAdvantages() != null) {
            existingResume.setAdvantages(dto.getAdvantages());
        }
        if (dto.getWorkExperience() != null) {
            existingResume.setWorkExperience(dto.getWorkExperience());
        }
        if (dto.getProjects() != null) {
            existingResume.setProjects(dto.getProjects());
        }
        if (dto.getEducation() != null) {
            existingResume.setEducation(dto.getEducation());
        }
        if (dto.getAwards() != null) {
            existingResume.setAwards(dto.getAwards());
        }

        existingResume.setUpdatedAt(LocalDateTime.now());
        updateById(existingResume);

        return BeanConvertUtil.to(existingResume, UserResumeVO.class);
    }

    /**
     * 删除简历
     * <p>
     * 逻辑删除当前登录用户的简历，如果简历不存在则抛出异常
     *
     * @return 删除结果，true表示删除成功
     */
    @Override
    public boolean deleteResume() {
        Long userId = securityUtil.getCurrentUserId();

        // 查询当前用户的简历
        LambdaQueryWrapper<UserResume> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserResume::getUserId, userId);
        UserResume existingResume = getOne(queryWrapper);
        AssertUtils.notNull(existingResume, UserResultCodeEnum.RESUME_NOT_FOUND);

        // 逻辑删除（MyBatis-Plus @TableLogic 会自动处理）
        return removeById(existingResume.getId());
    }
}
