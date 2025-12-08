package com.hngy.siae.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hngy.siae.media.domain.entity.FileEntity;
import com.hngy.siae.media.domain.enums.AccessPolicy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 文件数据访问接口
 * 
 * 继承 MyBatis-Plus 的 BaseMapper，提供基础的 CRUD 操作
 * 
 * 自定义查询方法：
 * - 按租户查询文件列表（支持分页）
 * - 按业务标签查询文件列表
 * - 按访问策略查询文件列表
 * - 按租户和访问策略组合查询
 * 
 * 软删除支持：
 * - MyBatis-Plus 的 @TableLogic 注解会自动过滤 deleted_at IS NOT NULL 的记录
 * - 所有查询方法都会自动应用软删除过滤
 * 
 * Requirements: 5.1, 5.3, 5.4, 10.7
 *
 * @author SIAE Team
 */
@Mapper
public interface FileMapper extends BaseMapper<FileEntity> {

    /**
     * 按租户查询文件列表（支持分页）
     * 
     * 自动过滤已删除的文件（deleted_at IS NULL）
     * 按创建时间倒序排列
     * 
     * @param page 分页对象
     * @param tenantId 租户ID
     * @return 分页结果
     * 
     * Requirements: 5.3, 10.7
     */
    @Select("SELECT * FROM files " +
            "WHERE tenant_id = #{tenantId} AND deleted_at IS NULL " +
            "ORDER BY created_at DESC")
    IPage<FileEntity> selectByTenantId(Page<FileEntity> page, @Param("tenantId") String tenantId);

    /**
     * 按业务标签查询文件列表（支持分页）
     * 
     * 使用 JSON_CONTAINS 函数查询 biz_tags 字段
     * 自动过滤已删除的文件
     * 
     * @param page 分页对象
     * @param tenantId 租户ID
     * @param bizTag 业务标签
     * @return 分页结果
     * 
     * Requirements: 5.4, 10.7
     */
    @Select("SELECT * FROM files " +
            "WHERE tenant_id = #{tenantId} " +
            "AND JSON_CONTAINS(biz_tags, JSON_QUOTE(#{bizTag})) " +
            "AND deleted_at IS NULL " +
            "ORDER BY created_at DESC")
    IPage<FileEntity> selectByTenantIdAndBizTag(Page<FileEntity> page, 
                                                 @Param("tenantId") String tenantId, 
                                                 @Param("bizTag") String bizTag);

    /**
     * 按多个业务标签查询文件列表（支持分页）
     * 
     * 查询包含任意一个指定标签的文件
     * 自动过滤已删除的文件
     * 
     * @param page 分页对象
     * @param tenantId 租户ID
     * @param bizTags 业务标签列表
     * @return 分页结果
     * 
     * Requirements: 5.4, 10.7
     */
    @Select("<script>" +
            "SELECT * FROM files " +
            "WHERE tenant_id = #{tenantId} " +
            "AND deleted_at IS NULL " +
            "<if test='bizTags != null and bizTags.size() > 0'>" +
            "AND (" +
            "<foreach collection='bizTags' item='tag' separator=' OR '>" +
            "JSON_CONTAINS(biz_tags, JSON_QUOTE(#{tag}))" +
            "</foreach>" +
            ")" +
            "</if>" +
            "ORDER BY created_at DESC" +
            "</script>")
    IPage<FileEntity> selectByTenantIdAndBizTags(Page<FileEntity> page, 
                                                  @Param("tenantId") String tenantId, 
                                                  @Param("bizTags") List<String> bizTags);

    /**
     * 按访问策略查询文件列表（支持分页）
     * 
     * 自动过滤已删除的文件
     * 
     * @param page 分页对象
     * @param accessPolicy 访问策略（PUBLIC/PRIVATE）
     * @return 分页结果
     * 
     * Requirements: 5.1, 10.7
     */
    @Select("SELECT * FROM files " +
            "WHERE access_policy = #{accessPolicy} AND deleted_at IS NULL " +
            "ORDER BY created_at DESC")
    IPage<FileEntity> selectByAccessPolicy(Page<FileEntity> page, @Param("accessPolicy") AccessPolicy accessPolicy);

    /**
     * 按租户和访问策略组合查询文件列表（支持分页）
     * 
     * 自动过滤已删除的文件
     * 
     * @param page 分页对象
     * @param tenantId 租户ID
     * @param accessPolicy 访问策略
     * @return 分页结果
     * 
     * Requirements: 5.3, 10.7
     */
    @Select("SELECT * FROM files " +
            "WHERE tenant_id = #{tenantId} AND access_policy = #{accessPolicy} AND deleted_at IS NULL " +
            "ORDER BY created_at DESC")
    IPage<FileEntity> selectByTenantIdAndAccessPolicy(Page<FileEntity> page, 
                                                       @Param("tenantId") String tenantId, 
                                                       @Param("accessPolicy") AccessPolicy accessPolicy);

    /**
     * 按租户查询文件总数（不包括已删除）
     * 
     * @param tenantId 租户ID
     * @return 文件总数
     * 
     * Requirements: 5.3, 10.7
     */
    @Select("SELECT COUNT(*) FROM files " +
            "WHERE tenant_id = #{tenantId} AND deleted_at IS NULL")
    long countByTenantId(@Param("tenantId") String tenantId);

    /**
     * 按租户和访问策略查询文件总数（不包括已删除）
     * 
     * @param tenantId 租户ID
     * @param accessPolicy 访问策略
     * @return 文件总数
     * 
     * Requirements: 5.3, 10.7
     */
    @Select("SELECT COUNT(*) FROM files " +
            "WHERE tenant_id = #{tenantId} AND access_policy = #{accessPolicy} AND deleted_at IS NULL")
    long countByTenantIdAndAccessPolicy(@Param("tenantId") String tenantId, 
                                        @Param("accessPolicy") AccessPolicy accessPolicy);

    /**
     * 按 SHA256 查询文件（用于去重）
     * 
     * 自动过滤已删除的文件
     * 
     * @param sha256 文件SHA256校验和
     * @return 文件实体（如果存在）
     * 
     * Requirements: 5.1, 10.7
     */
    @Select("SELECT * FROM files " +
            "WHERE sha256 = #{sha256} AND deleted_at IS NULL " +
            "LIMIT 1")
    FileEntity selectBySha256(@Param("sha256") String sha256);

    /**
     * 按租户和 SHA256 查询文件（用于租户内去重）
     * 
     * 自动过滤已删除的文件
     * 
     * @param tenantId 租户ID
     * @param sha256 文件SHA256校验和
     * @return 文件实体（如果存在）
     * 
     * Requirements: 5.3, 10.7
     */
    @Select("SELECT * FROM files " +
            "WHERE tenant_id = #{tenantId} AND sha256 = #{sha256} AND deleted_at IS NULL " +
            "LIMIT 1")
    FileEntity selectByTenantIdAndSha256(@Param("tenantId") String tenantId, @Param("sha256") String sha256);
}
