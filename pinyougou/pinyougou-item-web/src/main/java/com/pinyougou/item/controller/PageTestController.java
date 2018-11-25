package com.pinyougou.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbItemCat;
import com.pinyougou.sellergoods.service.GoodsService;
import com.pinyougou.sellergoods.service.ItemCatService;
import com.pinyougou.vo.Goods;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RequestMapping("/test")
@RestController
public class PageTestController {

    @Autowired
    private FreeMarkerConfigurer freeMarkerConfigurer;

    @Reference
    private GoodsService goodsService;

    @Reference
    private ItemCatService itemCatService;


    @Value("${ITEM_HTML_PATH}")
    private String ITEM_HTML_PATH;

    /**
     * 模拟商品批量审核之后；
     * 应该遍历每一个spu id获取商品基本、描述、sku列表，三级商品分类中文名称再结合item.ftl模版
     * 输出静态页面到指定路径下。
     * @param goodsIds spu id数组
     * @return 操作结果
     */
    @GetMapping("/auditGoods")
    public String auditGoods(Long[] goodsIds){
        if (goodsIds != null && goodsIds.length > 0) {
            for (Long goodsId : goodsIds) {
                genHtml(goodsId);
            }
        }
        return "success";
    }

    /**
     * 商品批量删除之后；应用遍历每一个spu id，到指定路径下删除该id对应的静态页面
     * @param goodsIds spu id数组
     * @return 操作结果
     */
    @GetMapping("/deleteGoods")
    public String deleteGoods(Long[] goodsIds){
        if (goodsIds != null && goodsIds.length > 0) {
            for (Long goodsId : goodsIds) {
                File file = new File(ITEM_HTML_PATH + goodsId + ".html");
                if (file.exists()) {
                    file.delete();
                }
            }
        }
        return "success";
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
