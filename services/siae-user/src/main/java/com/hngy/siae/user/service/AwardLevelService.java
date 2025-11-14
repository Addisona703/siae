package com.hngy.siae.user.service;

import com.hngy.siae.user.dto.request.AwardLevelCreateDTO;
import com.hngy.siae.user.dto.response.AwardLevelVO;

import java.util.List;

/**
 * 奖项等级字典服务接口
 *
 * @author KEYKB
 */
public interface AwardLevelService {

    /**
     * 创建奖项等级
     *
     * @param awardLevelCreateDTO 奖项等级创建参数
     * @return 奖项等级实体
     */
    AwardLevelVO createAwardLevel(AwardLevelCreateDTO awardLevelCreateDTO);

    /**
     * 更新奖项等级
     *
     * @param id 奖项等级ID
     * @param name 奖项等级名称
     * @param orderId 排序ID
     * @return 奖项等级实体
     */
    AwardLevelVO updateAwardLevel(Long id, String name, Integer orderId);

    /**
     * 根据ID获取奖项等级
     *
     * @param id 奖项等级ID
     * @return 奖项等级实体
     */
    AwardLevelVO getAwardLevelById(Long id);

    /**
     * 获取所有奖项等级（字典数据）
     *
     * @return 奖项等级实体列表，按orderId排序
     */
    List<AwardLevelVO> listAllAwardLevels();

    /**
     * 根据ID删除奖项等级
     *
     * @param id 奖项等级ID
     * @return 是否删除成功
     */
    boolean deleteAwardLevel(Long id);
} 