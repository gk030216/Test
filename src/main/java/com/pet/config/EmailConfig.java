package com.pet.config;

import com.pet.entity.SystemSetting;
import com.pet.mapper.SystemSettingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;

@Configuration
public class EmailConfig {

    @Autowired
    private SystemSettingMapper settingMapper;

    // 默认配置
    private String host = "smtp.qq.com";
    private String port = "465";
    private String auth = "true";
    private String sslEnable = "true";
    private String sslProtocols = "TLSv1.2";
    private String sslTrust = "smtp.qq.com";
    private String connectionTimeout = "5000";
    private String timeout = "5000";
    private String writeTimeout = "5000";
    private String senderEmail = "";
    private String senderPassword = "";

    @PostConstruct
    public void init() {
        loadFromDatabase();
    }

    /**
     * 从数据库加载配置
     */
    public void loadFromDatabase() {
        try {
            List<SystemSetting> settings = settingMapper.findAll();
            for (SystemSetting setting : settings) {
                switch (setting.getSettingKey()) {
                    case "smtp_host":
                        this.host = setting.getSettingValue();
                        break;
                    case "smtp_port":
                        this.port = setting.getSettingValue();
                        break;
                    case "sender_email":
                        this.senderEmail = setting.getSettingValue();
                        break;
                    case "sender_password":
                        this.senderPassword = setting.getSettingValue();
                        break;
                    case "ssl_enable":
                        this.sslEnable = setting.getSettingValue();
                        break;
                }
            }
        } catch (Exception e) {
            System.err.println("加载邮件配置失败，使用默认配置: " + e.getMessage());
        }
    }

    // getter 方法保持不变
    public String getHost() { return host; }
    public String getPort() { return port; }
    public String getAuth() { return auth; }
    public String getSslEnable() { return sslEnable; }
    public String getSslProtocols() { return sslProtocols; }
    public String getSslTrust() {
        return host != null ? host.replace("smtp.", "") : "qq.com";
    }
    public String getConnectionTimeout() { return connectionTimeout; }
    public String getTimeout() { return timeout; }
    public String getWriteTimeout() { return writeTimeout; }
    public String getSenderEmail() { return senderEmail; }
    public String getSenderPassword() { return senderPassword; }
}