package com.hngy.siae.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 奖项等级字典表实体类
 * 如：省一等奖、国三等奖
 *
 * @author KEYKB
 */
@Data
@TableName("award_level")
public class AwardLevel implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 奖项等级名称
     */
    private String name;
    
    /**
     * 排序ID，值越小排序越靠前
     */
    private Integer orderId;
} 