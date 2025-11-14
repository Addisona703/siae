package com.hngy.siae.content.strategy.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.content.enums.ContentTypeEnum;
import com.hngy.siae.content.dto.request.content.ContentDetailDTO;
import com.hngy.siae.content.dto.response.ContentDetailVO;
import com.hngy.siae.content.dto.response.detail.FileVO;
import com.hngy.siae.content.entity.detail.File;
import com.hngy.siae.content.mapper.FileMapper;
import com.hngy.siae.content.strategy.ContentStrategy;
import com.hngy.siae.content.strategy.StrategyType;
import com.hngy.siae.core.asserts.AssertUtils;
import com.hngy.siae.core.result.ContentResultCodeEnum;
import com.hngy.siae.core.utils.BeanConvertUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@StrategyType(ContentTypeEnum.FILE)
@Component
@RequiredArgsConstructor
public class FileContentStrategy
        extends ServiceImpl<FileMapper, File>
        implements ContentStrategy {

    @Override
    public ContentDetailVO insert(Long contentId, ContentDetailDTO dto) {
        File file = BeanConvertUtil.to(dto, File.class);
        file.setContentId(contentId);
        AssertUtils.isTrue(this.save(file), ContentResultCodeEnum.FILE_DETAIL_INSERT_FAILED);
        return BeanConvertUtil.to(file, FileVO.class);
    }

    @Override
    public ContentDetailVO update(Long contentId, ContentDetailDTO dto) {
        File file = BeanConvertUtil.to(dto, File.class);
        file.setContentId(contentId);
        AssertUtils.isTrue(this.updateById(file), ContentResultCodeEnum.FILE_DETAIL_UPDATE_FAILED);
        return BeanConvertUtil.to(file, FileVO.class);
    }

    @Override
    public void delete(Long contentId) {
        this.remove(new LambdaQueryWrapper<File>().eq(File::getContentId, contentId));
    }

    @Override
    public boolean batchDelete(List<Long> contentIds) {
        return this.remove(new LambdaQueryWrapper<File>().in(File::getContentId, contentIds));
    }

    @Override
    public ContentDetailVO getDetail(Long contentId) {
        File file = this.getOne(new LambdaQueryWrapper<File>().eq(File::getContentId, contentId));
        AssertUtils.notNull(file, ContentResultCodeEnum.CONTENT_DETAIL_NOT_FOUND);
        return BeanConvertUtil.to(file, FileVO.class);
    }
}