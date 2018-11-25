package com.pinyougou.seckill.service;

import com.pinyougou.pojo.TbSeckillGoods;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.PageResult;

import java.util.List;

public interface SeckillGoodsService extends BaseService<TbSeckillGoods> {

    PageResult search(Integer page, Integer rows, TbSeckillGoods seckillGoods);

    /**
     * 查询秒杀商品在状态是已审核（1），库存量大于0，
     * 开始时间小于等于当前时间，结束时间大于当前时间的秒杀商品数据并且按照开始时间升序排序。
     * @return 秒杀商品列表
     */
    List<TbSeckillGoods> findList();

    /**
     * 根据商品id到redis获取秒杀商品
     * @param id 商品id
     * @return 描述商品
     */
    TbSeckillGoods findSeckillGoodsInRedisById(Long id);
}