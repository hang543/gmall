package com.atguigu.gmall.order.feign;


import com.atguigu.gmall.cart.api.GmallCartApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/14
 * @Content:
 **/
@FeignClient("cart-service")
public interface GmallCartClient extends GmallCartApi {
}
