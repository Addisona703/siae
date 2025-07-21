package com.hngy.siae.auth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志实体类
 * 
 * @author KEYKB
 */
@Data
@TableName("login_log")
public class LoginLog {
    
    /**
     * 访问ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 登录账号
     */
    private String username;
    
    /**
     * 登录IP
     */
    private String loginIp;
    
    /**
     * 登录地点
     */
    private String loginLocation;
    
    /**
     * 浏览器类型
     */
    private String browser;
    
    /**
     * 操作系统
     */
    private String os;
    
    /**
     * 登录状态（0失败 1成功）
     */
    private Integer status;
    
    /**
     * 提示消息
     */
    private String msg;
    
    /**
     * 登录时间
     */
    private LocalDateTime loginTime;
} 