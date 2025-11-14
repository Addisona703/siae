package com.hngy.siae.user.service;

import com.hngy.siae.user.dto.request.PositionCreateDTO;
import com.hngy.siae.user.dto.response.PositionVO;

import java.util.List;

/**
 * 职位服务接口
 *
 * @author KEYKB
 */
public interface PositionService {

    /**
     * 创建职位
     */
    PositionVO createPosition(PositionCreateDTO createDTO);

    /**
     * 更新职位
     */
    PositionVO updatePosition(Long id, String name);

    /**
     * 根据ID查询职位
     */
    PositionVO getPositionById(Long id);

    /**
     * 查询所有职位
     */
    List<PositionVO> listAllPositions();

    /**
     * 删除职位
     */
    Boolean deletePosition(Long id);
}
