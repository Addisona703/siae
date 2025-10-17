<#--
  评论回复通知邮件模板
  用途：用户的评论收到回复时发送
  必需变量：${replierName}, ${originalComment}, ${replyContent}, ${contentTitle}, ${contentUrl}
  可选变量：${userName}, ${replierAvatar}
  作者：KEYKB
-->
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>评论回复通知 - SIAE Studio</title>
    <#include "../common/styles.ftl">
</head>
<body>
    <div class="email-container">
        <#-- 引入邮件头部 -->
        <#include "../common/header.ftl">

        <#-- 邮件内容 -->
        <div class="email-content">
            <h3>💬 您收到了新回复</h3>
            <p><strong>${replierName}</strong> 回复了您在《${contentTitle}》下的评论：</p>

            <#-- 原评论 -->
            <div class="quote-box" style="background: #f8f9fa; border-left-color: #667eea;">
                <p style="color: #666; margin-bottom: 10px; font-size: 12px;">您的评论：</p>
                <p style="color: #333;">${originalComment}</p>
            </div>

            <#-- 回复内容 -->
            <div class="quote-box" style="background: #f0f4ff; border-left-color: #764ba2;">
                <p style="color: #666; margin-bottom: 10px; font-size: 12px;">TA的回复：</p>
                <p style="color: #333;">${replyContent}</p>
            </div>

            <#-- 操作按钮 -->
            <div style="text-align: center; margin-top: 30px;">
                <a href="${contentUrl}" class="button">查看详情并回复</a>
            </div>

            <#-- 提示信息 -->
            <p class="tips" style="text-align: center; margin-top: 20px;">
                回复时间：${.now?string('yyyy-MM-dd HH:mm:ss')}
            </p>
        </div>

        <#-- 引入邮件底部 -->
        <#include "../common/footer.ftl">
    </div>
</body>
</html>