package com.pinyougou.user.service;

import com.pinyougou.pojo.TbUser;
import com.pinyougou.service.BaseService;
import com.pinyougou.vo.PageResult;

import java.text.ParseException;
import java.util.Map;

public interface UserService extends BaseService<TbUser> {

    PageResult search(Integer page, Integer rows, TbUser user);

    /**
     * 发送手机短信验证码
     * @param phone 手机号
     */
    void sendSmsCode(String phone);

    /**
     * 根据用户手机号码到redis中获取验证码与用户输入的验证码对比；如果一致则返回true并且要将redis中的验证码删除；如果不一致则返回false
     * @param phone 手机号码
     * @param smsCode 用户输入验证码
     * @return true or false
     */
    boolean checkSmsCode(String phone, String smsCode);

    Map<String,Object> getUserInformation(String username);

    void updateUserInformation(String information, String username) throws ParseException;
}