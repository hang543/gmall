package com.atguigu.gmall.index.service;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.annotation.GmallCache;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.vo.CategoryVO;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RCountDownLatch;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/9
 * @Content:
 **/
@Service
public class IndexServiceImpl implements IndexService {
    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    private static final String KEY_PREFIX="index:cates";

    @Override
    public List<CategoryEntity> queryLv1Categories() {
        Resp<List<CategoryEntity>> listResp = this.gmallPmsClient.queryCategoriesByPidOrLevel(1, null);
        return listResp.getData();
    }

    @Override
    @GmallCache(value = "index:cates",timeout = 7200,random = 100)
    public List<CategoryVO> querySubCategories(Long pid) {
        //先从redis中查询
//        String json = this.stringRedisTemplate.opsForValue().get(KEY_PREFIX + pid);
//        if(StringUtils.isNotEmpty(json)){
//            return JSON.parseArray(json,CategoryVO.class);
//        }
//        RLock lock = this.redissonClient.getLock("lock" + pid);
//        lock.lock();
//        String json2 = this.stringRedisTemplate.opsForValue().get(KEY_PREFIX + pid);
//        if(StringUtils.isNotEmpty(json2)){
//            lock.unlock();
//            return JSON.parseArray(json2,CategoryVO.class);
//        }

        Resp<List<CategoryVO>> listResp = this.gmallPmsClient.querySubCategories(pid);
        List<CategoryVO> categoryVOS = listResp.getData();
//        this.stringRedisTemplate.opsForValue().set(KEY_PREFIX+pid,JSON.toJSONString(categoryVOS),7+new Random().nextInt(5), TimeUnit.DAYS);
//        lock.unlock();
        return listResp.getData();
    }
//    @Override
//    public synchronized void testLock(){
//        Snowflake snowflake = IdUtil.createSnowflake(1, 1);
//        long id = snowflake.nextId();
//        Boolean lock= this.stringRedisTemplate.opsForValue().setIfAbsent("lock","id",5,TimeUnit.SECONDS);
//
//        if (lock) {
//
//            String numString =this.stringRedisTemplate.opsForValue().get("num");
//            if (StringUtils.isEmpty(numString)){
//            return;
//            }
//            int num=Integer.parseInt(numString);
//            this.stringRedisTemplate.opsForValue().set("num",String.valueOf(++num));
//
//            //防止别的锁误删
//            String script="if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
//            this.stringRedisTemplate.execute(new DefaultRedisScript<>(script), Arrays.asList("lock"),id);
////            if (StringUtils.equals(this.stringRedisTemplate.opsForValue().get("Lock"),String.valueOf(id))) {
////                this.stringRedisTemplate.delete("Lock");
////            }
//        }
//        else {
//            testLock();
//        }
//
//    }

    @Override
    public void testLock() {
        RLock lock = this.redissonClient.getLock("lock");
        lock.lock();
        String numString =this.stringRedisTemplate.opsForValue().get("num");
            if (StringUtils.isEmpty(numString)){
            return;
            }
            int num=Integer.parseInt(numString);
            this.stringRedisTemplate.opsForValue().set("num",String.valueOf(++num));
            lock.unlock();
    }

    @Override
    public String readLock() {
        RReadWriteLock reLock = this.redissonClient.getReadWriteLock("reLock");
        reLock.readLock().lock();
        String test = this.stringRedisTemplate.opsForValue().get("test");
        reLock.readLock().unlock();
        return test;
    }

    @Override
    public String writeLock() {
        Snowflake snowflake = IdUtil.createSnowflake(1, 1);
        long id = snowflake.nextId();
        RReadWriteLock reLock = this.redissonClient.getReadWriteLock("reLock");
        reLock.writeLock().lock();
        this.stringRedisTemplate.opsForValue().set("test",id+"");
        reLock.writeLock().unlock();
        return "数据写入";
    }

    @Override
    public String latch() throws InterruptedException {
        RCountDownLatch latch = this.redissonClient.getCountDownLatch("latch");
        latch.trySetCount(5);
        latch.await();
        return "主业务开始执行";
    }

    @Override
    public String countDown() {
        RCountDownLatch latch = this.redissonClient.getCountDownLatch("latch");
        latch.countDown();

        return "分支业务执行了一次";
    }
}
