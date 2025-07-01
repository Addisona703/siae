package com.hngy.siae.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.common.result.Result;
import com.hngy.siae.content.common.enums.ActionTypeEnum;
import com.hngy.siae.content.common.enums.status.ActionStatusEnum;
import com.hngy.siae.content.dto.request.ActionDTO;
import com.hngy.siae.content.entity.UserAction;
import com.hngy.siae.content.mapper.UserActionMapper;
import com.hngy.siae.content.service.InteractionsService;
import com.hngy.siae.content.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 交互服务实现类
 *
 * @author KEYKB
 * 创建时间: 2025/05/21
 */
@Service
@RequiredArgsConstructor
public class InteractionsServiceImpl
        extends ServiceImpl<UserActionMapper, UserAction>
        implements InteractionsService {

    private final StatisticsService statisticsService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> recordAction(ActionDTO actionDTO) {
        ActionTypeEnum actionType = actionDTO.getActionType();
        Long userId = actionDTO.getUserId();
        Long targetId = actionDTO.getTargetId();

        UserAction existing = getExistingAction(userId, targetId, actionType);

        if (existing != null) {
            if (existing.getStatus() == ActionStatusEnum.ACTIVATED) {
                return Result.success();
            }

            // 恢复为激活状态
            updateStatus(existing, ActionStatusEnum.ACTIVATED);
        } else {
            // 新建记录
            createUserAction(actionDTO);
        }

        statisticsService.incrementStatistics(targetId, actionType);
        return Result.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Void> cancelAction(ActionDTO actionDTO) {
        ActionTypeEnum actionType = actionDTO.getActionType();
        Long userId = actionDTO.getUserId();
        Long targetId = actionDTO.getTargetId();

        UserAction existing = getExistingAction(userId, targetId, actionType);

        if (existing == null || existing.getStatus() == ActionStatusEnum.CANCELLED) {
            return Result.success();
        }

        updateStatus(existing, ActionStatusEnum.CANCELLED);
        statisticsService.decrementStatistics(targetId, actionType);
        return Result.success();
    }


    /**
     * 查询现有行为记录
     */
    private UserAction getExistingAction(Long userId, Long targetId, ActionTypeEnum actionType) {
        return lambdaQuery()
                .eq(UserAction::getUserId, userId)
                .eq(UserAction::getTargetId, targetId)
                .eq(UserAction::getActionType, actionType)
                .last("LIMIT 1")
                .one();
    }

    /**
     * 创建新的行为记录
     */
    private void createUserAction(ActionDTO actionDTO) {
        UserAction userAction = BeanUtil.copyProperties(actionDTO, UserAction.class);
        userAction.setStatus(ActionStatusEnum.ACTIVATED);
        AssertUtil.isTrue(this.save(userAction),
                ActionStatusEnum.ACTIVATED.getDescription() +
                        actionDTO.getActionType().getDescription() + "失败");
    }

    /**
     * 更新行为状态
     */
    private void updateStatus(UserAction userAction, ActionStatusEnum newStatus) {
        userAction.setStatus(newStatus);
        AssertUtil.isTrue(this.updateById(userAction), "更新状态失败");
    }
}