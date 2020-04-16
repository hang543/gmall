package com.atguigu.gmall.order.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.atguigu.core.bean.Resp;
import com.atguigu.core.bean.UserInfo;
import com.atguigu.core.exception.OrderException;
import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.oms.entity.OrderEntity;
import com.atguigu.gmall.order.feign.*;
import com.atguigu.gmall.order.interceptors.LoginInterceptor;
import com.atguigu.gmall.order.service.OrderService;
import com.atguigu.gmall.order.vo.OrderConfirmVO;
import com.atguigu.gmall.oms.vo.OrderItemVO;
import com.atguigu.gmall.oms.vo.OrderSubmitVO;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import com.atguigu.gmall.vo.SaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.vo.SkuLockVO;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/14
 * @Content:
 **/
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private GmallCartClient gmallCartClient;
    @Autowired
    private GmallSmsClient gmallSmsClient;
    @Autowired
    private GmallUmsClient gmallUmsClient;
    @Autowired
    private GmallWmsClient gmallWmsClient;
    @Autowired
    private GmallOmsClient gmallOmsClient;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private AmqpTemplate amqpTemplate;

    private static final String TOKEN_PREFIX="order:token:";

    @Override
    public OrderConfirmVO confirm() {

        OrderConfirmVO confirmVO = new OrderConfirmVO();
        UserInfo userInfo = LoginInterceptor.getUserInfo();

        Long userId = userInfo.getId();
        if (userId == null) {
            return null;
        }

        //获取用户的收获地址列表
        CompletableFuture<Void> addressCompletableFuture = CompletableFuture.runAsync(() -> {
            Resp<List<MemberReceiveAddressEntity>> adderessesByUserId = this.gmallUmsClient.queryAdderessesByUserId(userId);
            List<MemberReceiveAddressEntity> adderesses = adderessesByUserId.getData();
            confirmVO.setAddresses(adderesses);
        }, threadPoolExecutor);

        //获取购物车中选中的商品信息
        CompletableFuture<Void> skuBigCompletableFuture = CompletableFuture.supplyAsync(() -> {
            Resp<List<Cart>> checkedCartsByUserId = this.gmallCartClient.queryCheckedCartsByUserId(userId);
            List<Cart> cartList = checkedCartsByUserId.getData();
            if (CollectionUtil.isEmpty(cartList)) {
                throw new OrderException("请勾选购物车商品");
            }
            return cartList;
        }, threadPoolExecutor).thenAcceptAsync(cartList -> {

            List<OrderItemVO> itemVOList = cartList.stream().map(cart -> {
                OrderItemVO orderItemVO = new OrderItemVO();
                Long skuId = cart.getSkuId();
                CompletableFuture<Void> skuCompletableFuture = CompletableFuture.runAsync(() -> {
                    Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.querySkuById(skuId);
                    SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
                    if (skuInfoEntity != null) {
                        orderItemVO.setWeight(skuInfoEntity.getWeight());
                        orderItemVO.setDefaultImage(skuInfoEntity.getSkuDefaultImg());
                        orderItemVO.setPrice(skuInfoEntity.getPrice());
                        orderItemVO.setTitle(skuInfoEntity.getSkuTitle());
                        orderItemVO.setSkuId(cart.getSkuId());
                        orderItemVO.setCount(cart.getCount());
                    }
                }, threadPoolExecutor);

                CompletableFuture<Void> saleAttrCompletableFuture = CompletableFuture.runAsync(() -> {
                    Resp<List<SkuSaleAttrValueEntity>> saleAttrValuesBySkuId = this.gmallPmsClient.querySkuSaleAttrValuesBySkuId(skuId);
                    List<SkuSaleAttrValueEntity> saleAttrValueEntities = saleAttrValuesBySkuId.getData();
                    orderItemVO.setSaleAttrValues(saleAttrValueEntities);
                }, threadPoolExecutor);

                CompletableFuture<Void> wareCompletableFuture = CompletableFuture.runAsync(() -> {
                    Resp<List<WareSkuEntity>> wareSkusBySkuId = this.gmallWmsClient.queryWareSkusBySkuId(skuId);
                    List<WareSkuEntity> wareSkuEntities = wareSkusBySkuId.getData();
                    if (CollectionUtil.isNotEmpty(wareSkuEntities)) {
                        orderItemVO.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
                    }
                }, threadPoolExecutor);

                CompletableFuture<Void> salesCompletableFuture = CompletableFuture.runAsync(() -> {
                    Resp<List<SaleVo>> listResp = this.gmallSmsClient.querySalesBySkuId(skuId);
                    List<SaleVo> saleVos = listResp.getData();
                    orderItemVO.setSales(saleVos);
                }, threadPoolExecutor);

                CompletableFuture.allOf(skuCompletableFuture, saleAttrCompletableFuture, wareCompletableFuture, salesCompletableFuture).join();

                return orderItemVO;
            }).collect(Collectors.toList());
            confirmVO.setOrderItems(itemVOList);

        }, threadPoolExecutor);

        //查询用户信息，获取积分
        CompletableFuture<Void> memberCompletableFuture = CompletableFuture.runAsync(() -> {
            Resp<MemberEntity> memberEntityResp = this.gmallUmsClient.queryMemberById(userId);
            MemberEntity memberEntity = memberEntityResp.getData();
            confirmVO.setBounds(memberEntity.getIntegration());
        }, threadPoolExecutor);
        //生成唯一标志防止重复提交（响应到页面有一份，有一份保存到redis）
        CompletableFuture<Void> idCompletableFuture = CompletableFuture.runAsync(() -> {
            Snowflake snowflake = IdUtil.createSnowflake(1, 1);
            long id = snowflake.nextId();
            confirmVO.setOrderToken(String.valueOf(id));
            this.stringRedisTemplate.opsForValue().set(TOKEN_PREFIX+id,String.valueOf(id));
        }, threadPoolExecutor);
        CompletableFuture.allOf(addressCompletableFuture,skuBigCompletableFuture,idCompletableFuture).join();
        return confirmVO;
    }

    @Override
    public void submit(OrderSubmitVO orderSubmitVO) {
        UserInfo userInfo = LoginInterceptor.getUserInfo();
        String orderToken = orderSubmitVO.getOrderToken();
        //1.防重复提交 查询redis有没有 有则是第一次，放行 并删除redis中token
        String script="if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        Long flag = this.stringRedisTemplate.execute(new DefaultRedisScript<>(script,Long.class), Arrays.asList(TOKEN_PREFIX + orderToken), orderToken);

        if (flag==0){
            throw  new OrderException("订单不可重复提交");
        }
        //2.校验总价格，总价一致放行
        List<OrderItemVO> items=orderSubmitVO.getItems();
        BigDecimal totalPrice=orderSubmitVO.getTotalPrice();
        if (CollectionUtil.isEmpty(items)){
            throw new OrderException("没有勾选商品，倾倒购物车中勾选");
        }
        BigDecimal currentTotalPrice = items.stream().map(item -> {
            Resp<SkuInfoEntity> skuInfoEntityResp = this.gmallPmsClient.querySkuById(item.getSkuId());
            SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
            if (skuInfoEntity != null) {
                return skuInfoEntity.getPrice().multiply(new BigDecimal(item.getCount()));
            }
            return new BigDecimal(0);

        }).reduce((a, b) -> a.add(b)).get();
        if (currentTotalPrice.compareTo(totalPrice)!=0){
             throw new OrderException("页面已过期，其刷新后再试");
        }


        //3.校验库存是否充足，一次性提示所有库存不够的商品
        List<SkuLockVO> lockVOS = items.stream().map(orderItemVO -> {
            SkuLockVO skuLockVO = new SkuLockVO();
            skuLockVO.setSkuId(orderItemVO.getSkuId());
            skuLockVO.setCount(orderItemVO.getCount());
            skuLockVO.setOrderToken(orderToken);
            return skuLockVO;
        }).collect(Collectors.toList());
        Resp<Object> wareResp = this.gmallWmsClient.checkAndLockStore(lockVOS);
        if (wareResp.getCode()!=0){
            throw new OrderException(wareResp.getMsg());
        }

        //4.下单
        orderSubmitVO.setUserId(userInfo.getId());
        try {
            Resp<OrderEntity> orderEntityResp = this.gmallOmsClient.saveOrder(orderSubmitVO);
            OrderEntity orderEntity = orderEntityResp.getData();
        } catch (Exception e) {
            e.printStackTrace();
            //发送消息给wms,解锁对应的库存
            this.amqpTemplate.convertAndSend("GMALL-ORDER-EXCHANGE","stock.unlock",orderToken);
            throw new OrderException("服务器错误，创建订单失败");
        }
        int i=1/0;

        //5.删除购物车
        Map<String,Object>map=new ConcurrentHashMap<>(16);
        map.put("userId",userInfo.getId());
        List<Long> skuIds = items.stream().map(OrderItemVO::getSkuId).collect(Collectors.toList());
        map.put("skuIds",skuIds);
        this.amqpTemplate.convertAndSend("GMALL-ORDER-EXCHANGE","cart.delete",map);


    }
}
