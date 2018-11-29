package com.pinyougou.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.pinyougou.pojo.TbAddress;
import com.pinyougou.pojo.TbAreas;
import com.pinyougou.pojo.TbCities;
import com.pinyougou.pojo.TbProvinces;
import com.pinyougou.cart.service.AddressService;
import com.pinyougou.vo.PageResult;
import com.pinyougou.vo.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("/address")
@RestController
public class AddressController {

    @Reference
    private AddressService addressService;

    @RequestMapping("/findAll")
    public List<TbAddress> findAll() {
        return addressService.findAll();
    }

    @GetMapping("/findPage")
    public PageResult findPage(@RequestParam(value = "page", defaultValue = "1") Integer page,
                               @RequestParam(value = "rows", defaultValue = "10") Integer rows) {
        return addressService.findPage(page, rows);
    }

    @PostMapping("/add")
    public Result add(@RequestBody TbAddress address) {
        try {
            addressService.add(address);
            return Result.ok("增加成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("增加失败");
    }

    @GetMapping("/findOne")
    public Map findOne(Long id) {
        TbAddress tbAddress = addressService.findOne(id);
        Map<String, Object> resultMap = new HashMap<>();

        Map<String, Object> addressMap = addressService.addressIdToName(tbAddress, resultMap);

        resultMap.put("id", tbAddress.getId());
        resultMap.put("contact", tbAddress.getContact());
        resultMap.put("mobile", tbAddress.getMobile());
        resultMap.put("address", tbAddress.getNotes());//这里写详细地址
        resultMap.put("default", tbAddress.getIsDefault());
        resultMap.put("alias", tbAddress.getAlias());
        resultMap.put("addressMap", addressMap);

        return resultMap;
    }

    @PostMapping("/update")
    public Result update(@RequestBody TbAddress address) {
        try {
            addressService.update(address);
            return Result.ok("修改成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("修改失败");
    }

    @GetMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            addressService.deleteByIds(ids);
            return Result.ok("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("删除失败");
    }

    /**
     * 分页查询列表
     *
     * @param address 查询条件
     * @param page    页号
     * @param rows    每页大小
     * @return
     */
    @PostMapping("/search")
    public PageResult search(@RequestBody TbAddress address, @RequestParam(value = "page", defaultValue = "1") Integer page,
                             @RequestParam(value = "rows", defaultValue = "10") Integer rows) {
        return addressService.search(page, rows, address);
    }

    @GetMapping("/getAddressInformation")
    public List getAddressInformation() {
        List<TbAddress> resultList;
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        resultList = addressService.getAddressInformation(username);
        return resultList;
    }

    @GetMapping("/setDefaultAddress")
    public Result setDefaultAddress(String addressId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        //设置默认地址
        try {
            addressService.setDefaultAddress(addressId, username);
            return Result.ok("修改成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("修改失败");
    }

    @PostMapping("/updateAddressInfromation")
    public Result updateAddressInfromation(@RequestBody String AddressInfromation) {
        TbAddress tbAddress = new TbAddress();
        try {
            parseAddressInfromation(AddressInfromation, tbAddress);
            addressService.updateByPrimaryKey(tbAddress);

            return Result.ok("修改成功");
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("修改失败");
        }

    }
    @PostMapping("/insertAddressInformation")
    public Result insertAddressInformation(@RequestBody String AddressInfromation) {
        TbAddress tbAddress = new TbAddress();

        try {
            parseAddressInfromation(AddressInfromation, tbAddress);
            addressService.insertAddressInformation(tbAddress);
            tbAddress.setCreateDate(new Date());
            return Result.ok("新增成功");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("新增失败");
    }

    private void parseAddressInfromation(@RequestBody String AddressInfromation, TbAddress tbAddress) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        JSONObject jsonObject = JSON.parseObject(AddressInfromation);
        try {
            tbAddress.setId(Long.valueOf(String.valueOf(jsonObject.get("id"))));
        } catch (Exception e) {

        }
        tbAddress.setUserId(username);
        tbAddress.setAlias((String) jsonObject.get("alias"));
        tbAddress.setMobile((String) jsonObject.get("mobile"));
        tbAddress.setIsDefault((String) jsonObject.get("default"));
        tbAddress.setContact((String) jsonObject.get("contact"));
        tbAddress.setNotes((String) jsonObject.get("address"));

        //解析地址map
        JSONObject addressMap = jsonObject.getJSONObject("addressMap");

        JSONObject provinceName = (JSONObject) addressMap.get("provinceName");
        Map<String, String> provinceMap = JSONObject.parseObject(provinceName.toJSONString(), new TypeReference<Map<String, String>>() {
        });

        JSONObject cityName = (JSONObject) addressMap.get("cityName");
        Map<String, String> cityMap = JSONObject.parseObject(cityName.toJSONString(), new TypeReference<Map<String, String>>() {
        });

        JSONObject townName = (JSONObject) addressMap.get("townName");
        Map<String, String> townMap = JSONObject.parseObject(townName.toJSONString(), new TypeReference<Map<String, String>>() {
        });

        String provinceId = provinceMap.get("id");
        String cityId = cityMap.get("id");
        String townId = townMap.get("id");

        tbAddress.setProvinceId(provinceId);
        tbAddress.setCityId(cityId);
        tbAddress.setTownId(townId);

        tbAddress.setAddress(provinceMap.get("province") + cityMap.get("city") + townMap.get("area") + jsonObject.get("address"));
    }


    @GetMapping("/findProvince")
    public List<TbProvinces> findProvince() {
        List<TbProvinces> provincesList = addressService.findProvince();
        return provincesList;
    }

    @GetMapping("/findCities")
    public List<TbCities> findCities(String provinceId) {
        List<TbCities> citiesList = addressService.findCities(provinceId);
        return citiesList;
    }

    @GetMapping("/findAreas")
    public List<TbAreas> findAreas(String cityId) {
        List<TbAreas> areasList = addressService.findAreas(cityId);
        return areasList;
    }

}
