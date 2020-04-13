package com.atguigu.gmall.cart.feign;

import com.atguigu.gmall.wms.api.GmallWmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/11
 * @Content:
 **/
@FeignClient("wms-service")
public interface GmallWmsClient extends GmallWmsApi {
}
