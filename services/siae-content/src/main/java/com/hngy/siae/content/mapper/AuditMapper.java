package com.hngy.siae.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.content.entity.Audit;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 31833
* @description 针对表【content_audit(审核记录表)】的数据库操作Mapper
* @createDate 2025-05-15 16:48:34
* @Entity com.hngy.siae.com.hngy.siae.content.model.entity.Audit
*/
@Mapper
public interface AuditMapper extends BaseMapper<Audit> {

}




