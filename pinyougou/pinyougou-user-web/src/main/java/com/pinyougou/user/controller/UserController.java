package com.pinyougou.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.common.util.PhoneFormatCheckUtils;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.user.service.UserService;
import com.pinyougou.vo.PageResult;
import com.pinyougou.vo.Result;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

@RequestMapping("/user")
@RestController
public class UserController {

    @Reference
    private UserService userService;

    private HttpServletRequest request;

    /**
     * 获取当前登录的用户名
     *
     * @return 用户信息
     */
    @GetMapping("/getUsername")
    public Map<String, Object> getUsername() {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        resultMap.put("username", username);
        return resultMap;
    }

    /**
     * 发送手机短信验证码
     *
     * @param phone 手机号
     * @return 操作结果
     */
    @GetMapping("/sendSmsCode")
    public Result sendSmsCode(String phone) {
        try {
            if (PhoneFormatCheckUtils.isPhoneLegal(phone)) {
                userService.sendSmsCode(phone);
                return Result.ok("发送短信验证码成");
            } else {
                return Result.fail("手机号码格式错误；发送短信验证码失败");
            }
        } catch (PatternSyntaxException e) {
            e.printStackTrace();
        }
        return Result.fail("发送短信验证码失败!");
    }

    @RequestMapping("/findAll")
    public List<TbUser> findAll() {
        return userService.findAll();
    }

    @GetMapping("/findPage")
    public PageResult findPage(@RequestParam(value = "page", defaultValue = "1") Integer page,
                               @RequestParam(value = "rows", defaultValue = "10") Integer rows) {
        return userService.findPage(page, rows);
    }

    /**
     * 验证手机验证码并保存用户信息到数据库中
     *
     * @param user    用户信息
     * @param smsCode 手机验证码
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result add(@RequestBody TbUser user, String smsCode) {
        try {
            if (PhoneFormatCheckUtils.isPhoneLegal(user.getPhone())) {
                if (userService.checkSmsCode(user.getPhone(), smsCode)) {
                    //正常
                    user.setStatus("Y");
                    user.setCreated(new Date());
                    user.setUpdated(user.getCreated());
                    //明文加密成为密码
                    user.setPassword(DigestUtils.md5Hex(user.getPassword()));

                    userService.add(user);
                    return Result.ok("注册成功");
                } else {
                    return Result.fail("验证码输入错误");
                }
            } else {
                return Result.fail("手机号码格式错误；发送短信验证码失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("注册失败");
    }

    @GetMapping("/findOne")
    public TbUser findOne(Long id) {
        return userService.findOne(id);
    }

    @PostMapping("/update")
    public Result update(@RequestBody TbUser user) {
        try {
            userService.update(user);
            return Result.ok("修改成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("修改失败");
    }

    @GetMapping("/delete")
    public Result delete(Long[] ids) {
        try {
            userService.deleteByIds(ids);
            return Result.ok("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("删除失败");
    }

    /**
     * 分页查询列表
     *
     * @param user 查询条件
     * @param page 页号
     * @param rows 每页大小
     * @return
     */
    @PostMapping("/search")
    public PageResult search(@RequestBody TbUser user, @RequestParam(value = "page", defaultValue = "1") Integer page,
                             @RequestParam(value = "rows", defaultValue = "10") Integer rows) {
        return userService.search(page, rows, user);
    }

    @GetMapping("/getUserInformation")
    public Map<String, Object> getUserInformation() {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        resultMap = userService.getUserInformation(username);
        return resultMap;
    }

    @PostMapping("/updateUserInformation")
    public Result updateUserInformation(@RequestBody String information) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            userService.updateUserInformation(information, username);
            return Result.ok("保存成功");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return Result.fail("保存失败");
    }

    @PostMapping("/checkPassword")
    public Result checkPassword(@RequestBody String password) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        JSONObject jsonObject = JSON.parseObject(password);
        String oldPasswordMD5 = DigestUtils.md5Hex((String) jsonObject.get("oldPassword"));
        String newPasswordMD5 = DigestUtils.md5Hex((String) jsonObject.get("newPassword"));
        String newPasswordRepeatMD5 = DigestUtils.md5Hex((String) jsonObject.get("newPasswordRepeat"));

        TbUser tempTbUser = new TbUser();
        tempTbUser.setUsername(username);
        List<TbUser> user = userService.findByWhere(tempTbUser);
        try {
            String passwordInDb = user.get(0).getPassword();
            if (oldPasswordMD5.equals(passwordInDb)) {
                if (newPasswordMD5.equals(newPasswordRepeatMD5)) {
                    TbUser tbUser = user.get(0);
                    tbUser.setPassword(newPasswordRepeatMD5);
                    userService.updateByExample(tbUser);

                    return Result.ok("修改密码成功,请重新登陆");
                }else {
                    return Result.fail("两次密码不一致");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("原密码不正确");
    }

}
