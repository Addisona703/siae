package com.hngy.siae.auth.feign;

import com.hngy.siae.auth.feign.dto.request.UserCreateDTO;
import com.hngy.siae.auth.feign.dto.response.UserAuthVO;
import com.hngy.siae.auth.feign.dto.response.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

/**
 * 用户服务Feign客户端
 * <p>
 * 提供用户服务的远程调用接口，与用户服务的UserFeignController接口保持一致。
 * 所有方法都通过Feign客户端进行远程服务调用。
 *
 * @author KEYKB
 */
@FeignClient(name = "siae-user", path = "api/v1/user/feign")
public interface UserClient {

    /**
     * 用户注册（基础信息）
     * 供认证服务调用，只需填写基础必要信息
     *
     * @param registerDTO 用户注册参数
     * @return 用户信息
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册接口，只需填写基础必要信息，供认证服务调用")
    UserVO registerUser(
            @Parameter(description = "用户注册参数") @Valid @RequestBody UserCreateDTO registerDTO);

    /**
     * 根据用户名获取用户信息
     * <p>
     * 根据用户名查询用户信息，供内部服务间调用。
     * 返回的 UserVO 包含密码字段（使用 @JsonProperty(access = WRITE_ONLY) 标注）
     *
     * @param username 用户名，不能为空或空白字符串
     * @return 用户信息，如果用户不存在则返回null
     */
    @GetMapping("/username/{username}")
    @Operation(summary = "根据用户名查询用户", description = "供认证服务调用，返回用户信息")
    UserAuthVO getUserByUsername(
            @Parameter(description = "用户名") @PathVariable("username") @NotBlank String username);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return true表示用户名已存在，false表示用户名可用
     */
    @GetMapping("/exists/username/{username}")
    @Operation(summary = "检查用户名是否存在", description = "验证用户名是否已被使用")
    Boolean checkUsernameExists(
            @Parameter(description = "用户名") @PathVariable("username") @NotBlank String username);

    /**
     * 检查学号是否存在
     *
     * @param studentId 学号
     * @return true表示学号已存在，false表示学号可用
     */
    @GetMapping("/exists/student-id/{studentId}")
    @Operation(summary = "检查学号是否存在", description = "验证学号是否已被使用")
    Boolean checkStudentIdExists(
            @Parameter(description = "学号") @PathVariable("studentId") @NotBlank String studentId);

    /**
     * 检查用户ID是否存在
     *
     * @param userId 用户ID
     * @return true表示用户存在，false表示用户不存在
     */
    @GetMapping("/exists/user-id/{userId}")
    @Operation(summary = "检查用户ID是否存在", description = "验证用户ID是否存在")
    Boolean checkUserIdExists(
            @Parameter(description = "用户ID") @PathVariable("userId") @NotNull Long userId);

    /**
     * 根据用户ID获取用户信息
     * 调用 UserController 的 getUser 接口
     *
     * @param userId 用户ID
     * @return 用户信息，如果用户不存在则返回null
     */
    @GetMapping("?id={userId}")
    @Operation(summary = "根据用户ID查询用户", description = "供认证服务调用，返回用户基本信息")
    UserVO getUserById(
            @Parameter(description = "用户ID") @RequestParam("id") @NotNull Long userId);
}