package com.hngy.siae.content.strategy.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.content.common.enums.ContentTypeEnum;
import com.hngy.siae.content.dto.request.content.ContentDetailDTO;
import com.hngy.siae.content.dto.response.ContentDetailVO;
import com.hngy.siae.content.dto.response.detail.ArticleVO;
import com.hngy.siae.content.entity.detail.Article;
import com.hngy.siae.content.mapper.ArticleMapper;
import com.hngy.siae.content.strategy.ContentStrategy;
import com.hngy.siae.content.strategy.StrategyType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.hngy.siae.core.asserts.AssertUtils;

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
        Article article = BeanUtil.copyProperties(dto, Article.class);
        article.setContentId(contentId);
        AssertUtils.isTrue(this.save(article), "文章详情插入失败");
        return BeanUtil.copyProperties(article, ArticleVO.class);
    }

    @Override
    public ContentDetailVO update(Long contentId, ContentDetailDTO dto) {
        Article article = BeanUtil.copyProperties(dto, Article.class);
        article.setContentId(contentId);
        AssertUtils.isTrue(this.updateById(article), "文章详情更新失败");
        return BeanUtil.copyProperties(article, ArticleVO.class);
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
        AssertUtils.notNull(article, "获取详情失败，改内容详情不存在");
        return BeanUtil.copyProperties(article, ArticleVO.class);
    }
}

