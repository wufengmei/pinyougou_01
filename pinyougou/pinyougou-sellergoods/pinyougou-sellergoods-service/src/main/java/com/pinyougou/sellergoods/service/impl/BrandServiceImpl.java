package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.BrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.sellergoods.service.BrandService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Map;

//使用的是ali的注解
@Service(interfaceClass = BrandService.class)
public class BrandServiceImpl extends BaseServiceImpl<TbBrand> implements BrandService{

    @Autowired
    private BrandMapper brandMapper;

    @Override
    public List<TbBrand> queryAll() {
        return brandMapper.queryAll();
    }

    @Override
    public List<TbBrand> testPage(Integer page, Integer rows) {
        //1、设置分页；参数1：页号，参数2：页大小
        //只对紧接着执行的查询语句生效
        PageHelper.startPage(page, rows);

        //2、查询 select * from tb_brand limit page,rows
        return brandMapper.selectAll();
    }

    @Override
    public PageResult search(TbBrand brand, Integer page, Integer rows) {
        //设置分页
        PageHelper.startPage(page, rows);

        //sql:select * from tb_brand where name like "%?%" and first_char=?
        //查询
        Example example = new Example(TbBrand.class);

        //创建查询条件对象
        Example.Criteria criteria = example.createCriteria();

        //根据首字母查询；参数1：要查询的实体类中对应的属性名称，参数2：查询条件值
        //if(brand.getFirstChar() != null && !brand.getFirstChar().equals("")) {
        if(!StringUtils.isEmpty(brand.getFirstChar())){
            criteria.andEqualTo("firstChar", brand.getFirstChar());
        }

        //根据名称模糊查询
        if(!StringUtils.isEmpty(brand.getName())){
            criteria.andLike("name", "%" + brand.getName() + "%");
        }

        List<TbBrand> list = brandMapper.selectByExample(example);

        //转换分页信息对象
        PageInfo<TbBrand> pageInfo = new PageInfo<>(list);

        //返回分页对象
        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public List<Map<String, Object>> selectOptionList() {
        return brandMapper.selectOptionList();
    }
}
