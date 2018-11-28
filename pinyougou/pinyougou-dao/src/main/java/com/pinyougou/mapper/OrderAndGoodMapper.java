package com.pinyougou.mapper;

import com.pinyougou.pojo.OrderAndGood;

import java.util.List;

/**
 * @Description: TODO
 * @date 2018/11/26
 */
public interface OrderAndGoodMapper {
    List<OrderAndGood> findOrderAndGood(OrderAndGood orderAndGood);
}
