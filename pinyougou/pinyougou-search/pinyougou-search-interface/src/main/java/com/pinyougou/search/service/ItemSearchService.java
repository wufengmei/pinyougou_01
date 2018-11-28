package com.pinyougou.search.service;

import com.pinyougou.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {
    /**
     * 根据搜索关键字分页查询solr中商品
     * @param searchMap 搜索条件
     * @return 搜索结果
     */
    Map<String, Object> search(Map<String, Object> searchMap);

    /**
     * 导入sku商品列表到solr中
     * @param itemList sku商品列表
     */
    void importItemList(List<TbItem> itemList);

    /**
     * 同步删除搜索系统中的sku商品
     * @param goodsIdList 商品spu id 集合
     */
    void deleteItemsByGoodsIds(List<Long> goodsIdList);

    Map<String, Object> searchBySeller(String seller);
}
