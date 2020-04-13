package com.atguigu.gmall.item.service;

import com.atguigu.gmall.item.vo.ItemVo;
import org.springframework.stereotype.Service;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/11
 * @Content:
 **/

public interface ItemService {
    public ItemVo queryItemVo(Long skuId) ;

}
