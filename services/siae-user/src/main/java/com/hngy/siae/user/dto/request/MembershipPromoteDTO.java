package com.hngy.siae.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 候选成员转正 DTO
 *
 * @author KEYKB
 */
@Data
public class MembershipPromoteDTO {

    /**
     * 成员ID
     */
    private Long id;

    /**
     * 加入日期（成为正式成员的日期）
     */
    @NotNull(message = "加入日期不能为空")
    private LocalDate joinDate;

    /**
     * 成员大头照文件ID（可选，转正时更新）
     */
    private String headshotFileId;
}
