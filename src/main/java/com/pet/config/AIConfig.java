package com.pet.config;

import com.pet.entity.SystemSetting;
import com.pet.mapper.SystemSettingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;

@Configuration
public class AIConfig {

    @Autowired
    private SystemSettingMapper settingMapper;

    // DeepSeek API配置
    private String apiKey = "";
    private String baseUrl = "https://api.deepseek.com";
    private String model = "deepseek-chat";
    private String provider = "deepseek";

    // 本地知识库配置
    private boolean localEnabled = true;
    private int matchThreshold = 30;

    @PostConstruct
    public void init() {
        loadConfigFromDatabase();
    }

    /**
     * 从数据库加载配置
     */
    private void loadConfigFromDatabase() {
        try {
            List<SystemSetting> settings = settingMapper.findAll();
            for (SystemSetting setting : settings) {
                switch (setting.getSettingKey()) {
                    case "ai_provider":
                        this.provider = setting.getSettingValue();
                        break;
                    case "ai_api_key":
                        this.apiKey = setting.getSettingValue();
                        break;
                    case "ai_base_url":
                        this.baseUrl = setting.getSettingValue();
                        break;
                    case "ai_model":
                        this.model = setting.getSettingValue();
                        break;
                    case "ai_match_threshold":
                        this.matchThreshold = Integer.parseInt(setting.getSettingValue());
                        break;
                    case "ai_local_enabled":
                        this.localEnabled = "true".equals(setting.getSettingValue());
                        break;
                }
            }
            System.out.println("✅ AI配置加载成功");
        } catch (Exception e) {
            System.err.println("❌ AI配置加载失败，使用默认配置");
            e.printStackTrace();
        }
    }

    /**
     * 重新加载配置
     */
    public void reloadConfig() {
        loadConfigFromDatabase();
    }

    // getter方法
    public String getApiKey() { return apiKey; }
    public String getBaseUrl() { return baseUrl; }
    public String getModel() { return model; }
    public String getProvider() { return provider; }
    public boolean isLocalEnabled() { return localEnabled; }
    public int getMatchThreshold() { return matchThreshold; }
}