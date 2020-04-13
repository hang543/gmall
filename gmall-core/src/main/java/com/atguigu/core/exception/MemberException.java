package com.atguigu.core.exception;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/12
 * @Content:
 **/
public class MemberException extends RuntimeException {

    public MemberException(String message) {
        super(message);
    }

    public MemberException() {
        super();
    }
}
