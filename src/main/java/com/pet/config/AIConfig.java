package com.pet.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class AIConfig {

    // DeepSeek API配置
    private String apiKey = "sk-61fbc844d1924cb8ad16937fdf242e7f";
    private String baseUrl = "https://api.deepseek.com";
    private String model = "deepseek-chat";

    // 本地知识库配置
    private boolean localEnabled = true;
    private int matchThreshold = 30;

    // getter方法
    public String getApiKey() { return apiKey; }
    public void setApiKey(String apiKey) { this.apiKey = apiKey; }

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public boolean isLocalEnabled() { return localEnabled; }
    public void setLocalEnabled(boolean localEnabled) { this.localEnabled = localEnabled; }

    public int getMatchThreshold() { return matchThreshold; }
    public void setMatchThreshold(int matchThreshold) { this.matchThreshold = matchThreshold; }
}