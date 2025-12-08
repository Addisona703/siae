package com.hngy.siae.user.service.impl;

import com.hngy.siae.api.media.client.MediaFeignClient;
import com.hngy.siae.api.media.dto.request.BatchUrlDTO;
import com.hngy.siae.api.media.dto.response.BatchUrlVO;
import com.hngy.siae.user.dto.response.statistics.*;
import com.hngy.siae.user.mapper.StatisticsMapper;
import com.hngy.siae.user.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统计服务实现
 *
 * @author SIAE Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsServiceImpl implements StatisticsService {

    private final StatisticsMapper statisticsMapper;
    private final MediaFeignClient mediaFeignClient;

    @Override
    public MemberOverviewVO getMemberOverview() {
        log.info("Getting member overview statistics");
        
        MemberOverviewVO vo = new MemberOverviewVO();
        vo.setTotalUsers(statisticsMapper.countTotalUsers());
        vo.setEnabledUsers(statisticsMapper.countEnabledUsers());
        vo.setDisabledUsers(statisticsMapper.countDisabledUsers());
        vo.setFormalMembers(statisticsMapper.countFormalMembers());
        vo.setCandidateMembers(statisticsMapper.countCandidateMembers());
        vo.setNewUsersThisMonth(statisticsMapper.countNewUsersThisMonth());
        vo.setNewUsersThisYear(statisticsMapper.countNewUsersThisYear());
        
        return vo;
    }

    @Override
    public List<DepartmentStatVO> getDepartmentStats() {
        log.info("Getting department statistics");
        return statisticsMapper.getDepartmentStats();
    }

    @Override
    public AwardOverviewVO getAwardOverview() {
        log.info("Getting award overview statistics");
        
        AwardOverviewVO vo = new AwardOverviewVO();
        vo.setTotalAwards(statisticsMapper.countTotalAwards());
        vo.setThisYearAwards(statisticsMapper.countThisYearAwards());
        vo.setThisMonthAwards(statisticsMapper.countThisMonthAwards());
        vo.setTotalAwardedUsers(statisticsMapper.countTotalAwardedUsers());
        vo.setTeamAwards(statisticsMapper.countTeamAwards());
        vo.setIndividualAwards(statisticsMapper.countIndividualAwards());
        
        return vo;
    }

    @Override
    public List<AwardDistributionVO> getAwardsByLevel() {
        log.info("Getting awards by level");
        return statisticsMapper.getAwardsByLevel();
    }

    @Override
    public List<AwardDistributionVO> getAwardsByType() {
        log.info("Getting awards by type");
        return statisticsMapper.getAwardsByType();
    }

    @Override
    public List<TrendVO> getAwardTrend(String period, Integer limit) {
        log.info("Getting award trend: period={}, limit={}", period, limit);
        
        // 默认值
        if (limit == null || limit <= 0) {
            limit = 12;
        }
        
        if ("year".equalsIgnoreCase(period)) {
            return statisticsMapper.getAwardTrendByYear(limit);
        } else {
            // 默认按月
            return statisticsMapper.getAwardTrendByMonth(limit);
        }
    }

    @Override
    public List<AwardRankVO> getAwardRanking(Integer limit) {
        log.info("Getting award ranking: limit={}", limit);
        
        // 默认TOP 10，最大100
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        if (limit > 100) {
            limit = 100;
        }
        
        List<AwardRankVO> ranking = statisticsMapper.getAwardRanking(limit);
        
        // 获取所有需要转换的文件ID
        List<String> fileIds = ranking.stream()
                .map(AwardRankVO::getAvatarUrl)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.toList());
        
        if (!fileIds.isEmpty()) {
            try {
                // 批量获取文件URL
                BatchUrlDTO request = new BatchUrlDTO();
                request.setFileIds(fileIds);
                request.setExpirySeconds(86400); // 24小时过期
                
                BatchUrlVO result = mediaFeignClient.batchGetFileUrls(request);
                
                if (result != null && result.getUrls() != null) {
                    Map<String, String> urlMap = result.getUrls();
                    
                    // 将文件ID替换为实际的URL
                    ranking.forEach(rank -> {
                        String fileId = rank.getAvatarUrl();
                        if (StringUtils.hasText(fileId) && urlMap.containsKey(fileId)) {
                            rank.setAvatarUrl(urlMap.get(fileId));
                        }
                    });
                    
                    log.info("Successfully converted {} file IDs to URLs", urlMap.size());
                } else {
                    log.warn("Failed to get file URLs from media service: empty result");
                }
            } catch (Exception e) {
                log.error("Error calling media service to get file URLs", e);
                // 如果调用失败，保持原始的文件ID
            }
        }
        
        return ranking;
    }

    @Override
    public List<GenderStatVO> getGenderStats() {
        log.info("Getting gender statistics");
        return statisticsMapper.getGenderStats();
    }

    @Override
    public List<GradeStatVO> getGradeStats() {
        log.info("Getting grade statistics");
        return statisticsMapper.getGradeStats();
    }

    @Override
    public List<MajorStatVO> getMajorStats() {
        log.info("Getting major statistics");
        return statisticsMapper.getMajorStats();
    }

    @Override
    public List<PositionStatVO> getPositionStats() {
        log.info("Getting position statistics");
        return statisticsMapper.getPositionStats();
    }

    @Override
    public List<MembershipTrendVO> getMembershipTrend(String period, Integer limit) {
        log.info("Getting membership trend: period={}, limit={}", period, limit);
        
        // 默认值
        if (limit == null || limit <= 0) {
            limit = 12;
        }
        
        if ("year".equalsIgnoreCase(period)) {
            return statisticsMapper.getMembershipTrendByYear(limit);
        } else {
            // 默认按月
            return statisticsMapper.getMembershipTrendByMonth(limit);
        }
    }

    @Override
    public List<AwardTrendDetailVO> getAwardTrendDetail(String period, String startPeriod, String endPeriod) {
        log.info("Getting award trend detail: period={}, startPeriod={}, endPeriod={}", period, startPeriod, endPeriod);
        
        // 查询明细数据
        List<AwardLevelTrendItemVO> items;
        if (startPeriod == null || startPeriod.trim().isEmpty()) {
            // 如果没有指定时间范围，默认最近12个周期
            if ("year".equalsIgnoreCase(period)) {
                items = statisticsMapper.getAwardLevelTrendByYear(null, null, 12);
            } else {
                items = statisticsMapper.getAwardLevelTrendByMonth(null, null, 12);
            }
        } else {
            // 使用指定的时间范围
            if ("year".equalsIgnoreCase(period)) {
                items = statisticsMapper.getAwardLevelTrendByYear(startPeriod, endPeriod, null);
            } else {
                items = statisticsMapper.getAwardLevelTrendByMonth(startPeriod, endPeriod, null);
            }
        }
        
        // 将明细数据按period分组，组装成最终结果
        Map<String, AwardTrendDetailVO> resultMap = new java.util.LinkedHashMap<>();
        
        for (AwardLevelTrendItemVO item : items) {
            String periodKey = item.getPeriod();
            AwardTrendDetailVO vo = resultMap.computeIfAbsent(periodKey, k -> {
                AwardTrendDetailVO newVo = new AwardTrendDetailVO();
                newVo.setPeriod(periodKey);
                newVo.setTotalCount(0L);
                newVo.setLevelCounts(new java.util.HashMap<>());
                return newVo;
            });
            
            // 累加总数
            vo.setTotalCount(vo.getTotalCount() + item.getCount());
            // 记录各等级数量
            vo.getLevelCounts().put(item.getLevelName(), item.getCount());
        }
        
        return new java.util.ArrayList<>(resultMap.values());
    }
}
