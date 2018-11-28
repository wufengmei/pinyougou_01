package com.pinyougou.vo;

import com.pinyougou.pojo.TbOrder;

import java.util.List;

public class OrderVo {
    private TbOrder tbOrder;
    private String createTime;
    private String createTime1;
    private String paymentTime;
    private String paymentTime1;
    private List orderItemList;

    public List getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List itemList) {
        this.orderItemList = itemList;
    }

    public String getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(String paymentTime) {
        this.paymentTime = paymentTime;
    }

    public String getPaymentTime1() {
        return paymentTime1;
    }

    public void setPaymentTime1(String paymentTime1) {
        this.paymentTime1 = paymentTime1;
    }

    public String getCreateTime1() {
        return createTime1;
    }

    public void setCreateTime1(String createTime1) {
        this.createTime1 = createTime1;
    }

    public TbOrder getTbOrder() {
        return tbOrder;
    }

    public void setTbOrder(TbOrder tbOrder) {
        this.tbOrder = tbOrder;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
