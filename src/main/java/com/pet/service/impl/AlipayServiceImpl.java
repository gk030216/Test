package com.pet.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.pet.config.AlipayConfig;
import com.pet.entity.Appointment;
import com.pet.entity.Order;
import com.pet.mapper.AppointmentMapper;
import com.pet.mapper.OrderMapper;
import com.pet.service.AlipayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class AlipayServiceImpl implements AlipayService {

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private AppointmentMapper appointmentMapper;

    @Override
    public String createPay(String orderNo, BigDecimal amount) throws AlipayApiException {
        System.out.println("========== 新版 AlipayServiceImpl ==========");
        System.out.println("创建支付宝支付，订单号: " + orderNo + ", 金额: " + amount);

        String subject;
        String body;

        // 根据订单号前缀判断类型
        if (orderNo.startsWith("AP")) {
            // 验证预约是否存在
            Appointment appointment = appointmentMapper.getByAppointmentNo(orderNo);
            if (appointment == null) {
                throw new RuntimeException("预约不存在");
            }
            subject = "宠物服务系统-服务预约";
            body = "预约宠物服务：" + appointment.getServiceName();
            System.out.println("服务预约支付，服务名: " + appointment.getServiceName());
        } else {
            // 验证商品订单是否存在
            Order order = orderMapper.getByOrderNo(orderNo);
            if (order == null) {
                throw new RuntimeException("订单不存在");
            }
            subject = "宠物服务系统-商品购买";
            body = "购买宠物商品";
            System.out.println("商品订单支付");
        }

        AlipayClient alipayClient = alipayConfig.getAlipayClient();
        AlipayTradePagePayRequest request = alipayConfig.createPayRequest(
                orderNo,
                amount.toString(),
                subject,
                body
        );

        AlipayTradePagePayResponse response = alipayClient.pageExecute(request);
        System.out.println("支付宝响应: " + response.getBody());

        if (response.isSuccess()) {
            return response.getBody();
        } else {
            throw new RuntimeException("支付宝支付创建失败：" + response.getMsg() + " " + response.getSubMsg());
        }
    }

    @Override
    public boolean queryPayResult(String orderNo) throws AlipayApiException {
        AlipayClient alipayClient = alipayConfig.getAlipayClient();

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        request.setBizContent("{\"out_trade_no\":\"" + orderNo + "\"}");

        AlipayTradeQueryResponse response = alipayClient.execute(request);

        if (response.isSuccess() && "TRADE_SUCCESS".equals(response.getTradeStatus())) {
            if (orderNo.startsWith("AP")) {
                appointmentMapper.updatePayStatus(orderNo, 1, response.getTradeNo(), new java.util.Date());
            } else {
                orderMapper.updatePayStatus(orderNo, 1, 1, response.getTradeNo());
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean refund(String orderNo, BigDecimal amount, String reason) throws AlipayApiException {
        System.out.println("========== 支付宝退款 ==========");
        System.out.println("订单号: " + orderNo);
        System.out.println("退款金额: " + amount);
        System.out.println("退款原因: " + reason);

        AlipayClient alipayClient = alipayConfig.getAlipayClient();
        AlipayTradeRefundRequest request = alipayConfig.createRefundRequest(orderNo, amount.toString(), reason);

        AlipayTradeRefundResponse response = alipayClient.execute(request);

        System.out.println("退款响应: " + response.getBody());

        if (response.isSuccess()) {
            System.out.println("✅ 退款成功");

            // 更新预约退款状态
            if (orderNo.startsWith("AP")) {
                appointmentMapper.updateRefundStatus(orderNo, 5); // status=5 已退款
            } else {
                orderMapper.updateOrderStatus(orderNo, 5);
            }
            return true;
        } else {
            System.out.println("❌ 退款失败: " + response.getMsg() + " " + response.getSubMsg());
            return false;
        }
    }
}