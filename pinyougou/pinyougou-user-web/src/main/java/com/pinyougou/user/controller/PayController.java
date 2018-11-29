package com.pinyougou.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.vo.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/pay")
@RestController
public class PayController {

    @Reference
    private OrderService orderService;

    @Reference
    private WeixinPayService weixinPayService;

    /**
     * 根据交易号到支付系统查询该交易的支付状态
     * @param outTradeNo 交易号
     * @return 操作结果
     */
    @GetMapping("/queryPayStatus")
    public Result queryPayStatus(String outTradeNo){
        Result result = Result.fail("查询支付状态失败");
        try {
            //3分钟内容每隔3秒查询
            int count = 0;
            while (true) {
                //1. 使用while(true)无限地在3分钟里面每隔3秒到微信支付系统查询订单的支付状态；
                Map<String, String> map = weixinPayService.queryPayStatus(outTradeNo);
                if (map == null) {
                   break;
                }
                if("SUCCESS".equals(map.get("result_code"))) {
                    if ("SUCCESS".equals(map.get("trade_state"))) {
                        //2. 如果支付成功；更新订单及其支付日志的支付状态为已支付；
                        orderService.updateOrderStatus(outTradeNo, map.get("transaction_id"));
                        result = Result.ok("支付成功");
                        break;
                    }
                } else {
                    result = Result.fail("支付失败");
                    break;
                }
                count++;
                if(count > 60){
                    //超过3分钟
                    result = Result.fail("支付超时");
                    break;
                }
                //每隔3秒
                Thread.sleep(3000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //3. 返回操作结果
        return result;
    }

    /**
     * 根据交易号获取支付二维码地址等信息（交易号，总金额，操作标识符，二维码链接地址）
     * @param outTradeNo 交易号（支付日志id）
     * @return 交易号，总金额，操作标识符，二维码链接地址
     */
    @GetMapping("/createNative")
    public Map<String, String> createNative(String outTradeNo){
        //1. 根据支付日志id查询支付日志获取总金额；
        TbPayLog payLog = orderService.findPayLogByOutTradeNo(outTradeNo);
        if (payLog != null) {
            //总金额
            String totalFee = payLog.getTotalFee() + "";
            //2. 调用支付系统的方法返回支付订单信息（totalFee,outTradeNo,result_code,code_url）
            return weixinPayService.createNative(outTradeNo, totalFee);
        }
        return new HashMap<>();
    }
}
