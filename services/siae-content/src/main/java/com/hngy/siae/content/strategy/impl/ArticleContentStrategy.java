package com.hngy.siae.content.strategy.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.content.enums.ContentTypeEnum;
import com.hngy.siae.content.dto.request.content.ContentDetailDTO;
import com.hngy.siae.content.dto.response.ContentDetailVO;
import com.hngy.siae.content.dto.response.detail.ArticleVO;
import com.hngy.siae.content.entity.detail.Article;
import com.hngy.siae.content.mapper.ArticleMapper;
import com.hngy.siae.content.strategy.ContentStrategy;
import com.hngy.siae.content.strategy.StrategyType;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.ContentResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文章内容策略
 *
 * @author KEYKB
 * &#064;date: 2025/05/19
 */

@StrategyType(ContentTypeEnum.ARTICLE)
@Component
@RequiredArgsConstructor
public class ArticleContentStrategy
        extends ServiceImpl<ArticleMapper, Article>
        implements ContentStrategy {

    @Override
    public ContentDetailVO insert(Long contentId, ContentDetailDTO dto) {
        Article article = BeanConvertUtil.to(dto, Article.class);
        article.setContentId(contentId);
        AssertUtils.isTrue(this.save(article), ContentResultCodeEnum.ARTICLE_DETAIL_INSERT_FAILED);
        return BeanConvertUtil.to(article, ArticleVO.class);
    }

    @Override
    public ContentDetailVO update(Long contentId, ContentDetailDTO dto) {
        Article article = BeanConvertUtil.to(dto, Article.class);
        article.setContentId(contentId);
        AssertUtils.isTrue(this.updateById(article), ContentResultCodeEnum.ARTICLE_DETAIL_UPDATE_FAILED);
        return BeanConvertUtil.to(article, ArticleVO.class);
    }

    @Override
    public void delete(Long contentId) {
        this.remove(new LambdaQueryWrapper<Article>().eq(Article::getContentId, contentId));
    }

    @Override
    public boolean batchDelete(List<Long> contentIds) {
        return this.remove(new LambdaQueryWrapper<Article>().in(Article::getContentId, contentIds));
    }

    @Override
    public ContentDetailVO getDetail(Long contentId) {
        Article article = this.getOne(new LambdaQueryWrapper<Article>().eq(Article::getContentId, contentId));
        AssertUtils.notNull(article, ContentResultCodeEnum.CONTENT_DETAIL_NOT_FOUND);
        return BeanConvertUtil.to(article, ArticleVO.class);
    }
}

