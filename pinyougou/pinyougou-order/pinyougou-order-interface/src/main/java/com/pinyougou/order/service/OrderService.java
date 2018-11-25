package com.pinyougou.order.service;

import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.PageResult;

public interface OrderService extends BaseService<TbOrder> {

    PageResult search(Integer page, Integer rows, TbOrder order);

    /**
     * 保存并生成订单、明细、支付日志到数据库中
     * @param order 订单基本信息
     * @return 如果是微信支付则需要返回支付日志id,否则为空
     */
    String addOrder(TbOrder order);

    /**
     * 根据支付日志id查询支付日志
     * @param outTradeNo 支付日志id
     * @return 支付日志id
     */
    TbPayLog findPayLogByOutTradeNo(String outTradeNo);

    /**
     * 根据支付日志id更新支付日志及其所有订单的支付状态为已支付
     * @param outTradeNo 支付日志id
     * @param transaction_id 微信订单号
     */
    void updateOrderStatus(String outTradeNo, String transaction_id);
}