package com.hngy.siae.media.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.media.domain.entity.ProcessingJob;
import org.apache.ibatis.annotations.Mapper;

/**
 * 异步处理任务数据访问接口
 *
 * @author SIAE Team
 */
@Mapper
public interface ProcessingJobRepository extends BaseMapper<ProcessingJob> {
}
