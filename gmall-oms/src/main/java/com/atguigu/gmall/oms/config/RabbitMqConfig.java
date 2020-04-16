package com.atguigu.gmall.oms.config;


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
    @Bean("ORDER-TTL-QUEUE")
    public Queue ttlqueue(){
        Map<String,Object>map =new ConcurrentHashMap<>(3);
         map.put("x-dead-letter-exchange","GMALL-ORDER-EXCHANGE");
         map.put("x-dead-letter-routing-key","order.dead");
         map.put("x-message-ttl",1200000);
        return new Queue("ORDER-TTL-QUEUE",true,false,false,map);
    }
    @Bean("ORDER-TTL-BINDING")
    public Binding queueBuilder(){
        return new Binding("ORDER-TTL-QUEUE",Binding.DestinationType.QUEUE,"GMALL-ORDER-EXCHANGE","order.ttl",null);
    }
    @Bean("ORDER-DEAD-QUEUE")
    public Queue dlQueue(){
     return new Queue("ORDER-DEAD-QUEUE",true,false,false,null);
    }
    @Bean("ORDER-DEAD-BINDING")
    public Binding deadQueueBing(){
        return new Binding("ORDER-DEAD-QUEUE",Binding.DestinationType.QUEUE,"GMALL-ORDER-EXCHANGE","order.dead",null);

    }
}
