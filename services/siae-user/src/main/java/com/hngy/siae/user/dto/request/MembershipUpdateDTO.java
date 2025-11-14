package com.hngy.siae.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


/**
 * 更新成员信息 DTO
 *
 * @author KEYKB
 */
@Data
public class MembershipUpdateDTO {

    /**
     * 成员ID
     */
    @NotNull(message = "成员ID不能为空")
    private Long id;

    /**
     * 成员大头照文件ID
     */
    private String headshotFileId;
}
