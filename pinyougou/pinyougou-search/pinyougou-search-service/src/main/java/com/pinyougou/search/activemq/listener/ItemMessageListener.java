package com.pinyougou.search.activemq.listener;

import com.alibaba.fastjson.JSONArray;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.listener.adapter.AbstractAdaptableMessageListener;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.util.List;

/**
 * 搜索系统接收消息并更新solr中的商品数据
 */
public class ItemMessageListener extends AbstractAdaptableMessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message, Session session) throws JMSException {
        //1、接收消息
        TextMessage textMessage = (TextMessage) message;

        //将sku json格式字符串转换为一个sku列表
        List<TbItem> itemList = JSONArray.parseArray(textMessage.getText(), TbItem.class);

        //2、更新到solr中
        itemSearchService.importItemList(itemList);

    }
}
