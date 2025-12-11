package com.hngy.siae.user.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hngy.siae.api.ai.dto.response.AwardInfo;
import com.hngy.siae.api.ai.dto.response.AwardStatistics;
import com.hngy.siae.api.ai.dto.response.MemberInfo;
import com.hngy.siae.api.ai.dto.response.MemberStatistics;
import com.hngy.siae.user.dto.response.statistics.AwardDistributionVO;
import com.hngy.siae.user.dto.response.statistics.DepartmentStatVO;
import com.hngy.siae.user.dto.response.statistics.GradeStatVO;
import com.hngy.siae.user.dto.response.statistics.PositionStatVO;
import com.hngy.siae.user.entity.Membership;
import com.hngy.siae.user.entity.User;
import com.hngy.siae.user.entity.UserAward;
import com.hngy.siae.user.entity.UserProfile;
import com.hngy.siae.user.mapper.MembershipMapper;
import com.hngy.siae.user.mapper.StatisticsMapper;
import com.hngy.siae.user.mapper.UserAwardMapper;
import com.hngy.siae.user.mapper.UserMapper;
import com.hngy.siae.user.mapper.UserProfileMapper;
import com.hngy.siae.user.service.AiDataService;
import com.hngy.siae.user.service.AwardLevelService;
import com.hngy.siae.user.service.AwardTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI数据服务实现类
 *
 * @author KEYKB
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiDataServiceImpl implements AiDataService {

    private final UserMapper userMapper;
    private final UserProfileMapper userProfileMapper;
    private final UserAwardMapper userAwardMapper;
    private final MembershipMapper membershipMapper;
    private final StatisticsMapper statisticsMapper;
    private final AwardTypeService awardTypeService;
    private final AwardLevelService awardLevelService;

    @Override
    public List<AwardInfo> getAwardsByMember(String memberName, String studentId) {
        log.info("AI查询获奖记录: memberName={}, studentId={}", memberName, studentId);
        
        // 1. 先查询符合条件的用户（通过关联查询user和user_profile）
        List<Long> userIds = new ArrayList<>();
        Map<Long, String> userNameMap = new HashMap<>();
        
        if (StrUtil.isNotBlank(studentId)) {
            // 按学号精确查询
            LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
            userQuery.eq(User::getIsDeleted, 0).eq(User::getStudentId, studentId);
            List<User> users = userMapper.selectList(userQuery);
            userIds = users.stream().map(User::getId).collect(Collectors.toList());
        }
        
        if (StrUtil.isNotBlank(memberName)) {
            // 按姓名模糊查询（从user_profile表）
            LambdaQueryWrapper<UserProfile> profileQuery = new LambdaQueryWrapper<>();
            profileQuery.like(UserProfile::getRealName, memberName);
            List<UserProfile> profiles = userProfileMapper.selectList(profileQuery);
            
            if (userIds.isEmpty()) {
                userIds = profiles.stream().map(UserProfile::getUserId).collect(Collectors.toList());
            } else {
                // 取交集
                Set<Long> profileUserIds = profiles.stream()
                        .map(UserProfile::getUserId).collect(Collectors.toSet());
                userIds = userIds.stream()
                        .filter(profileUserIds::contains).collect(Collectors.toList());
            }
            
            // 构建用户名映射
            for (UserProfile profile : profiles) {
                if (profile.getRealName() != null) {
                    userNameMap.put(profile.getUserId(), profile.getRealName());
                }
            }
        }
        
        if (userIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 2. 查询这些用户参与的获奖记录
        List<UserAward> awards = new ArrayList<>();
        for (Long userId : userIds) {
            LambdaQueryWrapper<UserAward> awardQuery = new LambdaQueryWrapper<>();
            awardQuery.eq(UserAward::getIsDeleted, 0)
                    .apply("JSON_CONTAINS(team_members, CAST({0} AS JSON))", userId);
            awards.addAll(userAwardMapper.selectList(awardQuery));
        }
        
        // 3. 去重并转换为AwardInfo
        Map<Long, UserAward> uniqueAwards = awards.stream()
                .collect(Collectors.toMap(UserAward::getId, a -> a, (a1, a2) -> a1));
        
        return uniqueAwards.values().stream()
                .map(award -> convertToAwardInfo(award, userNameMap))
                .collect(Collectors.toList());
    }

    @Override
    public List<MemberInfo> searchMembers(String name, String department, String position) {
        log.info("AI查询成员: name={}, department={}, position={}", name, department, position);
        
        // 使用Mapper查询成员信息（包含部门、职位等关联信息）
        List<MemberInfo> members = membershipMapper.searchMembersForAi(name, department, position);
        return members != null ? members : Collections.emptyList();
    }

    @Override
    public AwardStatistics getAwardStatistics(Long typeId, Long levelId, String startDate, String endDate) {
        log.info("AI查询获奖统计: typeId={}, levelId={}, startDate={}, endDate={}", 
                typeId, levelId, startDate, endDate);
        
        AwardStatistics stats = new AwardStatistics();
        
        // 解析日期
        LocalDate start = StrUtil.isNotBlank(startDate) ? 
                LocalDate.parse(startDate, DateTimeFormatter.ISO_LOCAL_DATE) : null;
        LocalDate end = StrUtil.isNotBlank(endDate) ? 
                LocalDate.parse(endDate, DateTimeFormatter.ISO_LOCAL_DATE) : null;
        
        // 构建查询条件
        LambdaQueryWrapper<UserAward> query = new LambdaQueryWrapper<>();
        query.eq(UserAward::getIsDeleted, 0);
        if (typeId != null) {
            query.eq(UserAward::getAwardTypeId, typeId);
        }
        if (levelId != null) {
            query.eq(UserAward::getAwardLevelId, levelId);
        }
        if (start != null) {
            query.ge(UserAward::getAwardedAt, start);
        }
        if (end != null) {
            query.le(UserAward::getAwardedAt, end);
        }
        
        // 查询总数
        Long total = userAwardMapper.selectCount(query);
        stats.setTotalAwards(total.intValue());
        
        // 按类型统计
        List<AwardDistributionVO> byType = statisticsMapper.getAwardsByType();
        stats.setByType(byType.stream()
                .collect(Collectors.toMap(AwardDistributionVO::getName, 
                        v -> v.getCount().intValue())));
        
        // 按等级统计
        List<AwardDistributionVO> byLevel = statisticsMapper.getAwardsByLevel();
        stats.setByLevel(byLevel.stream()
                .collect(Collectors.toMap(AwardDistributionVO::getName, 
                        v -> v.getCount().intValue())));
        
        // 按年份统计
        List<Map<String, Object>> yearData = statisticsMapper.getAwardCountByYear();
        Map<String, Integer> byYear = new HashMap<>();
        for (Map<String, Object> row : yearData) {
            String year = String.valueOf(row.get("key"));
            Integer count = ((Number) row.get("value")).intValue();
            byYear.put(year, count);
        }
        stats.setByYear(byYear);
        
        return stats;
    }

    @Override
    public MemberStatistics getMemberStatistics() {
        log.info("AI查询成员统计");
        
        MemberStatistics stats = new MemberStatistics();
        
        // 总成员数
        Long total = membershipMapper.selectCount(
                new LambdaQueryWrapper<Membership>().eq(Membership::getIsDeleted, 0));
        stats.setTotalMembers(total.intValue());
        
        // 按部门统计
        List<DepartmentStatVO> byDept = statisticsMapper.getDepartmentStats();
        stats.setByDepartment(byDept.stream()
                .collect(Collectors.toMap(DepartmentStatVO::getDepartmentName, 
                        v -> v.getTotalMembers().intValue())));
        
        // 按年级统计
        List<GradeStatVO> byGrade = statisticsMapper.getGradeStats();
        stats.setByGrade(byGrade.stream()
                .collect(Collectors.toMap(GradeStatVO::getGrade, 
                        v -> v.getCount().intValue())));
        
        // 按职位统计
        List<PositionStatVO> byPosition = statisticsMapper.getPositionStats();
        stats.setByPosition(byPosition.stream()
                .collect(Collectors.toMap(PositionStatVO::getPositionName, 
                        v -> v.getCount().intValue())));
        
        return stats;
    }

    /**
     * 将UserAward转换为AwardInfo
     */
    private AwardInfo convertToAwardInfo(UserAward award, Map<Long, String> userNameMap) {
        AwardInfo info = new AwardInfo();
        info.setId(award.getId());
        info.setAwardTitle(award.getAwardTitle());
        info.setAwardedBy(award.getAwardedBy());
        info.setAwardedAt(award.getAwardedAt());
        info.setDescription(award.getDescription());
        
        // 获取奖项类型和等级名称
        if (award.getAwardTypeId() != null) {
            var type = awardTypeService.getAwardTypeById(award.getAwardTypeId());
            info.setAwardType(type != null ? type.getName() : null);
        }
        if (award.getAwardLevelId() != null) {
            var level = awardLevelService.getAwardLevelById(award.getAwardLevelId());
            info.setAwardLevel(level != null ? level.getName() : null);
        }
        
        // 解析团队成员姓名
        if (StrUtil.isNotBlank(award.getTeamMembers())) {
            try {
                List<Long> memberIds = cn.hutool.json.JSONUtil.parseArray(award.getTeamMembers())
                        .stream()
                        .map(obj -> Long.valueOf(obj.toString()))
                        .collect(Collectors.toList());
                
                // 查询成员姓名
                List<String> memberNames = new ArrayList<>();
                for (Long memberId : memberIds) {
                    if (userNameMap.containsKey(memberId)) {
                        memberNames.add(userNameMap.get(memberId));
                    } else {
                        // 从user_profile表查询真实姓名
                        UserProfile profile = userProfileMapper.selectById(memberId);
                        if (profile != null && profile.getRealName() != null) {
                            memberNames.add(profile.getRealName());
                        }
                    }
                }
                info.setTeamMembers(memberNames);
            } catch (Exception e) {
                log.warn("解析团队成员失败: {}", e.getMessage());
                info.setTeamMembers(Collections.emptyList());
            }
        } else {
            info.setTeamMembers(Collections.emptyList());
        }
        
        return info;
    }
}
