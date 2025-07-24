package com.hngy.siae.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.UserResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.user.dto.request.AwardTypeDTO;
import com.hngy.siae.user.dto.response.AwardTypeVO;
import com.hngy.siae.user.entity.AwardType;
import com.hngy.siae.user.mapper.AwardTypeMapper;
import com.hngy.siae.user.service.AwardTypeService;
import com.hngy.siae.web.utils.PageConvertUtil;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 奖项类型字典服务实现类
 *
 * @author KEYKB
 */
@Service
public class AwardTypeServiceImpl extends ServiceImpl<AwardTypeMapper, AwardType> implements AwardTypeService {

    @Override
    public AwardTypeVO createAwardType(AwardTypeDTO awardTypeDTO) {
        // 检查名称是否已存在
        LambdaQueryWrapper<AwardType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AwardType::getName, awardTypeDTO.getName());
        AwardType existAwardType = getOne(queryWrapper); 
        AssertUtils.isNull(existAwardType, UserResultCodeEnum.AWARD_TYPE_ALREADY_EXISTS);

        // 保存奖项类型
        AwardType awardType = BeanConvertUtil.to(awardTypeDTO, AwardType.class);
        save(awardType); 

        return BeanConvertUtil.to(awardType, AwardTypeVO.class);
    }

    @Override
    public AwardTypeVO updateAwardType(AwardTypeDTO awardTypeDTO) {
        // 检查奖项类型是否存在
        AwardType existAwardType = getById(awardTypeDTO.getId()); 
        AssertUtils.notNull(existAwardType, UserResultCodeEnum.AWARD_TYPE_NOT_FOUND);

        // 如果更新名称，需要检查是否与其他奖项类型冲突
        String newName = awardTypeDTO.getName();
        if (StringUtils.hasText(newName) && !newName.equals(existAwardType.getName())) {
            boolean exists = lambdaQuery().eq(AwardType::getName, newName).exists();
            AssertUtils.isFalse(exists, UserResultCodeEnum.AWARD_TYPE_ALREADY_EXISTS);
        }

        // 更新奖项类型信息
        AwardType awardType = BeanConvertUtil.to(awardTypeDTO, AwardType.class);
        updateById(awardType); 

        return BeanConvertUtil.to(awardType, AwardTypeVO.class);
    }

    @Override
    public AwardTypeVO getAwardTypeById(Long id) {
        AwardType awardType = getById(id); 
        AssertUtils.notNull(awardType, UserResultCodeEnum.AWARD_TYPE_NOT_FOUND);
        return BeanConvertUtil.to(awardType, AwardTypeVO.class);
    }

    @Override
    public AwardTypeVO getAwardTypeByName(String name) {
        LambdaQueryWrapper<AwardType> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AwardType::getName, name);
        AwardType awardType = getOne(queryWrapper); 
        return BeanConvertUtil.to(awardType, AwardTypeVO.class);
    }

    @Override
    public List<AwardTypeVO> listAllAwardTypes() {
        List<AwardType> awardTypes = list(); 
        return BeanConvertUtil.toList(awardTypes, AwardTypeVO.class);
    }

    @Override
    public PageVO<AwardTypeVO> listAwardTypesByPage(PageDTO<AwardTypeDTO> pageDTO) {
        // 构建查询条件
        LambdaQueryWrapper<AwardType> queryWrapper = new LambdaQueryWrapper<>();
        AwardTypeDTO awardTypeDTO = pageDTO.getParams();

        // 添加查询条件
        if (awardTypeDTO != null && StringUtils.hasText(awardTypeDTO.getName())) {
            // 名称模糊查询
            queryWrapper.like(AwardType::getName, awardTypeDTO.getName());
        }

        // 执行分页查询
        Page<AwardType> page = PageConvertUtil.toPage(pageDTO);
        Page<AwardType> resultPage = page(page, queryWrapper); 
        
        return PageConvertUtil.convert(resultPage, AwardTypeVO.class);
    }

    @Override
    public boolean deleteAwardType(Long id) {
        // 检查奖项类型是否存在
        AwardType awardType = getById(id); 
        AssertUtils.notNull(awardType, UserResultCodeEnum.AWARD_TYPE_NOT_FOUND);
        return removeById(id); 
    }
}
