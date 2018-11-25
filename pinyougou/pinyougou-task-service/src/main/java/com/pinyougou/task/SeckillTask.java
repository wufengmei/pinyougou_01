package com.pinyougou.task;

import com.pinyougou.mapper.SeckillGoodsMapper;
import com.pinyougou.pojo.TbSeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Component
public class SeckillTask {

    @Autowired
    private RedisTemplate redisTemplate;

    //在redis中秒杀商品列表对应的key的名称
    public static final String SECKILL_GOODS = "SECKILL_GOODS";

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    /**
     * 如果有*新的可使用的* 秒杀商品数据的话；
     * 应该将这些数据自动更新到redis中；因为上课所以可以每3秒执行一次(0/3 * * * * ?)
     */
    @Scheduled(cron = "0/3 * * * * ?")
    public void refreshSeckillGoods(){

        //a,查询新的秒杀商品id列表
        //获取在redis中的那些秒杀商品id集合
        Set set = redisTemplate.boundHashOps(SECKILL_GOODS).keys();
        List ids = new ArrayList<>(set);

        //查询已审核（1），库存大于0，开始时间小于等于当前时间，
        // 结束时间大于当前时间并且排除掉已经在redis中的那些秒杀商品
        //select * from tb_seckill_goods where status='1' and stock_count>0 and start_time<=?
        //and end_time > ? and id not in(?,?)
        Example example = new Example(TbSeckillGoods.class);
        Example.Criteria criteria = example.createCriteria();

        criteria.andEqualTo("status", "1");
        criteria.andGreaterThan("stockCount", 0);
        criteria.andLessThanOrEqualTo("startTime", new Date());
        criteria.andGreaterThan("endTime", new Date());
        criteria.andNotIn("id", ids);

        List<TbSeckillGoods> seckillGoodsList = seckillGoodsMapper.selectByExample(example);

        //b,遍历商品列表将一个个的存入到redis中
        if (seckillGoodsList != null && seckillGoodsList.size() > 0) {
            for (TbSeckillGoods seckillGoods : seckillGoodsList) {
                redisTemplate.boundHashOps(SECKILL_GOODS).put(seckillGoods.getId(), seckillGoods);
            }
            System.out.println("更新了 "+seckillGoodsList.size() +"条秒杀商品到缓存中...");
        }
    }


    /**
     * 如果在redis中的秒杀商品数据过时的话那么应该自动的将其从redis中删除；
     * 因为上课所以可以每2秒执行一次(0/2 * * * * ?)
     */
    @Scheduled(cron = "0/2 * * * * ?")
    public void removeSeckillGoods(){

        //a,查询新的秒杀商品列表
        List<TbSeckillGoods> seckillGoodsList = redisTemplate.boundHashOps(SECKILL_GOODS).values();

        //b,遍历商品列表将一个个的存入到redis中
        if (seckillGoodsList != null && seckillGoodsList.size() > 0) {
            for (TbSeckillGoods seckillGoods : seckillGoodsList) {
                if (seckillGoods.getEndTime().getTime() < System.currentTimeMillis()) {

                    //更新秒杀商品回mysql
                    seckillGoodsMapper.updateByPrimaryKeySelective(seckillGoods);

                    redisTemplate.boundHashOps(SECKILL_GOODS).delete(seckillGoods.getId());


                    System.out.println("从redis中移除了秒杀商品id为 "+seckillGoods.getId() +" 的秒杀商品");
                }
            }

        }
    }
}
