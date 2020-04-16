package com.atguigu.gmall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.wms.vo.SkuLockVO;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.wms.dao.WareSkuDao;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.service.WareSkuService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {
    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private WareSkuDao wareSkuDao;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private AmqpTemplate amqpTemplate;

    private static  final  String KEY_PREFIX="stock:lock";

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageVo(page);
    }

    @Override
    @Transactional
    public String checkAndLockStore(List<SkuLockVO> skuLockVOs) {
        if (CollectionUtils.isEmpty(skuLockVOs)) {
            return "没有选中的商品";
        }
        //检验并锁定
        skuLockVOs.forEach(skuLockVO -> {
            lockStore(skuLockVO);
        });
        List<SkuLockVO> unlockVOS = skuLockVOs.stream().filter(skuLockVO -> skuLockVO.getLock() == false).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(unlockVOS)){
            List<SkuLockVO> lockVOS = skuLockVOs.stream().filter(SkuLockVO::getLock).collect(Collectors.toList());
            lockVOS.forEach(skuLockVO -> {
                this.wareSkuDao.unLockSore(skuLockVO.getWareSkuId(),skuLockVO.getCount());
            });
            //提示锁定失败的商品
            List<Long> skuIds = unlockVOS.stream().map(SkuLockVO::getSkuId).collect(Collectors.toList());
            return "下单失败，商品库存不足"+skuIds.toString();
        }
        String orderToken = skuLockVOs.get(0).getOrderToken();
        this.stringRedisTemplate.opsForValue().set(KEY_PREFIX+orderToken, JSON.toJSONString(skuLockVOs));
         //锁定成功发送延时消息定时解锁
        this.amqpTemplate.convertAndSend("GMALL-ORDER-EXCHANGE","stock.ttl",orderToken);
        return null;
    }

    //使用分布式锁 校验和锁定
    private void lockStore(SkuLockVO skuLockVO) {
        RLock lock = this.redissonClient.getLock("stock" + skuLockVO.getSkuId());
        lock.lock();
        //查询剩余库存够不够
       List<WareSkuEntity> wareSkuEntities= this.wareSkuDao.checkStore(skuLockVO.getSkuId(),skuLockVO.getCount());
       if (!CollectionUtils.isEmpty(wareSkuEntities)){
           //锁定库存信息
           Long id = wareSkuEntities.get(0).getId();
           this.wareSkuDao.lockStore(id,skuLockVO.getCount());
           skuLockVO.setLock(true);
           skuLockVO.setWareSkuId(id);

       }else {
           skuLockVO.setLock(false);
       }
        lock.unlock();
    }

}