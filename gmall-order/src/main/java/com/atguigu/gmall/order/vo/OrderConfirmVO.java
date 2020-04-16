package com.atguigu.gmall.order.vo;

import com.atguigu.gmall.oms.vo.OrderItemVO;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import lombok.Data;

import java.util.List;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/14
 * @Content:
 **/
@Data
public class OrderConfirmVO {
    private List<MemberReceiveAddressEntity> addresses;
    private List<OrderItemVO> orderItems;
    private Integer bounds;//积分
    private String orderToken;//订单的唯一信息

}
