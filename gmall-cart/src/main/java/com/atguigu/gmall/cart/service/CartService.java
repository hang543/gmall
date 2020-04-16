package com.atguigu.gmall.cart.service;


import com.atguigu.gmall.cart.pojo.Cart;

import java.util.List;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/13
 * @Content:
 **/
public interface CartService {
    void addCatr(Cart cart);

    List<Cart> queryCarts();

    void updateCart(Cart cart);

    void deleteCart(Long skuId);

    List<Cart> queryCheckedCartsByUserId(Long userId);
}
