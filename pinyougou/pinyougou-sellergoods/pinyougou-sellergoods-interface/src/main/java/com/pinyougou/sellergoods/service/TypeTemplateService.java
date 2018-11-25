package com.pinyougou.sellergoods.service;

import com.pinyougou.pojo.TbTypeTemplate;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.PageResult;

import java.util.List;
import java.util.Map;

public interface TypeTemplateService extends BaseService<TbTypeTemplate> {

    PageResult search(Integer page, Integer rows, TbTypeTemplate typeTemplate);

    /**
     * 根据分类模板id查询其对应的规格及其规格的选项；结构如：
     * [
     * {"id":27,"text":"网络","options":[{规格选项1（optionName）},{规格选项2（optionName）}]},
     * {"id":32,"text":"机身内存","options":[{规格选项（optionName）}]}
     * ]
     * @param id 分类模板id
     * @return 规格及其规格的选项
     */
    List<Map> findSpecList(Long id);

}