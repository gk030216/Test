package com.pet.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.pet.entity.SystemSetting;
import com.pet.mapper.SystemSettingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;

@Configuration
public class AlipayConfig {

    @Autowired
    private SystemSettingMapper settingMapper;

    // 默认配置（从数据库读取失败时使用）
    private String appId = "";
    private String appPrivateKey = "";
    private String alipayPublicKey = "";
    private String notifyUrl = "";
    private String returnUrl = "";
    private String signType = "RSA2";
    private String charset = "UTF-8";
    private String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private String format = "json";
    private Boolean payEnabled = true;

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
                    case "alipay_app_id":
                        this.appId = setting.getSettingValue();
                        break;
                    case "app_private_key":
                        this.appPrivateKey = setting.getSettingValue();
                        break;
                    case "alipay_public_key":
                        this.alipayPublicKey = setting.getSettingValue();
                        break;
                    case "notify_url":
                        this.notifyUrl = setting.getSettingValue();
                        break;
                    case "return_url":
                        this.returnUrl = setting.getSettingValue();
                        break;
                    case "pay_enabled":
                        this.payEnabled = "true".equals(setting.getSettingValue());
                        break;
                }
            }
            System.out.println("✅ 支付宝配置加载成功");
        } catch (Exception e) {
            System.err.println("❌ 支付宝配置加载失败，使用默认配置");
            e.printStackTrace();
        }
    }

    /**
     * 重新加载配置（供外部调用）
     */
    public void reloadConfig() {
        loadConfigFromDatabase();
    }

    // getter方法
    public String getAppId() { return appId; }
    public String getAppPrivateKey() { return appPrivateKey; }
    public String getAlipayPublicKey() { return alipayPublicKey; }
    public String getNotifyUrl() { return notifyUrl; }
    public String getReturnUrl() { return returnUrl; }
    public String getSignType() { return signType; }
    public String getCharset() { return charset; }
    public String getGatewayUrl() { return gatewayUrl; }
    public String getFormat() { return format; }
    public Boolean getPayEnabled() { return payEnabled; }

    /**
     * 获取支付宝客户端
     */
    public AlipayClient getAlipayClient() {
        return new DefaultAlipayClient(
                gatewayUrl,
                appId,
                appPrivateKey,
                format,
                charset,
                alipayPublicKey,
                signType
        );
    }

    /**
     * 创建支付请求
     */
    public AlipayTradePagePayRequest createPayRequest(String outTradeNo, String totalAmount, String subject, String body) {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(notifyUrl);
        request.setReturnUrl(returnUrl);

        String bizContent = "{" +
                "\"out_trade_no\":\"" + outTradeNo + "\"," +
                "\"total_amount\":\"" + totalAmount + "\"," +
                "\"subject\":\"" + subject + "\"," +
                "\"body\":\"" + body + "\"," +
                "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"" +
                "}";
        request.setBizContent(bizContent);
        return request;
    }

    /**
     * 创建退款请求
     */
    public AlipayTradeRefundRequest createRefundRequest(String outTradeNo, String refundAmount, String refundReason) {
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();

        String bizContent = "{" +
                "\"out_trade_no\":\"" + outTradeNo + "\"," +
                "\"refund_amount\":\"" + refundAmount + "\"," +
                "\"refund_reason\":\"" + (refundReason != null ? refundReason : "用户取消预约") + "\"" +
                "}";
        request.setBizContent(bizContent);
        return request;
    }
}