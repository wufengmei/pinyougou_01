package com.pinyougou.seckill.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeckillOrder;
import com.pinyougou.seckill.service.SeckillOrderService;
import com.pinyougou.vo.PageResult;
import com.pinyougou.vo.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/seckillOrder")
@RestController
public class SeckillOrderController {

    @Reference
    private SeckillOrderService seckillOrderService;

    /**
     * 根据秒杀商品id对该商品进行秒杀下单
     * @param seckillId 秒杀商品id
     * @return 操作结果
     */
    @GetMapping("/submitOrder")
    public Result submitOrder(Long seckillId){
        Result result = Result.fail("秒杀下单失败！");
        try {
            String userId = SecurityContextHolder.getContext().getAuthentication().getName();
            if(!"anonymousUser".equals(userId)){
                String orderId = seckillOrderService.submitOrder(userId, seckillId);
                if (orderId != null) {
                    result = Result.ok(orderId);
                }
            } else {
                result = Result.fail("请先登录再下单");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
