package com.hngy.siae.content.strategy.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.content.enums.ContentTypeEnum;
import com.hngy.siae.content.dto.request.content.ContentDetailDTO;
import com.hngy.siae.content.dto.response.ContentDetailVO;
import com.hngy.siae.content.dto.response.detail.VideoVO;
import com.hngy.siae.content.entity.detail.Video;
import com.hngy.siae.content.mapper.VideoMapper;
import com.hngy.siae.content.strategy.ContentStrategy;
import com.hngy.siae.content.strategy.StrategyType;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.ContentResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@StrategyType(ContentTypeEnum.VIDEO)
@Component
@RequiredArgsConstructor
public class VideoContentStrategy
        extends ServiceImpl<VideoMapper, Video>
        implements ContentStrategy {

    @Override
    public ContentDetailVO insert(Long contentId, ContentDetailDTO dto) {
        Video video = BeanConvertUtil.to(dto, Video.class);
        video.setContentId(contentId);
        AssertUtils.isTrue(this.save(video), ContentResultCodeEnum.VIDEO_DETAIL_INSERT_FAILED);
        return BeanConvertUtil.to(video, VideoVO.class);
    }

    @Override
    public ContentDetailVO update(Long contentId, ContentDetailDTO dto) {
        Video video = BeanConvertUtil.to(dto, Video.class);
        video.setContentId(contentId);
        AssertUtils.isTrue(this.updateById(video), ContentResultCodeEnum.VIDEO_DETAIL_UPDATE_FAILED);
        return BeanConvertUtil.to(video, VideoVO.class);
    }

    @Override
    public void delete(Long contentId) {
        this.remove(new LambdaQueryWrapper<Video>().eq(Video::getContentId, contentId));
    }

    @Override
    public boolean batchDelete(List<Long> contentIds) {
        return this.remove(new LambdaQueryWrapper<Video>().in(Video::getContentId, contentIds));
    }

    @Override
    public ContentDetailVO getDetail(Long contentId) {
        Video video = this.getOne(new LambdaQueryWrapper<Video>().eq(Video::getContentId, contentId));
        AssertUtils.notNull(video, ContentResultCodeEnum.CONTENT_DETAIL_NOT_FOUND);
        return BeanConvertUtil.to(video, VideoVO.class);
    }
}