package com.hngy.siae.user.dto.request;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户查询数据传输对象
 * <p>
 * 用于用户分页查询和条件查询的数据传输，包含查询条件字段。
 * 所有字段都是可选的，不需要校验注解，因为查询条件通常允许为空。
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
     * 学号
     */
    private String studentId;
    
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