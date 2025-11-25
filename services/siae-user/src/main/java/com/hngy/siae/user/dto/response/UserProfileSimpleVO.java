package com.hngy.siae.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户简要信息VO（用于Feign调用）
 *
 * @author KEYKB
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileSimpleVO {
    private Long userId;
    private String username;
    private String nickname;
    private String avatarFileId;
}
