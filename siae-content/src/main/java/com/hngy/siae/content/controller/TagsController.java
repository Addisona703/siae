package com.hngy.siae.content.controller;

import com.hngy.siae.common.dto.request.PageDTO;
import com.hngy.siae.common.dto.response.PageVO;
import com.hngy.siae.common.result.Result;
import com.hngy.siae.common.validation.CreateGroup;
import com.hngy.siae.content.dto.request.TagDTO;
import com.hngy.siae.content.dto.response.TagVO;
import com.hngy.siae.content.service.TagsService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


/**
 * 标签控制器
 *
 * @author KEYKB
 * &#064;date: 2025/05/20
 */
@RestController
@RequestMapping("/tags")
@Validated
@RequiredArgsConstructor
public class  TagsController {

    private final TagsService tagsService;


    @PostMapping
    public Result<TagVO> createTag(@Validated(CreateGroup.class) @RequestBody TagDTO tagDTO) {
        return tagsService.createTag(tagDTO);
    }


    @PutMapping
    public Result<TagVO> updateTag(@RequestParam(required = false) Long id,
                                   @Valid @RequestBody TagDTO tagDTO) {
        return tagsService.updateTag(id, tagDTO);
    }


    @DeleteMapping
    public Result<Void> deleteTag(@NotNull @RequestParam Long id) {
        return tagsService.deleteTag(id);
    }


    @GetMapping
    public Result<PageVO<TagVO>> listTags(PageDTO pageDTO) {
        return tagsService.listTags(pageDTO);
    }
}