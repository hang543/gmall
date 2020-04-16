package com.atguigu.gmall.wms.listener;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.wms.dao.WareSkuDao;
import com.atguigu.gmall.wms.vo.SkuLockVO;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/15
 * @Content:
 **/
@Component
public class WareListener {
    private static  final  String KEY_PREFIX="stock:lock";
    @Autowired
    private WareSkuDao wareSkuDao;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "WMS-UNLOCK-QUEUE",durable = "true"),
            exchange = @Exchange(value = "GMALL-ORDER-EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"stock.unlock"}
    ))
    public void unlockListener(String orderToken){
        String lockJson = this.stringRedisTemplate.opsForValue().get(KEY_PREFIX + orderToken);
        List<SkuLockVO> skuLockVOS = JSON.parseArray(lockJson, SkuLockVO.class);
        skuLockVOS.forEach(skuLockVO -> {
            this.wareSkuDao.unLockSore(skuLockVO.getWareSkuId(),skuLockVO.getCount());
        });
    }

    /**
     * 监听下单成功后减库存
     * @param orderToken
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "WMS-MINUS-QUEUE",durable = "true"),
            exchange = @Exchange(value = "GMALL-ORDER-EXCHANGE",ignoreDeclarationExceptions = "true",type = ExchangeTypes.TOPIC),
            key = {"stock.minus"}
    ))
    public void minusStoreListener(String orderToken){
        String lockJson = this.stringRedisTemplate.opsForValue().get(KEY_PREFIX + orderToken);
        List<SkuLockVO> skuLockVOS = JSON.parseArray(lockJson, SkuLockVO.class);
        skuLockVOS.forEach(skuLockVO -> {
            this.wareSkuDao.minusSore(skuLockVO.getWareSkuId(),skuLockVO.getCount());
        });
    }




}
