package com.hngy.siae.media.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.media.domain.entity.LifecyclePolicy;
import org.apache.ibatis.annotations.Mapper;

/**
 * 生命周期策略数据访问层
 *
 * @author SIAE Team
 */
@Mapper
public interface LifecyclePolicyRepository extends BaseMapper<LifecyclePolicy> {
}
