package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.common.util.HttpClient;
import com.pinyougou.pay.service.WeixinPayService;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.Map;

@Service
public class WeixinPayServiceImpl implements WeixinPayService {

    @Value("${appid}")
    private String appid;
    @Value("${partner}")
    private String partner;
    @Value("${partnerkey}")
    private String partnerkey;
    @Value("${notifyurl}")
    private String notifyurl;

    @Override
    public Map<String, String> createNative(String outTradeNo, String totalFee) {
        Map<String, String> resultMap = new HashMap<>();
        try {
            //1. 封装提交到微信的参数；
            Map<String, String> paramMap = new HashMap<>();
            //公众账号ID
            paramMap.put("appid", appid);
            //商户号
            paramMap.put("mch_id", partner);
            //随机字符串；可以使用微信提供的工具类生成
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            //签名；可以在组装的时候统一由工具类生成
            //paramMap.put("sign", null);
            //商品描述
            paramMap.put("body", "品优购-大二班");
            //商户订单号
            paramMap.put("out_trade_no", outTradeNo);
            //标价金额
            paramMap.put("total_fee", totalFee);
            //终端IP
            paramMap.put("spbill_create_ip", "127.0.0.1");
            //通知地址
            paramMap.put("notify_url", notifyurl);
            //交易类型
            paramMap.put("trade_type", "NATIVE");

            //2. 转换并生成签名了的数据；（可以利用微信的工具类）
            String signedXml = WXPayUtil.generateSignedXml(paramMap, partnerkey);
            System.out.println("发送到微信 统一下单 的数据为：" + signedXml);
            
            //3. 发送统一下单（https://api.mch.weixin.qq.com/pay/unifiedorder）的请求到微信支付系统；
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(signedXml);
            httpClient.post();

            //处理结果
            String content = httpClient.getContent();
            System.out.println("发送到微信 统一下单 的返回数据为：" + content);

            Map<String, String> map = WXPayUtil.xmlToMap(content);

            resultMap.put("outTradeNo", outTradeNo);
            resultMap.put("totalFee", totalFee);
            resultMap.put("result_code", map.get("result_code"));
            resultMap.put("code_url", map.get("code_url"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        //4. 返回结果
        return resultMap;
    }

    @Override
    public Map<String, String> queryPayStatus(String outTradeNo) {
        try {
            //1. 封装提交到微信的参数；
            Map<String, String> paramMap = new HashMap<>();
            //公众账号ID
            paramMap.put("appid", appid);
            //商户号
            paramMap.put("mch_id", partner);
            //随机字符串；可以使用微信提供的工具类生成
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            //签名；可以在组装的时候统一由工具类生成
            //paramMap.put("sign", null);
            //商户订单号
            paramMap.put("out_trade_no", outTradeNo);

            //2. 转换并生成签名了的数据；（可以利用微信的工具类）
            String signedXml = WXPayUtil.generateSignedXml(paramMap, partnerkey);
            System.out.println("发送到微信 统一下单 的数据为：" + signedXml);

            //3. 发送查询订单（https://api.mch.weixin.qq.com/pay/orderquery）的请求到微信支付系统；
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(signedXml);
            httpClient.post();

            //处理结果
            String content = httpClient.getContent();
            System.out.println("发送到微信 查询订单 的返回数据为：" + content);

            return WXPayUtil.xmlToMap(content);

        } catch (Exception e) {
            e.printStackTrace();
        }
        //4. 返回结果
        return null;
    }

    @Override
    public Map<String, String> closeOrder(String outTradeNo) {
        try {
            //1. 封装提交到微信的参数；
            Map<String, String> paramMap = new HashMap<>();
            //公众账号ID
            paramMap.put("appid", appid);
            //商户号
            paramMap.put("mch_id", partner);
            //随机字符串；可以使用微信提供的工具类生成
            paramMap.put("nonce_str", WXPayUtil.generateNonceStr());
            //签名；可以在组装的时候统一由工具类生成
            //paramMap.put("sign", null);
            //商户订单号
            paramMap.put("out_trade_no", outTradeNo);

            //2. 转换并生成签名了的数据；（可以利用微信的工具类）
            String signedXml = WXPayUtil.generateSignedXml(paramMap, partnerkey);
            System.out.println("发送到微信 关闭下单 的数据为：" + signedXml);

            //3. 发送关闭订单（https://api.mch.weixin.qq.com/pay/closeorder）的请求到微信支付系统；
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/closeorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(signedXml);
            httpClient.post();

            //处理结果
            String content = httpClient.getContent();
            System.out.println("发送到微信 关闭订单 的返回数据为：" + content);

            return WXPayUtil.xmlToMap(content);

        } catch (Exception e) {
            e.printStackTrace();
        }
        //4. 返回结果
        return null;

    }
}
