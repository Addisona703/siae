package com.hngy.siae.media.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.media.domain.entity.MultipartPart;
import org.apache.ibatis.annotations.Mapper;

/**
 * 分片上传记录数据访问接口
 *
 * @author SIAE Team
 */
@Mapper
public interface MultipartPartRepository extends BaseMapper<MultipartPart> {
}
