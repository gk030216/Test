package com.pet.service;

import com.alipay.api.AlipayApiException;

import java.math.BigDecimal;

public interface AlipayService {

    /**
     * 创建支付宝支付
     */
    String createPay(String orderNo, BigDecimal amount) throws AlipayApiException;

    /**
     * 查询支付结果
     */
    boolean queryPayResult(String orderNo) throws AlipayApiException;

    /**
     * 支付宝退款
     */
    boolean refund(String orderNo, BigDecimal amount, String reason) throws AlipayApiException;

}