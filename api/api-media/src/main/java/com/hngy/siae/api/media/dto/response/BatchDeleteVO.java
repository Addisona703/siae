package com.hngy.siae.api.media.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量删除文件响应VO
 *
 * @author SIAE Team
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchDeleteVO {

    /**
     * 成功删除的文件ID列表
     */
    private List<String> successIds;

    /**
     * 删除失败的文件ID列表
     */
    private List<String> failedIds;

    /**
     * 成功数量
     */
    private Integer successCount;

    /**
     * 失败数量
     */
    private Integer failedCount;
}
