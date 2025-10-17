<#--
  注册验证码邮件模板
  用途：用户注册时发送的验证码邮件
  必需变量：${code}, ${expiryMinutes}
  可选变量：${userName}, ${email}
  作者：KEYKB
-->
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>注册验证码 - SIAE Studio</title>
    <#include "../common/styles.ftl">
</head>
<body>
    <div class="email-container">
        <#-- 引入邮件头部 -->
        <#include "../common/header.ftl">

        <#-- 邮件内容 -->
        <div class="email-content">
            <h3>📧 注册验证码</h3>
            <p>感谢您注册 SIAE Studio！</p>
            <p>您的验证码为：</p>

            <#-- 验证码展示区域 -->
            <div class="verification-code">${code}</div>

            <#-- 提示信息 -->
            <p class="tips">
                · 验证码有效期：<strong>${expiryMinutes}分钟</strong><br>
                · 请勿将验证码透露给他人<br>
                · 如非本人操作，请忽略此邮件
            </p>

            <#-- 如果提供了邮箱地址，显示邮箱信息 -->
            <#if email??>
                <div class="info-box">
                    <p>注册邮箱：<strong>${email}</strong></p>
                </div>
            </#if>
        </div>

        <#-- 引入邮件底部 -->
        <#include "../common/footer.ftl">
    </div>
</body>
</html>