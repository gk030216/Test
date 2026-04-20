package com.pet.util;

import com.pet.config.EmailConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

@Component
public class EmailUtil {

    @Autowired
    private EmailConfig emailConfig;

    // 临时配置（用于测试）
    private String tempSmtpHost;
    private String tempSmtpPort;
    private String tempSenderEmail;
    private String tempSenderPassword;
    private Boolean tempSslEnable;

    /**
     * 更新邮件配置（用于测试）
     */
    public void updateConfig(String smtpHost, String smtpPort, String senderEmail,
                             String senderPassword, Boolean sslEnable) {
        this.tempSmtpHost = smtpHost;
        this.tempSmtpPort = smtpPort;
        this.tempSenderEmail = senderEmail;
        this.tempSenderPassword = senderPassword;
        this.tempSslEnable = sslEnable;
    }

    /**
     * 发送测试邮件
     */
    public boolean sendTestEmail(String toEmail) {
        Properties props = new Properties();

        String host = tempSmtpHost != null ? tempSmtpHost : emailConfig.getHost();
        String port = tempSmtpPort != null ? tempSmtpPort : emailConfig.getPort();
        String sender = tempSenderEmail != null ? tempSenderEmail : emailConfig.getSenderEmail();
        String password = tempSenderPassword != null ? tempSenderPassword : emailConfig.getSenderPassword();
        Boolean ssl = tempSslEnable != null ? tempSslEnable : true;

        props.setProperty("mail.smtp.host", host);
        props.setProperty("mail.smtp.port", port);
        props.setProperty("mail.smtp.auth", "true");

        if (ssl) {
            props.setProperty("mail.smtp.ssl.enable", "true");
            props.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
            props.setProperty("mail.smtp.ssl.trust", host);
        }

        props.setProperty("mail.smtp.connectiontimeout", "5000");
        props.setProperty("mail.smtp.timeout", "5000");
        props.setProperty("mail.smtp.writetimeout", "5000");

        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(sender, password);
            }
        };

        Session session = Session.getInstance(props, auth);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(sender, "宠物服务系统"));
            message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject("📧 邮件配置测试 - 宠物服务系统");
            message.setContent(getTestEmailContent(), "text/html;charset=utf-8");

            Transport.send(message);
            System.out.println("✅ 测试邮件发送成功 to: " + toEmail);

            clearTempConfig();
            return true;
        } catch (Exception e) {
            System.err.println("❌ 测试邮件发送失败: " + e.getMessage());
            clearTempConfig();
            return false;
        }
    }

    private void clearTempConfig() {
        this.tempSmtpHost = null;
        this.tempSmtpPort = null;
        this.tempSenderEmail = null;
        this.tempSenderPassword = null;
        this.tempSslEnable = null;
    }

    private String getTestEmailContent() {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'></head>" +
                "<body style='font-family:Microsoft YaHei;background:#f4f7fc;'>" +
                "<div style='max-width:600px;margin:20px auto;background:#fff;border-radius:16px;overflow:hidden;'>" +
                "<div style='background:linear-gradient(135deg,#667eea,#764ba2);padding:30px;text-align:center;'>" +
                "<h1 style='color:#fff;margin:0;'>🐾 宠物服务系统</h1>" +
                "<p style='color:#e0e0ff;margin:10px 0 0;'>邮件配置测试</p>" +
                "</div>" +
                "<div style='padding:40px;'>" +
                "<h2 style='color:#333;margin:0 0 20px;'>✅ 邮件配置成功！</h2>" +
                "<p style='color:#666;line-height:1.8;font-size:16px;'>恭喜！你的邮件服务器配置正确，系统可以正常发送邮件。</p>" +
                "<div style='background:#f0f9f4;border-radius:12px;padding:20px;margin:25px 0;border-left:4px solid #67c23a;'>" +
                "<p style='color:#666;margin:0;'>📌 如果你收到了这封邮件，说明邮件配置已经生效。</p>" +
                "</div>" +
                "</div>" +
                "<div style='background:#f8f9fa;padding:20px;text-align:center;border-top:1px solid #e9ecef;'>" +
                "<p style='color:#868e96;margin:0;font-size:14px;'>此为系统自动发送的测试邮件</p>" +
                "</div>" +
                "</div>" +
                "</body></html>";
    }

    /**
     * 发送验证码邮件
     */
    public boolean sendVerificationCode(String toEmail, String code, String type) {
        Properties props = new Properties();
        props.setProperty("mail.smtp.host", emailConfig.getHost());
        props.setProperty("mail.smtp.port", emailConfig.getPort());
        props.setProperty("mail.smtp.auth", emailConfig.getAuth());

        // ✅ 修复 SSL 信任问题
        props.setProperty("mail.smtp.ssl.enable", emailConfig.getSslEnable());
        props.setProperty("mail.smtp.ssl.protocols", emailConfig.getSslProtocols());
        props.setProperty("mail.smtp.ssl.trust", "*");  // 关键：信任所有证书

        props.setProperty("mail.smtp.connectiontimeout", emailConfig.getConnectionTimeout());
        props.setProperty("mail.smtp.timeout", emailConfig.getTimeout());
        props.setProperty("mail.smtp.writetimeout", emailConfig.getWriteTimeout());

        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailConfig.getSenderEmail(), emailConfig.getSenderPassword());
            }
        };

        Session session = Session.getInstance(props, auth);
        session.setDebug(true);  // 调试用

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailConfig.getSenderEmail(), "宠物服务系统"));
            message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(toEmail));

            String subject = "";
            String content = "";

            if ("register".equals(type)) {
                subject = "🐾 欢迎注册宠物服务系统 - 验证码";
                content = getRegisterEmailContent(code);
            } else if ("forget".equals(type)) {
                subject = "🔐 找回密码 - 宠物服务系统验证码";
                content = getForgetPasswordEmailContent(code);
            } else if ("change_email".equals(type)) {
                subject = "📧 修改邮箱 - 宠物服务系统验证码";
                content = getChangeEmailContent(code);
            }

            message.setSubject(subject);
            message.setContent(content, "text/html;charset=utf-8");

            Transport.send(message);
            System.out.println("✅ 邮件发送成功 to: " + toEmail + ", type: " + type);
            return true;

        } catch (Exception e) {
            System.err.println("❌ 邮件发送失败 to: " + toEmail + ", type: " + type);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 注册验证码邮件模板
     */
    private String getRegisterEmailContent(String code) {
        return "<!DOCTYPE html>" +
                "<html lang='zh-CN'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>注册验证码</title>" +
                "</head>" +
                "<body style='margin:0; padding:0; font-family: \"Microsoft YaHei\", sans-serif; background-color:#f4f7fc;'>" +
                "<div style='max-width:600px; margin:20px auto; background-color:#ffffff; border-radius:16px; overflow:hidden; box-shadow:0 10px 25px rgba(0,0,0,0.1);'>" +

                // 头部
                "<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding:30px 20px; text-align:center;'>" +
                "<h1 style='color:#ffffff; margin:0; font-size:28px; letter-spacing:1px;'>🐾 宠物服务系统</h1>" +
                "<p style='color:#e0e0ff; margin:10px 0 0; font-size:16px;'>你的贴心宠物生活助手</p>" +
                "</div>" +

                // 内容区域
                "<div style='padding:40px 30px;'>" +
                "<h2 style='color:#333333; margin:0 0 20px; font-size:22px; font-weight:400;'>亲爱的用户，你好！</h2>" +
                "<p style='color:#666666; line-height:1.8; margin:0 0 25px; font-size:16px;'>感谢你注册宠物服务系统，你的验证码是：</p>" +

                // 验证码卡片
                "<div style='background: linear-gradient(145deg, #f6f9fc 0%, #eef2f7 100%); border-radius:12px; padding:20px; text-align:center; margin:25px 0; border:1px dashed #667eea;'>" +
                "<span style='font-size:48px; font-weight:700; letter-spacing:8px; color:#667eea; font-family: \"Courier New\", monospace;'>" + code + "</span>" +
                "</div>" +

                "<p style='color:#666666; line-height:1.8; margin:0 0 15px; font-size:15px;'>⏰ 验证码有效期为 <strong style='color:#ff6b6b;'>5分钟</strong>，请勿泄露给他人。</p>" +
                "<p style='color:#999999; line-height:1.6; margin:0; font-size:14px; background-color:#f8f8f8; padding:15px; border-radius:8px; border-left:4px solid #ffb347;'>" +
                "📌 如果不是你本人操作，请忽略此邮件，你的账号依然是安全的。</p>" +
                "</div>" +

                // 底部
                "<div style='background-color:#f8f9fa; padding:20px 30px; border-top:1px solid #e9ecef; text-align:center;'>" +
                "<p style='color:#868e96; margin:0 0 10px; font-size:14px;'>这是系统自动发送的邮件，请勿回复</p>" +
                "<p style='color:#adb5bd; margin:0; font-size:13px;'>© 2026 宠物服务系统 · 用心服务每一个宠物家庭</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * 找回密码验证码邮件模板
     */
    private String getForgetPasswordEmailContent(String code) {
        return "<!DOCTYPE html>" +
                "<html lang='zh-CN'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>找回密码验证码</title>" +
                "</head>" +
                "<body style='margin:0; padding:0; font-family: \"Microsoft YaHei\", sans-serif; background-color:#f4f7fc;'>" +
                "<div style='max-width:600px; margin:20px auto; background-color:#ffffff; border-radius:16px; overflow:hidden; box-shadow:0 10px 25px rgba(0,0,0,0.1);'>" +

                // 头部
                "<div style='background: linear-gradient(135deg, #ff9a9e 0%, #fad0c4 100%); padding:30px 20px; text-align:center;'>" +
                "<h1 style='color:#ffffff; margin:0; font-size:28px; letter-spacing:1px;'>🐾 宠物服务系统</h1>" +
                "<p style='color:#fff0f0; margin:10px 0 0; font-size:16px;'>安全中心 · 找回密码</p>" +
                "</div>" +

                // 内容区域
                "<div style='padding:40px 30px;'>" +
                "<h2 style='color:#333333; margin:0 0 20px; font-size:22px; font-weight:400;'>尊敬的宠物家长，你好！</h2>" +
                "<p style='color:#666666; line-height:1.8; margin:0 0 15px; font-size:16px;'>你正在进行找回密码操作，验证码是：</p>" +

                // 验证码卡片
                "<div style='background: linear-gradient(145deg, #fff5f5 0%, #ffe9e9 100%); border-radius:12px; padding:20px; text-align:center; margin:25px 0; border:1px dashed #ff6b6b;'>" +
                "<span style='font-size:48px; font-weight:700; letter-spacing:8px; color:#ff6b6b; font-family: \"Courier New\", monospace;'>" + code + "</span>" +
                "</div>" +

                "<div style='background-color:#fff9f0; padding:20px; border-radius:8px; margin:25px 0;'>" +
                "<p style='color:#666666; line-height:1.8; margin:0 0 12px; font-size:15px;'>🔔 <strong>安全提示：</strong></p>" +
                "<ul style='color:#666666; line-height:1.8; margin:0; padding-left:20px; font-size:14px;'>" +
                "<li>验证码有效期为 <strong style='color:#ff6b6b;'>5分钟</strong></li>" +
                "<li>请勿将验证码告知他人</li>" +
                "<li>如果不是你本人操作，请立即修改密码</li>" +
                "</ul>" +
                "</div>" +

                "<p style='color:#999999; line-height:1.6; margin:0; font-size:14px; background-color:#f8f8f8; padding:15px; border-radius:8px;'>" +
                "📧 如有任何疑问，请联系客服 support@petservice.com</p>" +
                "</div>" +

                // 底部
                "<div style='background-color:#f8f9fa; padding:20px 30px; border-top:1px solid #e9ecef; text-align:center;'>" +
                "<p style='color:#868e96; margin:0 0 10px; font-size:14px;'>此为系统自动发送邮件，请勿直接回复</p>" +
                "<p style='color:#adb5bd; margin:0; font-size:13px;'>© 2026 宠物服务系统 · 守护你和宠物的美好时光</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * 修改邮箱验证码邮件模板
     */
    private String getChangeEmailContent(String code) {
        return "<!DOCTYPE html>" +
                "<html lang='zh-CN'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>修改邮箱验证码</title>" +
                "</head>" +
                "<body style='margin:0; padding:0; font-family: \"Microsoft YaHei\", sans-serif; background-color:#f4f7fc;'>" +
                "<div style='max-width:600px; margin:20px auto; background-color:#ffffff; border-radius:16px; overflow:hidden; box-shadow:0 10px 25px rgba(0,0,0,0.1);'>" +

                // 头部
                "<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding:30px 20px; text-align:center;'>" +
                "<h1 style='color:#ffffff; margin:0; font-size:28px; letter-spacing:1px;'>🐾 宠物服务系统</h1>" +
                "<p style='color:#e0e0ff; margin:10px 0 0; font-size:16px;'>安全中心 · 修改邮箱</p>" +
                "</div>" +

                // 内容区域
                "<div style='padding:40px 30px;'>" +
                "<h2 style='color:#333333; margin:0 0 20px; font-size:22px; font-weight:400;'>亲爱的用户，你好！</h2>" +
                "<p style='color:#666666; line-height:1.8; margin:0 0 25px; font-size:16px;'>你正在进行修改邮箱操作，验证码是：</p>" +

                // 验证码卡片
                "<div style='background: linear-gradient(145deg, #f6f9fc 0%, #eef2f7 100%); border-radius:12px; padding:20px; text-align:center; margin:25px 0; border:1px dashed #667eea;'>" +
                "<span style='font-size:48px; font-weight:700; letter-spacing:8px; color:#667eea; font-family: \"Courier New\", monospace;'>" + code + "</span>" +
                "</div>" +

                "<div style='background-color:#fff9f0; padding:20px; border-radius:8px; margin:25px 0;'>" +
                "<p style='color:#666666; line-height:1.8; margin:0 0 12px; font-size:15px;'>🔔 <strong>安全提示：</strong></p>" +
                "<ul style='color:#666666; line-height:1.8; margin:0; padding-left:20px; font-size:14px;'>" +
                "<li>验证码有效期为 <strong style='color:#ff6b6b;'>5分钟</strong></li>" +
                "<li>请勿将验证码告知他人</li>" +
                "<li>如果不是你本人操作，请忽略此邮件</li>" +
                "</ul>" +
                "</div>" +

                "<p style='color:#999999; line-height:1.6; margin:0; font-size:14px; background-color:#f8f8f8; padding:15px; border-radius:8px;'>" +
                "📧 如有任何疑问，请联系客服 support@petservice.com</p>" +
                "</div>" +

                // 底部
                "<div style='background-color:#f8f9fa; padding:20px 30px; border-top:1px solid #e9ecef; text-align:center;'>" +
                "<p style='color:#868e96; margin:0 0 10px; font-size:14px;'>此为系统自动发送邮件，请勿直接回复</p>" +
                "<p style='color:#adb5bd; margin:0; font-size:13px;'>© 2026 宠物服务系统 · 守护你和宠物的美好时光</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * 发送预约成功通知邮件
     */
    public boolean sendAppointmentNotification(String toEmail, String userName,
                                               String serviceName, String appointmentDate, String appointmentTime,
                                               String petName, String appointmentNo) {

        Properties props = new Properties();
        props.setProperty("mail.smtp.host", emailConfig.getHost());
        props.setProperty("mail.smtp.port", emailConfig.getPort());
        props.setProperty("mail.smtp.auth", emailConfig.getAuth());
        props.setProperty("mail.smtp.ssl.enable", emailConfig.getSslEnable());
        props.setProperty("mail.smtp.ssl.protocols", emailConfig.getSslProtocols());
        props.setProperty("mail.smtp.ssl.trust", emailConfig.getSslTrust());
        props.setProperty("mail.smtp.connectiontimeout", emailConfig.getConnectionTimeout());
        props.setProperty("mail.smtp.timeout", emailConfig.getTimeout());
        props.setProperty("mail.smtp.writetimeout", emailConfig.getWriteTimeout());

        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailConfig.getSenderEmail(), emailConfig.getSenderPassword());
            }
        };

        Session session = Session.getInstance(props, auth);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailConfig.getSenderEmail(), "宠物服务系统"));
            message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject("🐾 预约成功通知 - " + serviceName);
            message.setContent(getAppointmentEmailContent(userName, serviceName, appointmentDate,
                    appointmentTime, petName, appointmentNo), "text/html;charset=utf-8");

            Transport.send(message);
            System.out.println("✅ 预约通知邮件发送成功 to: " + toEmail);
            return true;

        } catch (Exception e) {
            System.err.println("❌ 预约通知邮件发送失败 to: " + toEmail);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 预约成功邮件模板
     */
    private String getAppointmentEmailContent(String userName, String serviceName,
                                              String appointmentDate, String appointmentTime, String petName, String appointmentNo) {

        return "<!DOCTYPE html>" +
                "<html lang='zh-CN'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>预约成功通知</title>" +
                "</head>" +
                "<body style='margin:0; padding:0; font-family: \"Microsoft YaHei\", sans-serif; background-color:#f4f7fc;'>" +
                "<div style='max-width:600px; margin:20px auto; background-color:#ffffff; border-radius:16px; overflow:hidden; box-shadow:0 10px 25px rgba(0,0,0,0.1);'>" +

                // 头部
                "<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding:30px 20px; text-align:center;'>" +
                "<h1 style='color:#ffffff; margin:0; font-size:28px; letter-spacing:1px;'>🐾 预约成功</h1>" +
                "<p style='color:#e0e0ff; margin:10px 0 0; font-size:16px;'>感谢你的信任，我们已收到你的预约</p>" +
                "</div>" +

                // 内容区域
                "<div style='padding:30px;'>" +
                "<p style='color:#333333; font-size:16px; margin:0 0 20px;'>亲爱的 <strong>" + (userName != null ? userName : "用户") + "</strong>，你好！</p>" +
                "<p style='color:#666666; line-height:1.8; margin:0 0 25px; font-size:15px;'>你的预约已提交成功，请等待商家确认。以下是你的预约详情：</p>" +

                // 预约信息卡片
                "<div style='background: linear-gradient(145deg, #f6f9fc 0%, #eef2f7 100%); border-radius:12px; padding:20px; margin:20px 0;'>" +
                "<table style='width:100%; border-collapse:collapse;'>" +
                "<tr><td style='padding:8px 0; color:#666; width:100px;'>预约编号：</td><td style='padding:8px 0; color:#333; font-weight:500;'>" + appointmentNo + "</td></tr>" +
                "<tr><td style='padding:8px 0; color:#666;'>服务项目：</td><td style='padding:8px 0; color:#333; font-weight:500;'>" + serviceName + "</td></tr>" +
                "<tr><td style='padding:8px 0; color:#666;'>预约宠物：</td><td style='padding:8px 0; color:#333; font-weight:500;'>" + (petName != null ? petName : "未指定") + "</td></tr>" +
                "<tr><td style='padding:8px 0; color:#666;'>预约日期：</td><td style='padding:8px 0; color:#333; font-weight:500;'>" + appointmentDate + "</td></tr>" +
                "<tr><td style='padding:8px 0; color:#666;'>预约时段：</td><td style='padding:8px 0; color:#333; font-weight:500;'>" + appointmentTime + "</td></tr>" +
                "</table>" +
                "</div>" +

                // 提示信息
                "<div style='background-color:#fff9f0; padding:15px; border-radius:8px; margin:20px 0; border-left:4px solid #ffb347;'>" +
                "<p style='color:#666; margin:0; font-size:14px;'>📌 <strong>温馨提示：</strong></p>" +
                "<ul style='color:#666; margin:10px 0 0; padding-left:20px; font-size:14px;'>" +
                "<li>商家确认后，你将收到确认通知</li>" +
                "<li>如需取消预约，请至少提前2小时操作</li>" +
                "<li>如有疑问，请联系客服 400-888-6666</li>" +
                "</ul>" +
                "</div>" +

                // 按钮
                "<div style='text-align:center; margin:30px 0;'>" +
                "<a href='http://localhost:8081/appointments' style='display:inline-block; background:linear-gradient(135deg, #667eea, #764ba2); color:white; text-decoration:none; padding:12px 40px; border-radius:30px; font-size:16px; font-weight:500;'>查看我的预约</a>" +
                "</div>" +
                "</div>" +

                // 底部
                "<div style='background-color:#f8f9fa; padding:20px 30px; border-top:1px solid #e9ecef; text-align:center;'>" +
                "<p style='color:#868e96; margin:0 0 10px; font-size:14px;'>这是系统自动发送的邮件，请勿回复</p>" +
                "<p style='color:#adb5bd; margin:0; font-size:13px;'>© 2026 宠物服务系统 · 用心服务每一个宠物家庭</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }


    public boolean sendAppointmentPaymentSuccess(String toEmail, String userName,
                                                 String serviceName, Date appointmentDate,
                                                 String appointmentTime, String petName,
                                                 String appointmentNo) {
        Properties props = new Properties();
        props.setProperty("mail.smtp.host", emailConfig.getHost());
        props.setProperty("mail.smtp.port", emailConfig.getPort());
        props.setProperty("mail.smtp.auth", emailConfig.getAuth());
        props.setProperty("mail.smtp.ssl.enable", emailConfig.getSslEnable());
        props.setProperty("mail.smtp.ssl.protocols", emailConfig.getSslProtocols());
        // ✅ 关键修复：指定信任的服务器，避免 SSL 握手失败
        props.setProperty("mail.smtp.ssl.trust", "smtp.qq.com");
        props.setProperty("mail.smtp.connectiontimeout", emailConfig.getConnectionTimeout());
        props.setProperty("mail.smtp.timeout", emailConfig.getTimeout());
        props.setProperty("mail.smtp.writetimeout", emailConfig.getWriteTimeout());

        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailConfig.getSenderEmail(), emailConfig.getSenderPassword());
            }
        };

        Session session = Session.getInstance(props, auth);
        // 为调试添加，可以打印更详细的日志
        // session.setDebug(true);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailConfig.getSenderEmail(), "宠物服务系统"));
            message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject("🐾 预约支付成功 - " + serviceName);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
            String formattedDate = dateFormat.format(appointmentDate);

            message.setContent(getPaymentSuccessEmailContent(userName, serviceName, formattedDate,
                    appointmentTime, petName, appointmentNo), "text/html;charset=utf-8");

            Transport.send(message);
            System.out.println("✅ 支付成功邮件发送成功 to: " + toEmail);
            return true;

        } catch (Exception e) {
            System.err.println("❌ 支付成功邮件发送失败 to: " + toEmail);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 支付成功邮件模板
     */
    private String getPaymentSuccessEmailContent(String userName, String serviceName,
                                                 String appointmentDate, String appointmentTime,
                                                 String petName, String appointmentNo) {
        return "<!DOCTYPE html>" +
                "<html lang='zh-CN'>" +
                "<head><meta charset='UTF-8'><title>预约支付成功</title></head>" +
                "<body style='margin:0; padding:0; font-family: \"Microsoft YaHei\", sans-serif; background-color:#f4f7fc;'>" +
                "<div style='max-width:600px; margin:20px auto; background-color:#ffffff; border-radius:16px; overflow:hidden; box-shadow:0 10px 25px rgba(0,0,0,0.1);'>" +

                // 头部
                "<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); padding:30px 20px; text-align:center;'>" +
                "<h1 style='color:#ffffff; margin:0; font-size:28px;'>🐾 支付成功！</h1>" +
                "<p style='color:#e0e0ff; margin:10px 0 0;'>预约已确认，请等待商家接单</p>" +
                "</div>" +

                // 内容区域
                "<div style='padding:30px;'>" +
                "<p style='color:#333333; font-size:16px;'>亲爱的 <strong>" + (userName != null ? userName : "用户") + "</strong>，你好！</p>" +
                "<p style='color:#666666; line-height:1.8;'>你的预约已支付成功，商家将尽快确认。以下是预约详情：</p>" +

                // 预约信息卡片
                "<div style='background: linear-gradient(145deg, #f6f9fc 0%, #eef2f7 100%); border-radius:12px; padding:20px; margin:20px 0;'>" +
                "<table style='width:100%; border-collapse:collapse;'>" +
                "<tr><td style='padding:8px 0; color:#666; width:100px;'>预约编号：</td>" +
                "<td style='padding:8px 0; color:#333; font-weight:500;'>" + appointmentNo + "</td></tr>" +
                "<tr><td style='padding:8px 0; color:#666;'>服务项目：</td>" +
                "<td style='padding:8px 0; color:#333; font-weight:500;'>" + serviceName + "</td></tr>" +
                "<tr><td style='padding:8px 0; color:#666;'>预约宠物：</td>" +
                "<td style='padding:8px 0; color:#333; font-weight:500;'>" + (petName != null ? petName : "未指定") + "</td></tr>" +
                "<tr><td style='padding:8px 0; color:#666;'>预约日期：</td>" +
                "<td style='padding:8px 0; color:#333; font-weight:500;'>" + appointmentDate + "</td></tr>" +
                "<tr><td style='padding:8px 0; color:#666;'>预约时段：</td>" +
                "<td style='padding:8px 0; color:#333; font-weight:500;'>" + appointmentTime + "</td></tr>" +
                "</table>" +
                "</div>" +

                // 提示信息
                "<div style='background-color:#fff9f0; padding:15px; border-radius:8px; margin:20px 0; border-left:4px solid #ffb347;'>" +
                "<p style='color:#666; margin:0; font-size:14px;'>📌 <strong>温馨提示：</strong></p>" +
                "<ul style='color:#666; margin:10px 0 0; padding-left:20px; font-size:14px;'>" +
                "<li>商家确认后，你可以在「我的预约」中查看最新状态</li>" +
                "<li>如需取消预约，请至少提前2小时操作</li>" +
                "<li>如有疑问，请联系客服 400-888-6666</li>" +
                "</ul>" +
                "</div>" +

                // 按钮
                "<div style='text-align:center; margin:30px 0;'>" +
                "<a href='http://localhost:8081/personal/appointments' style='display:inline-block; background:linear-gradient(135deg, #667eea, #764ba2); color:white; text-decoration:none; padding:12px 40px; border-radius:30px; font-size:16px; font-weight:500;'>查看我的预约</a>" +
                "</div>" +
                "</div>" +

                // 底部
                "<div style='background-color:#f8f9fa; padding:20px; text-align:center; border-top:1px solid #e9ecef;'>" +
                "<p style='color:#868e96; margin:0; font-size:14px;'>© 2026 宠物服务系统 · 用心服务每一个宠物家庭</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }

    /**
     * 发送待支付提醒邮件
     */
    public boolean sendAppointmentPendingReminder(String toEmail, String userName,
                                                  String serviceName, Date appointmentDate,
                                                  String appointmentTime, String petName,
                                                  String appointmentNo) {
        Properties props = new Properties();
        props.setProperty("mail.smtp.host", emailConfig.getHost());
        props.setProperty("mail.smtp.port", emailConfig.getPort());
        props.setProperty("mail.smtp.auth", emailConfig.getAuth());
        props.setProperty("mail.smtp.ssl.enable", emailConfig.getSslEnable());
        props.setProperty("mail.smtp.ssl.protocols", emailConfig.getSslProtocols());
        props.setProperty("mail.smtp.ssl.trust", "smtp.qq.com");
        props.setProperty("mail.smtp.connectiontimeout", emailConfig.getConnectionTimeout());
        props.setProperty("mail.smtp.timeout", emailConfig.getTimeout());
        props.setProperty("mail.smtp.writetimeout", emailConfig.getWriteTimeout());

        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(emailConfig.getSenderEmail(), emailConfig.getSenderPassword());
            }
        };

        Session session = Session.getInstance(props, auth);

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(emailConfig.getSenderEmail(), "宠物服务系统"));
            message.setRecipient(MimeMessage.RecipientType.TO, new InternetAddress(toEmail));
            message.setSubject("⏰ 待支付提醒 - " + serviceName);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月dd日");
            String formattedDate = dateFormat.format(appointmentDate);

            message.setContent(getPendingPaymentEmailContent(userName, serviceName, formattedDate,
                    appointmentTime, petName, appointmentNo), "text/html;charset=utf-8");

            Transport.send(message);
            System.out.println("✅ 待支付提醒邮件已发送至: " + toEmail);
            return true;

        } catch (Exception e) {
            System.err.println("❌ 待支付提醒邮件发送失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 待支付提醒邮件模板
     */
    private String getPendingPaymentEmailContent(String userName, String serviceName,
                                                 String appointmentDate, String appointmentTime,
                                                 String petName, String appointmentNo) {
        return "<!DOCTYPE html>" +
                "<html lang='zh-CN'>" +
                "<head><meta charset='UTF-8'><title>待支付提醒</title></head>" +
                "<body style='margin:0; padding:0; font-family: \"Microsoft YaHei\", sans-serif; background-color:#f4f7fc;'>" +
                "<div style='max-width:600px; margin:20px auto; background-color:#ffffff; border-radius:16px; overflow:hidden; box-shadow:0 10px 25px rgba(0,0,0,0.1);'>" +
                "<div style='background: linear-gradient(135deg, #e6a23c 0%, #f0b45c 100%); padding:30px 20px; text-align:center;'>" +
                "<h1 style='color:#ffffff; margin:0; font-size:28px;'>⏰ 待支付提醒</h1>" +
                "<p style='color:#fff0e0; margin:10px 0 0;'>请尽快完成支付，否则预约将自动取消</p>" +
                "</div>" +
                "<div style='padding:30px;'>" +
                "<p style='color:#333333; font-size:16px;'>亲爱的 <strong>" + (userName != null ? userName : "用户") + "</strong>，你好！</p>" +
                "<p style='color:#666666; line-height:1.8;'>您有一个预约待支付，请在30分钟内完成支付：</p>" +
                "<div style='background: linear-gradient(145deg, #f6f9fc 0%, #eef2f7 100%); border-radius:12px; padding:20px; margin:20px 0;'>" +
                "<table style='width:100%; border-collapse:collapse;'>" +
                "<tr><td style='padding:8px 0; color:#666; width:100px;'>预约编号：</td>" +
                "<td style='padding:8px 0; color:#333; font-weight:500;'>" + appointmentNo + "</td></tr>" +
                "<tr><td style='padding:8px 0; color:#666;'>服务项目：</td>" +
                "<td style='padding:8px 0; color:#333; font-weight:500;'>" + serviceName + "</td></tr>" +
                "<tr><td style='padding:8px 0; color:#666;'>预约宠物：</td>" +
                "<td style='padding:8px 0; color:#333; font-weight:500;'>" + (petName != null ? petName : "未指定") + "</td></tr>" +
                "<tr><td style='padding:8px 0; color:#666;'>预约日期：</td>" +
                "<td style='padding:8px 0; color:#333; font-weight:500;'>" + appointmentDate + "</td></tr>" +
                "<tr><td style='padding:8px 0; color:#666;'>预约时段：</td>" +
                "<td style='padding:8px 0; color:#333; font-weight:500;'>" + appointmentTime + "</td></tr>" +
                "</table>" +
                "</div>" +
                "<div style='background-color:#fef9e6; padding:15px; border-radius:8px; margin:20px 0; border-left:4px solid #e6a23c;'>" +
                "<p style='color:#666; margin:0; font-size:14px;'>⚠️ <strong>温馨提示：</strong> 请尽快完成支付，超时未支付预约将自动取消。</p>" +
                "</div>" +
                "<div style='text-align:center; margin:30px 0;'>" +
                "<a href='http://localhost:8081/pay/" + appointmentNo + "' style='display:inline-block; background:linear-gradient(135deg, #e6a23c, #f0b45c); color:white; text-decoration:none; padding:12px 40px; border-radius:30px; font-size:16px; font-weight:500;'>立即支付</a>" +
                "</div>" +
                "</div>" +
                "<div style='background-color:#f8f9fa; padding:20px; text-align:center; border-top:1px solid #e9ecef;'>" +
                "<p style='color:#868e96; margin:0; font-size:14px;'>© 2026 宠物服务系统 · 用心服务每一个宠物家庭</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}