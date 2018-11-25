package com.pinyougou.content.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pinyougou.mapper.ContentMapper;
import com.pinyougou.pojo.TbContent;
import com.pinyougou.content.service.ContentService;
import com.pinyougou.service.impl.BaseServiceImpl;
import com.pinyougou.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

@Service(interfaceClass = ContentService.class)
public class ContentServiceImpl extends BaseServiceImpl<TbContent> implements ContentService {

    //内容列表在redis中的key的名称
    private static final String CONTENT = "content";

    @Autowired
    private ContentMapper contentMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void add(TbContent tbContent) {
        super.add(tbContent);
        //更新内容分类缓存中对应的内容列表
        updateContentListInRedisByCategoryId(tbContent.getCategoryId());
    }

    /**
     * 根据分类id删除在redis中的缓存数据
     * @param categoryId 内容分类id
     */
    private void updateContentListInRedisByCategoryId(Long categoryId) {
        redisTemplate.boundHashOps(CONTENT).delete(categoryId);
    }

    @Override
    public void update(TbContent tbContent) {
        //1、查询原有的内容
        TbContent oldContent = findOne(tbContent.getId());

        super.update(tbContent);

        //更新新的内容对应的内容分类缓存
        updateContentListInRedisByCategoryId(tbContent.getCategoryId());

        //如果修改过内容分类则需要将原有的内容分类对应的缓存数据删除
        if (!oldContent.getCategoryId().equals(tbContent.getCategoryId())) {
            updateContentListInRedisByCategoryId(oldContent.getCategoryId());
        }
    }

    @Override
    public void deleteByIds(Serializable[] ids) {
        //根据内容id数组查询这些内容列表；遍历没一个内容，根据这个内容对应的内容分类删除缓存
        Example example = new Example(TbContent.class);
        example.createCriteria().andIn("id", Arrays.asList(ids));

        List<TbContent> contentList = contentMapper.selectByExample(example);
        if (contentList != null && contentList.size() > 0) {
            for (TbContent content : contentList) {
                updateContentListInRedisByCategoryId(content.getCategoryId());
            }
        }

        super.deleteByIds(ids);
    }

    @Override
    public PageResult search(Integer page, Integer rows, TbContent content) {
        PageHelper.startPage(page, rows);

        Example example = new Example(TbContent.class);
        Example.Criteria criteria = example.createCriteria();
        /*if(!StringUtils.isEmpty(content.get***())){
            criteria.andLike("***", "%" + content.get***() + "%");
        }*/

        List<TbContent> list = contentMapper.selectByExample(example);
        PageInfo<TbContent> pageInfo = new PageInfo<>(list);

        return new PageResult(pageInfo.getTotal(), pageInfo.getList());
    }

    @Override
    public List<TbContent> findContentListByCategoryId(Long categoryId) {
        List<TbContent> contentList = null;
        //select * from tb_content where `status` = ? and category_id = ? order by sort_order desc

        try {
            //在查询内容列表的时候先从redis中查询；如果查询到数据则直接返回
            contentList = (List<TbContent>) redisTemplate.boundHashOps(CONTENT).get(categoryId);
            if (contentList != null) {
                return contentList;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Example example = new Example(TbContent.class);

        //查询条件对象
        Example.Criteria criteria = example.createCriteria();

        //内容分类id
        criteria.andEqualTo("categoryId", categoryId);
        //查询有效
        criteria.andEqualTo("status", "1");

        //根据排序字段降序排序
        example.orderBy("sortOrder").desc();

        contentList = contentMapper.selectByExample(example);

        try {
            //如果没有查询到则查询mysql中的数据并在返回之前将内容列表数据设置到redis
            redisTemplate.boundHashOps(CONTENT).put(categoryId, contentList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return contentList;
    }
}
