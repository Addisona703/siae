package com.hngy.siae.media.service.sign;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hngy.siae.media.domain.entity.DownloadToken;
import com.hngy.siae.media.repository.DownloadTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 下载令牌清理服务
 * 定期清理过期的下载令牌
 *
 * @author SIAE Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DownloadTokenCleanupService {

    private final DownloadTokenRepository downloadTokenRepository;

    /**
     * 清理过期的下载令牌
     * 每小时执行一次
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting cleanup of expired download tokens");

        try {
            LocalDateTime now = LocalDateTime.now();
            
            // 查询过期的令牌
            LambdaQueryWrapper<DownloadToken> wrapper = new LambdaQueryWrapper<>();
            wrapper.lt(DownloadToken::getExpiresAt, now);

            List<DownloadToken> expiredTokens = downloadTokenRepository.selectList(wrapper);
            
            if (expiredTokens.isEmpty()) {
                log.info("No expired download tokens found");
                return;
            }

            // 删除过期令牌
            int deleted = downloadTokenRepository.delete(wrapper);
            log.info("Cleaned up {} expired download tokens", deleted);

        } catch (Exception e) {
            log.error("Failed to cleanup expired download tokens", e);
        }
    }

    /**
     * 清理已使用的单次令牌
     * 每天执行一次，清理7天前的已使用令牌
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupUsedTokens() {
        log.info("Starting cleanup of used single-use tokens");

        try {
            LocalDateTime threshold = LocalDateTime.now().minusDays(7);
            
            // 查询已使用的单次令牌
            LambdaQueryWrapper<DownloadToken> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(DownloadToken::getSingleUse, true)
                   .eq(DownloadToken::getUsed, true)
                   .lt(DownloadToken::getUsedAt, threshold);

            int deleted = downloadTokenRepository.delete(wrapper);
            log.info("Cleaned up {} used single-use tokens", deleted);

        } catch (Exception e) {
            log.error("Failed to cleanup used tokens", e);
        }
    }

}
