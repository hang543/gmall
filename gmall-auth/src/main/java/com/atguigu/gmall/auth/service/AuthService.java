package com.atguigu.gmall.auth.service;

import org.springframework.stereotype.Service;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/12
 * @Content:
 **/
@Service
public interface AuthService {
    String accredit(String username, String password);
}
