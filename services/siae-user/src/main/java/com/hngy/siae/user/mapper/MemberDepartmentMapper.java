package com.hngy.siae.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.user.entity.MemberDepartment;
import org.apache.ibatis.annotations.Mapper;

/**
 * 成员部门关联 Mapper
 * 已更新为使用 membershipId 而非 userId
 *
 * @author KEYKB
 */
@Mapper
public interface MemberDepartmentMapper extends BaseMapper<MemberDepartment> {
}
