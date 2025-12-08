package com.hngy.siae.api.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 用户简要信息VO（用于Feign调用）
 *
 * @author KEYKB
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileSimpleVO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    private Long userId;
    private String username;
    private String nickname;
    private String avatarFileId;
    private String avatarUrl;
}
