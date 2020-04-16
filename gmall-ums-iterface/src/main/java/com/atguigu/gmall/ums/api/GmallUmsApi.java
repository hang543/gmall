package com.atguigu.gmall.ums.api;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/12
 * @Content:
 **/
public interface GmallUmsApi {
    @GetMapping("ums/member/query")
    public Resp<MemberEntity> queryUser(@RequestParam("username") String username, @RequestParam("password") String password);

    @GetMapping("ums/memberreceiveaddress/{userId}")
    public Resp<List<MemberReceiveAddressEntity>> queryAdderessesByUserId(@PathVariable("userId") Long userId);

    @GetMapping("ums/member/info/{id}")
    public Resp<MemberEntity> queryMemberById(@PathVariable("id") Long id);
}
