package com.hngy.siae.user.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.time.LocalDate;
import java.util.List;

/**
 * 用户详细信息视图对象
 * 
 * @author KEYKB
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserDetailVO extends UserVO {
    
    @Serial
    private static final long serialVersionUID = 1L;
    
    /**
     * 个人简介
     */
    private String bio;
    
    /**
     * QQ号
     */
    private String qq;
    
    /**
     * 微信号
     */
    private String wechat;
    
    /**
     * 出生日期
     */
    private LocalDate birthday;
    
    /**
     * 所属班级信息
     */
    private List<ClassInfoVO> classes;
    
    /**
     * 会员信息
     */
    private MemberCandidateVO memberInfo;
    
    /**
     * 获奖信息
     */
    private List<UserAwardVO> awards;
} 