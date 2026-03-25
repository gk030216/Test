package com.pet.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailConfig {

    // QQ邮箱465端口SSL配置
    private String host = "smtp.qq.com";
    private String port = "465";
    private String auth = "true";
    private String sslEnable = "true";
    private String sslProtocols = "TLSv1.2";
    private String sslTrust = "smtp.qq.com";
    private String connectionTimeout = "5000";
    private String timeout = "5000";
    private String writeTimeout = "5000";
    private String senderEmail = "2173015402@qq.com";
    private String senderPassword = "pdxcyarbfvluebcf";

    // getter方法
    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getAuth() {
        return auth;
    }

    public String getSslEnable() {
        return sslEnable;
    }

    public String getSslProtocols() {
        return sslProtocols;
    }

    public String getSslTrust() {
        return sslTrust;
    }

    public String getConnectionTimeout() {
        return connectionTimeout;
    }

    public String getTimeout() {
        return timeout;
    }

    public String getWriteTimeout() {
        return writeTimeout;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public String getSenderPassword() {
        return senderPassword;
    }
}