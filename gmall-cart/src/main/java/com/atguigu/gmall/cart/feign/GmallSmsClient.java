package com.atguigu.gmall.cart.feign;

import com.atguigu.gmall.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/11
 * @Content:
 **/
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi {
}
