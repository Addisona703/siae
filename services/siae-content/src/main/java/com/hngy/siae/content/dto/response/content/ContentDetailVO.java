package com.hngy.siae.content.dto.response.content;

/**
 * 内容详情 VO 接口
 * 所有内容类型的详情 VO 都需要实现此接口
 *
 * @author KEYKB
 */
public interface ContentDetailVO {

    /**
     * 获取内容正文
     * 文章、笔记、问题等文字类内容需要实现此方法
     *
     * @return 内容正文，如果不支持则返回 null
     */
    default String getContent() {
        return null;
    }

    /**
     * 设置内容正文
     * 文章、笔记、问题等文字类内容需要实现此方法
     *
     * @param content 内容正文
     */
    default void setContent(String content) {
        // 默认空实现，子类按需覆盖
    }
}
