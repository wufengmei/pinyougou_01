package com.pinyougou.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.OrderAndGood;
import com.pinyougou.pojo.SeckillOrderAndGood;
import com.pinyougou.pojo.TbOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import com.pinyougou.vo.PageResult;
import com.pinyougou.vo.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/order")
@RestController
public class OrderController {

    @Reference
    private OrderService orderService;
    @Reference
    private SeckillOrderService seckillOrderService;

    /**
     * 查询符合条件的普通订单列表
     * @param orderAndGood 查询条件
     * @param page 页号
     * @param rows 每页大小
     * @return
     */
    @PostMapping("/search")
    public PageResult search(@RequestBody OrderAndGood orderAndGood, @RequestParam(value = "page", defaultValue = "1")Integer page,
                             @RequestParam(value = "rows", defaultValue = "10")Integer rows) {
        orderAndGood.setUsername("");
        return orderService.searchByUsername(page, rows, orderAndGood);
    }

    /**
     * 查询符合条件的秒杀订单列表
     * @param seckillOrderAndGood 查询条件
     * @param page 页号
     * @param rows 每页大小
     * @return
     */
    @PostMapping("/Seckillsearch")
    public PageResult Seckillsearch(@RequestBody SeckillOrderAndGood seckillOrderAndGood, @RequestParam(value = "page", defaultValue = "1")Integer page,
                                    @RequestParam(value = "rows", defaultValue = "10")Integer rows) {
        seckillOrderAndGood.setUsername("");
        return seckillOrderService.searchSeckillGoods(page, rows, seckillOrderAndGood);
    }
    @GetMapping("/updateStatus")
    public Result updateStatus(Long[] ids, String status){
        try {
            seckillOrderService.updateCloseatus(ids, status);

            return Result.ok("更新状态成功");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Result.fail("更新状态失败");
    }

}
