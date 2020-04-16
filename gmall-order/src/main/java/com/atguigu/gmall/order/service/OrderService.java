package com.atguigu.gmall.order.service;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.order.vo.OrderConfirmVO;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/14
 * @Content:
 **/
public interface OrderService {
    OrderConfirmVO confirm();

    OrderEntity submit(OrderSubmitVO orderSubmitVO);
}
