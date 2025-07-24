package com.hngy.siae.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.UserResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.user.dto.request.AwardLevelDTO;
import com.hngy.siae.user.dto.response.AwardLevelVO;
import com.hngy.siae.user.entity.AwardLevel;
import com.hngy.siae.user.mapper.AwardLevelMapper;
import com.hngy.siae.user.service.AwardLevelService;
import com.hngy.siae.web.utils.PageConvertUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 奖项等级字典服务实现类
 *
 * @author KEYKB
 */
@Service
public class AwardLevelServiceImpl
        extends ServiceImpl<AwardLevelMapper, AwardLevel>
        implements AwardLevelService {

    @Override
    public AwardLevelVO createAwardLevel(AwardLevelDTO awardLevelDTO) {
        // 检查名称是否已存在
        LambdaQueryWrapper<AwardLevel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AwardLevel::getName, awardLevelDTO.getName());
        AwardLevel existAwardLevel = getOne(queryWrapper);
        AssertUtils.isNull(existAwardLevel, UserResultCodeEnum.AWARD_LEVEL_ALREADY_EXISTS);

        // 保存奖项等级
        AwardLevel awardLevel = BeanConvertUtil.to(awardLevelDTO, AwardLevel.class);
        save(awardLevel);

        return BeanConvertUtil.to(awardLevel, AwardLevelVO.class);
    }

    @Override
    public AwardLevelVO updateAwardLevel(AwardLevelDTO awardLevelDTO) {
        // 检查奖项等级是否存在
        AwardLevel existAwardLevel = getById(awardLevelDTO.getId()); 
        AssertUtils.notNull(existAwardLevel, UserResultCodeEnum.AWARD_LEVEL_NOT_FOUND);

        // 如果更新名称，需要检查是否与其他奖项等级冲突
        String newName = awardLevelDTO.getName();
        if (StringUtils.hasText(newName) && !newName.equals(existAwardLevel.getName())) {
            boolean exists = lambdaQuery().eq(AwardLevel::getName, newName).exists();
            AssertUtils.isFalse(exists, UserResultCodeEnum.AWARD_LEVEL_ALREADY_EXISTS);
        }

        // 更新奖项等级信息
        AwardLevel awardLevel = BeanConvertUtil.to(awardLevelDTO, AwardLevel.class);
        updateById(awardLevel); 

        return BeanConvertUtil.to(awardLevel, AwardLevelVO.class);
    }

    @Override
    public AwardLevelVO getAwardLevelById(Long id) {
        AwardLevel awardLevel = getById(id); 
        AssertUtils.notNull(awardLevel, UserResultCodeEnum.AWARD_LEVEL_NOT_FOUND);
        return BeanConvertUtil.to(awardLevel, AwardLevelVO.class);
    }

    @Override
    public AwardLevelVO getAwardLevelByName(String name) {
        LambdaQueryWrapper<AwardLevel> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AwardLevel::getName, name);
        AwardLevel awardLevel = getOne(queryWrapper); 
        return BeanConvertUtil.to(awardLevel, AwardLevelVO.class);
    }

    @Override
    public List<AwardLevelVO> listAllAwardLevels() {
        // TODO:排序处理逻辑暂且前端做，后续数据量大时由后端完善
        List<AwardLevel> awardLevels = lambdaQuery()
                .orderByAsc(AwardLevel::getOrderId, AwardLevel::getId)
                .list(); // 使用ServiceImpl提供的链式调用
        return BeanConvertUtil.toList(awardLevels, AwardLevelVO.class);
    }

    @Override
    public PageVO<AwardLevelVO> listAwardLevelsByPage(PageDTO<AwardLevelDTO> pageDTO) {
        // 构建查询条件
        LambdaQueryWrapper<AwardLevel> queryWrapper = new LambdaQueryWrapper<>();
        AwardLevelDTO awardLevelDTO = pageDTO.getParams();
        // 添加查询条件
        if (awardLevelDTO != null && StringUtils.hasText(awardLevelDTO.getName())) {
            // 名称模糊查询
            queryWrapper.like(AwardLevel::getName, awardLevelDTO.getName());
        }

        // TODO:排序处理逻辑暂且前端做，后续数据量大时由后端完善
        queryWrapper.orderByAsc(AwardLevel::getOrderId, AwardLevel::getId);

        // 执行分页查询
        Page<AwardLevel> page = PageConvertUtil.toPage(pageDTO);
        Page<AwardLevel> resultPage = page(page, queryWrapper); 

        return PageConvertUtil.convert(resultPage, AwardLevelVO.class);
    }

    @Override
    public boolean deleteAwardLevel(Long id) {
        // 检查奖项等级是否存在
        AwardLevel awardLevel = getById(id); 
        AssertUtils.notNull(awardLevel, UserResultCodeEnum.AWARD_LEVEL_NOT_FOUND);
        return removeById(id); 
    }
}
