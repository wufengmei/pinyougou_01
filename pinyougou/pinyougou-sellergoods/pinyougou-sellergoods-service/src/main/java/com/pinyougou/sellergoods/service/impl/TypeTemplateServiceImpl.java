package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.SpecificationOptionMapper;
import com.pinyougou.mapper.TypeTemplateMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbTypeTemplate;
import com.pinyougou.sellergoods.service.TypeTemplateService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;

@Service(interfaceClass = TypeTemplateService.class)
public class TypeTemplateServiceImpl extends BaseServiceImpl<TbTypeTemplate> implements TypeTemplateService {

    @Autowired
    private TypeTemplateMapper typeTemplateMapper;

    @Autowired
    private SpecificationOptionMapper specificationOptionMapper;

    @Override
    public PageResult search(Integer page, Integer rows, TbTypeTemplate typeTemplate) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbTypeTemplate.class);
        Example.Criteria criteria = example.createCriteria();
        if(!StringUtils.isEmpty(typeTemplate.getName())){
            criteria.andLike("name", "%" + typeTemplate.getName() + "%");
        }

        List<TbTypeTemplate> list = typeTemplateMapper.selectByExample(example);
        PageInfo<TbTypeTemplate> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public List<Map> findSpecList(Long id) {
        //根据分类模版id查询该分类模版对应的规格json格式字符串，需要转换规格json格式字符串为规格集合；
        // 遍历规格集合的每一个规格，根据规格id查询该规格对应的所有规格选项列表并设置会到该规格，属性名称为：options

        //1、根据分类模版id查询该分类模版
        TbTypeTemplate typeTemplate = findOne(id);
        //2、转换规格json格式字符串为规格集合
        List<Map> specList = JSONArray.parseArray(typeTemplate.getSpecIds(), Map.class);
        //3、遍历规格集合的每一个规格，根据规格id查询该规格对应的所有规格选项列表并设置会到该规格，属性名称为：options
        if (specList != null && specList.size() > 0) {
            for (Map map : specList) {
                //根据规格id查询该规格对应的所有规格选项列表
                //select * from tb-specification_option where spec_id=?
                TbSpecificationOption param = new TbSpecificationOption();
                param.setSpecId(Long.parseLong(map.get("id").toString()));

                List<TbSpecificationOption> options = specificationOptionMapper.select(param);

                map.put("options", options);
            }
        }
        //4、返回
        return specList;
    }
}
