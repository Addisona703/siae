package com.hngy.siae.media.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.media.domain.entity.FileDerivative;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件衍生物数据访问层
 *
 * @author SIAE Team
 */
@Mapper
public interface FileDerivativeRepository extends BaseMapper<FileDerivative> {
}
