package com.pinyougou.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.AddressMapper;
import com.pinyougou.mapper.AreasMapper;
import com.pinyougou.mapper.CityMapper;
import com.pinyougou.mapper.ProvinceMapper;
import com.pinyougou.pojo.TbAddress;
import com.pinyougou.pojo.TbAreas;
import com.pinyougou.pojo.TbCities;
import com.pinyougou.pojo.TbProvinces;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.user.service.AddressService;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(interfaceClass = AddressService.class)
public class AddressServiceImpl extends BaseServiceImpl<TbAddress> implements AddressService {

    @Autowired
    private AddressMapper addressMapper;

    @Autowired
    private ProvinceMapper provinceMapper;

    @Autowired
    private CityMapper cityMapper;

    @Autowired
    private AreasMapper areasMapper;


    @Override
    public PageResult search(Integer page, Integer rows, TbAddress address) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbAddress.class);
        Example.Criteria criteria = example.createCriteria();
        /*if(!StringUtils.isEmpty(address.get***())){
            criteria.andLike("***", "%" + address.get***() + "%");
        }*/

        List<TbAddress> list = addressMapper.selectByExample(example);
        PageInfo<TbAddress> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public List getAddressInformation(String username) {
        //返回list
        List resultList = new ArrayList<>();
        Example example = new Example(TbAddress.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("userId",username);
//        还要根据地址id去找地址
        List<TbAddress> tempList = addressMapper.selectByExample(example);

        for (TbAddress tbAddress : tempList) {
            Map<String, Object> addressInformation = new HashMap<>();
            addressInformation.put("id", tbAddress.getId());
            addressInformation.put("contact",tbAddress.getContact());
            addressInformation.put("mobile", tbAddress.getMobile());
            addressInformation.put("address", tbAddress.getAddress());
            addressInformation.put("default", tbAddress.getIsDefault());

            addressIdToName(tbAddress, addressInformation);

            resultList.add(addressInformation);
        }

        return resultList;
    }


    //传入一个TbAddress一个map，就会自动给你将id转成地名
    @Override
    public Map<String, Object> addressIdToName(TbAddress tbAddress, Map<String, Object> addressInformation) {
        //获取省
        Example exampleProvinces = new Example(TbProvinces.class);
        Example.Criteria criteriaProvinces = exampleProvinces.createCriteria();
        criteriaProvinces.andEqualTo("id", Integer.parseInt(tbAddress.getProvinceId()));
        List<TbProvinces> tbProvinces1 = provinceMapper.selectByExample(exampleProvinces);
        if (tbProvinces1 != null) {
            addressInformation.put("provinceName", tbProvinces1.get(0));
        }

        //获取市
        Example exampleCities = new Example(TbCities.class);
        Example.Criteria criteriaCities = exampleCities.createCriteria();
        criteriaCities.andEqualTo("id", Integer.parseInt(tbAddress.getCityId()));
        List<TbCities> tbCities1 = cityMapper.selectByExample(exampleCities);
        if (tbCities1 != null) {
            addressInformation.put("cityName", tbCities1.get(0));
        }

        //获取镇
        Example exampleAreas = new Example(TbAreas.class);
        Example.Criteria criteriaAreas= exampleAreas.createCriteria();
        criteriaAreas.andEqualTo("id", Integer.parseInt(tbAddress.getTownId()));
        List<TbAreas> tbTown1 = areasMapper.selectByExample(exampleAreas);
        if (tbTown1 != null) {
            addressInformation.put("townName", tbTown1.get(0));
        }
        return addressInformation;
    }
    @Override
    /**
     * 修改传入id的isDefault为1，其他设置为0
     */
    public void setDefaultAddress(String addressId, String username) {
        TbAddress tbAddress = new TbAddress();
        tbAddress.setIsDefault("1");
        Example example = new Example(tbAddress.getClass());
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", Long.parseLong(addressId));
        criteria.andEqualTo("userId", username);

        addressMapper.updateByExampleSelective(tbAddress, example);

        //修改其他为0
        TbAddress tbAddressToZero = new TbAddress();
        tbAddressToZero.setIsDefault("0");
        Example exampleToZero = new Example(tbAddress.getClass());
        Example.Criteria criteriaToZero = exampleToZero.createCriteria();
        criteriaToZero.andNotEqualTo("id", Long.parseLong(addressId));
        criteriaToZero.andEqualTo("userId", username);

        addressMapper.updateByExampleSelective(tbAddressToZero, exampleToZero);
    }

    @Override
    public void updateByPrimaryKey(TbAddress tbAddress) {
        addressMapper.updateByPrimaryKeySelective(tbAddress);
    }
    @Override
    public void insertAddressInformation(TbAddress tbAddress) {
        addressMapper.insertSelective(tbAddress);
    }

    @Override
    public List<TbProvinces> findProvince() {
        return provinceMapper.selectAll();
    }

    @Override
    public List<TbCities> findCities(String provinceId) {

        Example exampleCities = new Example(TbCities.class);
        Example.Criteria criteriaCities = exampleCities.createCriteria();
        criteriaCities.andEqualTo("provinceid", provinceId);
        List<TbCities> citiesList = cityMapper.selectByExample(exampleCities);

        return citiesList;
    }

    @Override
    public List<TbAreas> findAreas(String cityId) {

        Example exampleTbAreas = new Example(TbAreas.class);
        Example.Criteria criteria = exampleTbAreas.createCriteria();
        criteria.andEqualTo("cityId", cityId);
        List<TbAreas> areasList = areasMapper.selectByExample(exampleTbAreas);

        return areasList;
    }



}
