package com.atguigu.gmall.auth.controller;


import com.atguigu.core.bean.Resp;
import com.atguigu.core.exception.MemberException;
import com.atguigu.core.utils.CookieUtils;
import com.atguigu.gmall.auth.config.JwtProperties;
import com.atguigu.gmall.auth.service.AuthService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.HttpRequest;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/12
 * @Content:
 **/
@RestController
@RequestMapping("auth")
public class AuthController {
    @Autowired
    private JwtProperties jwtProperties;
    @Autowired
    private AuthService authService;

    @PostMapping("accredit")
    public Resp<Object> accredit(@RequestParam("username") String username, @RequestParam("password") String password,
                                 HttpServletRequest request, HttpServletResponse response) {

        String token = this.authService.accredit(username, password);
        if (StringUtils.isNotEmpty(token)){
            CookieUtils.setCookie(request,response,this.jwtProperties.getCookieName(),token,
                    this.jwtProperties.getExpire()*60);
            return Resp.ok(null);
        }

        throw new MemberException("用户名活密码错误");
    }
}
