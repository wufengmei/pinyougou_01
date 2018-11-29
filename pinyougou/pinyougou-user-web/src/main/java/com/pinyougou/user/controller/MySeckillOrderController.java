package com.pinyougou.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.seckill.service.SeckillOrderService;
import com.pinyougou.vo.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @Description: TODO
 * @date 2018/11/27
 */
@RequestMapping("/mySeckillOrder")
@RestController
public class MySeckillOrderController {

    @Reference
    private SeckillOrderService seckillOrderService;

    /**
     * 根据用户名搜索秒杀订单
     * @return 搜索结果
     */
    @PostMapping("/findMySeckillOrder")
    public Map<String, Object> findMySeckillOrder(){
        // 查询登陆用户
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return seckillOrderService.findMySeckillOrder(username);
    }

    @GetMapping("/findOne")
    public Map<String,Object> findOne(Long id){
        System.out.println(id);
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println(username);
        return seckillOrderService.findMyOneSeckillOrder(id,username);
    }

    /**
     * 确认收货后的操作
     * @return  操作结果
     */
    @GetMapping("/updateEndTime")
    public Result updateEndTime(Long id){
        Result result = Result.fail("确认收货失败");
        try {
            // 修改状态和交易成功时间
            seckillOrderService.updateEndTime(id);
            return Result.ok("交易成功");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
