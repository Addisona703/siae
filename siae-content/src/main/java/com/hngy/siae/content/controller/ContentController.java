package com.hngy.siae.content.controller;

import com.hngy.siae.common.result.Result;
import com.hngy.siae.common.validation.CreateGroup;
import com.hngy.siae.common.validation.UpdateGroup;
import com.hngy.siae.content.dto.request.content.ContentDTO;
import com.hngy.siae.content.dto.request.content.ContentHotPageDTO;
import com.hngy.siae.content.dto.request.content.ContentPageDTO;
import com.hngy.siae.content.dto.response.ContentDetailVO;
import com.hngy.siae.content.dto.response.ContentVO;
import com.hngy.siae.content.dto.response.HotContentVO;
import com.hngy.siae.content.dto.response.detail.EmptyDetailVO;
import com.hngy.siae.content.facade.ContentFacade;
import com.hngy.siae.content.service.ContentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * 内容控制器
 *
 * @author KEYKB
 * &#064;date: 2025/05/18
 */
@RestController
@RequestMapping("/")
@Validated
@RequiredArgsConstructor
public class ContentController {

    private final ContentFacade contentFacade;
    private final ContentService contentService;


    @PostMapping("/")
    public Result<ContentVO<ContentDetailVO>> publishContent(
            @RequestBody @Validated(CreateGroup.class) ContentDTO contentDTO) {
        return contentFacade.publishContent(contentDTO);
    }


    @PutMapping("/")
    public Result<ContentVO<ContentDetailVO>> editContent(
            @RequestBody @Validated(UpdateGroup.class) ContentDTO contentDTO) {
        return contentFacade.editContent(contentDTO);
    }


    @DeleteMapping("/")
    public Result<Void> deleteContent(@NotNull @RequestParam Integer id,
                                      @NotNull @RequestParam Integer isTrash) {
        return contentService.deleteContent(id, isTrash);
    }


    @GetMapping("/")
    public Result<PageVO<ContentVO<EmptyDetailVO>>> queryContentList(
            @RequestBody @Valid ContentPageDTO contentPageDTO) {
        return contentService.getContentPage(contentPageDTO);
    }


    @GetMapping("/query/{contentId}")
    public Result<ContentVO<ContentDetailVO>> queryContent(
            @NotNull @PathVariable Long contentId) {
        return contentFacade.queryContent(contentId);
    }


    @GetMapping("/hot")
    public Result<List<HotContentVO>> queryHotContent(
            @Valid ContentHotPageDTO contentHotPageDTO) {
        return contentFacade.queryHotContent(contentHotPageDTO);
    }
}