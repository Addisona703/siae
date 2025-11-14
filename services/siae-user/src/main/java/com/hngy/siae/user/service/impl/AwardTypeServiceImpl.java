package com.hngy.siae.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.UserResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.user.dto.request.AwardTypeCreateDTO;
import com.hngy.siae.user.dto.response.AwardTypeVO;
import com.hngy.siae.user.entity.AwardType;
import com.hngy.siae.user.mapper.AwardTypeMapper;
import com.hngy.siae.user.service.AwardTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 奖项类型字典服务实现类
 * <p>
 * 提供奖项类型的增删改查功能，包括创建、更新、查询和删除奖项类型信息。
 * 支持分页查询和条件查询，确保奖项类型名称的唯一性。
 *
 * @author KEYKB
 */
@Service
@RequiredArgsConstructor
public class AwardTypeServiceImpl 
        extends ServiceImpl<AwardTypeMapper, AwardType> 
        implements AwardTypeService {

    /**
     * 创建奖项类型
     *
     * @param awardTypeCreateDTO 奖项类型创建参数
     * @return 创建成功的奖项类型信息
     */
    @Override
    public AwardTypeVO createAwardType(AwardTypeCreateDTO awardTypeCreateDTO) {
        // 检查名称是否已存在
        LambdaQueryWrapper<AwardType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AwardType::getName, awardTypeCreateDTO.getName());
        AwardType existAwardType = getOne(queryWrapper);
        AssertUtils.isNull(existAwardType, UserResultCodeEnum.AWARD_TYPE_ALREADY_EXISTS);

        // 保存奖项类型
        AwardType awardType = BeanConvertUtil.to(awardTypeCreateDTO, AwardType.class);
        save(awardType);

        return BeanConvertUtil.to(awardType, AwardTypeVO.class);
    }

    /**
     * 更新奖项类型信息
     *
     * @param id 奖项类型ID
     * @param name 奖项类型名称
     * @param orderId 排序ID
     * @return 更新后的奖项类型信息
     */
    @Override
    public AwardTypeVO updateAwardType(Long id, String name, Integer orderId) {
        // 检查奖项类型是否存在
        AwardType existAwardType = getById(id);
        AssertUtils.notNull(existAwardType, UserResultCodeEnum.AWARD_TYPE_NOT_FOUND);

        // 如果更新名称，需要检查是否与其他奖项类型冲突
        if (StrUtil.isNotBlank(name) && !name.equals(existAwardType.getName())) {
            boolean exists = lambdaQuery().eq(AwardType::getName, name).exists();
            AssertUtils.isFalse(exists, UserResultCodeEnum.AWARD_TYPE_ALREADY_EXISTS);
        }

        // 更新奖项类型信息
        if (StrUtil.isNotBlank(name)) {
            existAwardType.setName(name);
        }
        if (orderId != null) {
            existAwardType.setOrderId(orderId);
        }
        updateById(existAwardType);

        return BeanConvertUtil.to(existAwardType, AwardTypeVO.class);
    }

    /**
     * 根据ID获取奖项类型信息
     *
     * @param id 奖项类型ID
     * @return 奖项类型详细信息
     */
    @Override
    public AwardTypeVO getAwardTypeById(Long id) {
        AwardType awardType = getById(id);
        AssertUtils.notNull(awardType, UserResultCodeEnum.AWARD_TYPE_NOT_FOUND);
        return BeanConvertUtil.to(awardType, AwardTypeVO.class);
    }

    /**
     * 获取所有奖项类型列表
     *
     * @return 所有奖项类型列表
     */
    @Override
    public List<AwardTypeVO> listAllAwardTypes() {
        List<AwardType> awardTypes = list();
        return BeanConvertUtil.toList(awardTypes, AwardTypeVO.class);
    }

    /**
     * 根据ID删除奖项类型
     *
     * @param id 奖项类型ID
     * @return 删除结果，true表示删除成功，false表示删除失败
     */
    @Override
    public boolean deleteAwardType(Long id) {
        // 检查奖项类型是否存在
        AwardType awardType = getById(id);
        AssertUtils.notNull(awardType, UserResultCodeEnum.AWARD_TYPE_NOT_FOUND);
        return removeById(id);
    }
}
