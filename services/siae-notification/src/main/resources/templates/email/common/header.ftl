<#--
  邮件头部公共片段
  用途：统一的邮件顶部，包含品牌logo和欢迎语
  引用变量：${userName}（可选）
  作者：KEYKB
-->
<div class="email-header">
    <#-- Logo图片，可以替换为实际的logo URL -->
    <img src="https://via.placeholder.com/120x120?text=SIAE" alt="SIAE Studio Logo" class="logo">

    <#-- 根据是否有用户名显示不同的欢迎语 -->
    <#if userName??>
        <h2>你好，${userName}！</h2>
    <#else>
        <h2>你好！</h2>
    </#if>
</div>