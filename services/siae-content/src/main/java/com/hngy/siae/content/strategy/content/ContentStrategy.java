package com.hngy.siae.content.strategy.content;

import com.hngy.siae.content.dto.request.content.ContentDetailDTO;
import com.hngy.siae.content.dto.response.content.ContentDetailVO;
import com.hngy.siae.content.enums.ContentTypeEnum;

import java.util.List;

/**
 * 内容策略接口
 * 定义不同内容类型的处理逻辑
 * 使用组合方式，支持通过 supports() 方法或 @StrategyType 注解两种方式注册策略
 *
 * @author KEYKB
 * &#064;date: 2025/05/19
 */
public interface ContentStrategy {

    /**
     * 判断是否支持指定的内容类型
     * 默认返回 false，子类可覆盖此方法声明支持的类型
     * 也可以使用 @StrategyType 注解方式注册
     *
     * @param type 内容类型
     * @return 是否支持该类型
     */
    default boolean supports(ContentTypeEnum type) {
        return false;
    }

    ContentDetailVO insert(Long contentId, ContentDetailDTO dto);

    ContentDetailVO update(Long contentId, ContentDetailDTO dto);

    void delete(Long contentId);

    boolean batchDelete(List<Long> contentIds);

    ContentDetailVO getDetail(Long contentId);

    /**
     * 批量获取内容关联的媒体文件ID
     * 用于删除内容时同步删除关联的媒体文件
     * 
     * @param contentIds 内容ID列表
     * @return 媒体文件ID列表（可能为空）
     */
    default List<String> getMediaFileIds(List<Long> contentIds) {
        return List.of();
    }
}
