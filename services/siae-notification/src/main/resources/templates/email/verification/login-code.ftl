<#--
  登录验证码邮件模板
  用途：用户登录时发送的验证码邮件
  必需变量：${code}, ${expiryMinutes}, ${ipAddress}, ${location}
  可选变量：${userName}
  作者：KEYKB
-->
<!DOCTYPE html>
<html lang="zh-CN">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>登录验证码 - SIAE Studio</title>
    <#include "../common/styles.ftl">
</head>
<body>
    <div class="email-container">
        <#-- 引入邮件头部 -->
        <#include "../common/header.ftl">

        <#-- 邮件内容 -->
        <div class="email-content">
            <h3>🔐 登录验证码</h3>
            <p>检测到您正在进行登录操作。</p>

            <#-- 验证码展示区域 -->
            <div class="verification-code">${code}</div>

            <#-- 登录信息 -->
            <div class="info-box">
                <p><strong>登录信息：</strong></p>
                <p>IP地址：${ipAddress}</p>
                <p>地理位置：${location}</p>
                <p>时间：${.now?string('yyyy-MM-dd HH:mm:ss')}</p>
            </div>

            <#-- 安全提示 -->
            <p class="tips">
                · 验证码有效期：<strong>${expiryMinutes}分钟</strong><br>
                · 请勿将验证码透露给他人<br>
                · 如非本人操作，您的账户可能存在安全风险，请立即修改密码
            </p>
        </div>

        <#-- 引入邮件底部 -->
        <#include "../common/footer.ftl">
    </div>
</body>
</html>