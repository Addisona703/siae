package com.hngy.siae.attendance.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hngy.siae.attendance.constant.CacheConstants;
import com.hngy.siae.attendance.dto.request.AttendanceRuleCreateDTO;
import com.hngy.siae.attendance.dto.request.AttendanceRuleUpdateDTO;
import com.hngy.siae.attendance.dto.response.AttendanceRuleVO;
import com.hngy.siae.attendance.entity.AttendanceRule;
import com.hngy.siae.attendance.enums.AttendanceResultCodeEnum;
import com.hngy.siae.attendance.enums.RuleStatus;
import com.hngy.siae.attendance.mapper.AttendanceRuleMapper;
import com.hngy.siae.attendance.service.IRuleService;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.utils.BeanConvertUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 考勤规则服务实现
 *
 * @author SIAE Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RuleServiceImpl implements IRuleService {

    private final AttendanceRuleMapper attendanceRuleMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CacheConstants.CACHE_ATTENDANCE_RULE, allEntries = true)
    public AttendanceRuleVO createRule(AttendanceRuleCreateDTO dto) {
        log.info("创建考勤规则: {}", dto);

        // 创建规则实体
        AttendanceRule rule = BeanConvertUtil.to(dto, AttendanceRule.class);
        
        // 设置默认值
        if (rule.getStatus() == null) {
            rule.setStatus(RuleStatus.ENABLED);
        }
        if (rule.getPriority() == null) {
            rule.setPriority(0);
        }
        if (rule.getLateThresholdMinutes() == null) {
            rule.setLateThresholdMinutes(0);
        }
        if (rule.getEarlyThresholdMinutes() == null) {
            rule.setEarlyThresholdMinutes(0);
        }
        if (rule.getLocationRequired() == null) {
            rule.setLocationRequired(false);
        }

        // 验证规则有效性
        validateRule(rule);

        // 保存规则
        attendanceRuleMapper.insert(rule);

        // 清除用户规则缓存
        clearUserRuleCache();

        log.info("考勤规则创建成功，规则ID: {}", rule.getId());
        return BeanConvertUtil.to(rule, AttendanceRuleVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CacheConstants.CACHE_ATTENDANCE_RULE, allEntries = true)
    public AttendanceRuleVO updateRule(Long id, AttendanceRuleUpdateDTO dto) {
        log.info("更新考勤规则，规则ID: {}, 更新内容: {}", id, dto);

        // 查询规则是否存在
        AttendanceRule rule = attendanceRuleMapper.selectById(id);
        AssertUtils.notNull(rule, AttendanceResultCodeEnum.RULE_NOT_FOUND);

        // 使用 XML 动态 SQL 更新（只更新非空字段）
        attendanceRuleMapper.updateRuleSelective(id, dto);

        // 重新查询更新后的规则进行验证
        rule = attendanceRuleMapper.selectById(id);
        validateRule(rule);

        // 清除用户规则缓存
        clearUserRuleCache();

        log.info("考勤规则更新成功，规则ID: {}", id);
        return BeanConvertUtil.to(rule, AttendanceRuleVO.class);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CacheConstants.CACHE_ATTENDANCE_RULE, allEntries = true)
    public Boolean deleteRule(Long id) {
        log.info("删除考勤规则，规则ID: {}", id);

        // 查询规则是否存在
        AttendanceRule rule = attendanceRuleMapper.selectById(id);
        AssertUtils.notNull(rule, AttendanceResultCodeEnum.RULE_NOT_FOUND);

        // 逻辑删除规则
        int result = attendanceRuleMapper.deleteById(id);

        // 清除用户规则缓存
        clearUserRuleCache();

        log.info("考勤规则删除成功，规则ID: {}", id);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CacheConstants.CACHE_ATTENDANCE_RULE, allEntries = true)
    public Boolean enableRule(Long id) {
        log.info("启用考勤规则，规则ID: {}", id);

        // 查询规则是否存在
        AttendanceRule rule = attendanceRuleMapper.selectById(id);
        AssertUtils.notNull(rule, AttendanceResultCodeEnum.RULE_NOT_FOUND);

        // 更新规则状态为启用
        rule.setStatus(RuleStatus.ENABLED);
        int result = attendanceRuleMapper.updateById(rule);

        // 清除用户规则缓存
        clearUserRuleCache();

        log.info("考勤规则启用成功，规则ID: {}", id);
        return result > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = CacheConstants.CACHE_ATTENDANCE_RULE, allEntries = true)
    public Boolean disableRule(Long id) {
        log.info("禁用考勤规则，规则ID: {}", id);

        // 查询规则是否存在
        AttendanceRule rule = attendanceRuleMapper.selectById(id);
        AssertUtils.notNull(rule, AttendanceResultCodeEnum.RULE_NOT_FOUND);

        // 更新规则状态为禁用
        rule.setStatus(RuleStatus.DISABLED);
        int result = attendanceRuleMapper.updateById(rule);

        // 清除用户规则缓存
        clearUserRuleCache();

        log.info("考勤规则禁用成功，规则ID: {}", id);
        return result > 0;
    }

    @Override
    public AttendanceRule getApplicableRule(Long userId, LocalDate date) {
        log.debug("获取适用规则，用户ID: {}, 日期: {}", userId, date);

        // 尝试从缓存获取
        String cacheKey = CacheConstants.generateKey(CacheConstants.CACHE_USER_APPLICABLE_RULE, userId, date);
        AttendanceRule cachedRule = (AttendanceRule) redisTemplate.opsForValue().get(cacheKey);
        if (cachedRule != null) {
            log.debug("从缓存获取到适用规则，用户ID: {}, 规则ID: {}", userId, cachedRule.getId());
            return cachedRule;
        }

        // 查询所有启用的规则，按优先级降序排序
        LambdaQueryWrapper<AttendanceRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceRule::getStatus, RuleStatus.ENABLED)
                .le(AttendanceRule::getEffectiveDate, date)
                .and(wrapper -> wrapper.isNull(AttendanceRule::getExpiryDate)
                        .or()
                        .ge(AttendanceRule::getExpiryDate, date))
                .orderByDesc(AttendanceRule::getPriority);

        List<AttendanceRule> rules = attendanceRuleMapper.selectList(queryWrapper);

        if (rules.isEmpty()) {
            log.warn("系统中没有启用的考勤规则，用户ID: {}, 日期: {}", userId, date);
            return null;
        }

        log.debug("找到 {} 条启用的规则，开始匹配适用规则", rules.size());

        // 获取用户部门信息（用于部门规则匹配）
        Long userDepartmentId = getUserDepartmentId(userId);

        // 按优先级从高到低查找第一个适用的规则
        // 这样可以确保返回优先级最高的适用规则
        AttendanceRule selectedRule = rules.stream()
                .filter(rule -> isRuleApplicableToUser(rule, userId, userDepartmentId))
                .findFirst()
                .orElse(null);

        if (selectedRule == null) {
            log.warn("未找到适用的考勤规则，用户ID: {}, 日期: {}", userId, date);
            return null;
        }

        // 缓存结果
        redisTemplate.opsForValue().set(cacheKey, selectedRule, 
                CacheConstants.CACHE_USER_APPLICABLE_RULE_TTL, TimeUnit.SECONDS);

        log.info("找到适用规则，规则ID: {}, 规则名称: {}, 优先级: {}, 适用类型: {}", 
                selectedRule.getId(), selectedRule.getName(), 
                selectedRule.getPriority(), selectedRule.getTargetType());
        return selectedRule;
    }

    @Override
    public void validateRule(AttendanceRule rule) {
        log.debug("验证考勤规则: {}", rule);

        // 验证签到时间窗口
        AssertUtils.isTrue(
                rule.getCheckInStartTime().isBefore(rule.getCheckInEndTime()),
                AttendanceResultCodeEnum.RULE_TIME_WINDOW_INVALID
        );

        // 验证签退时间窗口
        AssertUtils.isTrue(
                rule.getCheckOutStartTime().isBefore(rule.getCheckOutEndTime()),
                AttendanceResultCodeEnum.RULE_TIME_WINDOW_INVALID
        );

        // 验证生效日期和失效日期
        if (rule.getExpiryDate() != null) {
            AssertUtils.isTrue(
                    !rule.getEffectiveDate().isAfter(rule.getExpiryDate()),
                    AttendanceResultCodeEnum.RULE_TIME_WINDOW_INVALID
            );
        }

        // 验证位置配置
        if (Boolean.TRUE.equals(rule.getLocationRequired())) {
            AssertUtils.notEmpty(
                    rule.getAllowedLocations(),
                    "位置验证已启用，但未配置允许的位置列表"
            );
            AssertUtils.notNull(
                    rule.getLocationRadiusMeters(),
                    "位置验证已启用，但未配置位置半径"
            );
        }

        log.debug("考勤规则验证通过");
    }

    @Override
    public List<AttendanceRule> getAllApplicableRules(Long userId, LocalDate date) {
        log.debug("获取所有适用规则，用户ID: {}, 日期: {}", userId, date);

        // 查询所有启用的规则，按优先级降序排序
        LambdaQueryWrapper<AttendanceRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceRule::getStatus, RuleStatus.ENABLED)
                .le(AttendanceRule::getEffectiveDate, date)
                .and(wrapper -> wrapper.isNull(AttendanceRule::getExpiryDate)
                        .or()
                        .ge(AttendanceRule::getExpiryDate, date))
                .orderByDesc(AttendanceRule::getPriority);

        List<AttendanceRule> rules = attendanceRuleMapper.selectList(queryWrapper);

        if (rules.isEmpty()) {
            log.warn("系统中没有启用的考勤规则，用户ID: {}, 日期: {}", userId, date);
            return List.of();
        }

        // 获取用户部门信息
        Long userDepartmentId = getUserDepartmentId(userId);

        // 过滤出所有适用于该用户的规则
        List<AttendanceRule> applicableRules = rules.stream()
                .filter(rule -> isRuleApplicableToUser(rule, userId, userDepartmentId))
                .toList();

        log.info("找到 {} 条适用规则，用户ID: {}, 日期: {}", applicableRules.size(), userId, date);
        
        if (!applicableRules.isEmpty()) {
            log.debug("适用规则列表: {}", 
                    applicableRules.stream()
                            .map(r -> String.format("ID=%d, 名称=%s, 优先级=%d", 
                                    r.getId(), r.getName(), r.getPriority()))
                            .toList());
        }

        return applicableRules;
    }

    @Override
    public List<AttendanceRuleVO> listRules() {
        log.info("查询所有考勤规则");
        
        LambdaQueryWrapper<AttendanceRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(AttendanceRule::getPriority)
                .orderByDesc(AttendanceRule::getCreatedAt);
        
        List<AttendanceRule> rules = attendanceRuleMapper.selectList(queryWrapper);
        
        return rules.stream()
                .map(rule -> BeanConvertUtil.to(rule, AttendanceRuleVO.class))
                .collect(java.util.stream.Collectors.toList());
    }

    @Override
    public com.hngy.siae.attendance.dto.response.AttendanceRuleDetailVO getRuleDetail(Long id) {
        log.info("查询考勤规则详情，规则ID: {}", id);

        // 查询规则
        AttendanceRule rule = attendanceRuleMapper.selectById(id);
        AssertUtils.notNull(rule, AttendanceResultCodeEnum.RULE_NOT_FOUND);

        // 转换为详情VO
        com.hngy.siae.attendance.dto.response.AttendanceRuleDetailVO detailVO = 
                BeanConvertUtil.to(rule, com.hngy.siae.attendance.dto.response.AttendanceRuleDetailVO.class);

        log.info("考勤规则详情查询成功，规则ID: {}, 规则名称: {}", id, rule.getName());
        return detailVO;
    }

    @Override
    @Cacheable(value = CacheConstants.CACHE_ACTIVITY_RULE, key = "#activityId")
    public AttendanceRule getActivityRule(Long activityId) {
        log.info("查询活动考勤规则，活动ID: {}", activityId);

        LambdaQueryWrapper<AttendanceRule> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AttendanceRule::getAttendanceType, com.hngy.siae.attendance.enums.AttendanceType.ACTIVITY)
                .eq(AttendanceRule::getRelatedId, activityId)
                .eq(AttendanceRule::getStatus, RuleStatus.ENABLED)
                .orderByDesc(AttendanceRule::getPriority)
                .last("LIMIT 1");

        AttendanceRule rule = attendanceRuleMapper.selectOne(queryWrapper);
        
        if (rule == null) {
            log.warn("未找到活动考勤规则，活动ID: {}", activityId);
        } else {
            log.info("找到活动考勤规则，规则ID: {}, 规则名称: {}", rule.getId(), rule.getName());
        }
        
        return rule;
    }

    /**
     * 判断规则是否适用于指定用户
     *
     * @param rule 考勤规则
     * @param userId 用户ID
     * @param userDepartmentId 用户部门ID（可能为null）
     * @return 是否适用
     */
    private boolean isRuleApplicableToUser(AttendanceRule rule, Long userId, Long userDepartmentId) {
        switch (rule.getTargetType()) {
            case ALL:
                // 全体成员规则适用于所有人
                log.debug("规则 {} 适用于全体成员", rule.getId());
                return true;
                
            case INDIVIDUAL:
                // 个人规则需要检查用户ID是否在目标列表中
                boolean individualMatch = rule.getTargetIds() != null && rule.getTargetIds().contains(userId);
                if (individualMatch) {
                    log.debug("规则 {} 适用于个人，用户ID: {}", rule.getId(), userId);
                } else {
                    log.debug("规则 {} 不适用于个人，用户ID: {} 不在目标列表中", rule.getId(), userId);
                }
                return individualMatch;
                
            case DEPARTMENT:
                // 部门规则需要检查用户所属部门是否在目标列表中
                if (userDepartmentId == null) {
                    log.debug("规则 {} 是部门规则，但用户 {} 的部门信息未获取到", rule.getId(), userId);
                    return false;
                }
                boolean departmentMatch = rule.getTargetIds() != null && rule.getTargetIds().contains(userDepartmentId);
                if (departmentMatch) {
                    log.debug("规则 {} 适用于部门，部门ID: {}", rule.getId(), userDepartmentId);
                } else {
                    log.debug("规则 {} 不适用于部门，部门ID: {} 不在目标列表中", rule.getId(), userDepartmentId);
                }
                return departmentMatch;
                
            default:
                log.warn("未知的规则目标类型: {}", rule.getTargetType());
                return false;
        }
    }

    /**
     * 获取用户所属部门ID
     * 
     * 注意：这是一个占位方法，实际应该调用用户服务获取用户部门信息
     * 当前实现返回null，表示部门信息不可用
     * 
     * TODO: 集成用户服务，实现真实的部门查询逻辑
     * 可能的实现方式：
     * 1. 通过 Feign 客户端调用用户服务
     * 2. 通过消息队列异步获取并缓存用户部门信息
     * 3. 在考勤服务中维护用户部门映射表（定期同步）
     *
     * @param userId 用户ID
     * @return 用户部门ID，如果无法获取则返回null
     */
    private Long getUserDepartmentId(Long userId) {
        // TODO: 实现用户部门查询逻辑
        // 示例代码（需要实际实现）：
        // try {
        //     UserDTO user = userServiceClient.getUserById(userId);
        //     return user.getDepartmentId();
        // } catch (Exception e) {
        //     log.error("获取用户部门信息失败，用户ID: {}", userId, e);
        //     return null;
        // }
        
        log.debug("用户部门查询功能尚未实现，用户ID: {}", userId);
        return null;
    }

    /**
     * 清除用户规则缓存
     * 当规则发生变化时，需要清除所有用户的规则缓存
     */
    private void clearUserRuleCache() {
        try {
            // 清除所有用户适用规则缓存
            String pattern = CacheConstants.CACHE_USER_APPLICABLE_RULE + ":*";
            redisTemplate.delete(redisTemplate.keys(pattern));
            log.info("已清除用户规则缓存");
        } catch (Exception e) {
            log.error("清除用户规则缓存失败", e);
        }
    }

    // ==================== 新增方法：支持过滤和活动考勤 ====================

    /**
     * 查询规则列表（支持过滤）
     */
    @Override
    public List<AttendanceRuleVO> listRules(com.hngy.siae.attendance.enums.RuleStatus status, String attendanceType, String targetType) {
        log.info("查询规则列表: status={}, attendanceType={}, targetType={}", status, attendanceType, targetType);

        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AttendanceRule> queryWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

        // 状态过滤
        if (status != null) {
            queryWrapper.eq(AttendanceRule::getStatus, status);
        }

        // 考勤类型过滤
        if (attendanceType != null && !attendanceType.trim().isEmpty()) {
            try {
                // 将字符串转换为枚举类型
                com.hngy.siae.attendance.enums.AttendanceType attendanceTypeEnum =
                        com.hngy.siae.attendance.enums.AttendanceType.valueOf(attendanceType);
                queryWrapper.eq(AttendanceRule::getAttendanceType, attendanceTypeEnum);
            } catch (IllegalArgumentException e) {
                log.warn("无效的 attendanceType 参数: {}", attendanceType);
            }
        }

        // 适用对象类型过滤
        if (targetType != null && !targetType.trim().isEmpty()) {
            try {
                // 将字符串转换为枚举类型
                com.hngy.siae.attendance.enums.RuleTargetType targetTypeEnum =
                        com.hngy.siae.attendance.enums.RuleTargetType.valueOf(targetType);
                queryWrapper.eq(AttendanceRule::getTargetType, targetTypeEnum);
            } catch (IllegalArgumentException e) {
                log.warn("无效的 targetType 参数: {}", targetType);
            }
        }

        queryWrapper.orderByDesc(AttendanceRule::getPriority)
                .orderByDesc(AttendanceRule::getCreatedAt);

        List<AttendanceRule> rules = attendanceRuleMapper.selectList(queryWrapper);

        return rules.stream()
                .map(rule -> com.hngy.siae.core.utils.BeanConvertUtil.to(rule, AttendanceRuleVO.class))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 创建活动考勤规则
     */
    @Override
    @org.springframework.transaction.annotation.Transactional(rollbackFor = Exception.class)
    public AttendanceRuleVO createActivityRule(AttendanceRuleCreateDTO dto) {
        log.info("创建活动考勤规则: activityId={}, ruleName={}", dto.getRelatedId(), dto.getName());
        
        // 确保考勤类型为活动考勤
        dto.setAttendanceType(com.hngy.siae.attendance.enums.AttendanceType.ACTIVITY);
        
        // 验证活动ID必须存在
        com.hngy.siae.core.asserts.AssertUtils.notNull(dto.getRelatedId(), "活动ID不能为空");
        
        // 调用通用创建方法
        return createRule(dto);
    }

    /**
     * 查询活动考勤规则列表
     */
    @Override
    public List<AttendanceRuleVO> listActivityRules(Long activityId) {
        log.info("查询活动考勤规则列表: activityId={}", activityId);
        
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AttendanceRule> queryWrapper = 
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        
        queryWrapper.eq(AttendanceRule::getAttendanceType, com.hngy.siae.attendance.enums.AttendanceType.ACTIVITY);
        
        if (activityId != null) {
            queryWrapper.eq(AttendanceRule::getRelatedId, activityId);
        }
        
        queryWrapper.orderByDesc(AttendanceRule::getPriority)
                .orderByDesc(AttendanceRule::getCreatedAt);
        
        List<AttendanceRule> rules = attendanceRuleMapper.selectList(queryWrapper);
        
        return rules.stream()
                .map(rule -> com.hngy.siae.core.utils.BeanConvertUtil.to(rule, AttendanceRuleVO.class))
                .collect(java.util.stream.Collectors.toList());
    }
}
