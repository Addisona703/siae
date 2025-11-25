package com.hngy.siae.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.user.dto.response.AwardLevelVO;
import com.hngy.siae.user.entity.AwardLevel;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 奖项等级字典表 Mapper 接口
 * 
 * @author AI开发助手
 */
@Mapper
public interface AwardLevelMapper extends BaseMapper<AwardLevel> {

    /**
     * 查询所有奖项等级及其关联的奖项数量
     *
     * @return 奖项等级VO列表（包含关联数量）
     */
    List<AwardLevelVO> selectAllWithRefCount();
} 