package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.OrderAndGood;
import com.pinyougou.pojo.SeckillOrderAndGood;
import com.pinyougou.seckill.service.SeckillOrderService;
import com.pinyougou.vo.PageResult;
import com.pinyougou.vo.Result;
import jdk.nashorn.internal.ir.IfNode;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/order")
@RestController
public class OrderController {

    @Reference
    private OrderService orderService;
    @Reference
    private SeckillOrderService seckillOrderService;


    /**
     * 查询所有的订单列表
     * @param orderAndGood 查询条件
     * @param page 页号
     * @param rows 每页大小
     * @return
     */
    @PostMapping("/search")
    public PageResult search(@RequestBody OrderAndGood orderAndGood, @RequestParam(value = "page", defaultValue = "1")Integer page,
                             @RequestParam(value = "rows", defaultValue = "10")Integer rows) {
        // 获取登陆用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        orderAndGood.setUsername(username);

        return orderService.searchByUsername(page, rows, orderAndGood);
    }/*
     *查询所有的订单列表
     * @param orderAndGood 查询条件
     * @param page 页号
     * @param rows 每页大小
     * @return
             */
    @PostMapping("/Seckillsearch")
    public PageResult Seckillsearch(@RequestBody SeckillOrderAndGood seckillOrderAndGood, @RequestParam(value = "page", defaultValue = "1")Integer page,
                                    @RequestParam(value = "rows", defaultValue = "10")Integer rows) {
        // 获取登陆用户名
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        seckillOrderAndGood.setUsername(username);
        return seckillOrderService.searchSeckillGoods(page, rows, seckillOrderAndGood);
    }

    /**
     *
     * @param ids
     * @param status
     * @return
     */
    // 更新商品的状态
    @GetMapping("/updateStatus")
    public Result updateStatus(Long[] ids, String status){
        try {
            seckillOrderService.updateStatus(ids, status);

            return Result.ok("更新状态成功");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Result.fail("更新状态失败");
    }

}
