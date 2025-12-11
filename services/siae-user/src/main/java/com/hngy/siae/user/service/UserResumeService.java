package com.hngy.siae.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.user.dto.request.UserResumeCreateDTO;
import com.hngy.siae.user.dto.request.UserResumeUpdateDTO;
import com.hngy.siae.user.dto.response.UserResumeVO;
import com.hngy.siae.user.entity.UserResume;

/**
 * 用户简历服务接口
 * <p>
 * 提供用户简历的增删改查功能。每个用户只能有一份简历。
 *
 * @author KEYKB
 */
public interface UserResumeService extends IService<UserResume> {

    /**
     * 创建简历
     * <p>
     * 为当前登录用户创建简历，如果用户已有简历则抛出异常
     *
     * @param dto 简历创建参数
     * @return 创建成功的简历信息
     * @throws RuntimeException 当用户已有简历时
     */
    UserResumeVO createResume(UserResumeCreateDTO dto);

    /**
     * 获取当前用户简历
     * <p>
     * 返回当前登录用户的简历信息，如果不存在则返回null
     *
     * @return 用户简历信息，不存在时返回null
     */
    UserResumeVO getMyResume();

    /**
     * 更新简历
     * <p>
     * 更新当前登录用户的简历信息，如果简历不存在则抛出异常
     *
     * @param dto 简历更新参数
     * @return 更新后的简历信息
     * @throws RuntimeException 当简历不存在时
     */
    UserResumeVO updateResume(UserResumeUpdateDTO dto);

    /**
     * 删除简历
     * <p>
     * 逻辑删除当前登录用户的简历，如果简历不存在则抛出异常
     *
     * @return 删除结果，true表示删除成功
     * @throws RuntimeException 当简历不存在时
     */
    boolean deleteResume();
}
