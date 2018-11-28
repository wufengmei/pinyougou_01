package com.pinyougou.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.Cart;
import com.pinyougou.vo.Order;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Service(interfaceClass = OrderService.class)
public class OrderServiceImpl extends BaseServiceImpl<TbOrder> implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private PayLogMapper payLogMapper;

    @Autowired
    private GoodsMapper goodsMapper;
    @Autowired
    private OrderAndGoodMapper orderAndGoodMapper;


    @Override
    public PageResult search(Integer page, Integer rows, TbOrder order) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbOrder.class);
        Example.Criteria criteria = example.createCriteria();
        if(!StringUtils.isEmpty(order.getSellerId())){
            criteria.andLike("sellerId", "%" + order.getSellerId() + "%");
        }

        List<TbOrder> list = orderMapper.selectByExample(example);
        PageInfo<TbOrder> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public String addOrder(TbOrder order) {
        //支付日志id
        String outTradeNo = "";
        //1、查询购物车中的所有商品
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("CART_LIST").get(order.getUserId());
        if(cartList != null && cartList.size() > 0) {
            //2、遍历购物车列表根据每一个购物车对象cart生成一个订单
            //支付的总金额
            double totalFee = 0.0;
            //本次支付里面包含那些订单;使用,隔开
            String orderIds = "";

            for (Cart cart : cartList) {
                TbOrder tbOrder = new TbOrder();
                tbOrder.setOrderId(idWorker.nextId());
                //未支付
                tbOrder.setStatus("0");
                tbOrder.setCreateTime(new Date());
                tbOrder.setUpdateTime(tbOrder.getCreateTime());
                tbOrder.setSellerId(cart.getSellerId());
                //本笔订单总金额 = 在这个商家里面买的所有商品的单价*购买数量
                double totalPayment = 0.0;

                //2.1、遍历订单商品列表生成一个个的订单明细
                for (TbOrderItem orderItem : cart.getOrderItemList()) {
                    orderItem.setId(idWorker.nextId());
                    orderItem.setOrderId(tbOrder.getOrderId());

                    //保存订单明细
                    orderItemMapper.insertSelective(orderItem);

                    totalPayment += orderItem.getTotalFee().doubleValue();
                }

                //本笔订单总金额
                tbOrder.setPayment(new BigDecimal(totalPayment));

                //本次支付总金额
                totalFee += tbOrder.getPayment().doubleValue();

                //保存订单
                orderMapper.insertSelective(tbOrder);

                if (orderIds.length() > 0) {
                    orderIds += "," + tbOrder.getOrderId();
                } else {
                    orderIds = tbOrder.getOrderId().toString();
                }
            }

            //3、如果是微信支付的话那么要生成支付日志
            if ("1".equals(order.getPaymentType())) {
                TbPayLog payLog = new TbPayLog();
                outTradeNo = idWorker.nextId() + "";
                payLog.setOutTradeNo(outTradeNo);
                //未支付
                payLog.setTradeState("0");
                payLog.setCreateTime(new Date());
                payLog.setUserId(order.getUserId());
                //支付类型 微信支付
                payLog.setPayType(order.getPaymentType());
                //本次要支付的总金额 = 所有订单的总金额 ，微信那边要求是长整型，精确到分
                payLog.setTotalFee((long)(totalFee*100));

                //本次支付里面包含那些订单;使用,隔开
                payLog.setOrderList(orderIds);

                payLogMapper.insertSelective(payLog);
            }
            //4、清空当前用户的购物车数据
            redisTemplate.boundHashOps("CART_LIST").delete(order.getUserId());
        }
        //5、返回支付日志id；只有微信支付返回id，如果是货到付款则返回空字符串
        return outTradeNo;
    }

    @Override
    public TbPayLog findPayLogByOutTradeNo(String outTradeNo) {
        return payLogMapper.selectByPrimaryKey(outTradeNo);
    }

    @Override
    public void updateOrderStatus(String outTradeNo, String transaction_id) {
        //1、根据支付日志id查询支付日志；
        TbPayLog payLog = findPayLogByOutTradeNo(outTradeNo);
        //2、更新支付日志的状态和支付时间及在微信中的订单号；
        //已支付
        payLog.setTradeState("1");
        payLog.setPayTime(new Date());
        payLog.setTransactionId(transaction_id);
        payLogMapper.updateByPrimaryKeySelective(payLog);

        //3、对支付日志对应的所有订单的状态都修改为已支付；
        //update  tb_order set status="2"  where order_id=in(?,?,?....)
        String[] orderIds = payLog.getOrderList().split(",");

        TbOrder order = new TbOrder();
        order.setStatus("2");
        order.setPaymentTime(new Date());

        Example example = new Example(TbOrder.class);
        example.createCriteria().andIn("orderId", Arrays.asList(orderIds));

        orderMapper.updateByExampleSelective(order, example);
    }

    /**
     * 查询商家对应的所有订单列表
     * @param page 第几页
     * @param rows 页大小
     * @param orderAndGood 订单条件
     * @return  订单列表和总页数
     */
    @Override
    public PageResult searchByUsername(Integer page, Integer rows, OrderAndGood orderAndGood) {
        // 分页
        PageHelper.startPage(page,rows);
        List<OrderAndGood> list = orderAndGoodMapper.findOrderAndGood(orderAndGood);

        PageInfo<OrderAndGood> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }
}
