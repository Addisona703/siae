package com.hngy.siae.messaging.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户删除事件消息
 * <p>
 * 当用户被物理删除时，发送此事件通知其他服务清理关联数据
 *
 * @author KEYKB
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDeletedEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 事件ID
     */
    private String eventId;

    /**
     * 被删除的用户ID列表
     */
    private List<Long> userIds;

    /**
     * 事件发生时间
     */
    private LocalDateTime eventTime;

    /**
     * 事件来源服务
     */
    private String sourceService;

    /**
     * 清理原因
     */
    private String reason;
}
