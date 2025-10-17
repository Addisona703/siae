package com.hngy.siae.notification.service;

import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.Map;

/**
 * 邮件模板服务接口
 * 用途：处理邮件模板的渲染
 *
 * @author KEYKB
 */
public interface EmailTemplateService {

    /**
     * 渲染邮件模板为HTML字符串
     *
     * @param templateName 模板名称（相对于templates/email/的路径，例如：verification/register-code.ftl）
     * @param model        模板变量
     * @return 渲染后的HTML字符串
     * @throws IOException       模板读取失败
     * @throws TemplateException 模板渲染失败
     */
    String renderTemplate(String templateName, Map<String, Object> model) throws IOException, TemplateException;

    /**
     * 渲染注册验证码邮件
     *
     * @param userName      用户名（可选）
     * @param email         邮箱地址
     * @param code          验证码
     * @param expiryMinutes 有效期（分钟）
     * @return 渲染后的HTML字符串
     */
    String renderRegisterCodeEmail(String userName, String email, String code, int expiryMinutes) throws IOException, TemplateException;

    /**
     * 渲染登录验证码邮件
     *
     * @param userName      用户名（可选）
     * @param code          验证码
     * @param expiryMinutes 有效期（分钟）
     * @param ipAddress     登录IP地址
     * @param location      登录地理位置
     * @return 渲染后的HTML字符串
     */
    String renderLoginCodeEmail(String userName, String code, int expiryMinutes, String ipAddress, String location) throws IOException, TemplateException;

    /**
     * 渲染系统通知邮件
     *
     * @param userName   用户名（可选）
     * @param title      通知标题
     * @param content    通知内容（支持HTML）
     * @param noticeType 通知类型
     * @param actionUrl  操作链接（可选）
     * @return 渲染后的HTML字符串
     */
    String renderSystemNoticeEmail(String userName, String title, String content, String noticeType, String actionUrl) throws IOException, TemplateException;

    /**
     * 渲染评论回复通知邮件
     *
     * @param userName        用户名（可选）
     * @param replierName     回复者名称
     * @param originalComment 原评论内容
     * @param replyContent    回复内容
     * @param contentTitle    内容标题
     * @param contentUrl      内容链接
     * @return 渲染后的HTML字符串
     */
    String renderCommentReplyEmail(String userName, String replierName, String originalComment,
                                    String replyContent, String contentTitle, String contentUrl) throws IOException, TemplateException;

    /**
     * 渲染内容审核结果邮件
     *
     * @param userName     用户名（可选）
     * @param contentTitle 内容标题
     * @param contentType  内容类型（文章、视频等）
     * @param auditStatus  审核状态（APPROVED/REJECTED/PENDING）
     * @param auditReason  审核原因（可选，未通过时必填）
     * @param contentUrl   内容链接
     * @return 渲染后的HTML字符串
     */
    String renderContentAuditEmail(String userName, String contentTitle, String contentType,
                                    String auditStatus, String auditReason, String contentUrl) throws IOException, TemplateException;

    /**
     * 渲染活动邀请邮件
     *
     * @param userName            用户名（可选）
     * @param activityTitle       活动标题
     * @param activityTime        活动时间
     * @param activityLocation    活动地点
     * @param activityDescription 活动描述（支持HTML）
     * @param activityBanner      活动Banner图片URL（可选）
     * @param organizerName       主办方名称（可选）
     * @param registerUrl         报名链接
     * @return 渲染后的HTML字符串
     */
    String renderActivityInviteEmail(String userName, String activityTitle, String activityTime,
                                      String activityLocation, String activityDescription, String activityBanner,
                                      String organizerName, String registerUrl) throws IOException, TemplateException;

    /**
     * 渲染活动提醒邮件
     *
     * @param userName       用户名（可选）
     * @param activityTitle  活动标题
     * @param startTime      开始时间
     * @param location       活动地点
     * @param remainingHours 剩余小时数
     * @param activityUrl    活动详情链接
     * @param checkInUrl     签到链接（可选）
     * @param contactInfo    联系方式（可选）
     * @return 渲染后的HTML字符串
     */
    String renderActivityReminderEmail(String userName, String activityTitle, String startTime,
                                        String location, String remainingHours, String activityUrl,
                                        String checkInUrl, String contactInfo) throws IOException, TemplateException;
}