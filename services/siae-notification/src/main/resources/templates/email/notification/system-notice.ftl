<#--
  系统通知邮件模板
  用途：系统重要通知（维护、升级、政策变更等）
  必需变量：${title}, ${content}, ${noticeType}
  可选变量：${userName}, ${actionUrl}
  作者：KEYKB
-->
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${title} - SIAE Studio</title>
    <#include "../common/styles.ftl">
</head>
<body>
    <div class="email-container">
        <#-- 引入邮件头部 -->
        <#include "../common/header.ftl">

        <#-- 邮件内容 -->
        <div class="email-content">
            <h3>📢 ${title}</h3>

            <#-- 通知内容，支持HTML格式 -->
            <div style="line-height: 1.8; color: #555;">
                ${content}
            </div>

            <#-- 如果有操作链接，显示按钮 -->
            <#if actionUrl??>
                <div style="text-align: center; margin-top: 30px;">
                    <a href="${actionUrl}" class="button">查看详情</a>
                </div>
            </#if>

            <#-- 通知类型标识 -->
            <div class="info-box" style="margin-top: 30px;">
                <p>通知类型：<strong>${noticeType}</strong></p>
                <p>发送时间：${.now?string('yyyy-MM-dd HH:mm:ss')}</p>
            </div>
        </div>

        <#-- 引入邮件底部 -->
        <#include "../common/footer.ftl">
    </div>
</body>
</html>