package com.atguigu.gmall.ums.api;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.ums.entity.MemberEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/12
 * @Content:
 **/
public interface GmallUmsApi {
    @GetMapping("ums/member/query")
    public Resp<MemberEntity> queryUser(@RequestParam("username") String username, @RequestParam("password") String password);
}
