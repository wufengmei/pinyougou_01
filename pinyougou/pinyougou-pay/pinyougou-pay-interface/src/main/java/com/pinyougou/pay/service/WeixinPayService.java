package com.pinyougou.pay.service;

import java.util.Map;

public interface WeixinPayService {
    /**
     * 到微信支付系统根据交易号生成一个预支付订单并返回二维码链接地址等信息
     * @param outTradeNo 交易号
     * @param totalFee 总金额
     * @return 交易号，总金额，操作标识符，二维码链接地址
     */
    Map<String, String> createNative(String outTradeNo, String totalFee);

    /**
     * 据交易号到支付系统查询该交易的支付状态
     * @param outTradeNo 交易号
     * @return 支付订单信息
     */
    Map<String, String> queryPayStatus(String outTradeNo);

    /**
     * 根据订单号关闭微信那边的订单
     * @param outTradeNo 订单号
     * @return 操作结果
     */
    Map<String, String> closeOrder(String outTradeNo);
}
