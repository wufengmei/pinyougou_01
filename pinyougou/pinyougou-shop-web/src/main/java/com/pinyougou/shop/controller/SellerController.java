package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import com.pinyougou.vo.PageResult;
import com.pinyougou.vo.Password;
import com.pinyougou.vo.Result;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/seller")
@RestController
public class SellerController {

    @Reference
    private SellerService sellerService;

    @RequestMapping("/findAll")
    public List<TbSeller> findAll() {
        return sellerService.findAll();
    }

    @GetMapping("/findPage")
    public PageResult findPage(@RequestParam(value = "page", defaultValue = "1")Integer page,
                               @RequestParam(value = "rows", defaultValue = "10")Integer rows) {
        return sellerService.findPage(page, rows);
    }

    /**
     * 保存商家信息到数据库中
     * @param seller 商家
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result add(@RequestBody TbSeller seller) {
        try {
            //刚注册的时候；商家状态应该为 未审核
            seller.setStatus("0");
            //对密码的明文；使用bcrypt进行加油
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            seller.setPassword(passwordEncoder.encode(seller.getPassword()));
            sellerService.add(seller);
            return Result.ok("商家注册成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("商家注册失败");
    }

    @GetMapping("/findOne")
    public TbSeller findOne(String id) {
        return sellerService.findOne(id);
    }

    @PostMapping("/update")
    public Result update(@RequestBody TbSeller seller) {
        try {
            sellerService.update(seller);
            return Result.ok("修改成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("修改失败");
    }

    @GetMapping("/delete")
    public Result delete(String[] ids) {
        try {
            sellerService.deleteByIds(ids);
            return Result.ok("删除成功");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Result.fail("删除失败");
    }

    /**
     * 分页查询列表
     * @param seller 查询条件
     * @param page 页号
     * @param rows 每页大小
     * @return
     */
    @PostMapping("/search")
    public PageResult search(@RequestBody  TbSeller seller, @RequestParam(value = "page", defaultValue = "1")Integer page,
                               @RequestParam(value = "rows", defaultValue = "10")Integer rows) {
        return sellerService.search(page, rows, seller);
    }

    @RequestMapping("/updatePassword")
    public Result updatePassword(@RequestBody Password password){
        try{
            //对密码进行加密
            BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
            String newPwd = passwordEncoder.encode(password.getNewPwd());
            //获取用户名
            String name = SecurityContextHolder.getContext().getAuthentication().getName();
            TbSeller seller = findOne(name);
            //校验密码
            if(BCrypt.checkpw(password.getOldPwd(),seller.getPassword())){
                sellerService.updatePassword(name,newPwd);
                return Result.ok("修改密码成功！");
            }
            return Result.fail("原密码错误！");

        }catch (Exception e){
            e.printStackTrace();
            return Result.fail("修改密码失败！");
        }
    }

}
