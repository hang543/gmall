package com.atguigu.gmall.pms.feign;

import com.atguigu.gmall.api.GmallSmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/4
 * @Content:
 **/
@FeignClient("sms-service")
public interface GmallSmsClients extends GmallSmsApi {

}
