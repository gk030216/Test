package com.pet.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfig {

    @Value("${mail.smtp.host}")
    private String host;

    @Value("${mail.smtp.port}")
    private String port;

    @Value("${mail.smtp.auth}")
    private String auth;

    @Value("${mail.smtp.ssl.enable}")
    private String sslEnable;

    // 添加缺失的配置字段
    @Value("${mail.smtp.ssl.protocols:TLSv1.2}")
    private String sslProtocols;

    @Value("${mail.smtp.ssl.trust:smtp.qq.com}")
    private String sslTrust;

    @Value("${mail.smtp.connectiontimeout:5000}")
    private String connectionTimeout;

    @Value("${mail.smtp.timeout:5000}")
    private String timeout;

    @Value("${mail.smtp.writetimeout:5000}")
    private String writeTimeout;

    @Value("${mail.sender.email}")
    private String senderEmail;

    @Value("${mail.sender.password}")
    private String senderPassword;

    // getter方法（需要为新字段添加）
    public String getHost() { return host; }
    public String getPort() { return port; }
    public String getAuth() { return auth; }
    public String getSslEnable() { return sslEnable; }
    public String getSslProtocols() { return sslProtocols; }
    public String getSslTrust() { return sslTrust; }
    public String getConnectionTimeout() { return connectionTimeout; }
    public String getTimeout() { return timeout; }
    public String getWriteTimeout() { return writeTimeout; }
    public String getSenderEmail() { return senderEmail; }
    public String getSenderPassword() { return senderPassword; }
}