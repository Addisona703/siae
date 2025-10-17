<#--
  邮件通用样式定义
  用途：统一的邮件样式，确保在各邮箱客户端显示一致
  作者：KEYKB
-->
<style>
    /* 全局样式 */
    body {
        font-family: 'Microsoft YaHei', 'PingFang SC', 'Helvetica Neue', Arial, sans-serif;
        background-color: #f5f5f5;
        margin: 0;
        padding: 0;
        line-height: 1.6;
    }

    /* 邮件容器 */
    .email-container {
        max-width: 600px;
        margin: 20px auto;
        background: #ffffff;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
        overflow: hidden;
    }

    /* 邮件头部 */
    .email-header {
        text-align: center;
        padding: 40px 30px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white;
    }

    .email-header .logo {
        width: 120px;
        height: auto;
        margin-bottom: 15px;
    }

    .email-header h2 {
        margin: 0;
        font-size: 24px;
        font-weight: 500;
    }

    /* 邮件内容区 */
    .email-content {
        padding: 40px 30px;
    }

    .email-content h3 {
        margin: 0 0 20px 0;
        font-size: 20px;
        color: #333;
    }

    .email-content p {
        margin: 10px 0;
        color: #555;
        font-size: 14px;
    }

    /* 验证码样式 */
    .verification-code {
        font-size: 32px;
        font-weight: bold;
        color: #667eea;
        letter-spacing: 8px;
        text-align: center;
        padding: 20px;
        background: #f0f4ff;
        border-radius: 8px;
        margin: 20px 0;
        border: 2px dashed #667eea;
    }

    /* 按钮样式 */
    .button {
        display: inline-block;
        padding: 12px 30px;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        color: white !important;
        text-decoration: none;
        border-radius: 5px;
        font-size: 14px;
        font-weight: 500;
        transition: all 0.3s ease;
    }

    .button:hover {
        box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
    }

    /* 信息框样式 */
    .info-box {
        background: #f8f9fa;
        padding: 15px;
        border-radius: 5px;
        margin: 15px 0;
    }

    .info-box p {
        margin: 5px 0;
    }

    /* 提示文本样式 */
    .tips {
        color: #999;
        font-size: 12px;
        line-height: 1.8;
        margin-top: 15px;
    }

    .tips strong {
        color: #667eea;
    }

    /* 引用框样式 */
    .quote-box {
        background: #f8f9fa;
        padding: 15px;
        border-left: 4px solid #667eea;
        margin: 15px 0;
    }

    /* 邮件底部 */
    .email-footer {
        text-align: center;
        padding: 30px 20px;
        background: #f8f9fa;
        color: #6c757d;
        font-size: 12px;
        border-top: 1px solid #e9ecef;
    }

    .email-footer p {
        margin: 8px 0;
    }

    .email-footer .copyright {
        font-weight: 500;
        color: #495057;
    }

    .email-footer .contact {
        color: #6c757d;
    }

    .email-footer .tips {
        color: #adb5bd;
        margin-top: 10px;
    }

    /* 响应式设计 */
    @media only screen and (max-width: 600px) {
        .email-container {
            margin: 10px;
            border-radius: 0;
        }

        .email-content {
            padding: 30px 20px;
        }

        .verification-code {
            font-size: 28px;
            letter-spacing: 5px;
        }
    }
</style>