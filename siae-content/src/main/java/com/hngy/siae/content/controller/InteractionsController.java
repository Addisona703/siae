package com.hngy.siae.content.controller;

import com.hngy.siae.common.result.Result;
import com.hngy.siae.content.dto.request.ActionDTO;
import com.hngy.siae.content.service.InteractionsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 内容互动管理控制器
 *
 * @author KEYKB
 * &#064;date: 2025/05/21
 */
@RestController
@RequestMapping("/interactions")
@Validated
@RequiredArgsConstructor
public class InteractionsController {

    private final InteractionsService interactionsService;


    @PostMapping("/action")
    public Result<Void> recordAction(@Valid @RequestBody ActionDTO actionDTO) {
        return interactionsService.recordAction(actionDTO);
    }


    @DeleteMapping("/action")
    public Result<Void> cancelAction(@Valid @RequestBody ActionDTO actionDTO) {
        return interactionsService.cancelAction(actionDTO);
    }
}