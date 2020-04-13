package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.dao.SkuInfoDao;
import com.atguigu.gmall.pms.dao.SpuInfoDescDao;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.feign.GmallSmsClients;
import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.pms.vo.BaseAttrVO;
import com.atguigu.gmall.vo.SkuSaleVO;
import com.atguigu.gmall.pms.vo.SkuinfoVO;
import com.atguigu.gmall.pms.vo.SpuInfoVO;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;

import com.atguigu.gmall.pms.dao.SpuInfoDao;
import org.springframework.util.CollectionUtils;


@Service("spuInfoService")
public class SpuInfoServiceImpl extends ServiceImpl<SpuInfoDao, SpuInfoEntity> implements SpuInfoService {
    @Autowired
    private SpuInfoDescDao spuInfoDescDao;
    @Autowired
    private ProductAttrValueService productAttrValueService;
    @Autowired
    private SkuInfoDao skuInfoDao;
    @Autowired
    private SkuImagesService skuImagesService;
    @Autowired
    private SkuSaleAttrValueService skuSaleAttrValueService;
    @Autowired
    private GmallSmsClients gmallSmsClients;
    @Autowired
    private SpuInfoDescService saveSpuInfoDesc;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Value("${item.rabbitmq.exchange}")
    private String EXCHANGE_NAME;

    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(params),
                new QueryWrapper<SpuInfoEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public PageVo querySpuPage(QueryCondition queryCondition, Long cid) {
        QueryWrapper<SpuInfoEntity> wrapper = new QueryWrapper<>();
        if (cid != 0) {
            wrapper.eq("catalog_id", cid);
        }
        String key = queryCondition.getKey();
        if (StringUtils.isNotBlank(key)) {
            wrapper.and(t -> t.eq("id", key).or().like("spu_name", key));
        }
        IPage<SpuInfoEntity> page = this.page(
                new Query<SpuInfoEntity>().getPage(queryCondition),
                wrapper
        );
        return new PageVo(page);
    }
    @GlobalTransactional(name = "fsp_tx_group" ,rollbackFor = Exception.class)
    @Override
    public void bigSave(SpuInfoVO spuInfoVo) {
        /**
         * 1.保存spu的三张表
         * 1.1保存pms_spu_info
         * 1.2保存pms_spu_info_desc
         * 1.3保存pms_product_attr_value
         *
         */
        Long spuInfoId = saveSpuInfo(spuInfoVo);
        //*********************

        this.saveSpuInfoDesc.saveSpuInfoDesc(spuInfoVo, spuInfoId);
        //******************
        saveBaseAttrVlue(spuInfoVo, spuInfoId);


        saveSkuAndSale(spuInfoVo, spuInfoId);
        sendMsg("insert",spuInfoId);

    }
     /**
      *发送消息
      */
    private void sendMsg(String type,Long spuId){
        this.amqpTemplate.convertAndSend(EXCHANGE_NAME,"item."+type,spuId);
    }

    private void saveSkuAndSale(SpuInfoVO spuInfoVo, Long spuInfoId) {
        List<SkuinfoVO> skus = spuInfoVo.getSkus();
        if (CollectionUtils.isEmpty(skus)){
            return;
        }
        skus.forEach(skuinfoVO -> {
            /**
             * 2.保存sku的三张表
             * 2.1保存pms_sku_info
             * 2.2保存pms_sku_images
             * 2.3保存pms_sku_attr_value
             */
            skuinfoVO.setSpuId(spuInfoId);
            skuinfoVO.setSkuCode(UUID.randomUUID().toString());
            skuinfoVO.setBrandId(spuInfoVo.getBrandId());
            skuinfoVO.setCatalogId(spuInfoVo.getCatalogId());
            List<String> images = skuinfoVO.getImages();
            if (!CollectionUtils.isEmpty(images)) {
                skuinfoVO.setSkuDefaultImg(StringUtils.isNotBlank(skuinfoVO.getSkuDefaultImg())?skuinfoVO.getSkuDefaultImg():images.get(0));
            }
            this.skuInfoDao.insert(skuinfoVO);
            Long skuId = skuinfoVO.getSkuId();
            //**********************2**********************
            if (!CollectionUtils.isEmpty(images)){
                List<SkuImagesEntity> skuImagesEntities = images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setImgUrl(image);
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setDefaultImg(StringUtils.equals(skuinfoVO.getSkuDefaultImg(), image) ? 1 : 0);
                    return skuImagesEntity;
                }).collect(Collectors.toList());
                this.skuImagesService.saveBatch(skuImagesEntities);


            }
            //*********************3*************************************
            List<SkuSaleAttrValueEntity> saleAttrs = skuinfoVO.getSaleAttrs();
            if (!CollectionUtils.isEmpty(saleAttrs)){
                saleAttrs.forEach(skuSaleAttrValueEntity -> skuSaleAttrValueEntity.setSkuId(skuId));
                this.skuSaleAttrValueService.saveBatch(saleAttrs);
            }


            /**
             * fei远程调用
             *
             */
            SkuSaleVO skuSaleVO = new SkuSaleVO();
            BeanUtils.copyProperties(skuinfoVO,skuSaleVO);
            skuSaleVO.setSkuId(skuId);
            this.gmallSmsClients.saveSale(skuSaleVO);

        });
    }

    private void saveBaseAttrVlue(SpuInfoVO spuInfoVo, Long spuInfoId) {
        List<BaseAttrVO> baseAttrs = spuInfoVo.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)) {
            List<ProductAttrValueEntity> entityList = baseAttrs.stream().map(baseAttrVO -> {
                ProductAttrValueEntity productAttrValueEntity = baseAttrVO;
                productAttrValueEntity.setSpuId(spuInfoId);
                return productAttrValueEntity;
            }).collect(Collectors.toList());
            this.productAttrValueService.saveBatch(entityList);
        }
    }


    private Long saveSpuInfo(SpuInfoVO spuInfoVo) {
        spuInfoVo.setCreateTime(new Date());
        spuInfoVo.setUodateTime(spuInfoVo.getCreateTime());
        this.save(spuInfoVo);
        return spuInfoVo.getId();
    }
}