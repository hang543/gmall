package com.atguigu.gmall.cart.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.interceptors.LoginInterceptor;

import com.atguigu.core.bean.UserInfo;
import com.atguigu.gmall.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/13
 * @Content:
 **/
@RestController
@RequestMapping("cart")
public class CartController {
    @Autowired
    private CartService cartService;

    @PostMapping()
    public Resp<Object> addCatr(@RequestBody Cart cart) {
       this.cartService.addCatr(cart);
       return Resp.ok(null);

    }
    @GetMapping("{userId}")
    public Resp<List<Cart>> queryCheckedCartsByUserId(@PathVariable("userId") Long userId){
        List<Cart> cartList=  this.cartService.queryCheckedCartsByUserId(userId);
        return Resp.ok(cartList);
    }
    @GetMapping
    public Resp<List<Cart>> queryCarts(){
     List<Cart> carts= this.cartService.queryCarts();
     return Resp.ok(carts);
    }
    @PostMapping("update")
    public Resp<Object> updateCart(@RequestBody Cart cart){
        this.cartService.updateCart(cart);
        return Resp.ok(null);

    }
    @PostMapping("delete/{skuId}")
    public Resp<Object> deleteCart(@PathVariable("skuId")Long skuId){
       this.cartService.deleteCart(skuId);
       return Resp.ok(null);
    }

    @GetMapping("test")
    public String test() {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        return "hello";
    }
}
