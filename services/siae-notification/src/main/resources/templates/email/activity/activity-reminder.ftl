<#--
  活动提醒邮件模板
  用途：活动开始前提醒已报名用户
  必需变量：${activityTitle}, ${startTime}, ${location}, ${activityUrl}, ${remainingHours}
  可选变量：${userName}, ${checkInUrl}, ${contactInfo}
  作者：KEYKB
-->
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>活动提醒 - SIAE Studio</title>
    <#include "../common/styles.ftl">
</head>
<body>
    <div class="email-container">
        <#-- 引入邮件头部 -->
        <#include "../common/header.ftl">

        <#-- 邮件内容 -->
        <div class="email-content">
            <h3>⏰ 活动即将开始</h3>
            <p>您报名的活动《<strong>${activityTitle}</strong>》即将开始，请做好准备！</p>

            <#-- 倒计时提醒 -->
            <div style="background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                        color: white;
                        padding: 30px;
                        border-radius: 8px;
                        text-align: center;
                        margin: 25px 0;">
                <h2 style="margin: 0 0 10px 0; font-size: 18px; font-weight: 500;">距离活动开始还有</h2>
                <p style="font-size: 48px; font-weight: bold; margin: 15px 0; letter-spacing: 2px;">
                    ${remainingHours}
                </p>
                <p style="margin: 0; font-size: 16px;">小时</p>
            </div>

            <#-- 活动信息 -->
            <div class="info-box" style="background: #f8f9fa; border-left: 4px solid #667eea;">
                <p style="margin: 8px 0;"><strong>⏰ 开始时间：</strong>${startTime}</p>
                <p style="margin: 8px 0;"><strong>📍 活动地点：</strong>${location}</p>
                <#if contactInfo??>
                    <p style="margin: 8px 0;"><strong>📞 联系方式：</strong>${contactInfo}</p>
                </#if>
            </div>

            <#-- 温馨提示 -->
            <div style="background: #fff3cd; padding: 15px; border-radius: 5px; margin: 20px 0;">
                <p style="margin: 5px 0; color: #856404;"><strong>📝 温馨提示：</strong></p>
                <p style="margin: 5px 0; color: #856404;">· 请提前15分钟到达现场进行签到</p>
                <p style="margin: 5px 0; color: #856404;">· 请携带有效证件以便核验身份</p>
                <p style="margin: 5px 0; color: #856404;">· 如有特殊情况无法参加，请及时联系主办方</p>
            </div>

            <#-- 操作按钮 -->
            <div style="text-align: center; margin-top: 30px;">
                <#if checkInUrl??>
                    <a href="${checkInUrl}" class="button" style="margin: 0 10px;">在线签到</a>
                </#if>
                <a href="${activityUrl}" class="button" style="margin: 0 10px; background: linear-gradient(135deg, #764ba2 0%, #667eea 100%);">查看详情</a>
            </div>

            <#-- 期待信息 -->
            <p class="tips" style="text-align: center; margin-top: 25px; font-size: 14px;">
                期待与您相见！🎊
            </p>
        </div>

        <#-- 引入邮件底部 -->
        <#include "../common/footer.ftl">
    </div>
</body>
</html>