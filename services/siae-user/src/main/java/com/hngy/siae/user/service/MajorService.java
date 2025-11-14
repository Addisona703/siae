package com.hngy.siae.user.service;

import com.hngy.siae.user.dto.request.MajorCreateDTO;
import com.hngy.siae.user.dto.response.MajorVO;

import java.util.List;

/**
 * 专业服务接口
 *
 * @author KEYKB
 */
public interface MajorService {

    /**
     * 创建专业
     */
    MajorVO createMajor(MajorCreateDTO createDTO);

    /**
     * 更新专业
     */
    MajorVO updateMajor(Long id, String name, String code, String abbr, String collegeName);

    /**
     * 根据ID查询专业
     */
    MajorVO getMajorById(Long id);

    /**
     * 查询所有专业
     */
    List<MajorVO> listAllMajors();

    /**
     * 删除专业
     */
    Boolean deleteMajor(Long id);
}
