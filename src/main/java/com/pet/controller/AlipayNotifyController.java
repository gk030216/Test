package com.pet.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.pet.config.AlipayConfig;
import com.pet.mapper.AppointmentMapper;
import com.pet.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/pay/alipay")
public class AlipayNotifyController {

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        try {
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (String name : requestParams.keySet()) {
                String[] values = requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
                }
                params.put(name, valueStr);
            }

            System.out.println("========== 支付宝回调开始 ==========");
            System.out.println("回调参数: " + params);

            boolean signVerified = AlipaySignature.rsaCheckV1(
                    params,
                    alipayConfig.getAlipayPublicKey(),
                    alipayConfig.getCharset(),
                    alipayConfig.getSignType()
            );

            System.out.println("签名验证结果: " + signVerified);

            if (signVerified) {
                String outTradeNo = params.get("out_trade_no");
                String tradeNo = params.get("trade_no");
                String tradeStatus = params.get("trade_status");

                System.out.println("订单号: " + outTradeNo + ", 交易号: " + tradeNo + ", 状态: " + tradeStatus);

                if ("TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus)) {
                    boolean result;

                    // 根据订单号前缀判断类型
                    if (outTradeNo.startsWith("AP")) {
                        // 服务预约：更新预约支付状态
                        int updateResult = appointmentMapper.updatePayStatus(outTradeNo, 1, tradeNo, new Date());
                        result = updateResult > 0;
                        System.out.println("预约支付回调处理结果: " + (result ? "成功" : "失败"));
                    } else {
                        // 商品订单：调用订单服务处理
                        result = orderService.handlePayCallback(outTradeNo, tradeNo);
                        System.out.println("商品订单支付回调处理结果: " + (result ? "成功" : "失败"));
                    }

                    return result ? "success" : "failure";
                }
                return "failure";
            } else {
                System.out.println("签名验证失败");
                return "failure";
            }
        } catch (AlipayApiException e) {
            System.err.println("支付宝回调异常: " + e.getMessage());
            e.printStackTrace();
            return "failure";
        } catch (Exception e) {
            System.err.println("处理回调异常: " + e.getMessage());
            e.printStackTrace();
            return "failure";
        } finally {
            System.out.println("========== 支付宝回调结束 ==========");
        }
    }
}