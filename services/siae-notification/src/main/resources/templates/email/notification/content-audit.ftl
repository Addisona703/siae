<#--
  内容审核结果通知邮件模板
  用途：用户发布的内容审核完成后发送
  必需变量：${contentTitle}, ${contentType}, ${auditStatus}, ${contentUrl}
  可选变量：${userName}, ${auditReason}
  作者：KEYKB
-->
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>内容审核通知 - SIAE Studio</title>
    <#include "../common/styles.ftl">
</head>
<body>
    <div class="email-container">
        <#-- 引入邮件头部 -->
        <#include "../common/header.ftl">

        <#-- 邮件内容 -->
        <div class="email-content">
            <#-- 根据审核状态显示不同内容 -->
            <#if auditStatus == 'APPROVED'>
                <h3>✅ 内容审核通过</h3>
                <p>恭喜！您的${contentType}《<strong>${contentTitle}</strong>》已通过审核，现已成功发布。</p>

                <div class="info-box" style="background: #d4edda; border-left: 4px solid #28a745;">
                    <p style="color: #155724; margin: 0;">
                        <strong>审核结果：</strong>通过审核 ✓
                    </p>
                </div>

            <#elseif auditStatus == 'REJECTED'>
                <h3>❌ 内容审核未通过</h3>
                <p>很抱歉，您的${contentType}《<strong>${contentTitle}</strong>》未通过审核。</p>

                <#if auditReason??>
                    <div class="info-box" style="background: #fff3cd; border-left: 4px solid #ffc107;">
                        <p style="color: #856404; margin: 0;">
                            <strong>未通过原因：</strong>${auditReason}
                        </p>
                    </div>
                </#if>

                <p class="tips">
                    请根据审核意见修改后重新提交，感谢您的理解与配合。
                </p>

            <#else>
                <h3>⏳ 内容审核中</h3>
                <p>您的${contentType}《<strong>${contentTitle}</strong>》正在审核中，请耐心等待。</p>

                <div class="info-box" style="background: #d1ecf1; border-left: 4px solid #17a2b8;">
                    <p style="color: #0c5460; margin: 0;">
                        <strong>审核状态：</strong>审核中...
                    </p>
                </div>
            </#if>

            <#-- 操作按钮 -->
            <div style="text-align: center; margin-top: 30px;">
                <a href="${contentUrl}" class="button">查看详情</a>
            </div>

            <#-- 审核时间 -->
            <p class="tips" style="text-align: center; margin-top: 20px;">
                通知时间：${.now?string('yyyy-MM-dd HH:mm:ss')}
            </p>
        </div>

        <#-- 引入邮件底部 -->
        <#include "../common/footer.ftl">
    </div>
</body>
</html>