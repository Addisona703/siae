package com.hngy.siae.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.ai.entity.ChatSessionEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * AI聊天会话Mapper
 *
 * @author SIAE Team
 */
@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSessionEntity> {

    /**
     * 查询用户的会话列表（不含messages，轻量查询）
     */
    @Select("SELECT id, session_id, user_id, title, create_time, update_time " +
            "FROM ai_chat_session WHERE user_id = #{userId} ORDER BY update_time DESC LIMIT #{limit}")
    List<ChatSessionEntity> selectSessionListByUserId(@Param("userId") Long userId, @Param("limit") int limit);

    // selectBySessionId 改用 MyBatis-Plus 的 selectOne + LambdaQueryWrapper
    // 这样可以正确触发 JacksonTypeHandler
}
