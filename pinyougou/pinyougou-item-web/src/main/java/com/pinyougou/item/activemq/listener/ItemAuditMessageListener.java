package com.pinyougou.item.activemq.listener;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.sellergoods.service.ItemCatService;
import com.pinyougou.vo.Goods;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.listener.adapter.AbstractAdaptableMessageListener;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import java.io.FileWriter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * 详情系统接收消息并根据每一个spu id生产商品详情静态页面到指定路径
 */
public class ItemAuditMessageListener extends AbstractAdaptableMessageListener {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Reference
    private GoodsService goodsService;

    @Reference
    private ItemCatService itemCatService;


    @Value("${ITEM_HTML_PATH}")
    private String ITEM_HTML_PATH;


    @Override
    public void onMessage(Message message, Session session) throws JMSException {
        //接收商品spu id数组
        ObjectMessage objectMessage = (ObjectMessage) message;
        Long[] goodsIds = (Long[]) objectMessage.getObject();

        //生成具体的html页面
        if (goodsIds != null && goodsIds.length > 0) {
            for (Long goodsId : goodsIds) {
                genHtml(goodsId);
            }
        }
    }


    /**
     * spu id获取商品基本、描述、sku列表，三级商品分类中文名称再结合item.ftl模版
     * 输出静态页面到指定路径下。
     * @param goodsId spu id
     */
    private void genHtml(Long goodsId) {
        try {
            Configuration configuration = freeMarkerConfigurer.getConfiguration();
            //模版
            Template template = configuration.getTemplate("item.ftl");

            //数据
            Map<String, Object> dataModel = new HashMap<>();

            //根据商品spu id查询商品基本、描述、sku列表（根据是否默认降序排序sku列表）
            Goods goods = goodsService.findGoodsByIdAndStatus(goodsId, "1");

            //goodsDesc 商品描述
            dataModel.put("goodsDesc", goods.getGoodsDesc());
            //goods 商品基本
            dataModel.put("goods", goods.getGoods());
            //
            //itemList 商品sku列表
            dataModel.put("itemList", goods.getItemList());

            //itemCat1 商品第1级商品分类中文名称
            TbItemCat itemCat1 = itemCatService.findOne(goods.getGoods().getCategory1Id());
            dataModel.put("itemCat1", itemCat1.getName());
            //itemCat2 商品第2级商品分类中文名称
            TbItemCat itemCat2 = itemCatService.findOne(goods.getGoods().getCategory2Id());
            dataModel.put("itemCat2", itemCat2.getName());

            //itemCat3 商品第3级商品分类中文名称
            TbItemCat itemCat3 = itemCatService.findOne(goods.getGoods().getCategory3Id());
            dataModel.put("itemCat3", itemCat3.getName());



            FileWriter fileWriter = new FileWriter(ITEM_HTML_PATH + goodsId + ".html");

            //输出
            template.process(dataModel, fileWriter);

            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
