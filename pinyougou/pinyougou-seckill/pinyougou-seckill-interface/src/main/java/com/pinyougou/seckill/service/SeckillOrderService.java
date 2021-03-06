package com.pinyougou.seckill.service;

import com.pinyougou.pojo.SeckillOrderAndGood;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.PageResult;

import java.util.Map;

public interface SeckillOrderService extends BaseService<TbSeckillOrder> {

    PageResult search(Integer page, Integer rows, TbSeckillOrder seckillOrder);

    /**
     * 生成用户的秒杀订单保存到redis中
     * @param userId 用户id
     * @param seckillId 秒杀商品id
     * @return 秒杀订单id
     */
    String submitOrder(String userId, Long seckillId) throws InterruptedException;

    /**
     * 根据订单号查询在redis中的秒杀订单
     * @param orderId 订单号
     * @return 秒杀订单
     */
    TbSeckillOrder findSeckillOrderInRedisByOrderId(String orderId);

    /**
     * 将redis中的秒杀订单同步保存到mysql
     * @param outTradeNo 订单号
     * @param transaction_id 微信那边的订单号
     */
    void saveOrderInRedisToDb(String outTradeNo, String transaction_id);

    /**
     * 删除redis中的订单并将redis中的秒杀商品库存加1
     * @param outTradeNo 秒杀商品订单号
     */
    void deleteOrderInRedisByOutTradeNo(String outTradeNo) throws InterruptedException;

    Map<String, Object> findMySeckillOrder(String username);

    Map<String, Object> findMyOneSeckillOrder(Long id,String username);
    PageResult searchSeckillGoods(Integer page, Integer rows, SeckillOrderAndGood seckillOrderAndGood);

    void updateStatus(Long[] ids, String status);

    void updateEndTime(Long id);

    void updateCloseatus(Long[] ids, String status);
}