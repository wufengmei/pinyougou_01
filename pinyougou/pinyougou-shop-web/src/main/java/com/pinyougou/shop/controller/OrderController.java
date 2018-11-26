package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.order.service.OrderService;
import com.pinyougou.pojo.OrderAndGood;
import com.pinyougou.vo.PageResult;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/order")
@RestController
public class OrderController {

    @Reference
    private OrderService orderService;

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
        System.out.println(username);

        return orderService.searchByUsername(page, rows, orderAndGood);
    }

}
