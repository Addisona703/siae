package com.hngy.siae.attendance.controller;

import com.hngy.siae.api.user.client.UserFeignClient;
import com.hngy.siae.api.user.dto.response.UserFaceAuthVO;
import com.hngy.siae.core.result.Result;
import com.hngy.siae.security.annotation.SiaeAuthorize;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 人脸认证控制器
 * <p>
 * 提供人脸识别打卡所需的用户认证信息接口
 *
 * @author KEYKB
 */
@Tag(name = "人脸认证管理")
@RestController
@RequestMapping("/face-auth")
@RequiredArgsConstructor
@Validated
public class FaceAuthController {

    private final UserFeignClient userFeignClient;

    /**
     * 获取当前用户的人脸认证信息
     * <p>
     * 用于人脸识别打卡前获取用户的真实姓名和身份证号，
     * 前端可用于人脸识别SDK进行身份验证
     *
     * @param userId 用户ID
     * @return 用户人脸认证信息（真实姓名和身份证号）
     */
    @Operation(summary = "获取用户人脸认证信息", description = "用于人脸识别打卡，返回真实姓名和身份证号")
    @GetMapping("/{userId}")
    @SiaeAuthorize("isAuthenticated()")
    public Result<UserFaceAuthVO> getUserFaceAuthInfo(
            @Parameter(description = "用户ID") @PathVariable("userId") @NotNull Long userId) {
        UserFaceAuthVO faceAuthInfo = userFeignClient.getUserFaceAuthInfo(userId);
        return Result.success(faceAuthInfo);
    }
}
