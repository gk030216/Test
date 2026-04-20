package com.pet.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.pet.config.AlipayConfig;
import com.pet.entity.Appointment;
import com.pet.entity.User;
import com.pet.mapper.AppointmentMapper;
import com.pet.mapper.UserMapper;
import com.pet.service.OrderService;
import com.pet.service.NotificationService;  // ✅ 新增
import com.pet.util.EmailUtil;
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

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private EmailUtil emailUtil;

    @Autowired
    private NotificationService notificationService;  // ✅ 新增

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

                    if (outTradeNo.startsWith("AP")) {
                        // 1. 更新预约支付状态
                        int updateResult = appointmentMapper.updatePayStatus(outTradeNo, 1, tradeNo, new Date());
                        result = updateResult > 0;

                        // 2. 支付成功后发送通知
                        if (result) {
                            Appointment appointment = appointmentMapper.getByAppointmentNo(outTradeNo);
                            if (appointment != null) {
                                // 获取用户信息
                                User user = userMapper.findById(appointment.getUserId());

                                // ✅ 发送邮件通知
                                if (user != null && user.getEmail() != null && !user.getEmail().isEmpty()) {
                                    emailUtil.sendAppointmentPaymentSuccess(
                                            user.getEmail(),
                                            user.getNickname() != null ? user.getNickname() : user.getUsername(),
                                            appointment.getServiceName(),
                                            appointment.getAppointmentDate(),
                                            appointment.getAppointmentTime(),
                                            appointment.getPetName(),
                                            appointment.getAppointmentNo()
                                    );
                                    System.out.println("✅ 支付成功邮件已发送至: " + user.getEmail());
                                }

                                // ✅ 发送站内消息通知
                                if (user != null) {
                                    notificationService.sendNotification(
                                            appointment.getUserId(),
                                            "appointment",
                                            "预约支付成功",
                                            "您的服务【" + appointment.getServiceName() + "】已支付成功，请等待商家确认。",
                                            "/personal/appointments"
                                    );
                                    System.out.println("✅ 站内消息已发送至用户: " + appointment.getUserId());
                                }
                            }
                        }

                        System.out.println("预约支付回调处理结果: " + (result ? "成功" : "失败"));
                    } else {
                        // 商品订单
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