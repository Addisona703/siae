package com.hngy.siae.content.strategy.content.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hngy.siae.content.enums.ContentTypeEnum;
import com.hngy.siae.content.dto.request.content.ContentDetailDTO;
import com.hngy.siae.content.dto.response.content.ContentDetailVO;
import com.hngy.siae.content.dto.response.content.detail.ArticleVO;
import com.hngy.siae.content.entity.detail.Article;
import com.hngy.siae.content.mapper.ArticleMapper;
import com.hngy.siae.content.strategy.content.ContentStrategy;
import com.hngy.siae.content.strategy.content.ContentType;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.ContentResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文章内容策略
 * 使用组合方式，直接注入 Mapper
 *
 * @author KEYKB
 * &#064;date: 2025/05/19
 */
@ContentType(ContentTypeEnum.ARTICLE)
@Component
@RequiredArgsConstructor
public class ArticleContentStrategy implements ContentStrategy {

    private final ArticleMapper articleMapper;

    @Override
    public ContentDetailVO insert(Long contentId, ContentDetailDTO dto) {
        Article article = BeanConvertUtil.to(dto, Article.class);
        article.setContentId(contentId);
        int rows = articleMapper.insert(article);
        AssertUtils.isTrue(rows > 0, ContentResultCodeEnum.ARTICLE_DETAIL_INSERT_FAILED);
        return BeanConvertUtil.to(article, ArticleVO.class);
    }

    @Override
    public ContentDetailVO update(Long contentId, ContentDetailDTO dto) {
        // 先查询现有记录
        Article existingArticle = articleMapper.selectOne(
                new LambdaQueryWrapper<Article>().eq(Article::getContentId, contentId));
        AssertUtils.notNull(existingArticle, ContentResultCodeEnum.CONTENT_DETAIL_NOT_FOUND);
        
        // 复制 DTO 属性到现有记录
        BeanConvertUtil.to(dto, existingArticle);
        existingArticle.setContentId(contentId);
        
        int rows = articleMapper.updateById(existingArticle);
        AssertUtils.isTrue(rows > 0, ContentResultCodeEnum.ARTICLE_DETAIL_UPDATE_FAILED);
        return BeanConvertUtil.to(existingArticle, ArticleVO.class);
    }

    @Override
    public void delete(Long contentId) {
        articleMapper.delete(new LambdaQueryWrapper<Article>().eq(Article::getContentId, contentId));
    }

    @Override
    public boolean batchDelete(List<Long> contentIds) {
        return articleMapper.delete(new LambdaQueryWrapper<Article>().in(Article::getContentId, contentIds)) > 0;
    }

    @Override
    public ContentDetailVO getDetail(Long contentId) {
        Article article = articleMapper.selectOne(new LambdaQueryWrapper<Article>().eq(Article::getContentId, contentId));
        AssertUtils.notNull(article, ContentResultCodeEnum.CONTENT_DETAIL_NOT_FOUND);
        return BeanConvertUtil.to(article, ArticleVO.class);
    }
}
