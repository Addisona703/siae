package com.hngy.siae.content.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.content.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

/**
* @author 31833
* @description 针对表【content_comment(内容评论表)】的数据库操作Mapper
* @createDate 2025-05-15 16:48:34
* @Entity com.hngy.siae.com.hngy.siae.content.model.entity.Comment
*/
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

}




