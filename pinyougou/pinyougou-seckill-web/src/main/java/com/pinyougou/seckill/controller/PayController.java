package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
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
    private SeckillOrderService orderService;

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
            //1分钟内容每隔3秒查询
            int count = 0;
            while (true) {
                //1. 使用while(true)无限地在1分钟里面每隔3秒到微信支付系统查询订单的支付状态；
                Map<String, String> map = weixinPayService.queryPayStatus(outTradeNo);
                if (map == null) {
                   break;
                }
                if("SUCCESS".equals(map.get("result_code"))) {
                    if ("SUCCESS".equals(map.get("trade_state"))) {
                        //2. 如果支付成功；更保存订单到mysql；
                        orderService.saveOrderInRedisToDb(outTradeNo, map.get("transaction_id"));
                        result = Result.ok("支付成功");
                        break;
                    }
                } else {
                    result = Result.fail("支付失败");
                    break;
                }
                count++;
                if(count > 20){
                    //超过1分钟
                    result = Result.fail("支付超时");

                    //关闭微信那边的订单
                    Map<String, String> resultMap = weixinPayService.closeOrder(outTradeNo);
                    //如果在关闭订单的过程中，被用户支付了也是支付成功的返回结果
                    if(resultMap != null && "ORDERPAID".equals(resultMap.get("err_code"))){
                        orderService.saveOrderInRedisToDb(outTradeNo, map.get("transaction_id"));
                        result = Result.ok("支付成功");
                        break;
                    }

                    //删除redis中订单
                    orderService.deleteOrderInRedisByOutTradeNo(outTradeNo);

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
        //1. 根据订单id查询订单总金额；
        TbSeckillOrder seckillOrder = orderService.findSeckillOrderInRedisByOrderId(outTradeNo);
        if (seckillOrder != null) {
            //总金额
            String totalFee = (long)(seckillOrder.getMoney().doubleValue()*100) + "";

            //2. 调用支付系统的方法返回支付订单信息（totalFee,outTradeNo,result_code,code_url）
            return weixinPayService.createNative(outTradeNo, totalFee);
        }
        return new HashMap<>();
    }
}
