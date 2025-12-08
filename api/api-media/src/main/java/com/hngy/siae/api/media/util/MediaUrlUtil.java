package com.hngy.siae.api.media.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 媒体URL工具类
 * 处理内容中 media://{fileId} 格式的转换
 *
 * @author KEYKB
 * @date 2025/11/27
 */
public final class MediaUrlUtil {

    /**
     * media:// 协议正则表达式
     * 匹配格式: media://{fileId}
     */
    private static final Pattern MEDIA_PROTOCOL_PATTERN = Pattern.compile("media://([a-zA-Z0-9_-]+)");

    private MediaUrlUtil() {
    }

    /**
     * 从内容中提取所有 media:// 的 fileId
     *
     * @param content 内容文本
     * @return fileId 列表（去重）
     */
    public static List<String> extractFileIds(String content) {
        List<String> fileIds = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return fileIds;
        }

        Matcher matcher = MEDIA_PROTOCOL_PATTERN.matcher(content);
        while (matcher.find()) {
            String fileId = matcher.group(1);
            if (!fileIds.contains(fileId)) {
                fileIds.add(fileId);
            }
        }
        return fileIds;
    }

    /**
     * 检查内容中是否包含 media:// 协议
     *
     * @param content 内容文本
     * @return 是否包含
     */
    public static boolean hasMediaProtocol(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }
        return MEDIA_PROTOCOL_PATTERN.matcher(content).find();
    }

    /**
     * 将内容中的 media://{fileId} 替换为实际URL
     *
     * @param content 内容文本
     * @param urlMap  fileId 到 URL 的映射
     * @return 替换后的内容
     */
    public static String replaceWithUrls(String content, Map<String, String> urlMap) {
        if (content == null || content.isEmpty() || urlMap == null || urlMap.isEmpty()) {
            return content;
        }

        StringBuffer result = new StringBuffer();
        Matcher matcher = MEDIA_PROTOCOL_PATTERN.matcher(content);

        while (matcher.find()) {
            String fileId = matcher.group(1);
            String url = urlMap.get(fileId);
            // 如果找到对应URL则替换，否则保留原样
            String replacement = (url != null) ? Matcher.quoteReplacement(url) : matcher.group(0);
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 将内容中的 media://{fileId} 替换为实际URL（函数式接口版本）
     * 适用于需要动态获取URL的场景
     *
     * @param content     内容文本
     * @param urlResolver URL解析函数，输入fileId，返回URL
     * @return 替换后的内容
     */
    public static String replaceWithUrls(String content, Function<String, String> urlResolver) {
        if (content == null || content.isEmpty() || urlResolver == null) {
            return content;
        }

        StringBuffer result = new StringBuffer();
        Matcher matcher = MEDIA_PROTOCOL_PATTERN.matcher(content);

        while (matcher.find()) {
            String fileId = matcher.group(1);
            String url = urlResolver.apply(fileId);
            String replacement = (url != null) ? Matcher.quoteReplacement(url) : matcher.group(0);
            matcher.appendReplacement(result, replacement);
        }
        matcher.appendTail(result);

        return result.toString();
    }
}
