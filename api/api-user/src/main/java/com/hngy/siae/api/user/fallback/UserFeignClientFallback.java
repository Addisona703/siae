package com.hngy.siae.api.user.fallback;

import com.hngy.siae.core.exception.ServiceException;
import com.hngy.siae.api.user.client.UserFeignClient;
import com.hngy.siae.api.user.dto.request.UserCreateDTO;
import com.hngy.siae.api.user.dto.response.UserFaceAuthVO;
import com.hngy.siae.api.user.dto.response.UserLoginVO;
import com.hngy.siae.api.user.dto.response.UserProfileSimpleVO;
import com.hngy.siae.api.user.dto.response.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * UserFeignClient 降级处理类
 * <p>
 * 当用户服务不可用时，执行降级逻辑。
 * 主要用于网络故障、服务宕机等无法获取响应的情况。
 *
 * @author KEYKB
 */
@Slf4j
@Component
public class UserFeignClientFallback implements UserFeignClient {
    
    @Override
    public UserVO register(UserCreateDTO registerDTO) {
        log.error("用户注册服务不可用，触发降级。用户名: {}", 
            registerDTO != null ? registerDTO.getUsername() : "null");
        throw new ServiceException(503, "用户服务暂时不可用，请稍后重试");
    }
    
    @Override
    public UserLoginVO getUserByUsername(String username) {
        log.error("获取用户认证信息服务不可用，触发降级。用户名: {}", username);
        throw new ServiceException(503, "用户服务暂时不可用，请稍后重试");
    }
    
    @Override
    public Boolean checkUsernameExists(String username) {
        log.error("检查用户名服务不可用，触发降级。用户名: {}", username);
        // 降级策略：保守处理，假设用户名已存在，阻止注册
        // 这样可以避免在服务恢复后出现重复用户名的情况
        log.warn("降级策略：返回 true（假设用户名已存在），阻止可能的重复注册");
        return true;
    }
    
    @Override
    public Boolean checkStudentIdExists(String studentId) {
        log.error("检查学号服务不可用，触发降级。学号: {}", studentId);
        // 降级策略：保守处理，假设学号已存在，阻止注册
        log.warn("降级策略：返回 true（假设学号已存在），阻止可能的重复注册");
        return true;
    }
    
    @Override
    public Boolean checkUserIdExists(Long userId) {
        log.error("检查用户ID服务不可用，触发降级。用户ID: {}", userId);
        // 降级策略：保守处理，假设用户不存在
        log.warn("降级策略：返回 false（假设用户不存在）");
        return false;
    }

    @Override
    public UserVO getUserById(Long userId) {
        log.error("获取用户信息服务不可用，触发降级。用户ID: {}", userId);
        throw new ServiceException(503, "用户服务暂时不可用，请稍后重试");
    }
    
    @Override
    public Map<Long, UserProfileSimpleVO> batchGetUserProfiles(List<Long> userIds) {
        log.error("批量查询用户信息服务不可用，触发降级。用户ID数量: {}", 
            userIds != null ? userIds.size() : 0);
        // 降级策略：返回空Map，避免空指针异常
        log.warn("降级策略：返回空Map");
        return Collections.emptyMap();
    }

    @Override
    public List<Long> getAllUserIds() {
        log.error("获取所有用户ID服务不可用，触发降级");
        // 降级策略：返回空列表，广播通知将被取消
        log.warn("降级策略：返回空列表，广播通知将被取消");
        return Collections.emptyList();
    }

    @Override
    public UserFaceAuthVO getUserFaceAuthInfo(Long userId) {
        log.error("获取用户人脸认证信息服务不可用，触发降级。用户ID: {}", userId);
        throw new ServiceException(503, "用户服务暂时不可用，请稍后重试");
    }

    @Override
    public void updatePassword(Long userId, String encodedPassword) {
        log.error("更新用户密码服务不可用，触发降级。用户ID: {}", userId);
        throw new ServiceException(503, "用户服务暂时不可用，请稍后重试");
    }
}
