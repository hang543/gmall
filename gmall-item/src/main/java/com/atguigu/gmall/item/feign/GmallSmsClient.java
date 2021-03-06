package com.atguigu.gmall.item.feign;

import com.atguigu.gmall.api.GmallSmsApi;
import com.atguigu.gmall.wms.api.GmallWmsApi;
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
