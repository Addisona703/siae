package com.hngy.siae.media.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.media.domain.entity.Upload;
import org.apache.ibatis.annotations.Mapper;

/**
 * 上传会话数据访问接口
 *
 * @author SIAE Team
 */
@Mapper
public interface UploadRepository extends BaseMapper<Upload> {
}
