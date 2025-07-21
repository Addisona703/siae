package com.hngy.siae.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 专业字典表实体类
 *
 * @author KEYKB
 */
@Data
@TableName("major")
public class Major implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 所属学院ID
     */
    private Long collegeId;

    /**
     * 专业名称
     */
    private String name;

    /**
     * 专业编码
     */
    private String code;

    /**
     * 专业简称
     */
    private String abbr;
} 