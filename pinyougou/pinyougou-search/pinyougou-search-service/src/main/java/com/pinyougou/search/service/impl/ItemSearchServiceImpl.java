package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Override
    public Map<String, Object> search(Map<String, Object> searchMap) {
        Map<String, Object> resultMap = new HashMap<>();

        //如果搜索关键字中包含有空格的话；那么将空格替换为空
        if (!StringUtils.isEmpty(searchMap.get("keywords"))) {
            searchMap.put("keywords", searchMap.get("keywords").toString().replaceAll(" ", ""));
        }

        //创建查询对象
        //SimpleQuery query = new SimpleQuery();
        SimpleHighlightQuery query = new SimpleHighlightQuery();

        //根据关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));

        query.addCriteria(criteria);

        //设置高亮
        HighlightOptions highlightOptions = new HighlightOptions();
        //设置高亮域名
        highlightOptions.addField("item_title");
        //高亮起始标签
        highlightOptions.setSimplePrefix("<font style='color:red'>");
        //高亮结束标签
        highlightOptions.setSimplePostfix("</font>");
        query.setHighlightOptions(highlightOptions);

        //商品分类条件过滤
        if (!StringUtils.isEmpty(searchMap.get("category"))) {
            Criteria catCriteria = new Criteria("item_category").is(searchMap.get("category"));
            SimpleFilterQuery catFilterQuery = new SimpleFilterQuery(catCriteria);
            query.addFilterQuery(catFilterQuery);
        }

        //商品品牌条件过滤
        if (!StringUtils.isEmpty(searchMap.get("brand"))) {
            Criteria brandCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            SimpleFilterQuery brandFilterQuery = new SimpleFilterQuery(brandCriteria);
            query.addFilterQuery(brandFilterQuery);
        }

        //规格条件过滤
        if (searchMap.get("spec") != null) {

            Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");

            Set<Map.Entry<String, String>> entries = specMap.entrySet();
            for (Map.Entry<String, String> entry : entries) {
                Criteria specCriteria = new Criteria("item_spec_" + entry.getKey()).is(entry.getValue());
                SimpleFilterQuery specFilterQuery = new SimpleFilterQuery(specCriteria);
                query.addFilterQuery(specFilterQuery);
            }
        }

        //价格条件过滤
        if (!StringUtils.isEmpty(searchMap.get("price"))) {

            String[] prices = searchMap.get("price").toString().split("-");

            Criteria startPriceCriteria = new Criteria("item_price").greaterThanEqual(prices[0]);
            SimpleFilterQuery startPriceFilterQuery = new SimpleFilterQuery(startPriceCriteria);
            query.addFilterQuery(startPriceFilterQuery);

            if (!"*".equals(prices[1])) {
                Criteria endPriceCriteria = new Criteria("item_price").lessThanEqual(prices[1]);
                SimpleFilterQuery endPriceFilterQuery = new SimpleFilterQuery(endPriceCriteria);
                query.addFilterQuery(endPriceFilterQuery);
            }
        }

        //设置分页
        //页号
        int pageNo = 1;
        if (searchMap.get("pageNo") != null) {
            pageNo = Integer.parseInt(searchMap.get("pageNo").toString());
        }

        //页大小
        int pageSize = 20;
        if (searchMap.get("pageSize") != null) {
            pageSize = Integer.parseInt(searchMap.get("pageSize").toString());
        }

        //起始索引号 = （页号 -1）*页大小
        query.setOffset((pageNo-1)*pageSize);
        query.setRows(pageSize);

        //设置排序域和顺序
        if (!StringUtils.isEmpty(searchMap.get("sortField")) && !StringUtils.isEmpty(searchMap.get("sort"))) {
            //排序的域名称
            String sortField = "item_" + searchMap.get("sortField");
            //排序的顺序；升序ASC 降序DESC
            String sortOrder = searchMap.get("sort").toString();
            //参数1：排序的顺序，参数2：排序域名
            Sort sort = new Sort("DESC".equals(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC, sortField);
            query.addSort(sort);
        }


        //1、查询
        HighlightPage<TbItem> highlightPage = solrTemplate.queryForHighlightPage(query, TbItem.class);

        //处理高亮标题
        //获取高亮返回结果
        List<HighlightEntry<TbItem>> highlighted = highlightPage.getHighlighted();
        if (highlighted != null && highlighted.size() > 0) {

            for (HighlightEntry<TbItem> entry : highlighted) {
                if (entry.getHighlights()!= null && entry.getHighlights().size() > 0
                        && entry.getHighlights().get(0).getSnipplets().size() >0 ) {
                    //高亮标题；0 第一个域， 0多值时候选择第1个
                    String title = entry.getHighlights().get(0).getSnipplets().get(0);
                    entry.getEntity().setTitle(title);
                }
            }
        }

        //2、返回查询结果
        //查询结果列表
        resultMap.put("rows", highlightPage.getContent());
        //总记录数
        resultMap.put("total", highlightPage.getTotalElements());
        //总页数
        resultMap.put("totalPages", highlightPage.getTotalPages());

        return resultMap;
    }

    @Override
    public void importItemList(List<TbItem> itemList) {
        //需要将spec字符串转换为一个map
        if(itemList != null && itemList.size()>0) {
            for (TbItem item : itemList) {
                Map<String, String> specMap = JSON.parseObject(item.getSpec(), Map.class);
                item.setSpecMap(specMap);
            }

            solrTemplate.saveBeans(itemList);
            solrTemplate.commit();
        }
    }

    @Override
    public void deleteItemsByGoodsIds(List<Long> goodsIdList) {
        SimpleQuery query = new SimpleQuery();

        Criteria criteria = new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);

        solrTemplate.delete(query);
        solrTemplate.commit();
    }
}
