package com.pinyougou.mall.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.common.util.CookieUtils;
import com.pinyougou.vo.Cart;
import com.pinyougou.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/cart")
@RestController
public class CartController {

    //购物车数据保存在浏览器中cookie的名称
    private static final String COOKIE_CART_LIST = "PYG_CART_LIST";
    //购物车数据保存在浏览器中cookie的有效时间；默认1天
    private static final int COOKIE_CART_MAX_AGE = 60*60*24;

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private HttpServletResponse response;

    @Reference
    private CartService cartService;

    /**
     * 登录与未登录实现购物车购买商品数量的变更
     * @param itemId 商品sku id
     * @param num 购买数量
     * @return 操作结果
     */
    @GetMapping("/addItemToCartList")
    @CrossOrigin(origins = "http://item.pinyougou.com", allowCredentials = "true")
    public Result addItemToCartList(Long itemId, Integer num){
        try {

            //设置跨域请求
            //运行访问并响应的域名
            //response.setHeader("Access-Control-Allow-Origin","http://item.pinyougou.com");
            //接收并设置cookie
            //response.setHeader("Access-Control-Allow-Credentials","true");

            //1、查询购物车列表cartList
            List<Cart> cartList = findCartList();

            String username = SecurityContextHolder.getContext().getAuthentication().getName();

            //2、将商品和购买数量加入到购物车列表cartList
            cartList = cartService.addItemToCartList(cartList, itemId, num);

            if ("anonymousUser".equals(username)) {
                //没有登录；将商品加入到cookie
                //3、将最新的购物车列表cartList写回cookie
                CookieUtils.setCookie(request, response, COOKIE_CART_LIST, JSON.toJSONString(cartList),
                        COOKIE_CART_MAX_AGE, true);
            } else {
                //已登录；将商品加入到redis
                //3、将最新的购物车列表cartList写回redis
                cartService.saveCartListByUsername(username, cartList);
            }
            return Result.ok("加入购物车成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("加入购物车失败");
    }

    /**
     * 在登录或者未登录情况下获取用户购物车列表
     * @return 购物车列表
     */
    @GetMapping("/findCartList")
    public List<Cart> findCartList(){
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            List<Cart> cookieCartList = new ArrayList<>();
            //1、获取cookie中购物车json格式字符串
            String cartListJsonStr = CookieUtils.getCookieValue(request, COOKIE_CART_LIST, true);
            //2、转换为列表对象
            if (!StringUtils.isEmpty(cartListJsonStr)) {
                cookieCartList = JSONArray.parseArray(cartListJsonStr, Cart.class);
            }

            if ("anonymousUser".equals(username)) {
                //没有登录；从cookie中获取购物车数据
                return cookieCartList;
            } else {
                //已登录；从redis中获取购物车数据
                //1、查询cookie中购物车列表；

                //2、查询redis中的购物车列表；
                List<Cart> redisCartList = cartService.findCartListByUsername(username);

                if (cookieCartList.size() > 0) {
                    //3、将cookie和redis中的购物车列表进行合并成一个新的购物车列表
                    redisCartList = cartService.mergeCartList(cookieCartList, redisCartList);

                    //4、将合并之后最新的购物车列表写回redis
                    cartService.saveCartListByUsername(username, redisCartList);

                    //5、删除cookie中购物车列表
                    CookieUtils.deleteCookie(request, response, COOKIE_CART_LIST);
                }

                return redisCartList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取当前登录用户名
     * @return 用户信息
     */
    @GetMapping("/getUsername")
    public Map<String, Object> getUsername(){
        Map<String, Object> map = new HashMap<String, Object>();
        /**
         * 在security中配置了<intercept-url pattern="/cart/*.do" access="IS_AUTHENTICATED_ANONYMOUSLY"/>
         * 所以当用户没有登录的时候，获得到的用户名为 anonymousUser
         */
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        map.put("username", username);

        return map;
    }
}
