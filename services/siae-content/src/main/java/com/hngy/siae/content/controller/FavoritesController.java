package com.hngy.siae.content.controller;

import com.hngy.siae.core.dto.PageDTO;
import com.hngy.siae.core.dto.PageVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.content.dto.request.favorite.FavoriteFolderCreateDTO;
import com.hngy.siae.content.dto.request.favorite.FavoriteFolderUpdateDTO;
import com.hngy.siae.content.dto.request.favorite.FavoriteItemAddDTO;
import com.hngy.siae.content.dto.request.favorite.FavoriteItemUpdateDTO;
import com.hngy.siae.content.dto.response.favorite.FavoriteFolderVO;
import com.hngy.siae.content.dto.response.favorite.FavoriteItemVO;
import com.hngy.siae.content.service.FavoriteService;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.hngy.siae.content.permissions.ContentPermissions.*;

/**
 * 收藏管理控制器
 *
 * @author KEYKB
 */
@Tag(name = "收藏管理", description = "用户收藏夹和收藏内容的管理操作")
@RestController
@RequestMapping("/favorites")
@Validated
@RequiredArgsConstructor
public class FavoritesController {

    private final FavoriteService favoriteService;

    // ==================== 收藏夹管理 ====================

    @Operation(summary = "创建收藏夹", description = "创建一个新的收藏夹")
    @PostMapping("/folders")
    @SiaeAuthorize("hasAuthority('" + CONTENT_FAVORITE_MANAGE + "')")
    public Result<FavoriteFolderVO> createFolder(
            @Parameter(description = "创建收藏夹请求数据", required = true)
            @Valid @RequestBody FavoriteFolderCreateDTO createDTO) {
        return Result.success(favoriteService.createFolder(createDTO));
    }


    @Operation(summary = "更新收藏夹", description = "更新收藏夹的名称、描述等信息")
    @PutMapping("/folders")
    @SiaeAuthorize("hasAuthority('" + CONTENT_FAVORITE_MANAGE + "')")
    public Result<FavoriteFolderVO> updateFolder(
            @Parameter(description = "更新收藏夹请求数据", required = true)
            @Valid @RequestBody FavoriteFolderUpdateDTO updateDTO) {
        return Result.success(favoriteService.updateFolder(updateDTO));
    }


    @Operation(summary = "删除收藏夹", description = "删除指定的收藏夹，默认收藏夹不可删除")
    @DeleteMapping("/folders/{folderId}")
    @SiaeAuthorize("hasAuthority('" + CONTENT_FAVORITE_MANAGE + "')")
    public Result<Void> deleteFolder(
            @Parameter(description = "收藏夹ID", required = true, example = "1")
            @NotNull @PathVariable Long folderId) {
        favoriteService.deleteFolder(folderId);
        return Result.success();
    }


    @Operation(summary = "查询收藏夹列表", description = "获取指定用户的所有收藏夹")
    @GetMapping("/folders/user/{userId}")
    @SiaeAuthorize("hasAuthority('" + CONTENT_FAVORITE_VIEW + "')")
    public Result<List<FavoriteFolderVO>> getUserFolders(
            @Parameter(description = "用户ID", required = true, example = "1")
            @NotNull @PathVariable Long userId) {
        return Result.success(favoriteService.getUserFolders(userId));
    }


    @Operation(summary = "查询收藏夹详情", description = "获取指定收藏夹的详细信息")
    @GetMapping("/folders/{folderId}")
    @SiaeAuthorize("hasAuthority('" + CONTENT_FAVORITE_VIEW + "')")
    public Result<FavoriteFolderVO> getFolderDetail(
            @Parameter(description = "收藏夹ID", required = true, example = "1")
            @NotNull @PathVariable Long folderId) {
        return Result.success(favoriteService.getFolderDetail(folderId));
    }

    // ==================== 收藏内容管理 ====================

    @Operation(summary = "添加收藏", description = "将内容添加到收藏夹")
    @PostMapping("/items")
    @SiaeAuthorize("hasAuthority('" + CONTENT_FAVORITE_ADD + "')")
    public Result<FavoriteItemVO> addFavorite(
            @Parameter(description = "添加收藏请求数据", required = true)
            @Valid @RequestBody FavoriteItemAddDTO addDTO) {
        return Result.success(favoriteService.addFavorite(addDTO));
    }


    @Operation(summary = "更新收藏", description = "更新收藏的备注、移动到其他收藏夹等")
    @PutMapping("/items")
    @SiaeAuthorize("hasAuthority('" + CONTENT_FAVORITE_MANAGE + "')")
    public Result<FavoriteItemVO> updateFavorite(
            @Parameter(description = "更新收藏请求数据", required = true)
            @Valid @RequestBody FavoriteItemUpdateDTO updateDTO) {
        return Result.success(favoriteService.updateFavorite(updateDTO));
    }


    @Operation(summary = "取消收藏", description = "从收藏夹中移除指定内容")
    @DeleteMapping("/items/{itemId}")
    @SiaeAuthorize("hasAuthority('" + CONTENT_FAVORITE_REMOVE + "')")
    public Result<Void> removeFavorite(
            @Parameter(description = "收藏ID", required = true, example = "1")
            @NotNull @PathVariable Long itemId) {
        favoriteService.removeFavorite(itemId);
        return Result.success();
    }


    @Operation(summary = "查询收藏内容列表", description = "分页查询指定收藏夹中的收藏内容")
    @PostMapping("/items/folder/{folderId}")
    @SiaeAuthorize("hasAuthority('" + CONTENT_FAVORITE_VIEW + "')")
    public Result<PageVO<FavoriteItemVO>> getFolderItems(
            @Parameter(description = "收藏夹ID", required = true, example = "1")
            @NotNull @PathVariable Long folderId,
            @Parameter(description = "分页参数", required = true)
            @Valid @RequestBody PageDTO<Void> pageDTO) {
        return Result.success(favoriteService.getFolderItems(folderId, pageDTO));
    }


    @Operation(summary = "查询用户所有收藏", description = "分页查询用户在所有收藏夹中的收藏内容")
    @PostMapping("/items/user/{userId}")
    @SiaeAuthorize("hasAuthority('" + CONTENT_FAVORITE_VIEW + "')")
    public Result<PageVO<FavoriteItemVO>> getUserFavorites(
            @Parameter(description = "用户ID", required = true, example = "1")
            @NotNull @PathVariable Long userId,
            @Parameter(description = "分页参数", required = true)
            @Valid @RequestBody PageDTO<Void> pageDTO) {
        return Result.success(favoriteService.getUserFavorites(userId, pageDTO));
    }


    @Operation(summary = "检查是否已收藏", description = "检查用户是否已收藏指定内容，如果已收藏则返回收藏ID，否则返回null")
    @GetMapping("/items/check")
    @SiaeAuthorize("hasAuthority('" + CONTENT_FAVORITE_VIEW + "')")
    public Result<Long> checkFavorite(
            @Parameter(description = "用户ID", required = true, example = "1")
            @NotNull @RequestParam Long userId,
            @Parameter(description = "内容ID", required = true, example = "1")
            @NotNull @RequestParam Long contentId) {
        return Result.success(favoriteService.checkFavorite(userId, contentId));
    }
}
