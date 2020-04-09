package com.atguigu.gmall.index.feign;

import com.atguigu.gmall.pms.api.GmallPmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/9
 * @Content:
 **/
@FeignClient("pms-service")
public interface GmallPmsClient extends GmallPmsApi {
}
