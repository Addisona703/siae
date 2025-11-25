package com.hngy.siae.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.user.dto.response.AwardTypeVO;
import com.hngy.siae.user.entity.AwardType;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 奖项类型字典表 Mapper 接口
 * 
 * @author AI开发助手
 */
@Mapper
public interface AwardTypeMapper extends BaseMapper<AwardType> {

    /**
     * 查询所有奖项类型及其关联的奖项数量
     *
     * @return 奖项类型VO列表（包含关联数量）
     */
    List<AwardTypeVO> selectAllWithRefCount();
} 