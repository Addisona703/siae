package com.hngy.siae.notification.service.impl;

import com.hngy.siae.notification.service.EmailTemplateService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * 邮件模板服务实现类
 * 用途：使用FreeMarker渲染邮件模板
 *
 * @author KEYKB
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailTemplateServiceImpl implements EmailTemplateService {

    private final Configuration freemarkerConfig;

    @Override
    public String renderTemplate(String templateName, Map<String, Object> model) throws IOException, TemplateException {
        log.debug("开始渲染邮件模板：{}", templateName);

        // 获取模板
        Template template = freemarkerConfig.getTemplate("email/" + templateName);

        // 渲染模板
        StringWriter writer = new StringWriter();
        template.process(model, writer);

        String result = writer.toString();
        log.debug("邮件模板渲染完成，HTML长度：{}", result.length());

        return result;
    }

    @Override
    public String renderRegisterCodeEmail(String userName, String email, String code, int expiryMinutes)
            throws IOException, TemplateException {
        Map<String, Object> model = new HashMap<>();
        if (userName != null && !userName.isEmpty()) {
            model.put("userName", userName);
        }
        if (email != null && !email.isEmpty()) {
            model.put("email", email);
        }
        model.put("code", code);
        model.put("expiryMinutes", expiryMinutes);

        return renderTemplate("verification/register-code.ftl", model);
    }

    @Override
    public String renderLoginCodeEmail(String userName, String code, int expiryMinutes,
                                        String ipAddress, String location) throws IOException, TemplateException {
        Map<String, Object> model = new HashMap<>();
        if (userName != null && !userName.isEmpty()) {
            model.put("userName", userName);
        }
        model.put("code", code);
        model.put("expiryMinutes", expiryMinutes);
        model.put("ipAddress", ipAddress);
        model.put("location", location);

        return renderTemplate("verification/login-code.ftl", model);
    }

    @Override
    public String renderSystemNoticeEmail(String userName, String title, String content,
                                           String noticeType, String actionUrl) throws IOException, TemplateException {
        Map<String, Object> model = new HashMap<>();
        if (userName != null && !userName.isEmpty()) {
            model.put("userName", userName);
        }
        model.put("title", title);
        model.put("content", content);
        model.put("noticeType", noticeType);
        if (actionUrl != null && !actionUrl.isEmpty()) {
            model.put("actionUrl", actionUrl);
        }

        return renderTemplate("notification/system-notice.ftl", model);
    }

    @Override
    public String renderCommentReplyEmail(String userName, String replierName, String originalComment,
                                           String replyContent, String contentTitle, String contentUrl)
            throws IOException, TemplateException {
        Map<String, Object> model = new HashMap<>();
        if (userName != null && !userName.isEmpty()) {
            model.put("userName", userName);
        }
        model.put("replierName", replierName);
        model.put("originalComment", originalComment);
        model.put("replyContent", replyContent);
        model.put("contentTitle", contentTitle);
        model.put("contentUrl", contentUrl);

        return renderTemplate("notification/comment-reply.ftl", model);
    }

    @Override
    public String renderContentAuditEmail(String userName, String contentTitle, String contentType,
                                           String auditStatus, String auditReason, String contentUrl)
            throws IOException, TemplateException {
        Map<String, Object> model = new HashMap<>();
        if (userName != null && !userName.isEmpty()) {
            model.put("userName", userName);
        }
        model.put("contentTitle", contentTitle);
        model.put("contentType", contentType);
        model.put("auditStatus", auditStatus);
        if (auditReason != null && !auditReason.isEmpty()) {
            model.put("auditReason", auditReason);
        }
        model.put("contentUrl", contentUrl);

        return renderTemplate("notification/content-audit.ftl", model);
    }

    @Override
    public String renderActivityInviteEmail(String userName, String activityTitle, String activityTime,
                                             String activityLocation, String activityDescription,
                                             String activityBanner, String organizerName, String registerUrl)
            throws IOException, TemplateException {
        Map<String, Object> model = new HashMap<>();
        if (userName != null && !userName.isEmpty()) {
            model.put("userName", userName);
        }
        model.put("activityTitle", activityTitle);
        model.put("activityTime", activityTime);
        model.put("activityLocation", activityLocation);
        model.put("activityDescription", activityDescription);
        if (activityBanner != null && !activityBanner.isEmpty()) {
            model.put("activityBanner", activityBanner);
        }
        if (organizerName != null && !organizerName.isEmpty()) {
            model.put("organizerName", organizerName);
        }
        model.put("registerUrl", registerUrl);

        return renderTemplate("activity/activity-invite.ftl", model);
    }

    @Override
    public String renderActivityReminderEmail(String userName, String activityTitle, String startTime,
                                               String location, String remainingHours, String activityUrl,
                                               String checkInUrl, String contactInfo)
            throws IOException, TemplateException {
        Map<String, Object> model = new HashMap<>();
        if (userName != null && !userName.isEmpty()) {
            model.put("userName", userName);
        }
        model.put("activityTitle", activityTitle);
        model.put("startTime", startTime);
        model.put("location", location);
        model.put("remainingHours", remainingHours);
        model.put("activityUrl", activityUrl);
        if (checkInUrl != null && !checkInUrl.isEmpty()) {
            model.put("checkInUrl", checkInUrl);
        }
        if (contactInfo != null && !contactInfo.isEmpty()) {
            model.put("contactInfo", contactInfo);
        }

        return renderTemplate("activity/activity-reminder.ftl", model);
    }
}