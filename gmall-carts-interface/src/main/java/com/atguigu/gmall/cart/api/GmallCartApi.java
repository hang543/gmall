package com.atguigu.gmall.cart.api;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.pojo.Cart;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/14
 * @Content:
 **/
public interface GmallCartApi {


    @GetMapping("cart/{userId}")
    public Resp<List<Cart>> queryCheckedCartsByUserId(@PathVariable("userId") Long userId);
}
