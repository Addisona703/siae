package com.hngy.siae.content.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.api.media.client.MediaFeignClient;
import com.hngy.siae.api.media.dto.request.BatchUrlDTO;
import com.hngy.siae.api.media.dto.response.BatchUrlVO;
import com.hngy.siae.api.user.client.UserFeignClient;
import com.hngy.siae.api.user.dto.response.UserProfileSimpleVO;
import com.hngy.siae.content.dto.response.content.ContentQueryResultVO;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.ContentResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.core.utils.PageConvertUtil;

import com.hngy.siae.core.enums.BaseEnum;
import com.hngy.siae.content.enums.ContentTypeEnum;
import com.hngy.siae.content.enums.status.ContentStatusEnum;
import com.hngy.siae.content.dto.request.content.ContentCreateDTO;
import com.hngy.siae.content.dto.request.content.ContentUpdateDTO;
import com.hngy.siae.content.dto.request.content.ContentQueryDTO;
import com.hngy.siae.content.dto.response.content.ContentVO;
import com.hngy.siae.content.dto.response.content.detail.EmptyDetailVO;
import com.hngy.siae.content.entity.Content;
import com.hngy.siae.content.mapper.ContentMapper;
import com.hngy.siae.content.mapper.CategoryMapper;
import com.hngy.siae.content.service.ContentService;
import com.hngy.siae.security.utils.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
/** 
 * 内容服务impl
 *
 * @author KEYKB
 * &#064;date: 2025/05/19
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentServiceImpl
        extends ServiceImpl<ContentMapper, Content>
        implements ContentService {

    private final CategoryMapper categoryMapper;
    private final UserFeignClient userFeignClient;
    private final MediaFeignClient mediaFeignClient;
    private final SecurityUtil securityUtil;

    @Override
    public Content createContent(ContentCreateDTO dto) {
        // 1. 检查重复
        AssertUtils.isFalse(this.lambdaQuery()
                .eq(Content::getTitle, dto.getTitle())
                .eq(Content::getDescription, dto.getDescription())
                .exists(), ContentResultCodeEnum.CONTENT_DUPLICATE_SUBMIT);

        // 2. 类型转换
        ContentTypeEnum type = BaseEnum.fromDesc(ContentTypeEnum.class, dto.getType());
        AssertUtils.notNull(type, ContentResultCodeEnum.CONTENT_TYPE_NOT_SUPPORTED);

        // 3. 状态校验和转换：只允许草稿或发布，发布时设置为待审核
        ContentStatusEnum status = dto.getStatus();
        if (status == ContentStatusEnum.DRAFT) {
            // 草稿状态保持不变
        } else if (status == ContentStatusEnum.PUBLISHED || status == ContentStatusEnum.PENDING) {
            // 发布时默认为待审核状态
            status = ContentStatusEnum.PENDING;
        } else {
            // 只允许传入草稿或发布状态
            AssertUtils.fail(ContentResultCodeEnum.CONTENT_STATUS_INVALID);
        }

        // 4. 插入主内容，忽略 type 和 status 字段
        Content content = BeanConvertUtil.to(dto, Content.class, "type", "status");
        // 手动设置type和status字段
        content.setType(type);
        content.setStatus(status);
        // 自动回填主键，id
        AssertUtils.isTrue(this.save(content), ContentResultCodeEnum.CONTENT_INSERT_FAILED);

        return content;
    }


    @Override
    public Content updateContent(ContentUpdateDTO contentUpdateDTO) {
        // 1. 查询内容是否存在
        Content existingContent = this.getById(contentUpdateDTO.getId());
        AssertUtils.notNull(existingContent, ContentResultCodeEnum.CONTENT_NOT_EXISTS);

        // 2. 校验是否是内容所有者（只有创建者本人才能编辑）
        Long currentUserId = securityUtil.getCurrentUserId();
        AssertUtils.isTrue(existingContent.getUploadedBy().equals(currentUserId), 
                ContentResultCodeEnum.CONTENT_NO_PERMISSION);

        // 3. 拷贝 DTO 到实体，忽略 type 字段（需要手动转换枚举）
        Content content = BeanConvertUtil.to(contentUpdateDTO, Content.class, "type", "status");
        
        // 4. 手动处理类型转换（String -> ContentTypeEnum）
        Optional.ofNullable(contentUpdateDTO.getType())
                .map(typeStr -> {
                    ContentTypeEnum type = BaseEnum.fromDesc(ContentTypeEnum.class, typeStr);
                    AssertUtils.notNull(type, ContentResultCodeEnum.CONTENT_TYPE_NOT_SUPPORTED);
                    return type;
                })
                .ifPresent(content::setType);

        // 5. 更新数据库
        AssertUtils.isTrue(this.updateById(content), ContentResultCodeEnum.CONTENT_UPDATE_FAILED);

        return content;
    }


    @Override
    public void deleteContent(Integer id, Integer isTrash) {
        // 1. 参数校验
        AssertUtils.isTrue(isTrash == 0 || isTrash == 1, ContentResultCodeEnum.CONTENT_DELETE_INVALID_OPERATION);

        // 2. 查询内容是否存在
        Content content = this.getById(id);
        AssertUtils.notNull(content, ContentResultCodeEnum.CONTENT_NOT_FOUND);

        // 3. 删除
        if (isTrash == 1) {
            // 放入回收站（软删除）
            content.setStatus(ContentStatusEnum.TRASH);
            AssertUtils.isTrue(this.updateById(content), ContentResultCodeEnum.CONTENT_TRASH_FAILED);
        } else {
            // 不放入回收站（伪硬删除，通过定时任务实现硬删除）
            content.setStatus(ContentStatusEnum.DELETED);
            AssertUtils.isTrue(this.updateById(content), ContentResultCodeEnum.CONTENT_PERMANENT_DELETE_FAILED);
        }
    }

    @Override
    public void restoreContent(Long contentId) {
        // 1. 查询内容是否存在
        Content content = this.getById(contentId);
        AssertUtils.notNull(content, ContentResultCodeEnum.CONTENT_NOT_FOUND);

        // 2. 检查是否在回收站
        AssertUtils.isTrue(content.getStatus() == ContentStatusEnum.TRASH, ContentResultCodeEnum.CONTENT_NOT_IN_TRASH);

        // 3. 恢复为已发布状态
        content.setStatus(ContentStatusEnum.PUBLISHED);
        AssertUtils.isTrue(this.updateById(content), ContentResultCodeEnum.CONTENT_RESTORE_FAILED);
    }

    @Override
    public PageVO<ContentVO<EmptyDetailVO>> getContentPage(PageDTO<ContentQueryDTO> dto) {
        // 构建分页参数
        Page<ContentVO<EmptyDetailVO>> page = PageConvertUtil.toPage(dto);
        
        // 获取查询条件
        ContentQueryDTO query = dto.getParams();
        if (query == null) {
            query = new ContentQueryDTO();
        }

        // 获取当前用户ID
        Long currentUserId = securityUtil.getCurrentUserId();

        // 使用 XML 联表查询（已发布的内容 + 当前用户的草稿和待审核）
        Page<ContentVO<EmptyDetailVO>> resultPage = baseMapper.selectContentPageByQuery(page, query, currentUserId);
        
        // 批量填充作者昵称
        fillAuthorNicknames(resultPage.getRecords());
        
        // 批量填充封面URL
        fillCoverUrls(resultPage.getRecords());
        
        return PageConvertUtil.convert(resultPage);
    }

    @Override
    public PageVO<ContentVO<EmptyDetailVO>> getPendingContentPage(PageDTO<ContentQueryDTO> dto) {
        // 构建分页参数
        Page<ContentVO<EmptyDetailVO>> page = PageConvertUtil.toPage(dto);
        
        // 获取查询条件
        ContentQueryDTO query = dto.getParams();
        if (query == null) {
            query = new ContentQueryDTO();
        }

        // 使用 XML 联表查询所有待审核的内容（管理员）
        Page<ContentVO<EmptyDetailVO>> resultPage = baseMapper.selectPendingContentPage(page, query);
        
        // 批量填充作者昵称
        fillAuthorNicknames(resultPage.getRecords());
        
        // 批量填充封面URL
        fillCoverUrls(resultPage.getRecords());
        
        return PageConvertUtil.convert(resultPage);
    }

    /**
     * 批量填充作者昵称
     */
    private void fillAuthorNicknames(List<ContentVO<EmptyDetailVO>> contentList) {
        if (CollUtil.isEmpty(contentList)) {
            return;
        }

        try {
            // 收集所有作者ID
            List<Long> userIds = contentList.stream()
                    .map(ContentVO::getUploadedBy)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .toList();
            log.info("收集到的用户ID: {}", userIds);

            if (CollUtil.isEmpty(userIds)) {
                return;
            }

            // 批量查询用户信息
            Map<Long, UserProfileSimpleVO> userMap =
                    userFeignClient.batchGetUserProfiles(userIds);

            if (userMap != null && !userMap.isEmpty()) {
                // 填充昵称和头像
                contentList.forEach(content -> {
                    com.hngy.siae.api.user.dto.response.UserProfileSimpleVO userProfile = 
                            userMap.get(content.getUploadedBy());
                    if (userProfile != null) {
                        content.setAuthorNickname(userProfile.getNickname());
                        content.setAuthorAvatarUrl(userProfile.getAvatarUrl());
                    }
                });
            }
        } catch (Exception e) {
            log.warn("批量查询用户信息失败，跳过填充作者昵称", e);
        }
    }

    @Override
    public boolean updateStatus(Long contentId, ContentStatusEnum status) {
        Content content = this.getById(contentId);
        AssertUtils.notNull(content, ContentResultCodeEnum.CONTENT_NOT_FOUND);
        content.setStatus(status);
        // 使用乐观锁更新，version 字段会由 MyBatis-Plus 自动处理
        // 如果并发冲突，updateById 会返回 false
        return this.updateById(content);
    }

    @Override
    public ContentQueryResultVO getContentDetail(Long contentId) {
        ContentQueryResultVO contentQueryResultVO = baseMapper.selectContentDetailById(contentId);
        AssertUtils.notNull(contentQueryResultVO, ContentResultCodeEnum.CONTENT_NOT_FOUND);
        return contentQueryResultVO;
    }

    /**
     * 批量填充封面URL
     */
    private void fillCoverUrls(List<ContentVO<EmptyDetailVO>> contentList) {
        if (CollUtil.isEmpty(contentList)) {
            return;
        }

        try {
            // 收集所有封面文件ID
            List<String> coverFileIds = contentList.stream()
                    .map(ContentVO::getCoverFileId)
                    .filter(StrUtil::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());

            if (CollUtil.isEmpty(coverFileIds)) {
                return;
            }

            // 批量获取文件URL
            BatchUrlDTO request = new BatchUrlDTO();
            request.setFileIds(coverFileIds);
            request.setExpirySeconds(86400); // 24小时过期

            BatchUrlVO response = mediaFeignClient.batchGetFileUrls(request);

            if (response != null && response.getUrls() != null && !response.getUrls().isEmpty()) {
                Map<String, String> urlMap = response.getUrls();
                // 填充封面URL
                contentList.forEach(content -> {
                    String coverFileId = content.getCoverFileId();
                    if (StrUtil.isNotBlank(coverFileId)) {
                        String coverUrl = urlMap.get(coverFileId);
                        content.setCoverUrl(coverUrl);
                    }
                });
            }
        } catch (Exception e) {
            log.warn("批量获取封面URL失败，跳过填充封面URL", e);
        }
    }

    // ==================== AI 服务接口实现 ====================

    @Override
    public List<com.hngy.siae.api.ai.dto.response.ContentInfo> searchForAi(String keyword, String categoryName, Integer limit) {
        return baseMapper.searchForAi(keyword, categoryName, limit);
    }

    @Override
    public List<com.hngy.siae.api.ai.dto.response.ContentInfo> getHotContentForAi(Integer limit) {
        return baseMapper.getHotContentForAi(limit);
    }

    @Override
    public List<com.hngy.siae.api.ai.dto.response.ContentInfo> getLatestContentForAi(Integer limit) {
        return baseMapper.getLatestContentForAi(limit);
    }
}
