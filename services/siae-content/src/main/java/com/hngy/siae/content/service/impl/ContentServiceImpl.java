package com.hngy.siae.content.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.ContentResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import com.hngy.siae.web.utils.PageConvertUtil;

import com.hngy.siae.core.enums.BaseEnum;
import com.hngy.siae.content.enums.ContentTypeEnum;
import com.hngy.siae.content.enums.status.ContentStatusEnum;
import com.hngy.siae.content.dto.request.content.ContentCreateDTO;
import com.hngy.siae.content.dto.request.content.ContentUpdateDTO;
import com.hngy.siae.content.dto.request.content.ContentPageDTO;
import com.hngy.siae.content.dto.response.ContentVO;
import com.hngy.siae.content.dto.response.detail.EmptyDetailVO;
import com.hngy.siae.content.entity.Content;
import com.hngy.siae.content.entity.Category;
import com.hngy.siae.content.feign.UserFeignClient;
import com.hngy.siae.content.feign.dto.UserProfileDTO;
import com.hngy.siae.content.mapper.ContentMapper;
import com.hngy.siae.content.mapper.CategoryMapper;
import com.hngy.siae.content.service.ContentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
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

        // 3. 插入主内容，直接按下面的方式拷贝不会报错，遇到类型不同，字段相同的会自动跳过
        // 在之前的写法中，因为两个类的Type字段类型不同所以开启跳过异常配置，然后在重新设置Type
        // 或者写出需要忽略的字段
        Content content = BeanConvertUtil.to(dto, Content.class);
        // 手动设置type字段，因为DTO和Entity的类型不同
        content.setType(null);
        content.setType(type);
        // 自动回填主键，id
        AssertUtils.isTrue(this.save(content), ContentResultCodeEnum.CONTENT_INSERT_FAILED);

        return content;
    }


    @Override
    public Content updateContent(ContentUpdateDTO contentUpdateDTO) {
        // 1. 校验内容是否存在
        AssertUtils.isTrue(this.lambdaQuery()
                .eq(Content::getId, contentUpdateDTO.getId())
                .exists(), ContentResultCodeEnum.CONTENT_NOT_EXISTS);

        // 2. 拷贝 DTO 到实体，并处理类型转换
        Content content = BeanConvertUtil.to(contentUpdateDTO, Content.class);
        Optional.ofNullable(contentUpdateDTO.getType())
                .map(typeStr -> {
                    ContentTypeEnum type = BaseEnum.fromDesc(ContentTypeEnum.class, typeStr);
                    AssertUtils.notNull(type, ContentResultCodeEnum.CONTENT_TYPE_NOT_SUPPORTED);
                    return type;
                })
                .ifPresent(content::setType);

        // 3. 更新数据库
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
    public void deleteContent(Integer id, Integer isTrash, Long currentUserId, boolean isAdmin) {
        // 1. 参数校验
        AssertUtils.isTrue(isTrash == 0 || isTrash == 1, ContentResultCodeEnum.CONTENT_DELETE_INVALID_OPERATION);

        // 2. 查询内容是否存在
        Content content = this.getById(id);
        AssertUtils.notNull(content, ContentResultCodeEnum.CONTENT_NOT_FOUND);

        // 3. 权限校验：非管理员只能删除自己创建的内容
        if (!isAdmin) {
            AssertUtils.isTrue(content.getUploadedBy().equals(currentUserId), 
                    ContentResultCodeEnum.CONTENT_DELETE_PERMISSION_DENIED);
        }

        // 4. 删除
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
    public PageVO<ContentVO<EmptyDetailVO>> getContentPage(PageDTO<ContentPageDTO> dto) {
        // 如果查询条件简单，使用联表查询
        ContentPageDTO params = dto.getParams();
        boolean useJoinQuery = StrUtil.isBlank(dto.getKeyword()) 
                && (params == null || CollUtil.isEmpty(params.getTagIds()));
        
        if (useJoinQuery) {
            // 使用联表查询
            Page<ContentVO<EmptyDetailVO>> page = new Page<>(dto.getPageNum(), dto.getPageSize());
            Content queryContent = new Content();
            if (params != null) {
                queryContent.setCategoryId(params.getCategoryId());
                queryContent.setStatus(params.getStatus());
                queryContent.setType(params.getType());
            }
            
            Page<ContentVO<EmptyDetailVO>> resultPage = baseMapper.selectContentPageWithDetails(page, queryContent);
            
            // 批量填充作者昵称
            fillAuthorNicknames(resultPage.getRecords());
            
            // 设置空的detail对象
            resultPage.getRecords().forEach(vo -> vo.setDetail(new EmptyDetailVO()));
            
            return PageConvertUtil.convert(resultPage);
        }
        
        // 复杂查询使用原有逻辑
        Page<Content> page = PageConvertUtil.toPage(dto);

        LambdaQueryWrapper<Content> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.isNotNull(Content::getId);

        if (params != null) {
            if (params.getCategoryId() != null) {
                queryWrapper.eq(Content::getCategoryId, params.getCategoryId());
            }
            if (params.getStatus() != null) {
                queryWrapper.eq(Content::getStatus, params.getStatus());
            }
            if (params.getType() != null) {
                queryWrapper.eq(Content::getType, params.getType());
            }
            if (CollUtil.isNotEmpty(params.getTagIds())) {
                String tagIdSql = CollUtil.join(params.getTagIds(), ",");
                queryWrapper.inSql(Content::getId,
                        "SELECT ctr.content_id FROM content_tag_relation ctr WHERE ctr.tag_id IN (" + tagIdSql + ")");
            }
        }

        if (StrUtil.isNotBlank(dto.getKeyword())) {
            String keyword = dto.getKeyword();
            queryWrapper.and(wrapper ->
                    wrapper.like(Content::getTitle, keyword)
                            .or()
                            .like(Content::getDescription, keyword)
            );
        }

        queryWrapper.orderByDesc(Content::getCreateTime);

        Page<Content> resultPage = this.page(page, queryWrapper);

        PageVO<ContentVO<EmptyDetailVO>> pageVO = PageConvertUtil.convert(resultPage, this::convertToContentVO);
        
        // 批量填充作者昵称
        fillAuthorNicknames(pageVO.getRecords());
        
        return pageVO;
    }

    /**
     * 将Content实体转换为ContentVO（用于复杂查询场景）
     */
    private ContentVO<EmptyDetailVO> convertToContentVO(Content content) {
        // 使用联表查询获取完整信息
        Page<ContentVO<EmptyDetailVO>> page = new Page<>(1, 1);
        Content queryContent = new Content();
        queryContent.setId(content.getId());
        
        Page<ContentVO<EmptyDetailVO>> result = baseMapper.selectContentPageWithDetails(page, queryContent);
        
        if (CollUtil.isNotEmpty(result.getRecords())) {
            ContentVO<EmptyDetailVO> vo = result.getRecords().get(0);
            vo.setDetail(new EmptyDetailVO());
            return vo;
        }
        
        // 降级处理
        ContentVO<EmptyDetailVO> vo = BeanConvertUtil.to(content, ContentVO.class);
        vo.setDetail(new EmptyDetailVO());
        return vo;
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

            if (CollUtil.isEmpty(userIds)) {
                return;
            }

            // 批量查询用户信息
            Map<Long, UserProfileDTO> userMap = userFeignClient.batchGetUserProfiles(userIds);
            
            if (userMap != null && !userMap.isEmpty()) {
                // 填充昵称
                contentList.forEach(content -> {
                    UserProfileDTO userProfile = userMap.get(content.getUploadedBy());
                    if (userProfile != null) {
                        content.setAuthorNickname(userProfile.getNickname());
                    }
                });
            }
        } catch (Exception e) {
            log.warn("批量查询用户信息失败，跳过填充作者昵称", e);
        }
    }
}
