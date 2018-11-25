package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.Goods;
import com.pinyougou.vo.PageResult;

import java.util.List;

public interface GoodsService extends BaseService<TbGoods> {

    PageResult search(Integer page, Integer rows, TbGoods goods);

    /**
     * 保存商品基本、描述、sku列表到数据库中
     * @param goods 商品vo = {goods,goodsDesc,itemList}
     */
    void addGoods(Goods goods);

    /**
     * 根据商品spu id查询商品信息（基本、描述、sku列表）
     * @param id 商品spu id
     * @return 商品信息（基本、描述、sku列表）
     */
    Goods findGoodsById(Long id);

    /**
     * 将前台传递到后台的商品信息（基本、描述、sku列表）更新到数据库中
     * @param goods 商品信息（基本、描述、sku列表）
     */
    void updateGoods(Goods goods);

    /**
     * 根据商品spu id数组修改这些spu商品对应的审核状态
     *
     * @param ids 商品spu id数组
     * @param status 审核状态
     */
    void updateStatus(Long[] ids, String status);

    /**
     * 修改这些spu对应的sku的状态
     * @param status sku的启用状态（0未启用，1已启用）
     * @param ids 商品spu id数组
     */
    void updateItemStatusByGoodsIds(String status, Long[] ids);

    /**
     * 逻辑删除商品数据
     * @param ids 商品spu id数组
     */
    void deleteGoodsByIds(Long[] ids);

    /**
     * 根据spu id数组查询这些spu商品对应的已启用的sku商品列表
     * @param ids spu id数组
     * @param itemStatus sku的状态
     * @return sku 列表
     */
    List<TbItem> findItemListByGoodsIdsAndStatus(Long[] ids, String itemStatus);

    /**
     * 根据商品spu id查询商品基本、描述、sku列表（根据是否默认降序排序sku列表）
     * @param goodsId 商品spu id
     * @param itemStatus 商品sku状态
     * @return goods
     */
    Goods findGoodsByIdAndStatus(Long goodsId, String itemStatus);
}