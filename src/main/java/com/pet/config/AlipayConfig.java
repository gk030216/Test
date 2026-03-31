package com.pet.config;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlipayConfig {

    // 应用ID，沙箱环境下的AppID
    private String appId = "9021000162618128";

    // 商户私钥，RSA2格式
    private String appPrivateKey = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQDHmC58mryamSUV9AcmoIZA9qFxvXLYt01uL7njuke5/VHbbNs28eQSauemqkX47x8TQV30Snq6r/b7JJSFRrfuDw30B0e5M0hiTQ8HEcJiouUcQqvK8Kxm5EgQobb0cAGwd+D87+SO2gYWo4kawZPXCiAOmrC2LCPoSyVjpqgQPHlL4J7rhPkztPiJQ46G2QUyRTpsIKGZD3HG2i0cmFsluVeKYPuaz5UAGmPaRMsiNcDZg2N4TCzqSgCcyTXnUzU5rN5a1MMGl3UdDhrYSZ76zULCMqdqHfJIQ5KHtTy6hzIN690sZAxqAjZDPSnWJ/oUAdSQxApFwh81Bm3O6RVRAgMBAAECggEBAMDNjaJKKwRACWYCe2Hh0K4yT9O6SJV1w/M7YRrNV/oe7RLEPaH+2ey8nhcOBleoTWFhVGvmAlYSIseJF3sCzCvHGRhwa+VH15U1kN2oVxjG+ABrmt2HzVH7FG4UeNH43Hjl5E0RQryHeTgnMwrIkZI5MqxI24iubOU4qd5wH4rF2zmfxbHxnY3EOE9XWnArVPvt2PtIrd5AQdd17c3UFKWQWYTVGzQk4mpat7k2oVyH4IbfZgnkOfq1W05UATUKfwm4sxuC4I5hvlIjossdwRG6WuztJnh3S9N1RGcoacSy6FMXt5i0DXu9bv1ed+qIy7Qs/uCznFUv2C7N0l+ZTA0CgYEA5Q0+j+3KubnTHSX56wdqPBPtmbDu77gQGNLyKmZJvSnaFL587S4H3FuNZz7Py4W1rrm13wl8gWlbOayF4oHM4Wbirru6Jx00UXdDpg0FVV2mmWHlLeT3TbqbK0FphH8KQlMa8WG5LFo9phq9hKmJjxQfvvk3coDvoiE1SWAVIJ8CgYEA3xO4a15YLyI0rneZ2BGmH8zR+ydf959TatwbAXatt4vMb8+nMGp9s5dTttszrD50/qBIRE4pHW79NIz5giFkZEO5yP1Fi7Ht97qA1+mKVVGVQ78hDl6R85ovYrUat6Q7Q+e7+8ygR56ozsLHg6fD8of9eQJ8Mv6UM11ZYoYIVA8CgYBwLrZvg8WAhVSyuADZbb5IQnJijdfYrMulfNuCjKqc7TyUYF+LQroy4Nw2rvrS8V/0fZxn6puCPnmhm6/PKkeQNfXcIrGfqpwPMlx9nW75Sm7oPJwkiDnfZf/NhbIKZLY/mbhedMBeyqvbCMm9idOHshGCVUkk2aWEQMN6doaifQKBgBWdRYsxsv2uJMia0g87dosyP05EfGnzyBNcjElSBHb6Vq6mwh5Q0ykpinT3PER+MLvTL+1eXWF6tZH37t0K+z0zyraMw8ZLUTFlosJI4JxvsTTFOiC4TQ/7aof34ETW5t8nfvFg3E7UMqsF+GrrQHMpdgfZk8slG5r2eoivT/NFAoGAHSMFIJ66PJbsAGk71pjmzw9TiqHZZMV77apa+CYLp6BnNyyjohFF6lRD4IuI8uUBJaaCaRw++mpuOKX+MZ/lVVWb6Zj7hWYoHAcs9AJlRu1yomUJ9ISrCOBLMEz1jQNOWij9YwaCt1G30juuYT8qvb630qsEBny4Zjqg+WRAfFU=";
    // 支付宝公钥，RSA2格式
    private String alipayPublicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA6K1r0k1/ZoYf/Z4uUDDNkdndgtE2nyRKesQauhI/Qi8RVyvG8+XUSQ06hMWXwpdGX5ItD+r2E/LOf3FLnZLC0R1yTpJHQ0kAPDfVEu4xwRpqJNZvwqmklajATemdL/sYuV1zIMktHwiTDWyDR5JrotgZEuXI7RPCxSaHK2UHhcmH6mhLX1vYVqa6C/wNr4Nq/p/rXiH+3nYMkLFb2uN5SE7jum/8msKZuokkr/TT1XxRge8MdSuclerbo0YvUWgcgpkbRjqsJ9cKrt4nm8fPvGo0xw7Ojo8EWVarqM4De5eUDTusF6aWWqJUpQkchZGXv04KDV4qYBXNHHbcPPvXRwIDAQAB";

    // 服务器异步通知页面路径 - 需要修改为正确的内网穿透地址 + /api/pay/alipay/notify
    private String notifyUrl = "https://35300ed3.r20.vip.cpolar.cn/api/pay/alipay/notify";

    // 页面跳转同步通知页面路径
    private String returnUrl = "http://localhost:8081/pay/return";

    // 签名方式
    private String signType = "RSA2";

    // 字符编码格式
    private String charset = "UTF-8";

    // 支付宝网关，沙箱环境
    private String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    // 格式
    private String format = "json";

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

    // 获取支付宝客户端
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

    // 创建支付请求
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
}