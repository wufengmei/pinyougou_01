package com.pinyougou.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.cart.service.CartService;
import com.pinyougou.mapper.ItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbOrderItem;
import com.pinyougou.vo.Cart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service(interfaceClass = CartService.class)
public class CartServiceImpl implements CartService {

    //购物车数据在redis中的key的名称
    private static final String CART_LIST = "CART_LIST";

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Cart> addItemToCartList(List<Cart> cartList, Long itemId, Integer num) {
        TbItem item = itemMapper.selectByPrimaryKey(itemId);
        //1、查询sku商品是否存在和判断是否启用
        if (item == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!"1".equals(item.getStatus())) {
            throw new RuntimeException("商品非法");
        }
        Cart cart = findCartBySellerId(cartList, item.getSellerId());
        if(cart == null) {
            //2、商品对应的商家不存在；则创建一个购物车商品对象加入到商家购物车中并将商家购物车加入的购物车列表
            if (num > 0) {
                cart = new Cart();
                cart.setSellerId(item.getSellerId());
                cart.setSeller(item.getSeller());

                //商品列表
                List<TbOrderItem> orderItemList = new ArrayList<>();

                //购物车商品
                TbOrderItem orderItem = createOrderItem(item, num);

                orderItemList.add(orderItem);

                cart.setOrderItemList(orderItemList);

                cartList.add(cart);
            } else {
                throw new RuntimeException("商品购买数量非法");
            }
        } else {
            //3、商品对应的商家存在
            TbOrderItem orderItem = findOrderItemByItemId(cart.getOrderItemList(), itemId);
            if(orderItem != null) {
                //3.1、商品存在则只需要将商品的购买数量叠加并重新计算总价格；
                // 如果叠加只有商品的购买数量为0则需要将该商品从购物车对象cart中删除，
                // 如果购物车对象中的商品列表的数量为0则需要将其从购物车列表cartList中删除
                orderItem.setNum(orderItem.getNum() + num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));
                if(orderItem.getNum() < 1){
                    cart.getOrderItemList().remove(orderItem);
                }
                if (cart.getOrderItemList().size() == 0) {
                    cartList.remove(cart);
                }
            } else {
                //3.2、商品不存在则创建一个购物车商品加入商家对应的购物车
                if (num > 0) {
                    orderItem = createOrderItem(item, num);
                    cart.getOrderItemList().add(orderItem);
                } else {
                    throw new RuntimeException("商品购买数量非法");
                }
            }
        }
        //4、返回购物车列表
        return cartList;
    }

    @Override
    public List<Cart> findCartListByUsername(String username) {
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps(CART_LIST).get(username);
        if(cartList != null){
            return cartList;
        }
        return new ArrayList<>();
    }

    @Override
    public void saveCartListByUsername(String username, List<Cart> cartList) {
        redisTemplate.boundHashOps(CART_LIST).put(username, cartList);
    }

    @Override
    public List<Cart> mergeCartList(List<Cart> cartList1, List<Cart> cartList2) {
        for (Cart cart : cartList1) {
            for (TbOrderItem orderItem : cart.getOrderItemList()) {
                addItemToCartList(cartList2, orderItem.getItemId(), orderItem.getNum());
            }
        }
        return cartList2;
    }

    /**
     * 根据商品sku id在列表中查询购物车商品
     * @param orderItemList 购物车中对应的商品列表
     * @param itemId 商品sku id
     * @return 购物车商品
     */
    private TbOrderItem findOrderItemByItemId(List<TbOrderItem> orderItemList, Long itemId) {
        if (orderItemList != null & orderItemList.size() > 0) {
            for (TbOrderItem orderItem : orderItemList) {
                if (itemId.equals(orderItem.getItemId())) {
                    return orderItem;
                }
            }
        }
        return null;
    }

    /**
     * 根据商品和购买数量创建一个购物车商品对象orderItem
     * @param item 商品
     * @param num 购买数量
     * @return 购物车商品对象orderItem
     */
    private TbOrderItem createOrderItem(TbItem item, Integer num) {
        TbOrderItem orderItem = new TbOrderItem();
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setTitle(item.getTitle());
        orderItem.setPrice(item.getPrice());
        //总计=单价*购买数量
        orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue()*orderItem.getNum()));
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setPicPath(item.getImage());

        return orderItem;
    }

    /**
     * 根据商家id查询购物车对象
     * @param cartList 购物车列表
     * @param sellerId 商家id
     * @return 购物车对象cart
     */
    private Cart findCartBySellerId(List<Cart> cartList, String sellerId) {
        if (cartList != null && cartList.size() > 0) {
            for (Cart cart : cartList) {
                if (sellerId.equals(cart.getSellerId())) {
                    return cart;
                }
            }
        }
        return null;
    }
}
