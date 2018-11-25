package com.pinyougou.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.common.Mapper;

import java.io.Serializable;
import java.util.List;

public abstract class BaseServiceImpl<T> implements BaseService<T> {

    @Autowired //spring 4.x版本之后可以使用泛型依赖注入；---》如:brandMapper
    private Mapper<T> mapper;

    @Override
    public T findOne(Serializable id) {
        return mapper.selectByPrimaryKey(id);
    }

    @Override
    public List<T> findAll() {
        return mapper.selectAll();
    }

    @Override
    public List<T> findByWhere(T t) {
        return mapper.select(t);
    }

    @Override
    public PageResult findPage(Integer pageNo, Integer rows) {
        //设置分页
        PageHelper.startPage(pageNo, rows);
        //查询；该list是继承了Page的
        List<T> list = mapper.selectAll();

        //转换为一个分页信息对象
        PageInfo<T> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public PageResult findPage(Integer pageNo, Integer rows, T t) {
        //设置分页
        PageHelper.startPage(pageNo, rows);
        //查询；该list是继承了Page的
        List<T> list = mapper.select(t);

        //转换为一个分页信息对象
        PageInfo<T> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public void add(T t) {
        //选择性新增：可以在新增的时候判断t对象的属性值如果为空的话；则不会出现在insert语句
        //insert into tb_brand(id, name) values(?,?);
        mapper.insertSelective(t);

    }

    @Override
    public void update(T t) {
        //选择性更新：可以在更新的时候判断t对象的属性值如果为空的话；则不会出现在update语句
        //updata tb_brand set name=? where id=?
        //如果不用selective的话：updata tb_brand set name=?,first_char=null where id=?
        mapper.updateByPrimaryKeySelective(t);

    }

    @Override
    public void deleteByIds(Serializable[] ids) {
        for (Serializable id : ids) {
            mapper.deleteByPrimaryKey(id);
        }
    }
}
