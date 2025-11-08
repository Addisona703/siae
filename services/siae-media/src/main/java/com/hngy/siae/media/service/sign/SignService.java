package com.hngy.siae.media.service.sign;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.MediaResultCodeEnum;
import com.hngy.siae.media.domain.dto.sign.SignRequest;
import com.hngy.siae.media.domain.dto.sign.SignResponse;
import com.hngy.siae.media.domain.entity.DownloadToken;
import com.hngy.siae.media.domain.entity.FileEntity;
import com.hngy.siae.media.domain.enums.FileStatus;
import com.hngy.siae.media.infrastructure.storage.StorageService;
import com.hngy.siae.media.repository.DownloadTokenRepository;
import com.hngy.siae.media.repository.FileRepository;
import com.hngy.siae.media.service.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 签名服务
 * 负责生成下载签名URL和权限验证
 *
 * @author SIAE Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignService {

    private final FileRepository fileRepository;
    private final DownloadTokenRepository downloadTokenRepository;
    private final StorageService storageService;
    private final AuditService auditService;

    /**
     * 生成下载签名
     */
    @Transactional
    public SignResponse generateDownloadSign(SignRequest request, String tenantId, String userId, String ip) {
        // 1. 查询文件
        FileEntity file = fileRepository.selectById(request.getFileId());
        AssertUtils.notNull(file, MediaResultCodeEnum.FILE_NOT_FOUND);

        // 2. 验证租户隔离
        AssertUtils.isTrue(file.getTenantId().equals(tenantId), MediaResultCodeEnum.UNAUTHORIZED_FILE_ACCESS);

        // 3. 验证文件状态（必须是已完成状态）
        AssertUtils.isTrue(file.getStatus() == FileStatus.COMPLETED, MediaResultCodeEnum.FILE_STATUS_INVALID);

        // 4. 验证文件未被删除
        AssertUtils.isNull(file.getDeletedAt(), MediaResultCodeEnum.FILE_ALREADY_DELETED);

        // 5. 验证ACL权限
        validateAclPermission(file, userId);

        // 6. 生成下载令牌（如果需要）
        String token = null;
        if (Boolean.TRUE.equals(request.getSingleUse()) || Boolean.TRUE.equals(request.getBindIp())) {
            token = generateDownloadToken(file, tenantId, userId, ip, request);
        }

        // 7. 生成预签名URL
        String presignedUrl = storageService.generatePresignedDownloadUrl(
                file.getBucket(),
                file.getStorageKey(),
                request.getExpirySeconds()
        );

        // 8. 如果有令牌，将令牌附加到URL
        if (token != null) {
            presignedUrl = appendTokenToUrl(presignedUrl, token);
        }

        // 9. 记录审计日志
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("expirySeconds", request.getExpirySeconds());
        metadata.put("bindIp", request.getBindIp());
        metadata.put("singleUse", request.getSingleUse());
        metadata.put("ip", ip);
        auditService.logSignGenerate(file.getId(), tenantId, userId, metadata);

        // 10. 构建响应
        return SignResponse.builder()
                .fileId(file.getId())
                .url(presignedUrl)
                .token(token)
                .expiresAt(LocalDateTime.now().plusSeconds(request.getExpirySeconds()))
                .bindIp(request.getBindIp())
                .singleUse(request.getSingleUse())
                .build();
    }

    /**
     * 验证ACL权限
     */
    private void validateAclPermission(FileEntity file, String userId) {
        Map<String, Object> acl = file.getAcl();
        if (acl == null || acl.isEmpty()) {
            // 没有ACL配置，默认只有所有者可以访问
            if (!file.getOwnerId().equals(userId)) {
                AssertUtils.fail(MediaResultCodeEnum.UNAUTHORIZED_FILE_ACCESS);
            }
            return;
        }

        // 检查是否为所有者
        if (file.getOwnerId().equals(userId)) {
            return;
        }

        // 检查是否为公开文件
        Object publicAccess = acl.get("public");
        if (Boolean.TRUE.equals(publicAccess)) {
            return;
        }

        // 检查用户是否在允许列表中
        Object allowedUsers = acl.get("allowedUsers");
        if (allowedUsers instanceof java.util.List) {
            @SuppressWarnings("unchecked")
            java.util.List<String> userList = (java.util.List<String>) allowedUsers;
            if (userList.contains(userId)) {
                return;
            }
        }

        AssertUtils.fail(MediaResultCodeEnum.UNAUTHORIZED_FILE_ACCESS);
    }

    /**
     * 生成下载令牌
     */
    private String generateDownloadToken(FileEntity file, String tenantId, String userId, 
                                        String ip, SignRequest request) {
        DownloadToken downloadToken = new DownloadToken();
        downloadToken.setFileId(file.getId());
        downloadToken.setTenantId(tenantId);
        downloadToken.setUserId(userId);
        downloadToken.setToken(UUID.randomUUID().toString().replace("-", ""));
        downloadToken.setBindIp(Boolean.TRUE.equals(request.getBindIp()) ? ip : null);
        downloadToken.setSingleUse(request.getSingleUse());
        downloadToken.setUsed(false);
        downloadToken.setExpiresAt(LocalDateTime.now().plusSeconds(request.getExpirySeconds()));
        downloadToken.setCreatedAt(LocalDateTime.now());

        downloadTokenRepository.insert(downloadToken);
        log.info("Generated download token for file: {}, token: {}", file.getId(), downloadToken.getToken());

        return downloadToken.getToken();
    }

    /**
     * 将令牌附加到URL
     */
    private String appendTokenToUrl(String url, String token) {
        String separator = url.contains("?") ? "&" : "?";
        return url + separator + "token=" + token;
    }

    /**
     * 验证下载令牌
     */
    @Transactional
    public boolean validateDownloadToken(String token, String fileId, String ip) {
        if (token == null || token.isEmpty()) {
            return true; // 没有令牌要求，直接通过
        }

        LambdaQueryWrapper<DownloadToken> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DownloadToken::getToken, token)
               .eq(DownloadToken::getFileId, fileId);

        DownloadToken downloadToken = downloadTokenRepository.selectOne(wrapper);
        if (downloadToken == null) {
            log.warn("Download token not found: {}", token);
            return false;
        }

        // 检查是否已过期
        if (downloadToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Download token expired: {}", token);
            return false;
        }

        // 检查是否已使用
        if (Boolean.TRUE.equals(downloadToken.getSingleUse()) && Boolean.TRUE.equals(downloadToken.getUsed())) {
            log.warn("Download token already used: {}", token);
            return false;
        }

        // 检查IP绑定
        if (downloadToken.getBindIp() != null && !downloadToken.getBindIp().equals(ip)) {
            log.warn("Download token IP mismatch: expected={}, actual={}", downloadToken.getBindIp(), ip);
            return false;
        }

        // 标记为已使用
        if (Boolean.TRUE.equals(downloadToken.getSingleUse())) {
            downloadToken.setUsed(true);
            downloadToken.setUsedAt(LocalDateTime.now());
            downloadTokenRepository.updateById(downloadToken);
        }

        return true;
    }

}
