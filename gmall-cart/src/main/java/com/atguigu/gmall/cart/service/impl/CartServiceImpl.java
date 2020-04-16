package com.atguigu.gmall.cart.service.impl;

import cn.hutool.json.JSONUtil;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.cart.feign.GmallSmsClient;
import com.atguigu.gmall.cart.feign.GmallWmsClient;
import com.atguigu.gmall.cart.interceptors.LoginInterceptor;

import com.atguigu.core.bean.UserInfo;
import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.vo.SaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/13
 * @Content:
 **/
@Service
public class CartServiceImpl implements CartService {
    private static final String KEY_PREDFIX = "gmall:cart";
    private static final String PRICE_PREDFIX = "gmall:sku";
    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private GmallWmsClient gmallWmsClient;
    @Autowired
    private GmallSmsClient gmallSmsClientm;


    @Override
    public void addCatr(Cart cart) {
        String key = getLoginStatus();
        //获取购物车
        BoundHashOperations<String, Object, Object> hashOps = this.redisTemplate.boundHashOps(key);

        //判断购物车中是否有该sku记录
        String skuId = cart.getSkuId().toString();
        Integer count = cart.getCount();
        if (hashOps.hasKey(skuId)) {
            //有 则更新数量
            //获取记录
            String cartJson = hashOps.get(skuId).toString();
            //反序列化
            cart = JSONUtil.toBean(cartJson, Cart.class);
            cart.setCount(cart.getCount() + count);


        } else {
            //没有 增加购物车记录

            cart.setCheck(true);
            //查询sku相关信息
            Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.querySkuById(cart.getSkuId());
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            if (skuInfoEntity == null) {
                return;
            }

            cart.setDefaultImage(skuInfoEntity.getSkuDefaultImg());
            cart.setPrice(skuInfoEntity.getPrice());
            cart.setTitle(skuInfoEntity.getSkuTitle());
            //查询营销属性
            Resp<List<SkuSaleAttrValueEntity>> listResp = this.gmallPmsClient.querySkuSaleAttrValuesBySkuId(cart.getSkuId());
            List<SkuSaleAttrValueEntity> saleAttrValueEntities = listResp.getData();
            cart.setSaleAttrValues(saleAttrValueEntities);
            //查询营销信息
            Resp<List<SaleVo>> salesBySkuId = this.gmallSmsClientm.querySalesBySkuId(cart.getSkuId());
            List<SaleVo> salesBySkuIdData = salesBySkuId.getData();
            cart.setSales(salesBySkuIdData);
            //查询库存信息
            Resp<List<WareSkuEntity>> wareSkusBySkuId = this.gmallWmsClient.queryWareSkusBySkuId(cart.getSkuId());
            List<WareSkuEntity> wareSkuEntities = wareSkusBySkuId.getData();
            if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                cart.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
            }
            this.redisTemplate.opsForValue().set(PRICE_PREDFIX + skuId, skuInfoEntity.getPrice().toString());
        }

        hashOps.put(skuId, JSONUtil.toJsonStr(cart));
    }

    private String getLoginStatus() {
        //获取登录状态
        String key = KEY_PREDFIX;
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        if (userInfo.getId() != null) {
            key += userInfo.getId();
        } else {
            key += userInfo.getUserKey();
        }
        return key;
    }

    @Override
    public List<Cart> queryCarts() {
        //获取登录状态
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        //查询未登录的购物车
        String unLoginKey = KEY_PREDFIX + userInfo.getUserKey();
        BoundHashOperations<String, Object, Object> unLoginHashOps = this.redisTemplate.boundHashOps(unLoginKey);
        List<Object> cartJsonList = unLoginHashOps.values();

        List<Cart> unLoginCarts = null;
        if (!CollectionUtils.isEmpty(cartJsonList)) {
            unLoginCarts = cartJsonList.stream().map(cartJson -> {
                Cart cart = JSONUtil.toBean(cartJson.toString(), Cart.class);
                //查询当前价格
                String currtPrice = this.redisTemplate.opsForValue().get(PRICE_PREDFIX + cart.getSkuId());
                cart.setCurrentPrice(new BigDecimal(currtPrice));
                return cart;
            }).collect(Collectors.toList());

        }
        //判断是否登录，未登录直接返回
        if (userInfo.getId() == null) {
            return unLoginCarts;
        }
        //登录 进行购物车同步
        String loginKey = KEY_PREDFIX + userInfo.getId();
        BoundHashOperations<String, Object, Object> loginHashOps = this.redisTemplate.boundHashOps(loginKey);
        if (!CollectionUtils.isEmpty(unLoginCarts)) {
            unLoginCarts.forEach(cart -> {
                Integer count = cart.getCount();
                if (loginHashOps.hasKey(cart.getSkuId().toString())) {
                    String cartJson = loginHashOps.get(cart.getSkuId().toString()).toString();
                    cart = JSONUtil.toBean(cartJson, Cart.class);
                    cart.setCount(count + cart.getCount());
                }
                loginHashOps.put(cart.getSkuId().toString(), JSONUtil.toJsonStr(cart));
            });
            this.redisTemplate.delete(unLoginKey);
        }
        //查询登录状态的购物车
        List<Object> loginValuesJsonList = loginHashOps.values();
        return loginValuesJsonList.stream().map(cartJson -> {
            Cart cart = JSONUtil.toBean(cartJson.toString(), Cart.class);
            String currtPrice = this.redisTemplate.opsForValue().get(PRICE_PREDFIX + cart.getSkuId());
            cart.setCurrentPrice(new BigDecimal(currtPrice));
            return  cart;

                }
        ).collect(Collectors.toList());

    }

    @Override
    public void updateCart(Cart cart) {
        String key = this.getLoginStatus();
        BoundHashOperations<String, Object, Object> boundHashOps = this.redisTemplate.boundHashOps(key);
        Integer count = cart.getCount();
        if (boundHashOps.hasKey(cart.getSkuId().toString())) {
            String cartJson = boundHashOps.get(cart.getSkuId().toString()).toString();
            cart = JSONUtil.toBean(cartJson, Cart.class);
            cart.setCount(count);
            boundHashOps.put(cart.getSkuId().toString(), JSONUtil.toJsonStr(cart));
        }

    }

    @Override
    public void deleteCart(Long skuId) {
        String key = this.getLoginStatus();
        BoundHashOperations<String, Object, Object> boundHashOps = this.redisTemplate.boundHashOps(key);
        if (boundHashOps.hasKey(skuId.toString())) {
            boundHashOps.delete(skuId.toString());
        }

    }

    @Override
    public List<Cart> queryCheckedCartsByUserId(Long userId) {
        BoundHashOperations<String, Object, Object> hashOperations = this.redisTemplate.boundHashOps(KEY_PREDFIX + userId);
        List<Object> values = hashOperations.values();
        return values.stream().map(value->JSONUtil.toBean(value.toString(),Cart.class))
                .filter(Cart::getCheck).collect(Collectors.toList());
    }
}
