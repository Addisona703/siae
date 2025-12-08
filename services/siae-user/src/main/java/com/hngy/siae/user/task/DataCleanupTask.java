package com.hngy.siae.user.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hngy.siae.messaging.event.MessagingConstants;
import com.hngy.siae.messaging.event.UserDeletedEvent;
import com.hngy.siae.messaging.producer.SiaeMessagingTemplate;
import com.hngy.siae.user.entity.MajorClassEnrollment;
import com.hngy.siae.user.entity.Membership;
import com.hngy.siae.user.entity.User;
import com.hngy.siae.user.entity.UserProfile;
import com.hngy.siae.user.mapper.MajorClassEnrollmentMapper;
import com.hngy.siae.user.mapper.MembershipMapper;
import com.hngy.siae.user.mapper.UserMapper;
import com.hngy.siae.user.mapper.UserProfileMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 用户服务数据清理定时任务
 * <p>
 * 定期清理已逻辑删除超过指定天数的数据
 *
 * @author KEYKB
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataCleanupTask {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final MembershipMapper membershipMapper;
    private final MajorClassEnrollmentMapper majorClassEnrollmentMapper;
    private final SiaeMessagingTemplate messagingTemplate;

    /**
     * 数据保留天数，超过此天数的已删除数据将被物理删除
     */
    @Value("${siae.cleanup.retention-days:30}")
    private int retentionDays;

    /**
     * 每次清理的批次大小
     */
    @Value("${siae.cleanup.batch-size:100}")
    private int batchSize;

    /**
     * 每天凌晨2点执行清理任务
     */
    @Scheduled(cron = "${siae.cleanup.cron:0 0 2 * * ?}")
    public void cleanupDeletedData() {
        log.info("========== 开始执行数据清理任务 ==========");
        long startTime = System.currentTimeMillis();

        try {
            // 计算清理截止时间
            LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);
            log.info("清理 {} 天前已删除的数据，截止时间：{}", retentionDays, cutoffTime);

            // 1. 清理已删除用户的关联数据
            int deletedUsers = cleanupDeletedUsers(cutoffTime);

            // 2. 清理已删除的成员数据
            int deletedMemberships = cleanupDeletedMemberships(cutoffTime);

            // 3. 清理已删除的班级关联数据
            int deletedEnrollments = cleanupDeletedEnrollments(cutoffTime);

            long costTime = System.currentTimeMillis() - startTime;
            log.info("========== 数据清理任务完成 ==========");
            log.info("清理统计 - 用户: {}, 成员: {}, 班级关联: {}, 耗时: {}ms",
                    deletedUsers, deletedMemberships, deletedEnrollments, costTime);

        } catch (Exception e) {
            log.error("数据清理任务执行失败", e);
        }
    }

    /**
     * 清理已删除的用户及其关联数据
     */
    @Transactional(rollbackFor = Exception.class)
    public int cleanupDeletedUsers(LocalDateTime cutoffTime) {
        // 查询需要清理的用户ID
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getIsDeleted, 1)
                .lt(User::getUpdatedAt, cutoffTime)
                .last("LIMIT " + batchSize);

        List<User> deletedUsers = userMapper.selectList(wrapper);
        if (deletedUsers.isEmpty()) {
            log.info("没有需要清理的用户数据");
            return 0;
        }

        List<Long> userIds = deletedUsers.stream().map(User::getId).toList();
        log.info("准备清理 {} 个已删除用户: {}", userIds.size(), userIds);

        // 删除用户详情
        int profileCount = userProfileMapper.delete(
                new LambdaQueryWrapper<UserProfile>().in(UserProfile::getUserId, userIds)
        );
        log.info("删除用户详情: {} 条", profileCount);

        // 删除班级关联（物理删除）
        int enrollmentCount = majorClassEnrollmentMapper.delete(
                new LambdaQueryWrapper<MajorClassEnrollment>().in(MajorClassEnrollment::getUserId, userIds)
        );
        log.info("删除班级关联: {} 条", enrollmentCount);

        // 删除成员记录（物理删除）
        int membershipCount = membershipMapper.delete(
                new LambdaQueryWrapper<Membership>().in(Membership::getUserId, userIds)
        );
        log.info("删除成员记录: {} 条", membershipCount);

        // 最后删除用户主表（物理删除）
        int userCount = userMapper.deleteBatchIds(userIds);
        log.info("删除用户主表: {} 条", userCount);

        // 发送用户删除事件，通知其他服务清理关联数据
        sendUserDeletedEvent(userIds);

        return userCount;
    }

    /**
     * 发送用户删除事件到消息队列
     * 通知 Content、Notification、Media 等服务清理关联数据
     */
    private void sendUserDeletedEvent(List<Long> userIds) {
        try {
            UserDeletedEvent event = UserDeletedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .userIds(userIds)
                    .eventTime(LocalDateTime.now())
                    .sourceService("siae-user")
                    .reason("定时清理已删除用户数据")
                    .build();

            messagingTemplate.send(
                    MessagingConstants.CLEANUP_EXCHANGE,
                    MessagingConstants.CLEANUP_USER_DELETED,
                    event
            );

            log.info("已发送用户删除事件，userIds: {}", userIds);
        } catch (Exception e) {
            log.error("发送用户删除事件失败，userIds: {}", userIds, e);
            // 消息发送失败不影响主流程，下次清理任务会重新处理
        }
    }

    /**
     * 清理已删除的成员数据（孤立数据）
     */
    @Transactional(rollbackFor = Exception.class)
    public int cleanupDeletedMemberships(LocalDateTime cutoffTime) {
        LambdaQueryWrapper<Membership> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Membership::getIsDeleted, 1)
                .lt(Membership::getUpdatedAt, cutoffTime)
                .last("LIMIT " + batchSize);

        List<Membership> deletedMemberships = membershipMapper.selectList(wrapper);
        if (deletedMemberships.isEmpty()) {
            log.info("没有需要清理的成员数据");
            return 0;
        }

        List<Long> ids = deletedMemberships.stream().map(Membership::getId).toList();
        int count = membershipMapper.deleteBatchIds(ids);
        log.info("物理删除成员记录: {} 条", count);

        return count;
    }

    /**
     * 清理已删除的班级关联数据（孤立数据）
     */
    @Transactional(rollbackFor = Exception.class)
    public int cleanupDeletedEnrollments(LocalDateTime cutoffTime) {
        LambdaQueryWrapper<MajorClassEnrollment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MajorClassEnrollment::getIsDeleted, 1)
                .lt(MajorClassEnrollment::getUpdatedAt, cutoffTime)
                .last("LIMIT " + batchSize);

        List<MajorClassEnrollment> deletedEnrollments = majorClassEnrollmentMapper.selectList(wrapper);
        if (deletedEnrollments.isEmpty()) {
            log.info("没有需要清理的班级关联数据");
            return 0;
        }

        List<Long> ids = deletedEnrollments.stream().map(MajorClassEnrollment::getId).toList();
        int count = majorClassEnrollmentMapper.deleteBatchIds(ids);
        log.info("物理删除班级关联记录: {} 条", count);

        return count;
    }
}
