package com.atguigu.gmall.order.controller;

import com.alipay.api.AlipayApiException;
import com.atguigu.core.bean.Resp;
import com.atguigu.core.bean.UserInfo;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.order.interceptors.LoginInterceptor;
import com.atguigu.gmall.order.pay.AlipayTemplate;
import com.atguigu.gmall.order.pay.PayAsyncVo;
import com.atguigu.gmall.order.pay.PayVo;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.vo.OrderConfirmVO;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/14
 * @Content:
 **/
@RestController
@RequestMapping("order")
public class OrderController {
    @Autowired
    private OrderService orderService;
    @Autowired
    private AlipayTemplate alipayTemplate;
    @Autowired
    private AmqpTemplate amqpTemplate;

    @GetMapping("confirm")
    public Resp<OrderConfirmVO> confirm() {
        OrderConfirmVO confirmVO = this.orderService.confirm();
        return Resp.ok(confirmVO);

    }

    @PostMapping("submit")
    public Resp<Object> submit(@RequestBody OrderSubmitVO orderSubmitVO) {
        OrderEntity orderEntity = this.orderService.submit(orderSubmitVO);
        try {
            PayVo payVo = new PayVo();
            payVo.setOut_trade_no(orderEntity.getOrderSn());
            payVo.setTotal_amount(orderEntity.getPayAmount() != null ? orderEntity.getPayAmount().toString() : "100");
            payVo.setSubject("guli商城");
            payVo.setBody("支付平台");

            String form = this.alipayTemplate.pay(payVo);
            System.out.println(form);
            return Resp.ok(null);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return Resp.ok(null);
    }

    @PostMapping("pay/success")
    public Resp<Object> paySuccess(PayAsyncVo payAsyncVo) {
        //发送订单编号
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        Long id = userInfo.getId();
        this.amqpTemplate.convertAndSend("GMALL-ORDER-EXCHANGE", "order.pay", payAsyncVo.getOut_trade_no());
        return Resp.ok(null);

    }
}
