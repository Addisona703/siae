package com.hngy.siae.content.controller;

import com.hngy.siae.common.result.Result;
import com.hngy.siae.content.dto.request.StatisticsDTO;
import com.hngy.siae.content.dto.response.StatisticsVO;
import com.hngy.siae.content.service.StatisticsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 内容统计管理控制器
 *
 * @author KEYKB
 * &#064;date: 2025/05/21
 */
@RestController
@RequestMapping("/statistics")
@Validated
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;


    @GetMapping("/{contentId}")
    public Result<StatisticsVO> getStatistics(@NotNull @PathVariable Long contentId) {
        return statisticsService.getStatistics(contentId);
    }


    @PutMapping("/{contentId}")
    public Result<StatisticsVO> updateStatistics(
            @NotNull @PathVariable Long contentId,
            @Valid @RequestBody StatisticsDTO statisticsDTO) {
        return statisticsService.updateStatistics(contentId, statisticsDTO);
    }
}