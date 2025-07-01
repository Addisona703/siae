package com.hngy.siae.user.api.feign;

import com.hngy.siae.common.result.Result;
import com.hngy.siae.user.api.dto.request.LoginDTO;
import com.hngy.siae.user.api.dto.request.UserDTO;
import com.hngy.siae.user.api.dto.response.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "siae-user", path = "/users")
public interface UserClient {

    @PostMapping("/login")
    @Operation(summary = "手机号验证码登录")
    Result<UserVO> login(@RequestBody LoginDTO loginDTO);

    @PostMapping("/register")
    @Operation(summary = "注册账号")
    Result<UserVO> userRegister(@RequestBody UserDTO userDTO);
}
