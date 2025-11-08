package com.hngy.siae.media.constant;

import com.hngy.siae.core.asserts.AssertUtils;

/**
 * 媒体服务权限常量
 *
 * @author SIAE Team
 */
public class MediaPermissions {

    /**
     * 上传文件权限
     */
    public static final String MEDIA_UPLOAD = "media:upload";

    /**
     * 下载文件权限
     */
    public static final String MEDIA_DOWNLOAD = "media:download";

    /**
     * 文件管理权限
     */
    public static final String MEDIA_FILE_MANAGE = "media:file:manage";

    /**
     * 文件查询权限
     */
    public static final String MEDIA_FILE_QUERY = "media:file:query";

    /**
     * 文件删除权限
     */
    public static final String MEDIA_FILE_DELETE = "media:file:delete";

    /**
     * 配额管理权限
     */
    public static final String MEDIA_QUOTA_MANAGE = "media:quota:manage";

    /**
     * 配额查询权限
     */
    public static final String MEDIA_QUOTA_QUERY = "media:quota:query";

    /**
     * 审计日志查询权限
     */
    public static final String MEDIA_AUDIT_QUERY = "media:audit:query";

    /**
     * 审计日志导出权限
     */
    public static final String MEDIA_AUDIT_EXPORT = "media:audit:export";

    /**
     * 流式播放权限
     */
    public static final String MEDIA_STREAMING = "media:streaming";

    private MediaPermissions() {
        throw new UnsupportedOperationException("工具类不允许实例化");
    }

}
