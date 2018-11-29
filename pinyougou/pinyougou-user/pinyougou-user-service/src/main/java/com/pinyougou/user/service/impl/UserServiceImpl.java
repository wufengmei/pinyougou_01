package com.pinyougou.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.UserMapper;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.user.service.UserService;
import com.pinyougou.vo.PageResult;
import org.apache.activemq.command.ActiveMQQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import tk.mybatis.mapper.entity.Example;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Session;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service(interfaceClass = UserService.class)
public class UserServiceImpl extends BaseServiceImpl<TbUser> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private JmsTemplate jmsTemplate;

    @Autowired
    private ActiveMQQueue itcastSmsQueue;

    @Value("${signName}")
    private String signName;

    @Value("${templateCode}")
    private String templateCode;

    @Override
    public PageResult search(Integer page, Integer rows, TbUser user) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbUser.class);
        Example.Criteria criteria = example.createCriteria();
        /*if(!StringUtils.isEmpty(user.get***())){
            criteria.andLike("***", "%" + user.get***() + "%");
        }*/

        List<TbUser> list = userMapper.selectByExample(example);
        PageInfo<TbUser> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public void sendSmsCode(String phone) {
//        生成6位随机数
        String tempCode = "";
        while (true) {
            double random = Math.random();
            String stringRandom = random + "";
            tempCode = stringRandom.substring(2, 8);
            if (!tempCode.substring(0, 1).equals("0")) {
                break;
            }else {
                tempCode = "";
            }
        }
        String code = tempCode;
        redisTemplate.boundValueOps(phone).set(code);
        redisTemplate.boundValueOps(phone).expire(5, TimeUnit.MINUTES);
        //发送通知
        jmsTemplate.send(itcastSmsQueue, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage mapMessage = session.createMapMessage();
                mapMessage.setString("mobile", phone);
                mapMessage.setString("signName", signName);
                mapMessage.setString("templateCode", templateCode);
                mapMessage.setString("templateParam","{\"code\":\""+code+"\"}");

                System.out.println(mapMessage.getString("templateParam"));
                return mapMessage;
            }
        });

    }

    @Override
    public boolean checkSmsCode(String phone, String smsCode) {
        //1、获取redis中验证码
        //TODO 如果验证码位数小于6位则修正为6位的随机数
        String code = (String) redisTemplate.boundValueOps(phone).get();

        //2、对比验证码；如果一致则删除redis中验证码并返回true
        if (smsCode.equals(code)) {
            redisTemplate.delete(phone);

            return true;
        }

        return false;
    }

    @Override
    public Map<String, Object> getUserInformation(String username) {

        //返回的Map
        Map<String, Object> resultMap = new HashMap<String, Object>();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

        Example example = new Example(TbUser.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username", username);
        List<TbUser> tbUsers = userMapper.selectByExample(example);

        for (TbUser tbUser : tbUsers) {

            resultMap.put("nickName",tbUser.getNickName());
            resultMap.put("sex",tbUser.getSex());
            resultMap.put("phone", tbUser.getPhone());
            //切割日期
            String tempDate = simpleDateFormat.format(tbUser.getBirthday());
            String[] sourceStrArray = tempDate.split("-");
            resultMap.put("birthdayYear",sourceStrArray[0]);
            resultMap.put("birthdayMonth", sourceStrArray[1]);
            resultMap.put("birthdayDay", sourceStrArray[2]);
        }
        return resultMap;
    }

    @Override
    public void updateUserInformation(String information, String username) throws ParseException {
        TbUser tbUser = new TbUser();
        Map map = JSON.parseObject(information, Map.class);
        tbUser.setNickName((String)map.get("nickName"));
        tbUser.setSex(map.get("sex")+"");
        tbUser.setUpdated(new Date());

        //解析时间
        String tempDay = (String)map.get("birthdayYear")+"-"+(String)map.get("birthdayMonth")+"-"+(String)map.get("birthdayDay");
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date parse = simpleDateFormat.parse(tempDay);
        tbUser.setBirthday(parse);

        //获取userid
        Example example = new Example(TbUser.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username", username);
        List<TbUser> tbUsers = userMapper.selectByExample(example);
        Long id = tbUsers.get(0).getId();

        tbUser.setId(id);

        userMapper.updateByPrimaryKeySelective(tbUser);
    }

    @Override
    public void updateByExample(TbUser tbUser) {

        String username = tbUser.getUsername();
        Example example = new Example(TbUser.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("username", username);
        List<TbUser> tbUsers = userMapper.selectByExample(example);
        Long id = tbUsers.get(0).getId();

        tbUser.setId(id);

        userMapper.updateByPrimaryKeySelective(tbUser);

    }

}
