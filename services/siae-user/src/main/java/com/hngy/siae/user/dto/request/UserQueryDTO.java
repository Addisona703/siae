package com.hngy.siae.user.dto.request;

import com.hngy.siae.common.validation.QueryGroup;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户查询数据传输对象
 * 
 * @author KEYKB
 */
@Data
public class UserQueryDTO implements Serializable {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 状态：0禁用，1启用
     */
    private Integer status;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 创建时间范围-开始
     */
    private LocalDateTime createTimeStart;
    
    /**
     * 创建时间范围-结束
     */
    private LocalDateTime createTimeEnd;
    
    /**
     * 最后登录时间范围-开始
     */
    private LocalDateTime lastLoginTimeStart;
    
    /**
     * 最后登录时间范围-结束
     */
    private LocalDateTime lastLoginTimeEnd;
    
    /**
     * 是否包含已删除的用户
     */
    private Boolean includeDeleted = false;
} 