package com.pinyougou.mall.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RequestMapping("/mall")
@RestController
public class mallController {
    @Reference
    private ItemSearchService itemSearchService;
    @GetMapping("/findItemBySeller")
    public Map<String, Object> findItemBySeller(String seller){

        Map<String, Object> map = itemSearchService.searchBySeller(seller);
        if (map!=null&&map.size()>0){
            return map;
        }

        return new HashMap<>();
    }
    @GetMapping("/getUsername")
    public Map<String, Object> getUsername(){
        Map<String, Object> resultMap = new HashMap<String, Object>();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        resultMap.put("username", username);
        return resultMap;
    }
}
