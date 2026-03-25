package com.pet.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.pet.config.AlipayConfig;
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

    @Override
    public String createPay(String orderNo, BigDecimal amount) throws AlipayApiException {
        System.out.println("创建支付宝支付，订单号: " + orderNo + ", 金额: " + amount);

        AlipayClient alipayClient = alipayConfig.getAlipayClient();
        AlipayTradePagePayRequest request = alipayConfig.createPayRequest(
                orderNo,
                amount.toString(),
                "宠物服务系统-商品购买",
                "购买宠物商品"
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
            // 更新订单支付状态
            orderMapper.updatePayStatus(orderNo, 1, 1, response.getTradeNo());
            return true;
        }
        return false;
    }
}