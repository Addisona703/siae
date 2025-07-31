package com.hngy.siae.user.service;

import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.user.dto.request.AwardLevelCreateDTO;
import com.hngy.siae.user.dto.request.AwardLevelQueryDTO;
import com.hngy.siae.user.dto.request.AwardLevelUpdateDTO;
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
     * @param awardLevelUpdateDTO 奖项等级更新参数
     * @return 奖项等级实体
     */
    AwardLevelVO updateAwardLevel(AwardLevelUpdateDTO awardLevelUpdateDTO);

    /**
     * 根据ID获取奖项等级
     *
     * @param id 奖项等级ID
     * @return 奖项等级实体
     */
    AwardLevelVO getAwardLevelById(Long id);

    /**
     * 根据名称获取奖项等级
     *
     * @param name 奖项等级名称
     * @return 奖项等级实体
     */
    AwardLevelVO getAwardLevelByName(String name);

    /**
     * 获取所有奖项等级
     *
     * @return 奖项等级实体列表
     */
    List<AwardLevelVO> listAllAwardLevels();

    /**
     * 分页查询奖项等级
     *
     * @param pageDTO 分页查询参数
     * @return 分页奖项等级实体
     */
    PageVO<AwardLevelVO> listAwardLevelsByPage(PageDTO<AwardLevelQueryDTO> pageDTO);

    /**
     * 根据ID删除奖项等级
     *
     * @param id 奖项等级ID
     * @return 是否删除成功
     */
    boolean deleteAwardLevel(Long id);
} 