package com.hngy.siae.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.notification.entity.SmsLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 短信发送记录Mapper接口
 *
 * @author KEYKB
 */
@Mapper
public interface SmsLogMapper extends BaseMapper<SmsLog> {
}
