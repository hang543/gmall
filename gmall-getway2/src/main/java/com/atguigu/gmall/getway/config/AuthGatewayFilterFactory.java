package com.atguigu.gmall.getway.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;
import org.springframework.stereotype.Component;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/12
 * @Content:
 **/
@Component
public class AuthGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {
    @Autowired
    private AuthGatewayFilter authGatewayFilter;
    @Override
    public GatewayFilter apply(Object config) {
        return authGatewayFilter;
    }
}
