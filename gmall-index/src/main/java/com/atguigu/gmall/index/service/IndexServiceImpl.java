package com.atguigu.gmall.index.service;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.vo.CategoryVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/9
 * @Content:
 **/
@Service
public class IndexServiceImpl implements IndexService {
    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private static final String KEY_PREFIX="index:cates";

    @Override
    public List<CategoryEntity> queryLv1Categories() {
        Resp<List<CategoryEntity>> listResp = this.gmallPmsClient.queryCategoriesByPidOrLevel(1, null);
        return listResp.getData();
    }

    @Override
    public List<CategoryVO> querySubCategories(Long pid) {
        //先从redis中查询
        String json = this.stringRedisTemplate.opsForValue().get(KEY_PREFIX + pid);
        if(StringUtils.isNotEmpty(json)){
            return JSON.parseArray(json,CategoryVO.class);
        }

        Resp<List<CategoryVO>> listResp = this.gmallPmsClient.querySubCategories(pid);
        List<CategoryVO> categoryVOS = listResp.getData();
        this.stringRedisTemplate.opsForValue().set(KEY_PREFIX+pid,JSON.toJSONString(categoryVOS));
        return listResp.getData();
    }

}
