package com.pinyougou.pojo;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Description: TODO
 * @date 2018/11/26
 */
public class OrderAndGood implements Serializable {

    // 当前登陆的商家
    private String username;

    // 商家id
    private String sellerId;

    // 用户名
    private String userId;

    // 商品名称
    private String goodsName;

    // 商品分类
    private Long category1Id;

    private Long category2Id;

    private Long category3Id;

    // 单价
    private BigDecimal price;

    // 购买数量
    private Integer num;

    // 实付金额
    private BigDecimal totalFee;

    // 更新时间
    private String updateTime;

    // 状态
    private String status;

    // 地址
    private String receiverAreaName;

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

    public String getGoodsName() {
        return goodsName;
    }

    public void setGoodsName(String goodsName) {
        this.goodsName = goodsName;
    }

    public Long getCategory1Id() {
        return category1Id;
    }

    public void setCategory1Id(Long category1Id) {
        this.category1Id = category1Id;
    }

    public Long getCategory2Id() {
        return category2Id;
    }

    public void setCategory2Id(Long category2Id) {
        this.category2Id = category2Id;
    }

    public Long getCategory3Id() {
        return category3Id;
    }

    public void setCategory3Id(Long category3Id) {
        this.category3Id = category3Id;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public BigDecimal getTotalFee() {
        return totalFee;
    }

    public void setTotalFee(BigDecimal totalFee) {
        this.totalFee = totalFee;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReceiverAreaName() {
        return receiverAreaName;
    }

    public void setReceiverAreaName(String receiverAreaName) {
        this.receiverAreaName = receiverAreaName;
    }
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
