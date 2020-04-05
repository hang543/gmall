package com.atguigu.gmall.sms.service.impl;

import com.atguigu.gmall.sms.SkuSaleVO;
import com.atguigu.gmall.sms.dao.SkuFullReductionDao;
import com.atguigu.gmall.sms.dao.SkuLadderDao;
import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.sms.dao.SkuBoundsDao;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsDao, SkuBoundsEntity> implements SkuBoundsService {
     @Autowired
    private SkuLadderDao skuLadderDao;
     @Autowired
     private SkuFullReductionDao reductionDao;
    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SkuBoundsEntity> page = this.page(
                new Query<SkuBoundsEntity>().getPage(params),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public void saveSale(SkuSaleVO skuSaleVO) {
        /**
         *  3.保存营销信息的三张表
         *  3.1保存sms_sku_bounds
         *  3.2保存sms_sku_ladder
         *  3.3保存sms_sku_full_reduction
         *
         */
        SkuBoundsEntity skuBoundsEntity=new SkuBoundsEntity();
        skuBoundsEntity.setSkuId(skuSaleVO.getSkuId());
        skuBoundsEntity.setGrowBounds(skuSaleVO.getGrowBounds());
        skuBoundsEntity.setBuyBounds(skuSaleVO.getBuyBounds());
        List<Integer> work = skuSaleVO.getWork();

        skuBoundsEntity.setWork(work.get(3)*1+work.get(2)*2+work.get(1)*4+work.get(0)*8);
        this.save(skuBoundsEntity);
        /*********************2********************************
         *
         */
        SkuLadderEntity skuLadderEntity = new SkuLadderEntity();
         skuLadderEntity.setSkuId(skuSaleVO.getSkuId());
         skuLadderEntity.setAddOther(skuSaleVO.getLadderAddOther());
         skuLadderEntity.setDiscount(skuSaleVO.getDiscount());
         skuLadderEntity.setFullCount(skuSaleVO.getFullCount());

        this.skuLadderDao.insert(skuLadderEntity);

        /************************3*************************************************************
         *
         */
        SkuFullReductionEntity skuFullReductionEntity = new SkuFullReductionEntity();
        skuFullReductionEntity.setAddOther(skuSaleVO.getFullAddOther());
        skuFullReductionEntity.setFullPrice(skuSaleVO.getFullPrice());
        skuFullReductionEntity.setReducePrice(skuSaleVO.getReducePrice());
        skuFullReductionEntity.setSkuId(skuSaleVO.getSkuId());
        this.reductionDao.insert(skuFullReductionEntity);

    }
}