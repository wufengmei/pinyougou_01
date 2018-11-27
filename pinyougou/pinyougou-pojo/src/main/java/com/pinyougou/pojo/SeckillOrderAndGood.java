package com.pinyougou.pojo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description: TODO
 * @date 2018/11/26
 */
public class SeckillOrderAndGood implements Serializable {

    // 当前登陆的商家
    private String username;

    // 商家id
    private String sellerId;

    // 用户名
    private String userId;

    // 商品名称
    private String title;

    // 原价
    private BigDecimal price;

    // 枪杀价
    private BigDecimal costPrice;

    // 金额
    private BigDecimal money;

    // 数量
    private Integer num;

    // 库存
    private Integer stockCount;

    // 已销售数量
    private Integer count;

    // 支付时间
    private String payTime;

    // 地址
    private String receiverAddress;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(BigDecimal costPrice) {
        this.costPrice = costPrice;
    }

    public BigDecimal getMoney() {
        return money;
    }

    public void setMoney(BigDecimal money) {
        this.money = money;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public Integer getStockCount() {
        return stockCount;
    }

    public void setStockCount(Integer stockCount) {
        this.stockCount = stockCount;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount() {
        this.count = this.num - this.stockCount;
    }

    public String getPayTime() {
        return payTime;
    }

    public void setPayTime(String payTime) {
        this.payTime = payTime;
    }

    public String getReceiverAddress() {
        return receiverAddress;
    }

    public void setReceiverAddress(String receiverAddress) {
        this.receiverAddress = receiverAddress;
    }
}
