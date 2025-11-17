package com.hngy.siae.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hngy.siae.auth.entity.OAuthAccount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 第三方账号绑定Mapper接口
 * 
 * @author SIAE
 */
@Mapper
public interface OAuthAccountMapper extends BaseMapper<OAuthAccount> {
    
    /**
     * 根据提供商和提供商用户ID查询绑定账号
     * 
     * @param provider 提供商
     * @param providerUserId 提供商用户ID
     * @return 绑定账号信息
     */
    @Select("SELECT * FROM oauth_account WHERE provider = #{provider} AND provider_user_id = #{providerUserId}")
    OAuthAccount selectByProviderAndUserId(@Param("provider") String provider, 
                                           @Param("providerUserId") String providerUserId);
    
    /**
     * 根据用户ID和提供商查询绑定账号
     * 
     * @param userId 用户ID
     * @param provider 提供商
     * @return 绑定账号信息
     */
    @Select("SELECT * FROM oauth_account WHERE user_id = #{userId} AND provider = #{provider}")
    OAuthAccount selectByUserIdAndProvider(@Param("userId") Long userId, @Param("provider") String provider);
    
    /**
     * 根据用户ID查询所有绑定账号
     * 
     * @param userId 用户ID
     * @return 绑定账号列表
     */
    @Select("SELECT * FROM oauth_account WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<OAuthAccount> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 统计用户绑定的账号数量
     * 
     * @param userId 用户ID
     * @return 绑定账号数量
     */
    @Select("SELECT COUNT(*) FROM oauth_account WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);
}
