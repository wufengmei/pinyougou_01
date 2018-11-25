package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbBrand;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.PageResult;

import java.util.List;
import java.util.Map;

public interface BrandService extends BaseService<TbBrand> {
    /**
     * 查询所有品牌数据
     * @return 品牌列表
     */
    List<TbBrand> queryAll();

    /**
     * 根据分页信息查询品牌列表
     * @param page 页号
     * @param rows 页大小
     * @return 品牌列表
     */
    List<TbBrand> testPage(Integer page, Integer rows);

    /**
     * 根据分页参数和查询条件查询品牌数据
     * @param brand 查询条件
     * @param page 页号
     * @param rows 页大小
     * @return 分页对象
     */
    PageResult search(TbBrand brand, Integer page, Integer rows);

    /**
     * 根据品牌列表；数据结构如：[{id:'1',text:'联想'},{id:'2',text:'华为'}]
     * @return 有格式的品牌列表数据
     */
    List<Map<String, Object>> selectOptionList();
}
