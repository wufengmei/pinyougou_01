package com.pinyougou.cart.service;

import com.pinyougou.pojo.TbAddress;
import com.pinyougou.pojo.TbAreas;
import com.pinyougou.pojo.TbCities;
import com.pinyougou.pojo.TbProvinces;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.PageResult;

import java.util.List;
import java.util.Map;

public interface AddressService extends BaseService<TbAddress> {

    PageResult search(Integer page, Integer rows, TbAddress address);

    List getAddressInformation(String username);

    //传入一个TbAddress一个map，就会自动给你将id转成地名
    Map<String, Object> addressIdToName(TbAddress tbAddress, Map<String, Object> addressInformation);

    void setDefaultAddress(String addressId, String username);

    void updateByPrimaryKey(TbAddress tbAddress);

    List<TbProvinces> findProvince();

    List<TbCities> findCities(String provinceId);

    List<TbAreas> findAreas(String cityId);

    void insertAddressInformation(TbAddress tbAddress);
}