package com.atguigu.gmall.cart.listener;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.cart.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/13
 * @Content:
 **/
@Component
public class CartListener {
    private static final String PRICE_PREDFIX = "gmall:sku";
    private static final String KEY_PREDFIX = "gmall:cart";

    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 保证商品价格的最终一致
     *
     * @param spuId
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "CART-ITEM-QUEUE", durable = "true"),
            exchange = @Exchange(value = "GMALL-PMS-EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"item.update"}
    ))

    public void listner(Long spuId) {
        Resp<List<SkuInfoEntity>> skuBySpuId = this.gmallPmsClient.querySkuBySpuId(spuId);
        List<SkuInfoEntity> skuInfoEntities = skuBySpuId.getData();
        skuInfoEntities.forEach(skuInfoEntity -> {
            this.stringRedisTemplate.opsForValue().set(PRICE_PREDFIX + skuInfoEntity.getSkuId(), skuInfoEntity.getPrice().toString());
        });
    }

    /**
     * 删除订单
     *
     * @param map
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "ORDER-CART-QUEUE", durable = "true"),
            exchange = @Exchange(value = "GMALL-ORDER-EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"cart.delete"}
    ))
    public void deleteListener(Map<String, Object> map) {
        Long userId = (Long) map.get("userId");
        List<Object> skuIds = (List<Object>) map.get("skuIds");
        BoundHashOperations<String, Object, Object> boundHashOps = this.stringRedisTemplate.boundHashOps(KEY_PREDFIX + userId);
        List<String> skus = skuIds.stream().map(s -> s.toString()).collect(Collectors.toList());
        String[] strings = skus.toArray(new String[skus.size()]);
        boundHashOps.delete(strings);

    }

}
