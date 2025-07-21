package com.hngy.siae.user.service;


import com.hngy.siae.common.dto.request.PageDTO;
import com.hngy.siae.common.dto.response.PageVO;
import com.hngy.siae.user.dto.request.AwardTypeDTO;
import com.hngy.siae.user.dto.response.AwardTypeVO;

import java.util.List;

/**
 * 奖项类型字典服务接口
 *
 * @author KEYKB
 */
public interface AwardTypeService {

    /**
     * 创建奖项类型
     *
     * @param awardTypeDTO 奖项类型实体
     * @return 奖项类型实体
     */
    AwardTypeVO createAwardType(AwardTypeDTO awardTypeDTO);

    /**
     * 更新奖项类型
     *
     * @param awardTypeDTO 奖项类型实体
     * @return 奖项类型实体
     */
    AwardTypeVO updateAwardType(AwardTypeDTO awardTypeDTO);

    /**
     * 根据ID获取奖项类型
     *
     * @param id 奖项类型ID
     * @return 奖项类型实体
     */
    AwardTypeVO getAwardTypeById(Long id);

    /**
     * 根据名称获取奖项类型
     *
     * @param name 奖项类型名称
     * @return 奖项类型实体
     */
    AwardTypeVO getAwardTypeByName(String name);

    /**
     * 获取所有奖项类型
     *
     * @return 奖项类型实体列表
     */
    List<AwardTypeVO> listAllAwardTypes();

    /**
     * 分页查询奖项类型
     *
     * @param pageDTO 分页参数
     * @return 分页奖项类型实体
     */
    PageVO<AwardTypeVO> listAwardTypesByPage(PageDTO<AwardTypeDTO> pageDTO);

    /**
     * 根据ID删除奖项类型
     *
     * @param id 奖项类型ID
     * @return 是否删除成功
     */
    boolean deleteAwardType(Long id);
} 