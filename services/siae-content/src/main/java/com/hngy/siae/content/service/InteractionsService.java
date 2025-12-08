package com.hngy.siae.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.content.dto.request.interaction.ActionDTO;
import com.hngy.siae.content.entity.UserAction;
import com.hngy.siae.content.enums.ActionTypeEnum;
import com.hngy.siae.content.enums.TypeEnum;

import java.util.List;
import java.util.Set;

/**
 * 交互服务接口
 *
 * @author KEYKB
 * 创建时间: 2025/05/21
 */
public interface InteractionsService extends IService<UserAction> {

    /**
     * 记录用户行为
     *
     * @param actionDTO 用户行为请求参数
     */
    void recordAction(ActionDTO actionDTO);

    /**
     * 取消用户行为
     *
     * @param actionDTO 用户行为请求参数
     */
    void cancelAction(ActionDTO actionDTO);

    /**
     * 批量查询用户已执行的行为目标ID
     *
     * @param userId     用户ID
     * @param targetIds  目标ID列表
     * @param targetType 目标类型
     * @param actionType 行为类型
     * @return 已执行行为的目标ID集合
     */
    Set<Long> getActivatedTargetIds(Long userId, List<Long> targetIds, TypeEnum targetType, ActionTypeEnum actionType);
}