package com.pinyougou.seckill.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.common.util.IdWorker;
import com.pinyougou.common.util.RedisLock;
import com.pinyougou.mapper.SeckillGoodsMapper;
import com.pinyougou.mapper.SeckillOrderMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
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
            seckillOrder.setReceiverMobile(idWorker.nextId()+"");
            System.out.println(seckillOrder.getSeckillId());

            //保存到redis
            redisTemplate.boundHashOps(SECKILL_ORDERS).put(seckillOrder.getId().toString(), seckillOrder);

            //4、返回秒杀订单号
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
    public Map<Object, Object> findMySeckillOrder(String username) {
        Map<Object, Object> resultMap = new HashMap<>();

        // 根据用户名获取Mysql数据库的相应的秒杀订单列表
        TbSeckillOrder seckillOrder = new TbSeckillOrder();
        seckillOrder.setUserId(username);
        // 判断该用户在MySQL数据库是否为空
        if (seckillOrderMapper.select(seckillOrder) != null && seckillOrderMapper.select(seckillOrder).size()>0){
            List<TbSeckillOrder> seckillOrderList = seckillOrderMapper.select(seckillOrder);
            // 获取map
            Map<String, Object> mySeckillOrderFromMySql = findMySeckillOrderFromMySql(seckillOrderList);
            resultMap.put("mysql",mySeckillOrderFromMySql);

        }


        // 获取redis的所有的秒杀订单

        List<TbSeckillOrder> values = redisTemplate.boundHashOps(SECKILL_ORDERS).values();
        List<Map<String,Object>> list = new ArrayList<>();
        Map<String, Object> mySeckillOrderFromMyRedis = new HashMap<>();
        // 根据用户名获取redis的相应的秒杀订单列表
        if (values != null && values.size()>0){
            for (TbSeckillOrder tbSeckillOrder : values) {
                if (username.equals(tbSeckillOrder.getUserId())){
                    Map<String, Object> map = findMySeckillOrderFromRedis(tbSeckillOrder);
                    list.add(map);
                }
            }
            mySeckillOrderFromMyRedis.put("rows",list);
            mySeckillOrderFromMyRedis.put("totalPages",Math.ceil(list.size()/3.0));
            resultMap.put("redis",mySeckillOrderFromMyRedis);
        }


            return  resultMap;
        }

    public Map<String,Object> findMySeckillOrderFromMySql(List<TbSeckillOrder> seckillOrderList){
        Map<String, Object> map = new HashMap<>();
        // 保存秒杀订单的list
        List<Map<String,Object>> seckillGoodsList = new ArrayList<>();
        // 订单号
        Set<String> set = new HashSet();
            for (TbSeckillOrder tbSeckillOrder : seckillOrderList) {
                // 获取订单号
                set.add(tbSeckillOrder.getReceiverMobile());
            }
            // 遍历每个订单编号，获取订单编号对应的秒杀商品列表
            for (String s : set) {
                // 根据交易号获取订单列表
                TbSeckillOrder param = new TbSeckillOrder();
                param.setReceiverMobile(s);
                List<TbSeckillOrder> select = seckillOrderMapper.select(param);

                Map<String,Object> map1 = new HashMap<>();



                List<TbSeckillGoods> seckillGoodsList1 = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                // 总金额
                double money = 0.00;
                // 遍历同一个交易号里面的订单
                for (TbSeckillOrder tbSeckillOrder : select) {
                    // 获取秒杀商品
                    TbSeckillGoods seckillGoods = seckillGoodsMapper.selectByPrimaryKey(tbSeckillOrder.getSeckillId());
                    seckillGoodsList1.add(seckillGoods);
                    money += tbSeckillOrder.getMoney().doubleValue();

                }
                // 存入状态
                map1.put("status",select.get(0).getStatus());

                // 存入商家
                map1.put("sellerId",select.get(0).getSellerId());

                String time = sdf.format(select.get(0).getCreateTime());
                // 存入支付时间
                map1.put("createTime",time);

                // 存入订单编号
                map1.put("receiverMobile",s);

                // 存入总金额
                map1.put("money",money);



                // 存入商品列表
                map1.put("seckillId",seckillGoodsList1);

                seckillGoodsList.add(map1);

            }
            map.put("rows",seckillGoodsList);
        // 设置总页数，每页显示三个交易号
            map.put("totalPages",Math.ceil(set.size()/3.0));

            // 返回总交易号的数量
        return map;
    }

    public Map<String,Object> findMySeckillOrderFromRedis(TbSeckillOrder seckillOrder){
        Map<String,Object> map = new HashMap<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // 存入状态
        map.put("status",seckillOrder.getStatus());

        // 存入商家
        map.put("sellerId",seckillOrder.getSellerId());

        String time = sdf.format(seckillOrder.getCreateTime());
        // 存入支付时间
        map.put("createTime",time);

        // 存入订单编号
        map.put("receiverMobile",seckillOrder.getReceiverMobile());

        // 存入总金额
        map.put("money",seckillOrder.getMoney());

        TbSeckillGoods seckillGoods = seckillGoodsMapper.selectByPrimaryKey(seckillOrder.getSeckillId());

        // 存入商品列表
        map.put("seckillId",seckillGoods);
        return map;

    }


}
