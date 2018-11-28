package com.pinyougou.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.common.util.RedisLock;
import com.pinyougou.mapper.SeckillGoodsMapper;
import com.pinyougou.mapper.SeckillOrderAndGoodMapper;
import com.pinyougou.mapper.SeckillOrderMapper;
import com.pinyougou.pojo.SeckillOrderAndGood;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service(interfaceClass = SeckillOrderService.class)
public class SeckillOrderServiceImpl extends BaseServiceImpl<TbSeckillOrder> implements SeckillOrderService {

    //秒杀订单在redis中key的名称
    private static final String SECKILL_ORDERS = "SECKILL_ORDERS";
    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private SeckillOrderAndGoodMapper seckillOrderAndGoodMapper;

    @Override
    public PageResult search(Integer page, Integer rows, TbSeckillOrder seckillOrder) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbSeckillOrder.class);
        Example.Criteria criteria = example.createCriteria();
        /*if(!StringUtils.isEmpty(seckillOrder.get***())){
            criteria.andLike("***", "%" + seckillOrder.get***() + "%");
        }*/

        List<TbSeckillOrder> list = seckillOrderMapper.selectByExample(example);
        PageInfo<TbSeckillOrder> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public String submitOrder(String userId, Long seckillId) throws InterruptedException {
        //添加分布式锁，锁定当前购买的商品
        RedisLock redisLock = new RedisLock(redisTemplate);
        if(redisLock.lock(seckillId.toString())) {
            //1、获取秒杀商品
            TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps(SeckillGoodsServiceImpl.SECKILL_GOODS).get(seckillId);
            //1.1、判断商品是否存在
            if (seckillGoods == null) {
                throw new RuntimeException("秒杀商品不存在");
            }
            //1.2、判断库存是否大于0
            if (seckillGoods.getStockCount() == 0) {
                throw new RuntimeException("商品已经被抢购完...");
            }
            //2、扣减当前秒杀商品的库存量，减1
            seckillGoods.setStockCount(seckillGoods.getStockCount()-1);
            if(seckillGoods.getStockCount() > 0) {
                //2.1、如果扣减1之后库存量大于0的话；更新redis中的秒杀商品
                redisTemplate.boundHashOps(SeckillGoodsServiceImpl.SECKILL_GOODS).put(seckillId, seckillGoods);
            } else {
                //2.2、如果扣减1之后库存量等于0的话；将秒杀商品数据同步回mysql，再将redis中的秒杀商品删除
                seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);

                redisTemplate.boundHashOps(SeckillGoodsServiceImpl.SECKILL_GOODS).delete(seckillId);
            }
            //释放分布式锁
            redisLock.unlock(seckillId.toString());

            //3、生成秒杀订单；保存到redis
            TbSeckillOrder seckillOrder = new TbSeckillOrder();
            seckillOrder.setId(idWorker.nextId());
            //未付款
            seckillOrder.setStatus("0");
            seckillOrder.setSeckillId(seckillGoods.getId());
            seckillOrder.setMoney(seckillGoods.getCostPrice());
            seckillOrder.setSellerId(seckillGoods.getSellerId());
            seckillOrder.setUserId(userId);
            seckillOrder.setCreateTime(new Date());
            System.out.println(seckillOrder.getSeckillId());

            //保存到redis
            redisTemplate.boundHashOps(SECKILL_ORDERS).put(seckillOrder.getId().toString(), seckillOrder);

            //4、返回秒杀订单号
            System.out.println("订单编号---"+seckillOrder.getId());
            return seckillOrder.getId().toString();
        }
        return null;
    }

    @Override
    public TbSeckillOrder findSeckillOrderInRedisByOrderId(String orderId) {
        return (TbSeckillOrder) redisTemplate.boundHashOps(SECKILL_ORDERS).get(orderId);
    }

    @Override
    public void saveOrderInRedisToDb(String outTradeNo, String transaction_id) {
        //1、获取redis中的订单
        TbSeckillOrder seckillOrder = findSeckillOrderInRedisByOrderId(outTradeNo);
        //2、更新订单的支付信息
        seckillOrder.setPayTime(new Date());
        //已支付
        seckillOrder.setStatus("1");
        seckillOrder.setTransactionId(transaction_id);
        //3、保存到mysql
        seckillOrderMapper.insertSelective(seckillOrder);
        //4、删除redis中订单
        redisTemplate.boundHashOps(SECKILL_ORDERS).delete(outTradeNo);
    }

    @Override
    public void deleteOrderInRedisByOutTradeNo(String outTradeNo) throws InterruptedException {
        //1、根据订单号获取到redis中的订单
        TbSeckillOrder seckillOrder = findSeckillOrderInRedisByOrderId(outTradeNo);
        //添加分布式锁
        RedisLock redisLock = new RedisLock(redisTemplate);
        if(redisLock.lock(seckillOrder.getSeckillId().toString())) {
            //2、根据商品id获取在redis中的秒杀商品
            TbSeckillGoods seckillGoods = (TbSeckillGoods) redisTemplate.boundHashOps(SeckillGoodsServiceImpl.SECKILL_GOODS).get(seckillOrder.getSeckillId());
            //2.1、如果秒杀商品不存在则从mysql中查询
            if (seckillGoods == null) {
                seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillOrder.getSeckillId());
            }
            //2.2、库存加1并更新回redis
            seckillGoods.setStockCount(seckillGoods.getStockCount()+1);

            redisTemplate.boundHashOps(SeckillGoodsServiceImpl.SECKILL_GOODS).put(seckillGoods.getId(), seckillGoods);

            //释放分布式锁
            redisLock.unlock(seckillOrder.getSeckillId().toString());

            //3、删除redis中的订单
            redisTemplate.boundHashOps(SECKILL_ORDERS).delete(outTradeNo);
        }
    }

    /**
     * 查询秒杀订单
     * @param username
     * @return
     */
    @Override
    public Map<String, Object> findMySeckillOrder(String username) {
        Map<String, Object> resultMap = new HashMap<>();

        // 登陆用户的秒杀的订单数组
        List<TbSeckillOrder> seckillOrderList = new ArrayList<>();

        // 根据用户名获取Mysql数据库的相应的秒杀订单列表
        TbSeckillOrder seckillOrder = new TbSeckillOrder();
        seckillOrder.setUserId(username);
        // 判断该用户在MySQL数据库是否为空
        if (seckillOrderMapper.select(seckillOrder) != null && seckillOrderMapper.select(seckillOrder).size()>0){
             seckillOrderList = seckillOrderMapper.select(seckillOrder);
        }

        // 获取redis的所有的秒杀订单
        List<TbSeckillOrder> values = redisTemplate.boundHashOps(SECKILL_ORDERS).values();
        Map<String, Object> mySeckillOrderFromMyRedis = new HashMap<>();
        // 根据用户名获取redis的相应的秒杀订单列表
        if (values != null && values.size()>0){
            for (TbSeckillOrder tbSeckillOrder : values) {
                if (username.equals(tbSeckillOrder.getUserId())){
                    seckillOrderList.add(tbSeckillOrder);
                }
            }
        }

        if (seckillOrderList != null && seckillOrderList.size()>0){
            // 获取所有的订单详情信息
            resultMap = findMySeckillOrder(seckillOrderList);
        }

        // 返回结果
        return  resultMap;
    }

    public Map<String,Object> findMySeckillOrder(List<TbSeckillOrder> seckillOrderList){
        Map<String, Object> resultMap = new HashMap<>();

        // 订单集合，每个订单是一个map(一个是订单，一个是该订单对应的秒杀商品)
        List<Map<String,Object>> list = new ArrayList<>();
        // 遍历每一个秒杀订单
        for (TbSeckillOrder seckillOrder : seckillOrderList) {
            Map<String,Object> map = new HashMap<>();
            // 设置订单编号
            map.put("id",seckillOrder);
            // 根据秒杀订单id,获取秒杀商品
            TbSeckillGoods seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillOrder.getSeckillId());
            map.put("seckillId",seckillGoods);
            map.put("outTradeNo",seckillOrder.getId().toString());
            list.add(map);
        }
        resultMap.put("rows",list);
        // 页大小为3，计算总页数
        resultMap.put("totalPages",Math.ceil(list.size()/3.0));

        // 返回秒杀商品的订单详情列表和总页数
        return resultMap;
    }

    /**
     * 获取某个订单的详情信息
     * @return
     * @param id 订单编号
     */
    @Override
    public Map<String, Object> findMyOneSeckillOrder(Long id,String username) {
        Map<String, Object> resultMap = new HashMap<>();

        // 先查询数据库
        TbSeckillOrder seckillOrder = seckillOrderMapper.selectByPrimaryKey(id);
            if (seckillOrder != null){
                // 校验用户名
                if (isUser(username,resultMap,seckillOrder)){
                    return resultMap;
                }else {
                    throw new RuntimeException("非法访问");
                }

            }else {
                // 去redis查找
                TbSeckillOrder seckillOrder1 = findSeckillOrderInRedisByOrderId(id+"");
                if (seckillOrder1 != null){
                    // 校验用户名
                    if (isUser(username,resultMap,seckillOrder1)){
                        return resultMap;
                    }else {
                        throw new RuntimeException("非法访问");
                    }
                }
            }
        return null;
    }

    // 校验用户名
    public boolean isUser(String username,Map<String, Object> resultMap,TbSeckillOrder seckillOrder){
        if (username.equals(seckillOrder.getUserId())){
            // 根据秒杀商品id获取秒杀商品
            TbSeckillGoods seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillOrder.getSeckillId());
            resultMap.put("id",seckillOrder);
            resultMap.put("seckillId",seckillGoods);
            return true;
        }else {
            return false;
        }
    }

    @Override
    public PageResult searchSeckillGoods(Integer page, Integer rows, SeckillOrderAndGood seckillOrderAndGood) {
        // 分页
        PageHelper.startPage(page,rows);
        List<SeckillOrderAndGood> list = seckillOrderAndGoodMapper.findSeckillOrderAndGood(seckillOrderAndGood);
        if (list != null && list.size()>0){
            for (SeckillOrderAndGood seckillOrderAndGood1 : list) {
                seckillOrderAndGood1.setOrderId(seckillOrderAndGood1.getId().toString());
            }
        }

        PageInfo<SeckillOrderAndGood> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public void updateStatus(Long[] ids, String status) {
        //update tb_goods set audit_status=? where id in (?,?...)


        TbSeckillOrder seckillOrder = new TbSeckillOrder();
        seckillOrder.setStatus(status);
        // 更新收货时间
        seckillOrder.setConsignTime(new Date());


        Example example = new Example(TbSeckillOrder.class);
        example.createCriteria().andIn("id", Arrays.asList(ids));

        //参数1：更新的内容也就是对应在update语句中的set
        //参数2：更新条件对应where子句
        seckillOrderMapper.updateByExampleSelective(seckillOrder, example);
    }
}
