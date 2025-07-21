package com.hngy.siae.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.content.dto.request.ActionDTO;
import com.hngy.siae.content.entity.UserAction;

/**
 * 交互服务接口
 *
 * @author KEYKB
 * 创建时间: 2025/05/21
 */
public interface InteractionsService extends IService<UserAction> {

    /**
     * 记录动作
     *
     * @param actionDTO 动作dto
     * @return {@link Result }<{@link Void }>
     */
    Result<Void> recordAction(ActionDTO actionDTO);

    /**
     * 取消操作
     *
     * @param actionDTO 动作dto
     * @return {@link Result }<{@link Void }>
     */
    Result<Void> cancelAction(ActionDTO actionDTO);
}