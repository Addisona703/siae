package com.hngy.siae.auth.feign;

import com.hngy.siae.auth.feign.dto.request.UserCreateDTO;
import com.hngy.siae.auth.feign.dto.response.UserBasicVO;
import com.hngy.siae.auth.feign.dto.response.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

/**
 * 用户服务Feign客户端
 * <p>
 * 提供用户服务的远程调用接口，与用户服务的UserService接口保持一致。
 * 所有方法都通过Feign客户端进行远程服务调用。
 *
 * @author KEYKB
 */
@FeignClient(name = "siae-user", path = "api/v1/user/internal")
public interface UserClient {
    // ==================== Feign专用内部服务调用接口 ====================

    /**
     * 创建用户（Feign专用接口）
     * <p>
     * 专门用于服务间调用的用户创建接口，不包装Result返回值
     *
     * @param userCreateDTO 用户创建参数
     * @return 用户信息
     */
    @Operation(summary = "创建用户", description = "创建新用户（Feign专用接口）")
    @PostMapping("/create")
    UserVO createUserForFeign(
            @Parameter(description = "用户创建参数") @Valid @RequestBody UserCreateDTO userCreateDTO);

    /**
     * 根据用户名获取用户基本信息（Feign专用接口）
     * <p>
     * 根据用户名查询用户的基本认证信息，包括用户ID、用户名和加密密码。
     * 此接口专门用于内部服务间的身份验证和用户查找。
     * <p>
     * <strong>安全警告：</strong>此接口返回用户密码信息，仅限内部服务调用使用，
     * 不得暴露给外部API，以防止敏感信息泄露。
     *
     * @param username 用户名，不能为空或空白字符串
     * @return 用户基本信息，如果用户不存在则返回null
     */
    @Operation(summary = "根据用户名获取用户基本信息", description = "根据用户名查询用户基本认证信息（Feign专用接口）")
    @GetMapping("/username/{username}")
    UserBasicVO getUserByUsername(
            @Parameter(description = "用户名") @PathVariable("username") @NotBlank String username);

    /**
     * 根据用户ID集合获取用户ID到用户名的映射
     *
     * @param userIds 用户ID集合
     * @return 用户ID到用户名的映射Map，key为用户ID，value为用户名
     */
    @Operation(summary = "批量获取用户名映射", description = "根据用户ID集合批量获取用户名映射")
    @GetMapping("/batch/usernames")
    Map<Long, String> getUserMapByIds(
            @Parameter(description = "用户ID集合") @RequestParam("userIds") @NotEmpty Set<Long> userIds);

    /**
     * 检查用户是否存在
     *
     * @param userId 用户ID
     * @return true表示用户存在，false表示用户不存在或已删除
     */
    @Operation(summary = "检查用户是否存在", description = "根据用户ID检查用户是否存在")
    @GetMapping("/exists/{userId}")
    Boolean checkUserExists(
            @Parameter(description = "用户ID") @PathVariable("userId") @NotNull Long userId);
}