package com.hngy.siae.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.media.domain.entity.MultipartPart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 分片上传记录数据访问接口
 * 
 * 继承 MyBatis-Plus 的 BaseMapper，提供基础的 CRUD 操作
 * 
 * 自定义查询方法：
 * - 按上传会话查询所有分片
 * - 按上传会话和分片编号查询单个分片
 * - 统计上传会话的已完成分片数
 * - 删除上传会话的所有分片
 * 
 * Requirements: 8.2, 8.3
 *
 * @author SIAE Team
 */
@Mapper
public interface MultipartPartMapper extends BaseMapper<MultipartPart> {

    /**
     * 按上传会话查询所有分片
     * 
     * 按分片编号升序排列
     * 用于验证分片完整性和合并分片
     * 
     * @param uploadId 上传会话ID
     * @return 分片列表
     * 
     * Requirements: 8.2, 8.3
     */
    @Select("SELECT * FROM multipart_parts " +
            "WHERE upload_id = #{uploadId} " +
            "ORDER BY part_number ASC")
    List<MultipartPart> selectByUploadId(@Param("uploadId") String uploadId);

    /**
     * 按上传会话和分片编号查询单个分片
     * 
     * @param uploadId 上传会话ID
     * @param partNumber 分片编号
     * @return 分片记录
     * 
     * Requirements: 8.2
     */
    @Select("SELECT * FROM multipart_parts " +
            "WHERE upload_id = #{uploadId} AND part_number = #{partNumber}")
    MultipartPart selectByUploadIdAndPartNumber(@Param("uploadId") String uploadId, 
                                                 @Param("partNumber") Integer partNumber);

    /**
     * 统计上传会话的已完成分片数
     * 
     * 用于跟踪上传进度
     * 
     * @param uploadId 上传会话ID
     * @return 已完成分片数
     * 
     * Requirements: 8.2
     */
    @Select("SELECT COUNT(*) FROM multipart_parts " +
            "WHERE upload_id = #{uploadId}")
    int countByUploadId(@Param("uploadId") String uploadId);

    /**
     * 查询上传会话的已上传分片编号列表
     * 
     * 用于断点续传，确定哪些分片已经上传
     * 
     * @param uploadId 上传会话ID
     * @return 分片编号列表
     * 
     * Requirements: 8.2
     */
    @Select("SELECT part_number FROM multipart_parts " +
            "WHERE upload_id = #{uploadId} " +
            "ORDER BY part_number ASC")
    List<Integer> selectPartNumbersByUploadId(@Param("uploadId") String uploadId);

    /**
     * 查询上传会话的所有分片 ETag
     * 
     * 按分片编号升序排列
     * 用于合并分片时提供 ETag 列表
     * 
     * @param uploadId 上传会话ID
     * @return ETag 列表
     * 
     * Requirements: 8.3
     */
    @Select("SELECT etag FROM multipart_parts " +
            "WHERE upload_id = #{uploadId} " +
            "ORDER BY part_number ASC")
    List<String> selectETagsByUploadId(@Param("uploadId") String uploadId);

    /**
     * 删除上传会话的所有分片
     * 
     * 用于中断上传或清理失败的上传
     * 
     * @param uploadId 上传会话ID
     * @return 删除的记录数
     * 
     * Requirements: 8.2
     */
    @Delete("DELETE FROM multipart_parts WHERE upload_id = #{uploadId}")
    int deleteByUploadId(@Param("uploadId") String uploadId);

    /**
     * 查询指定范围的分片
     * 
     * 用于批量处理分片
     * 
     * @param uploadId 上传会话ID
     * @param startPartNumber 起始分片编号（包含）
     * @param endPartNumber 结束分片编号（包含）
     * @return 分片列表
     * 
     * Requirements: 8.2
     */
    @Select("SELECT * FROM multipart_parts " +
            "WHERE upload_id = #{uploadId} " +
            "AND part_number BETWEEN #{startPartNumber} AND #{endPartNumber} " +
            "ORDER BY part_number ASC")
    List<MultipartPart> selectByUploadIdAndPartRange(@Param("uploadId") String uploadId, 
                                                      @Param("startPartNumber") Integer startPartNumber, 
                                                      @Param("endPartNumber") Integer endPartNumber);

    /**
     * 检查上传会话的所有分片是否都已上传
     * 
     * 比较已上传分片数与总分片数
     * 
     * @param uploadId 上传会话ID
     * @param totalParts 总分片数
     * @return 是否所有分片都已上传
     * 
     * Requirements: 8.3
     */
    @Select("SELECT COUNT(*) = #{totalParts} FROM multipart_parts " +
            "WHERE upload_id = #{uploadId}")
    boolean isAllPartsUploaded(@Param("uploadId") String uploadId, @Param("totalParts") Integer totalParts);

    /**
     * 查询缺失的分片编号
     * 
     * 用于断点续传，确定哪些分片还需要上传
     * 使用递归 CTE 生成完整的分片编号序列，然后找出缺失的编号
     * 
     * @param uploadId 上传会话ID
     * @param totalParts 总分片数
     * @return 缺失的分片编号列表
     * 
     * Requirements: 8.2
     */
    @Select("WITH RECURSIVE part_numbers AS (" +
            "  SELECT 1 AS part_number " +
            "  UNION ALL " +
            "  SELECT part_number + 1 FROM part_numbers WHERE part_number < #{totalParts}" +
            ") " +
            "SELECT pn.part_number FROM part_numbers pn " +
            "LEFT JOIN multipart_parts mp ON mp.upload_id = #{uploadId} AND mp.part_number = pn.part_number " +
            "WHERE mp.part_number IS NULL " +
            "ORDER BY pn.part_number ASC")
    List<Integer> selectMissingPartNumbers(@Param("uploadId") String uploadId, @Param("totalParts") Integer totalParts);
}
