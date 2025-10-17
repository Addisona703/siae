package com.hngy.siae.notification.controller;

import com.hngy.siae.core.result.Result;
import com.hngy.siae.notification.service.EmailTemplateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 邮件模板测试控制器
 * 用途：测试各种邮件模板的渲染效果
 *
 * @author KEYKB
 */
@Slf4j
@RestController
@RequestMapping("/email/template")
@RequiredArgsConstructor
@Tag(name = "邮件模板测试", description = "测试各种邮件模板的渲染效果")
public class EmailTemplateController {

    private final EmailTemplateService emailTemplateService;

    @PostMapping("/test/register-code")
    @Operation(summary = "测试注册验证码邮件模板")
    public Result<String> testRegisterCodeTemplate(
            @Parameter(description = "用户名（可选）") @RequestParam(required = false) String userName,
            @Parameter(description = "邮箱地址") @RequestParam String email,
            @Parameter(description = "验证码") @RequestParam(defaultValue = "123456") String code,
            @Parameter(description = "有效期（分钟）") @RequestParam(defaultValue = "5") int expiryMinutes) {
        try {
            String html = emailTemplateService.renderRegisterCodeEmail(userName, email, code, expiryMinutes);
            return Result.success("模板渲染成功", html);
        } catch (Exception e) {
            log.error("注册验证码模板渲染失败", e);
            return Result.error("模板渲染失败：" + e.getMessage());
        }
    }

    @PostMapping("/test/login-code")
    @Operation(summary = "测试登录验证码邮件模板")
    public Result<String> testLoginCodeTemplate(
            @Parameter(description = "用户名（可选）") @RequestParam(required = false) String userName,
            @Parameter(description = "验证码") @RequestParam(defaultValue = "123456") String code,
            @Parameter(description = "有效期（分钟）") @RequestParam(defaultValue = "5") int expiryMinutes,
            @Parameter(description = "IP地址") @RequestParam(defaultValue = "192.168.1.100") String ipAddress,
            @Parameter(description = "地理位置") @RequestParam(defaultValue = "中国 广东 广州") String location) {
        try {
            String html = emailTemplateService.renderLoginCodeEmail(userName, code, expiryMinutes, ipAddress, location);
            return Result.success("模板渲染成功", html);
        } catch (Exception e) {
            log.error("登录验证码模板渲染失败", e);
            return Result.error("模板渲染失败：" + e.getMessage());
        }
    }

    @PostMapping("/test/system-notice")
    @Operation(summary = "测试系统通知邮件模板")
    public Result<String> testSystemNoticeTemplate(
            @Parameter(description = "用户名（可选）") @RequestParam(required = false) String userName,
            @Parameter(description = "通知标题") @RequestParam(defaultValue = "系统维护通知") String title,
            @Parameter(description = "通知内容") @RequestParam(defaultValue = "<p>尊敬的用户，系统将于今晚22:00-24:00进行维护升级，届时服务将暂停使用。</p>") String content,
            @Parameter(description = "通知类型") @RequestParam(defaultValue = "系统维护") String noticeType,
            @Parameter(description = "操作链接（可选）") @RequestParam(required = false) String actionUrl) {
        try {
            String html = emailTemplateService.renderSystemNoticeEmail(userName, title, content, noticeType, actionUrl);
            return Result.success("模板渲染成功", html);
        } catch (Exception e) {
            log.error("系统通知模板渲染失败", e);
            return Result.error("模板渲染失败：" + e.getMessage());
        }
    }

    @PostMapping("/test/comment-reply")
    @Operation(summary = "测试评论回复通知邮件模板")
    public Result<String> testCommentReplyTemplate(
            @Parameter(description = "用户名（可选）") @RequestParam(required = false) String userName,
            @Parameter(description = "回复者名称") @RequestParam(defaultValue = "张三") String replierName,
            @Parameter(description = "原评论内容") @RequestParam(defaultValue = "这篇文章写得真不错！") String originalComment,
            @Parameter(description = "回复内容") @RequestParam(defaultValue = "谢谢你的支持，我会继续努力的！") String replyContent,
            @Parameter(description = "内容标题") @RequestParam(defaultValue = "如何使用Spring Boot") String contentTitle,
            @Parameter(description = "内容链接") @RequestParam(defaultValue = "https://siae.com/article/123") String contentUrl) {
        try {
            String html = emailTemplateService.renderCommentReplyEmail(
                    userName, replierName, originalComment, replyContent, contentTitle, contentUrl);
            return Result.success("模板渲染成功", html);
        } catch (Exception e) {
            log.error("评论回复模板渲染失败", e);
            return Result.error("模板渲染失败：" + e.getMessage());
        }
    }

    @PostMapping("/test/content-audit")
    @Operation(summary = "测试内容审核结果邮件模板")
    public Result<String> testContentAuditTemplate(
            @Parameter(description = "用户名（可选）") @RequestParam(required = false) String userName,
            @Parameter(description = "内容标题") @RequestParam(defaultValue = "我的第一篇文章") String contentTitle,
            @Parameter(description = "内容类型") @RequestParam(defaultValue = "文章") String contentType,
            @Parameter(description = "审核状态") @RequestParam(defaultValue = "APPROVED") String auditStatus,
            @Parameter(description = "审核原因（可选）") @RequestParam(required = false) String auditReason,
            @Parameter(description = "内容链接") @RequestParam(defaultValue = "https://siae.com/article/123") String contentUrl) {
        try {
            String html = emailTemplateService.renderContentAuditEmail(
                    userName, contentTitle, contentType, auditStatus, auditReason, contentUrl);
            return Result.success("模板渲染成功", html);
        } catch (Exception e) {
            log.error("内容审核模板渲染失败", e);
            return Result.error("模板渲染失败：" + e.getMessage());
        }
    }

    @PostMapping("/test/activity-invite")
    @Operation(summary = "测试活动邀请邮件模板")
    public Result<String> testActivityInviteTemplate(
            @Parameter(description = "用户名（可选）") @RequestParam(required = false) String userName,
            @Parameter(description = "活动标题") @RequestParam(defaultValue = "2025技术交流大会") String activityTitle,
            @Parameter(description = "活动时间") @RequestParam(defaultValue = "2025年10月1日 14:00-18:00") String activityTime,
            @Parameter(description = "活动地点") @RequestParam(defaultValue = "广州国际会议中心") String activityLocation,
            @Parameter(description = "活动描述") @RequestParam(defaultValue = "<p>诚邀您参加2025技术交流大会，与业界大咖面对面交流。</p>") String activityDescription,
            @Parameter(description = "活动Banner（可选）") @RequestParam(required = false) String activityBanner,
            @Parameter(description = "主办方（可选）") @RequestParam(required = false) String organizerName,
            @Parameter(description = "报名链接") @RequestParam(defaultValue = "https://siae.com/activity/register/123") String registerUrl) {
        try {
            String html = emailTemplateService.renderActivityInviteEmail(
                    userName, activityTitle, activityTime, activityLocation, activityDescription,
                    activityBanner, organizerName, registerUrl);
            return Result.success("模板渲染成功", html);
        } catch (Exception e) {
            log.error("活动邀请模板渲染失败", e);
            return Result.error("模板渲染失败：" + e.getMessage());
        }
    }

    @PostMapping("/test/activity-reminder")
    @Operation(summary = "测试活动提醒邮件模板")
    public Result<String> testActivityReminderTemplate(
            @Parameter(description = "用户名（可选）") @RequestParam(required = false) String userName,
            @Parameter(description = "活动标题") @RequestParam(defaultValue = "2025技术交流大会") String activityTitle,
            @Parameter(description = "开始时间") @RequestParam(defaultValue = "2025年10月1日 14:00") String startTime,
            @Parameter(description = "活动地点") @RequestParam(defaultValue = "广州国际会议中心") String location,
            @Parameter(description = "剩余小时数") @RequestParam(defaultValue = "24") String remainingHours,
            @Parameter(description = "活动链接") @RequestParam(defaultValue = "https://siae.com/activity/123") String activityUrl,
            @Parameter(description = "签到链接（可选）") @RequestParam(required = false) String checkInUrl,
            @Parameter(description = "联系方式（可选）") @RequestParam(required = false) String contactInfo) {
        try {
            String html = emailTemplateService.renderActivityReminderEmail(
                    userName, activityTitle, startTime, location, remainingHours,
                    activityUrl, checkInUrl, contactInfo);
            return Result.success("模板渲染成功", html);
        } catch (Exception e) {
            log.error("活动提醒模板渲染失败", e);
            return Result.error("模板渲染失败：" + e.getMessage());
        }
    }
}