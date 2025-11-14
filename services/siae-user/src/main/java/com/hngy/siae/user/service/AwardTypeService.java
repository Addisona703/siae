package com.hngy.siae.user.service;

import com.hngy.siae.user.dto.request.AwardTypeCreateDTO;
import com.hngy.siae.user.dto.response.AwardTypeVO;

import java.util.List;

/**
 * 奖项类型字典服务接口
 * <p>
 * 提供奖项类型的增删改查功能，包括创建、更新、查询和删除奖项类型信息。
 * 支持分页查询和条件查询，确保奖项类型名称的唯一性。
 *
 * @author KEYKB
 */
public interface AwardTypeService {

    /**
     * 创建奖项类型
     *
     * @param awardTypeCreateDTO 奖项类型创建参数
     * @return 奖项类型实体
     */
    AwardTypeVO createAwardType(AwardTypeCreateDTO awardTypeCreateDTO);

    /**
     * 更新奖项类型
     *
     * @param id 奖项类型ID
     * @param name 奖项类型名称
     * @param orderId 排序ID
     * @return 奖项类型实体
     */
    AwardTypeVO updateAwardType(Long id, String name, Integer orderId);

    /**
     * 根据ID获取奖项类型
     *
     * @param id 奖项类型ID
     * @return 奖项类型实体
     */
    AwardTypeVO getAwardTypeById(Long id);

    /**
     * 获取所有奖项类型（字典数据）
     *
     * @return 奖项类型实体列表，按orderId排序
     */
    List<AwardTypeVO> listAllAwardTypes();

    /**
     * 根据ID删除奖项类型
     *
     * @param id 奖项类型ID
     * @return 是否删除成功
     */
    boolean deleteAwardType(Long id);
} 