package com.atguigu.gmall.pms.service;

import com.atguigu.core.bean.Resp;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.pms.entity.AttrEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import org.springframework.web.bind.annotation.RequestParam;


/**
 * 商品属性
 *
 * @author hang3
 * @email hangsansan@aliyun.com
 * @date 2020-04-02 15:42:52
 */
public interface AttrService extends IService<AttrEntity> {

    PageVo queryPage(QueryCondition params);

     PageVo queryAttrsById(QueryCondition queryCondition, Long cid, Integer type);
}

