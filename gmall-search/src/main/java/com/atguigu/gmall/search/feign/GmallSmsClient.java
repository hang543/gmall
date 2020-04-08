package com.atguigu.gmall.search.feign;

import com.atguigu.gmall.api.GmallSmsApi;
import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/7
 * @Content:
 **/
@FeignClient("sms-service")
public interface GmallSmsClient extends GmallSmsApi{
}
