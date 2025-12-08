package com.hngy.siae.user.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


/**
 * 创建成员 DTO
 * 用于申请成为候选成员
 *
 * @author KEYKB
 */
@Data
public class MembershipCreateDTO {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Long userId;

    /**
     * 成员大头照文件ID
     */
    private String headshotFileId;

    /**
     * 部门ID
     */
    @NotNull(message = "部门ID不能为空")
    private Long departmentId;

    /**
     * 职位ID
     */
    @NotNull(message = "职位ID不能为空")
    private Long positionId;
}
