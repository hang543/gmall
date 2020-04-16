package com.atguigu.gmall.wms.config;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/15
 * @Content: 配置死信路由 延时队列等信息
 **/
@Configuration
public class RabbitMqConfig {
    @Bean("WMS-TTL-QUEUE")
    public Queue ttlqueue() {
        Map<String, Object> map = new ConcurrentHashMap<>(3);
        map.put("x-dead-letter-exchange", "GMALL-ORDER-EXCHANGE");
        map.put("x-dead-letter-routing-key", "stock.unlock");
        map.put("x-message-ttl", 9000000);
        return new Queue("WMS-TTL-QUEUE", true, false, false, map);
    }

    @Bean("WMS-TTL-BINDING")
    public Binding queueBuilder() {
        return new Binding("WMS-TTL-QUEUE", Binding.DestinationType.QUEUE, "GMALL-ORDER-EXCHANGE", "stock.ttl", null);
    }
}
