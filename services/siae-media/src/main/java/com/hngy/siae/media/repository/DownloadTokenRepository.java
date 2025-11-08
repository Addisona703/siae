package com.hngy.siae.media.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.media.domain.entity.DownloadToken;
import org.apache.ibatis.annotations.Mapper;

/**
 * 下载令牌数据访问层
 *
 * @author SIAE Team
 */
@Mapper
public interface DownloadTokenRepository extends BaseMapper<DownloadToken> {
}
