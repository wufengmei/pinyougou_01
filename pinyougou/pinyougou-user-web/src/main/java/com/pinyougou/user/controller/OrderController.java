package com.pinyougou.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pay.service.WeixinPayService;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.pojo.TbPayLog;
import com.pinyougou.vo.OrderVo;
import com.pinyougou.vo.PageResult;
import com.pinyougou.vo.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/order")
@RestController
public class OrderController {

    @Reference
    private WeixinPayService weixinPayService;
    @Reference
    private OrderService orderService;

    @RequestMapping("/findAll")
    public List<TbOrder> findAll() {
        return orderService.findAll();
    }

    @GetMapping("/findPage")
    public PageResult findPage(@RequestParam(value = "page", defaultValue = "1")Integer page,
                               @RequestParam(value = "rows", defaultValue = "10")Integer rows) {
        return orderService.findPage(page, rows);
    }

    /**
     * 保存并生成订单、明细、支付日志到数据库中
     * @param order 订单基本信息
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result add(@RequestBody TbOrder order) {
        try {
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            order.setUserId(userId);
            //订单下单来源 PC端
            order.setSourceType("2");
            String outTradeNo = orderService.addOrder(order);
            return Result.ok(outTradeNo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("下单失败");
    }

    @GetMapping("/findOne")
    public TbOrder findOne(Long id) {
        return orderService.findOne(id);
    }

    @PostMapping("/update")
    public Result update(@RequestBody TbOrder order) {
        try {
            orderService.update(order);
            return Result.ok("修改成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("修改失败");
    }

    @GetMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            orderService.deleteByIds(ids);
            return Result.ok("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("删除失败");
    }

    /**
     * 分页查询列表
     * @param order 查询条件
     * @param page 页号
     * @param rows 每页大小
     * @return
     */
    @PostMapping("/search")
    public PageResult search(@RequestBody  TbOrder order, @RequestParam(value = "page", defaultValue = "1")Integer page,
                               @RequestParam(value = "rows", defaultValue = "10")Integer rows) {
        return orderService.search(page, rows, order);
    }

    @GetMapping("/findOrderList")
    public String findOrderList(){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        List<TbOrder> orderList = orderService.findOrderListByUsername(username);


            for (TbOrder tbOrder : orderList) {
                List<TbOrderItem> orderItemList = orderService.findOrderItemListByOrderId(tbOrder.getOrderId());
                tbOrder.setOrderItemList(orderItemList);

            }

        return JSON.toJSONString(orderList, SerializerFeature.WriteNonStringValueAsString);
    }
    @GetMapping("/findOrderItemById")
    public String findOrderItemById(String orderItemId){
        TbOrderItem tbOrderItem = orderService.findOrderItemById(orderItemId);
        return JSON.toJSONString(tbOrderItem, SerializerFeature.WriteNonStringValueAsString);
    }

    @GetMapping("/findOrderById")
    public String findOrderById(String orderId){
        TbOrder order = orderService.findOrderById(orderId);
        OrderVo orderVo = new OrderVo();
        orderVo.setTbOrder(order);
        //获取create时间
        Date createTime = order.getCreateTime();
        String dateYmd = null;
        String dateHms = null;
        if (createTime!=null) {
            //设置格式化时间
            dateYmd = getDateYmd(createTime);
            orderVo.setCreateTime(dateYmd);
            dateHms = getDateHms(createTime);
            orderVo.setCreateTime1(dateHms);
        }
        //获取payment时间
        Date paymentTime = order.getPaymentTime();
        if (paymentTime!=null) {
            //设置格式化时间
            String dateYmd1 = getDateYmd(paymentTime);
            orderVo.setPaymentTime(dateYmd1);
            String dateHms1 = getDateHms(paymentTime);
            orderVo.setPaymentTime1(dateHms1);
        }
        List<TbOrderItem> orderItemList = orderService.findOrderItemListByOrderId(order.getOrderId());
        orderVo.setOrderItemList(orderItemList);
        return JSON.toJSONString(orderVo, SerializerFeature.WriteNonStringValueAsString);
    }
    public String getDateYmd(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String dateString = formatter.format(date);
        return dateString;
    }
    public String getDateHms(Date date){
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        String dateString = formatter.format(date);
        return dateString;
    }
    @GetMapping("/findOutTradeNo")
    public Map<String, String> findOutTradeNo(String orderId){
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(orderId);
        TbPayLog payLog = orderService.findOutTradeNo(orderId);



            //总金额

            if (payLog!=null){
                String totalFee = payLog.getTotalFee()+"";


            //2. 调用支付系统的方法返回支付订单信息（totalFee,outTradeNo,result_code,code_url）
            Map<String, String> aNative = weixinPayService.createNative(payLog.getOutTradeNo(),totalFee);
            return aNative;
            }

        return new HashMap<>();
    }

}
