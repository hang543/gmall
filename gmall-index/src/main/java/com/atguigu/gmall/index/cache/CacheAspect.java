package com.atguigu.gmall.index.cache;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.index.annotation.GmallCache;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/10
 * @Content:实现注解功能
 **/
@Aspect
@Component
public class CacheAspect {
        @Autowired
        private StringRedisTemplate redisTemplate;
        @Autowired
        private RedissonClient redissonClient;

        @Around("@annotation(com.atguigu.gmall.index.annotation.GmallCache)")
        public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
                Object result=null;
                //获取目标方法
                MethodSignature signature = (MethodSignature)joinPoint.getSignature();
                Method method = signature.getMethod();
                GmallCache gmallCache = method.getAnnotation(GmallCache.class);
                //获取注解中的缓存前缀
                String value=gmallCache.value();
                //获取目标方法的返回值
                Class<?> returnType = method.getReturnType();
                //获取目标方法的参数列表
                Object[] args = joinPoint.getArgs();
                String key= value+Arrays.asList(args).toString();
                //从缓存中查询
                result= this.cacheHit(key,returnType);
                if (result!=null){
                        return  result;
                }
                //没有命中加分布式锁
                RLock lock = this.redissonClient.getLock("lock" + Arrays.asList(args).toString());
                lock.lock();
                result= this.cacheHit(key,returnType);
                if (result!=null){
                        lock.unlock();
                        return  result;
                }
                //再次查询缓存，如果缓存中没有  执行目标方法
                result = joinPoint.proceed(args);
                //放入分布式缓存
                int timeout = gmallCache.timeout();
                int random = gmallCache.random();
                this.redisTemplate.opsForValue().set(key,JSON.toJSONString(result),(int)(Math.random()*random), TimeUnit.MINUTES);
                lock.unlock();
                return result;
        }

        public Object cacheHit(String key,Class<?> returnType){
                String json = this.redisTemplate.opsForValue().get(key);
                //命中 直接返回
                if (StringUtils.isNotEmpty(json)){
                        return JSON.parseObject(json,returnType);
                }
                return null;
        }
}
