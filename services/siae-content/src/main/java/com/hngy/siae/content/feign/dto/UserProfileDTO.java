package com.hngy.siae.content.feign.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户信息DTO（用于Feign调用）
 *
 * @author KEYKB
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private Long userId;
    private String username;
    private String nickname;
    private String avatarFileId;
}
