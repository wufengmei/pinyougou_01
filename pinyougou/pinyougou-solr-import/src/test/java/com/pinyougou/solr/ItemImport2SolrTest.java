package com.pinyougou.solr;

import com.alibaba.fastjson.JSONObject;
import com.pinyougou.mapper.ItemMapper;
import com.pinyougou.pojo.TbItem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath*:spring/applicationContext-*.xml")
public class ItemImport2SolrTest {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private ItemMapper itemMapper;

    @Test
    public void test(){
        //1、查询已启用的商品sku列表
        TbItem param = new TbItem();
        //已启用
        param.setStatus("1");
        List<TbItem> itemList = itemMapper.select(param);

        //2、遍历每一个商品，将spec对应的字符串转换到specMap中
        for (TbItem tbItem : itemList) {
            Map map = JSONObject.parseObject(tbItem.getSpec(), Map.class);
            tbItem.setSpecMap(map);
        }

        //3、导入数据
        solrTemplate.saveBeans(itemList);

        solrTemplate.commit();
    }
    @Test
    public void test1(){
        // 创建查询对象
        SimpleQuery query = new SimpleQuery("*:*");
        // 删除所有
        solrTemplate.delete(query);
        // 提交
        solrTemplate.commit();
    }
}
