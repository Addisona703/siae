package com.hngy.siae.media.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.media.domain.entity.FileEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件数据访问接口
 *
 * @author SIAE Team
 */
@Mapper
public interface FileRepository extends BaseMapper<FileEntity> {
}
