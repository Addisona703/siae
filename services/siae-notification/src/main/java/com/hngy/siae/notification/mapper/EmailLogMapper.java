package com.hngy.siae.notification.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.notification.entity.EmailLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 邮件发送记录Mapper接口
 *
 * @author KEYKB
 */
@Mapper
public interface EmailLogMapper extends BaseMapper<EmailLog> {
}
