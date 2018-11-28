package com.pinyougou.pojo;

import javax.persistence.*;
import java.io.Serializable;

@Table(name = "tb_cities")
public class TbCities implements Serializable{

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    @Column(name = "cityid")
    private String cityid;
    @Column(name = "city")
    private String city;
    @Column(name = "provinceid")
    private String provinceid;

    private static final long serialVersionUID = 1L;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCityId() {
        return cityid;
    }

    public void setCityId(String cityId) {
        this.cityid = cityId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getProvinceId() {
        return provinceid;
    }

    public void setProvinceId(String provinceId) {
        this.provinceid = provinceId;
    }
}
