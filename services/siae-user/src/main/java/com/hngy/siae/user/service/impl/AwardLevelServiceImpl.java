package com.hngy.siae.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.UserResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.user.dto.request.AwardLevelCreateDTO;
import com.hngy.siae.user.dto.response.AwardLevelVO;
import com.hngy.siae.user.entity.AwardLevel;
import com.hngy.siae.user.mapper.AwardLevelMapper;
import com.hngy.siae.user.service.AwardLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 奖项等级字典服务实现类
 * <p>
 * 提供奖项等级的增删改查功能，包括创建、更新、查询和删除奖项等级信息。
 * 支持分页查询和条件查询，确保奖项等级名称的唯一性。
 *
 * @author KEYKB
 */
@Service
@RequiredArgsConstructor
public class AwardLevelServiceImpl
        extends ServiceImpl<AwardLevelMapper, AwardLevel>
        implements AwardLevelService {

    /**
     * 创建奖项等级
     *
     * @param awardLevelCreateDTO 奖项等级创建参数
     * @return 创建成功的奖项等级信息
     */
    @Override
    public AwardLevelVO createAwardLevel(AwardLevelCreateDTO awardLevelCreateDTO) {
        // 检查名称是否已存在
        LambdaQueryWrapper<AwardLevel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AwardLevel::getName, awardLevelCreateDTO.getName());
        AwardLevel existAwardLevel = getOne(queryWrapper);
        AssertUtils.isNull(existAwardLevel, UserResultCodeEnum.AWARD_LEVEL_ALREADY_EXISTS);

        // 保存奖项等级
        AwardLevel awardLevel = BeanConvertUtil.to(awardLevelCreateDTO, AwardLevel.class);
        save(awardLevel);

        return BeanConvertUtil.to(awardLevel, AwardLevelVO.class);
    }

    /**
     * 更新奖项等级信息
     *
     * @param id 奖项等级ID
     * @param name 奖项等级名称
     * @param orderId 排序ID
     * @return 更新后的奖项等级信息
     */
    @Override
    public AwardLevelVO updateAwardLevel(Long id, String name, Integer orderId) {
        // 检查奖项等级是否存在
        AwardLevel existAwardLevel = getById(id);
        AssertUtils.notNull(existAwardLevel, UserResultCodeEnum.AWARD_LEVEL_NOT_FOUND);

        // 如果更新名称，需要检查是否与其他奖项等级冲突
        if (StrUtil.isNotBlank(name) && !name.equals(existAwardLevel.getName())) {
            boolean exists = lambdaQuery().eq(AwardLevel::getName, name).exists();
            AssertUtils.isFalse(exists, UserResultCodeEnum.AWARD_LEVEL_ALREADY_EXISTS);
        }

        // 更新奖项等级信息
        if (StrUtil.isNotBlank(name)) {
            existAwardLevel.setName(name);
        }
        if (orderId != null) {
            existAwardLevel.setOrderId(orderId);
        }
        updateById(existAwardLevel);

        return BeanConvertUtil.to(existAwardLevel, AwardLevelVO.class);
    }

    /**
     * 根据ID获取奖项等级信息
     *
     * @param id 奖项等级ID
     * @return 奖项等级详细信息
     */
    @Override
    public AwardLevelVO getAwardLevelById(Long id) {
        AwardLevel awardLevel = getById(id);
        AssertUtils.notNull(awardLevel, UserResultCodeEnum.AWARD_LEVEL_NOT_FOUND);
        return BeanConvertUtil.to(awardLevel, AwardLevelVO.class);
    }

    /**
     * 获取所有奖项等级列表
     *
     * @return 所有奖项等级列表，按排序ID和ID升序排列
     */
    @Override
    public List<AwardLevelVO> listAllAwardLevels() {
        List<AwardLevel> awardLevels = lambdaQuery()
                .orderByAsc(AwardLevel::getOrderId, AwardLevel::getId)
                .list();
        return BeanConvertUtil.toList(awardLevels, AwardLevelVO.class);
    }

    /**
     * 根据ID删除奖项等级
     *
     * @param id 奖项等级ID
     * @return 删除结果，true表示删除成功，false表示删除失败
     */
    @Override
    public boolean deleteAwardLevel(Long id) {
        // 检查奖项等级是否存在
        AwardLevel awardLevel = getById(id);
        AssertUtils.notNull(awardLevel, UserResultCodeEnum.AWARD_LEVEL_NOT_FOUND);
        return removeById(id);
    }
}
