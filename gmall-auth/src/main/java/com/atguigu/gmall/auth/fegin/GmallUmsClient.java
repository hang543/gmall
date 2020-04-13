package com.atguigu.gmall.auth.fegin;

import com.atguigu.gmall.ums.api.GmallUmsApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/12
 * @Content:
 **/
@FeignClient("ums-service")
public interface GmallUmsClient extends GmallUmsApi {

}
