package com.hngy.siae.api.user.dto.response;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户信息视图对象
 * 
 * @author KEYKB
 */
@Data
public class UserVO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    private Long id;

    /**
     * 登录名/用户名
     */
    private String username;

    /**
     * 学号
     */
    private String studentId;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 头像文件ID
     */
    private String avatarFileId;

    /**
     * 头像访问URL（从Media服务获取）
     */
    private String avatarUrl;

    /**
     * 状态：0禁用，1启用
     */
    private Integer status;

    /**
     * 是否删除
     */
    private Integer isDeleted;

    /**
     * 是否会员
     */
    private Boolean isMember;

    /**
     * 创建时间
     */
    private LocalDateTime createAt;

    /**
     * 更新时间
     */
    private LocalDateTime updateAt;
}
