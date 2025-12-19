package com.hngy.siae.content.constants;

/**
 * 通知业务类型常量
 * 用于标识不同类型的内容通知
 *
 * @author KEYKB
 */
public final class NotificationBusinessType {

    /**
     * 内容审核通过
     */
    public static final String CONTENT_APPROVED = "CONTENT_APPROVED";

    /**
     * 内容被点赞
     */
    public static final String CONTENT_LIKE = "CONTENT_LIKE";

    /**
     * 内容被收藏
     */
    public static final String CONTENT_FAVORITE = "CONTENT_FAVORITE";

    /**
     * 评论被点赞
     */
    public static final String COMMENT_LIKE = "COMMENT_LIKE";

    private NotificationBusinessType() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
