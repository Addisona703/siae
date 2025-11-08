package com.hngy.siae.media.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.media.domain.entity.Quota;
import org.apache.ibatis.annotations.Mapper;

/**
 * 租户配额数据访问接口
 *
 * @author SIAE Team
 */
@Mapper
public interface QuotaRepository extends BaseMapper<Quota> {
}
