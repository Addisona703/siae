package com.hngy.siae.api.user.client;

import com.hngy.siae.api.user.dto.request.UserCreateDTO;
import com.hngy.siae.api.user.dto.response.UserFaceAuthVO;
import com.hngy.siae.api.user.dto.response.UserLoginVO;
import com.hngy.siae.api.user.dto.response.UserProfileSimpleVO;
import com.hngy.siae.api.user.dto.response.UserVO;
import com.hngy.siae.api.user.fallback.UserFeignClientFallback;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户服务 Feign 客户端
 * <p>
 * 提供用户相关的远程调用接口，包括用户注册、查询、验证等功能。
 * 这些接口专门用于服务间调用，不需要用户权限验证。
 *
 * @author KEYKB
 */
@FeignClient(
    name = "siae-user",
    path = "/api/v1/user/feign",
    contextId = "userFeignClient",
    fallback = UserFeignClientFallback.class
)
public interface UserFeignClient {
    
    /**
     * 用户注册（基础信息）
     * <p>
     * 供认证服务调用，只需填写基础必要信息
     *
     * @param registerDTO 用户注册参数
     * @return 用户信息
     */
    @PostMapping("/register")
    UserVO register(@RequestBody @NotNull UserCreateDTO registerDTO);
    
    /**
     * 根据用户名获取用户认证信息（包含密码）
     * <p>
     * 根据用户名查询用户认证信息，供内部服务间调用。
     * <strong>警告：返回的信息包含加密密码，仅限内部Feign调用使用！</strong>
     *
     * @param username 用户名，不能为空或空白字符串
     * @return 用户认证信息，如果用户不存在则返回null
     */
    @GetMapping("/username/{username}")
    UserLoginVO getUserByUsername(@PathVariable("username") @NotBlank String username);
    
    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return true表示用户名已存在，false表示用户名可用
     */
    @GetMapping("/exists/username/{username}")
    Boolean checkUsernameExists(@PathVariable("username") @NotBlank String username);
    
    /**
     * 检查学号是否存在
     *
     * @param studentId 学号
     * @return true表示学号已存在，false表示学号可用
     */
    @GetMapping("/exists/student-id/{studentId}")
    Boolean checkStudentIdExists(@PathVariable("studentId") @NotBlank String studentId);
    
    /**
     * 检查用户ID是否存在
     *
     * @param userId 用户ID
     * @return true表示用户存在，false表示用户不存在
     */
    @GetMapping("/exists/user-id/{userId}")
    Boolean checkUserIdExists(@PathVariable("userId") @NotNull Long userId);

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息，如果用户不存在则返回null
     */
    @GetMapping("/user/{userId}")
    UserVO getUserById(@PathVariable("userId") @NotNull Long userId);
    
    /**
     * 批量查询用户信息
     *
     * @param userIds 用户ID列表
     * @return 用户ID -> 用户信息的映射
     */
    @GetMapping("/batch")
    Map<Long, UserProfileSimpleVO> batchGetUserProfiles(@RequestParam("userIds") List<Long> userIds);

    /**
     * 获取所有用户ID列表（用于广播通知）
     *
     * @return 所有用户的ID列表
     */
    @GetMapping("/all-ids")
    List<Long> getAllUserIds();

    /**
     * 获取用户人脸认证信息
     * <p>
     * 用于人脸识别打卡场景，返回用户的真实姓名和身份证号
     *
     * @param userId 用户ID
     * @return 用户人脸认证信息（真实姓名和身份证号）
     */
    @GetMapping("/face-auth/{userId}")
    UserFaceAuthVO getUserFaceAuthInfo(@PathVariable("userId") @NotNull Long userId);

    /**
     * 通过身份证和姓名验证用户身份
     * <p>
     * 用于人脸识别打卡场景，验证身份证和姓名是否匹配，返回用户ID
     *
     * @param idCard 身份证号
     * @param realName 真实姓名
     * @return 用户ID，如果验证失败返回null
     */
    @GetMapping("/verify-identity")
    Long verifyUserIdentity(@RequestParam("idCard") @NotBlank String idCard,
                           @RequestParam("realName") @NotBlank String realName);

    /**
     * 更新用户密码
     * <p>
     * 供认证服务调用，更新用户的加密密码
     *
     * @param userId 用户ID
     * @param encodedPassword 加密后的新密码
     */
    @PutMapping("/password/{userId}")
    void updatePassword(@PathVariable("userId") @NotNull Long userId, 
                        @RequestParam("encodedPassword") @NotBlank String encodedPassword);
}
