package com.pinyougou.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.SellerMapper;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service(interfaceClass = SellerService.class)
public class SellerServiceImpl extends BaseServiceImpl<TbSeller> implements SellerService {

    @Autowired
    private SellerMapper sellerMapper;

    @Override
    public PageResult search(Integer page, Integer rows, TbSeller seller) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbSeller.class);
        Example.Criteria criteria = example.createCriteria();

        //根据状态查询
        if(!StringUtils.isEmpty(seller.getStatus())){
            criteria.andEqualTo("status", seller.getStatus());
        }

        //根据名称查询
        if(!StringUtils.isEmpty(seller.getName())){
            criteria.andLike("name", "%" + seller.getName() + "%");
        }

        //根据店铺查询
        if(!StringUtils.isEmpty(seller.getNickName())){
            criteria.andLike("nickName", "%" + seller.getNickName() + "%");
        }

        List<TbSeller> list = sellerMapper.selectByExample(example);
        PageInfo<TbSeller> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public void updatePassword(String sellerId, String newPwd) {
        //查询商家
        TbSeller seller = sellerMapper.selectByPrimaryKey(sellerId);
        if(seller != null){
            //修改密码
            seller.setPassword(newPwd);
            sellerMapper.updateByPrimaryKey(seller);
        }

    }
}
