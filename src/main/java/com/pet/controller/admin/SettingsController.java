package com.pet.controller.admin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pet.config.AIConfig;
import com.pet.config.AlipayConfig;
import com.pet.config.EmailConfig;
import com.pet.entity.SystemSetting;
import com.pet.mapper.SystemSettingMapper;
import com.pet.util.EmailUtil;
import com.pet.util.FileUploadUtil;
import com.pet.util.Result;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/settings")
@CrossOrigin(origins = "*")
public class SettingsController {

    @Autowired
    private SystemSettingMapper settingMapper;

    @Autowired
    private EmailUtil emailUtil;

    @Autowired
    private EmailConfig emailConfig;

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    private AIConfig aiConfig;

    @Autowired
    private FileUploadUtil fileUploadUtil;

    /**
     * 获取所有设置
     */
    @GetMapping
    public Result<Map<String, Object>> getSettings() {
        try {
            List<SystemSetting> list = settingMapper.findAll();
            Map<String, String> settingsMap = new HashMap<>();
            for (SystemSetting setting : list) {
                settingsMap.put(setting.getSettingKey(), setting.getSettingValue());
            }

            // 构建返回数据
            Map<String, Object> result = new HashMap<>();

            // 基础设置
            Map<String, Object> basic = new HashMap<>();
            basic.put("siteName", getValue(settingsMap, "site_name", "宠物服务系统"));
            basic.put("siteLogo", getValue(settingsMap, "site_logo", ""));
            basic.put("siteDesc", getValue(settingsMap, "site_desc", "用心服务每一个宠物家庭"));
            basic.put("siteKeywords", getValue(settingsMap, "site_keywords", "宠物,服务,美容,寄养,医疗"));
            basic.put("copyright", getValue(settingsMap, "copyright", "© 2026 宠物服务系统"));
            basic.put("icp", getValue(settingsMap, "icp", ""));
            basic.put("servicePhone", getValue(settingsMap, "service_phone", "400-888-6666"));
            basic.put("serviceEmail", getValue(settingsMap, "service_email", "service@petservice.com"));
            basic.put("maintenanceMode", "true".equals(getValue(settingsMap, "maintenance_mode", "false")));
            result.put("basic", basic);

            // 邮件配置
            Map<String, Object> email = new HashMap<>();
            email.put("smtpHost", getValue(settingsMap, "smtp_host", "smtp.qq.com"));
            email.put("smtpPort", getValue(settingsMap, "smtp_port", "465"));
            email.put("senderEmail", getValue(settingsMap, "sender_email", ""));
            email.put("senderPassword", getValue(settingsMap, "sender_password", ""));
            email.put("sslEnable", "true".equals(getValue(settingsMap, "ssl_enable", "true")));
            result.put("email", email);

            // 支付配置
            Map<String, Object> pay = new HashMap<>();
            pay.put("appId", getValue(settingsMap, "alipay_app_id", ""));
            pay.put("alipayPublicKey", getValue(settingsMap, "alipay_public_key", ""));
            pay.put("appPrivateKey", getValue(settingsMap, "app_private_key", ""));
            pay.put("notifyUrl", getValue(settingsMap, "notify_url", ""));
            pay.put("returnUrl", getValue(settingsMap, "return_url", ""));
            pay.put("payEnabled", "true".equals(getValue(settingsMap, "pay_enabled", "true")));
            result.put("pay", pay);

            // 上传配置
            Map<String, Object> upload = new HashMap<>();
            upload.put("uploadPath", getValue(settingsMap, "upload_path", "D:/pet_uploads/"));
            upload.put("maxSize", Double.parseDouble(getValue(settingsMap, "upload_max_size", "2")));
            String types = getValue(settingsMap, "upload_allowed_types", "jpg,jpeg,png,gif");
            upload.put("allowedTypes", types.split(","));
            upload.put("carouselLimit", Integer.parseInt(getValue(settingsMap, "carousel_limit", "5")));
            result.put("upload", upload);

            // AI配置
            Map<String, Object> ai = new HashMap<>();
            ai.put("provider", getValue(settingsMap, "ai_provider", "deepseek"));
            ai.put("apiKey", getValue(settingsMap, "ai_api_key", ""));
            ai.put("baseUrl", getValue(settingsMap, "ai_base_url", "https://api.deepseek.com"));
            ai.put("model", getValue(settingsMap, "ai_model", "deepseek-chat"));
            ai.put("matchThreshold", (int) Double.parseDouble(getValue(settingsMap, "ai_match_threshold", "30")));
            ai.put("localEnabled", "true".equals(getValue(settingsMap, "ai_local_enabled", "true")));
            result.put("ai", ai);

            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
    }

    private String getValue(Map<String, String> map, String key, String defaultValue) {
        return map.getOrDefault(key, defaultValue);
    }

    /**
     * 保存基础设置
     */
    @PutMapping("/basic")
    public Result<?> saveBasicSettings(@RequestBody Map<String, Object> data) {
        try {
            saveSetting("site_name", data.get("siteName"));
            saveSetting("site_logo", data.get("siteLogo"));
            saveSetting("site_desc", data.get("siteDesc"));
            saveSetting("site_keywords", data.get("siteKeywords"));
            saveSetting("copyright", data.get("copyright"));
            saveSetting("icp", data.get("icp"));
            saveSetting("service_phone", data.get("servicePhone"));
            saveSetting("service_email", data.get("serviceEmail"));
            saveSetting("maintenance_mode", String.valueOf(data.get("maintenanceMode")));
            return Result.success("保存成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 保存邮件配置
     */
    @PutMapping("/email")
    public Result<?> saveEmailSettings(@RequestBody Map<String, Object> data) {
        try {
            saveSetting("smtp_host", data.get("smtpHost"));
            saveSetting("smtp_port", data.get("smtpPort"));
            saveSetting("sender_email", data.get("senderEmail"));
            saveSetting("sender_password", data.get("senderPassword"));
            saveSetting("ssl_enable", String.valueOf(data.get("sslEnable")));

            // 重新加载邮件配置
            emailConfig.loadFromDatabase();

            return Result.success("保存成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 测试邮件配置
     */
    @PostMapping("/email/test")
    public Result<?> testEmailConfig(@RequestBody Map<String, Object> data) {
        try {
            String testEmail = (String) data.get("testEmail");
            if (testEmail == null || testEmail.isEmpty()) {
                return Result.error("请输入测试邮箱");
            }

            // 获取邮件配置参数
            String smtpHost = (String) data.get("smtpHost");
            String smtpPort = (String) data.get("smtpPort");
            String senderEmail = (String) data.get("senderEmail");
            String senderPassword = (String) data.get("senderPassword");
            Boolean sslEnable = (Boolean) data.get("sslEnable");

            // 验证配置完整性
            if (smtpHost == null || smtpHost.isEmpty()) {
                return Result.error("SMTP服务器不能为空");
            }
            if (senderEmail == null || senderEmail.isEmpty()) {
                return Result.error("发件邮箱不能为空");
            }
            if (senderPassword == null || senderPassword.isEmpty()) {
                return Result.error("授权码不能为空");
            }

            // 临时更新邮件配置
            emailUtil.updateConfig(smtpHost, smtpPort, senderEmail, senderPassword, sslEnable);

            // 发送测试邮件
            boolean success = emailUtil.sendTestEmail(testEmail);

            if (success) {
                return Result.success("测试邮件发送成功，请查收");
            } else {
                return Result.error("测试邮件发送失败，请检查配置");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error("发送失败：" + e.getMessage());
        }
    }

    /**
     * 保存支付配置
     */
    @PutMapping("/pay")
    public Result<?> savePaySettings(@RequestBody Map<String, Object> data) {
        try {
            saveSetting("alipay_app_id", data.get("appId"));
            saveSetting("alipay_public_key", data.get("alipayPublicKey"));
            saveSetting("app_private_key", data.get("appPrivateKey"));
            saveSetting("notify_url", data.get("notifyUrl"));
            saveSetting("return_url", data.get("returnUrl"));
            saveSetting("pay_enabled", String.valueOf(data.get("payEnabled")));

            // 重新加载支付宝配置
            alipayConfig.reloadConfig();

            return Result.success("保存成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 保存AI配置
     */
    @PutMapping("/ai")
    public Result<?> saveAISettings(@RequestBody Map<String, Object> data) {
        try {
            saveSetting("ai_provider", data.get("provider"));
            saveSetting("ai_api_key", data.get("apiKey"));
            saveSetting("ai_base_url", data.get("baseUrl"));
            saveSetting("ai_model", data.get("model"));
            saveSetting("ai_match_threshold", String.valueOf(data.get("matchThreshold")));
            saveSetting("ai_local_enabled", String.valueOf(data.get("localEnabled")));

            // 重新加载AI配置
            aiConfig.reloadConfig();

            return Result.success("保存成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 测试AI连接
     */
    @PostMapping("/ai/test")
    public Result<?> testAIConfig(@RequestBody Map<String, Object> data) {
        try {
            String apiKey = (String) data.get("apiKey");
            String baseUrl = (String) data.get("baseUrl");
            String model = (String) data.get("model");

            // 简单的连接测试
            boolean success = testAIConnection(apiKey, baseUrl, model);

            if (success) {
                return Result.success("AI服务连接正常");
            } else {
                return Result.error("连接失败，请检查配置");
            }
        } catch (Exception e) {
            return Result.error("连接失败：" + e.getMessage());
        }
    }

    private boolean testAIConnection(String apiKey, String baseUrl, String model) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(baseUrl + "/v1/chat/completions");
            httpPost.setHeader("Content-Type", "application/json");
            httpPost.setHeader("Authorization", "Bearer " + apiKey);

            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);

            JSONArray messages = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", "你好，这是一个测试消息");
            messages.add(userMessage);

            requestBody.put("messages", messages);
            requestBody.put("max_tokens", 10);

            httpPost.setEntity(new StringEntity(requestBody.toJSONString(), "UTF-8"));

            try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    String responseBody = EntityUtils.toString(response.getEntity(), "UTF-8");
                    JSONObject jsonResponse = JSON.parseObject(responseBody);
                    return jsonResponse.containsKey("choices");
                }
                return false;
            }
        } catch (Exception e) {
            System.err.println("AI连接测试失败: " + e.getMessage());
            return false;
        }
    }

    /**
     * 保存上传配置
     */
    @PutMapping("/upload")
    public Result<?> saveUploadSettings(@RequestBody Map<String, Object> data) {
        try {
            saveSetting("upload_path", data.get("uploadPath"));
            saveSetting("upload_max_size", String.valueOf(data.get("maxSize")));
            Object types = data.get("allowedTypes");
            if (types instanceof List) {
                saveSetting("upload_allowed_types", String.join(",", (List) types));
            }
            saveSetting("carousel_limit", String.valueOf(data.get("carouselLimit")));

            // 重新加载上传配置
            fileUploadUtil.reloadConfig();

            return Result.success("保存成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }


    private void saveSetting(String key, Object value) {
        if (value == null) return;

        SystemSetting setting = new SystemSetting();
        setting.setSettingKey(key);
        setting.setSettingValue(value.toString());
        settingMapper.saveOrUpdate(setting);
    }
}