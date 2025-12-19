package com.hngy.siae.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.user.entity.UserResume;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户简历 Mapper 接口
 * 
 * @author KEYKB
 */
@Mapper
public interface UserResumeMapper extends BaseMapper<UserResume> {
    
    /**
     * 根据用户ID查询简历（包括软删除的记录）
     * 使用原生SQL绕过 @TableLogic
     */
    @Select("SELECT * FROM user_resume WHERE user_id = #{userId} LIMIT 1")
    UserResume selectByUserIdIncludeDeleted(@Param("userId") Long userId);
    
    /**
     * 更新简历（绕过 @TableLogic，可以更新软删除的记录）
     */
    @Update("UPDATE user_resume SET " +
            "avatar = #{resume.avatar}, " +
            "name = #{resume.name}, " +
            "gender = #{resume.gender}, " +
            "age = #{resume.age}, " +
            "work_status = #{resume.workStatus}, " +
            "phone = #{resume.phone}, " +
            "wechat = #{resume.wechat}, " +
            "job_status = #{resume.jobStatus}, " +
            "graduation_year = #{resume.graduationYear}, " +
            "expected_jobs = #{resume.expectedJobs}, " +
            "advantages = #{resume.advantages}, " +
            "work_experience = #{resume.workExperience}, " +
            "projects = #{resume.projects}, " +
            "education = #{resume.education}, " +
            "awards = #{resume.awards}, " +
            "is_deleted = #{resume.isDeleted}, " +
            "updated_at = #{resume.updatedAt} " +
            "WHERE id = #{resume.id}")
    int updateByIdIgnoreLogic(@Param("resume") UserResume resume);
}
