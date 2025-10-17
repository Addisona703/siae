<#--
  活动邀请邮件模板
  用途：邀请用户参加平台活动
  必需变量：${activityTitle}, ${activityTime}, ${activityLocation}, ${activityDescription}, ${registerUrl}
  可选变量：${userName}, ${activityBanner}, ${organizerName}
  作者：KEYKB
-->
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>活动邀请 - SIAE Studio</title>
    <#include "../common/styles.ftl">
</head>
<body>
    <div class="email-container">
        <#-- 引入邮件头部 -->
        <#include "../common/header.ftl">

        <#-- 邮件内容 -->
        <div class="email-content">
            <h3>🎉 ${activityTitle}</h3>

            <#-- 活动Banner图片（如果有） -->
            <#if activityBanner??>
                <img src="${activityBanner}"
                     alt="${activityTitle}"
                     style="width: 100%; border-radius: 8px; margin: 20px 0;">
            </#if>

            <#-- 活动描述 -->
            <div style="line-height: 1.8; color: #555; margin: 20px 0;">
                ${activityDescription}
            </div>

            <#-- 活动信息 -->
            <div class="info-box" style="background: linear-gradient(135deg, #f5f7fa 0%, #c3cfe2 100%); border-left: 4px solid #667eea;">
                <p style="margin: 8px 0;"><strong>⏰ 活动时间：</strong>${activityTime}</p>
                <p style="margin: 8px 0;"><strong>📍 活动地点：</strong>${activityLocation}</p>
                <#if organizerName??>
                    <p style="margin: 8px 0;"><strong>👤 主办方：</strong>${organizerName}</p>
                </#if>
            </div>

            <#-- 报名按钮 -->
            <div style="text-align: center; margin-top: 30px;">
                <a href="${registerUrl}" class="button" style="font-size: 16px; padding: 15px 40px;">立即报名</a>
            </div>

            <#-- 提示信息 -->
            <p class="tips" style="text-align: center; margin-top: 20px;">
                名额有限，先到先得！期待您的参与 ✨
            </p>
        </div>

        <#-- 引入邮件底部 -->
        <#include "../common/footer.ftl">
    </div>
</body>
</html>