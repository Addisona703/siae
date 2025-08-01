package com.hngy.siae.content.service;

import com.baomidou.mybatisplus.extension.service.IService;
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
}