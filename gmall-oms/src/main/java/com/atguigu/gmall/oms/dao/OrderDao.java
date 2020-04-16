package com.atguigu.gmall.oms.dao;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import javax.swing.*;

/**
 * 订单
 * 
 * @author hang3
 * @email hangsansan@aliyun.com
 * @date 2020-04-14 12:03:46
 */
@Mapper
public interface OrderDao extends BaseMapper<OrderEntity> {

    public int closeOrder(String orderToken);

    public int payOrder(String  orderToken);
	
}
