package com.atguigu.gmall.sms.service.impl;

import com.atguigu.gmall.vo.SaleVo;
import com.atguigu.gmall.vo.SkuSaleVO;
import com.atguigu.gmall.sms.dao.SkuFullReductionDao;
import com.atguigu.gmall.sms.dao.SkuLadderDao;
import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.sms.dao.SkuBoundsDao;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import org.springframework.transaction.annotation.Transactional;


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
    @Transactional(rollbackFor = Exception.class)
    public void saveSale(SkuSaleVO skuSaleVO) {
        /**
         *  3.保存营销信息的三张表
         *  3.1保存sms_sku_bounds
         *  3.2保存sms_sku_ladder
         *  3.3保存sms_sku_full_reduction
         *
         */
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        skuBoundsEntity.setSkuId(skuSaleVO.getSkuId());
        skuBoundsEntity.setGrowBounds(skuSaleVO.getGrowBounds());
        skuBoundsEntity.setBuyBounds(skuSaleVO.getBuyBounds());
        List<Integer> work = skuSaleVO.getWork();

        skuBoundsEntity.setWork(work.get(3) * 1 + work.get(2) * 2 + work.get(1) * 4 + work.get(0) * 8);
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

    /**
     * 查询打积分折满减等信息
     * @param skuId
     * @return
     */
    @Override
    public List<SaleVo> querySalesBySkuId(Long skuId) {
        List<SaleVo> saleVOS = new CopyOnWriteArrayList<>();
        //查询积分信息
        SkuBoundsEntity skuBoundsEntity = this.getOne(new QueryWrapper<SkuBoundsEntity>().eq("sku_id", skuId));
        if (skuBoundsEntity != null) {
            SaleVo bandsVO = new SaleVo();
            bandsVO.setType("积分");
            StringBuffer buffer = new StringBuffer();
            if (skuBoundsEntity.getGrowBounds() != null && skuBoundsEntity.getGrowBounds().intValue() > 0) {
                buffer.append("成长积分" + skuBoundsEntity.getGrowBounds());
            }
            if (skuBoundsEntity.getBuyBounds() != null && skuBoundsEntity.getBuyBounds().intValue() > 0) {
                if (StringUtils.isNotEmpty(buffer)) {
                    buffer.append(",");
                }
                buffer.append("赠送积分" + skuBoundsEntity.getBuyBounds());
            }
            bandsVO.setDesc(buffer.toString());
            saleVOS.add(bandsVO);

        }
        //查询打折信息
        SkuLadderEntity skuLadderEntity = this.skuLadderDao.selectOne(new QueryWrapper<SkuLadderEntity>().eq("sku_id", skuId));
        if (skuLadderEntity != null) {
            SaleVo ladderVO = new SaleVo();
            ladderVO.setType("打折");
            if (skuLadderEntity.getFullCount() != null && skuLadderEntity.getDiscount() != null) {
                ladderVO.setDesc("满" + skuLadderEntity.getFullCount() + "件，打" + skuLadderEntity.getDiscount().divide(new BigDecimal(10)) + "折");
            }
            saleVOS.add(ladderVO);

        }

        //查询满减信息
        SkuFullReductionEntity skuFullReductionEntity = this.reductionDao.selectOne(new QueryWrapper<SkuFullReductionEntity>().eq("sku_id", skuId));
           if (skuFullReductionEntity!=null){
               SaleVo reductionVO = new SaleVo();
               reductionVO.setType("满减");
               reductionVO.setDesc("满"+skuFullReductionEntity.getFullPrice()+"减"+skuFullReductionEntity.getReducePrice());
               saleVOS.add(reductionVO);
           }

        return saleVOS;
    }
}