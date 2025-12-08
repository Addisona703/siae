package com.hngy.siae.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.siae.content.dto.response.comment.CommentVO;
import com.hngy.siae.content.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 评论Mapper
 *
 * @author 31833
 * @description 针对表【comment(内容评论表)】的数据库操作Mapper
 * @createDate 2025-05-15 16:48:34
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 分页查询根评论（包含子评论数量）
     *
     * @param page      分页参数
     * @param contentId 内容ID
     * @param sortBy    排序字段（createTime/likeCount）
     * @param sortOrder 排序方向（asc/desc）
     * @return 根评论分页结果
     */
    IPage<CommentVO> selectRootCommentsPage(
            Page<CommentVO> page,
            @Param("contentId") Long contentId,
            @Param("sortBy") String sortBy,
            @Param("sortOrder") String sortOrder
    );

    /**
     * 查询指定根评论下的前N条子评论
     *
     * @param contentId 内容ID
     * @param rootId    根评论ID
     * @param limit     限制数量
     * @return 子评论列表
     */
    List<CommentVO> selectTopChildComments(
            @Param("contentId") Long contentId,
            @Param("rootId") Long rootId,
            @Param("limit") int limit
    );

    /**
     * 分页查询指定根评论下的所有子评论
     *
     * @param page      分页参数
     * @param contentId 内容ID
     * @param rootId    根评论ID
     * @return 子评论分页结果
     */
    IPage<CommentVO> selectChildCommentsPage(
            Page<CommentVO> page,
            @Param("contentId") Long contentId,
            @Param("rootId") Long rootId
    );
}




