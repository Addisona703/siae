package com.hngy.siae.content.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.content.common.enums.status.CommentStatusEnum;
import com.hngy.siae.content.dto.request.CommentDTO;
import com.hngy.siae.content.dto.response.CommentVO;
import com.hngy.siae.content.entity.Comment;
import com.hngy.siae.content.mapper.CommentMapper;
import com.hngy.siae.content.service.CommentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.hngy.siae.core.asserts.AssertUtils;

/**
 * 内容评论服务impl
 *
 * @author KEYKB
 * 创建时间: 2025/05/21
 */
@Service
@RequiredArgsConstructor
public class CommentsServiceImpl
        extends ServiceImpl<CommentMapper, Comment>
        implements CommentsService {


    @Override
    public Comment createComment(Long contentId, CommentDTO commentDTO) {
        Comment comment = BeanUtil.copyProperties(commentDTO, Comment.class);
        comment.setStatus(CommentStatusEnum.APPROVED);
        comment.setContentId(contentId);
        AssertUtils.isTrue(this.save(comment), "评论创建失败");
        return comment;
    }

    @Override
    public Result<CommentVO> updateComment(Long commentId, CommentDTO commentDTO) {
        Comment comment = this.getById(commentId);
        AssertUtils.notNull(comment, "该评论不存在");

        comment.setContent(commentDTO.getContent());
        AssertUtils.isTrue(this.updateById(comment), "评论更新失败");

        CommentVO commentVO = BeanUtil.copyProperties(comment, CommentVO.class);
        return Result.success(commentVO);
    }

    @Override
    public Result<Void> deleteComment(Long id) {
        AssertUtils.notNull(this.getById(id), "评论不存在");
        AssertUtils.isTrue(this.removeById(id), "删除评论失败");
        return Result.success();
    }


//    @Override
//    public Result<PageVO<CommentVO>> listComments(Long contentId, Integer page, Integer size) {
//        // 构建分页查询条件
//        Page<Comment> pageParam = new Page<>(page, size);
//        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<Comment>()
//                .eq(Comment::getContentId, contentId)
//                .eq(Comment::getStatus, CommentStatusEnum.APPROVED)
//                .orderByDesc(Comment::getCreateTime);
//
//        // 执行分页查询
//        IPage<Comment> commentPage = baseMapper.selectPage(pageParam, queryWrapper);
//
//        // 转换评论实体到VO
//        List<CommentVO> commentVOList = commentPage.getRecords().stream()
//                .map(comment -> BeanUtil.copyProperties(comment, CommentVO.class))
//                .collect(Collectors.toList());
//
//        // 封装分页结果
//        PageVO<CommentVO> pageVO = new PageVO<>();
//        pageVO.setTotal((int) commentPage.getTotal());
//        pageVO.setRecords(commentVOList);
//        pageVO.setPage(page);
//        pageVO.setPageSize(size);
//
//        return Result.success(pageVO);
//    }
}