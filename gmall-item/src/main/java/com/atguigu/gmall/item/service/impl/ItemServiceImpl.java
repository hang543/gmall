package com.atguigu.gmall.item.service.impl;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.item.annotation.GmallCache;
import com.atguigu.gmall.item.feign.GmallPmsClient;
import com.atguigu.gmall.item.feign.GmallSmsClient;
import com.atguigu.gmall.item.feign.GmallWmsClient;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.item.vo.ItemVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.entity.vo.ItemGroupVo;
import com.atguigu.gmall.vo.SaleVo;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/11
 * @Content:
 **/
@Service
public class ItemServiceImpl implements ItemService {
    @Autowired
    private GmallPmsClient gmallPmsClient;
    @Autowired
    private GmallSmsClient gmallSmsClient;
    @Autowired
    private GmallWmsClient gmallWmsClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    @GmallCache(value = "item:queryVO",timeout = 7200,random = 100)
    public ItemVo queryItemVo(Long skuId) {
        ItemVo itemVo = new ItemVo();
        itemVo.setSkuId(skuId);
        CompletableFuture<Object> skucompletableFuture = CompletableFuture.supplyAsync(() -> {
            //根据id查询sku
            Resp<SkuInfoEntity> skuResp = this.gmallPmsClient.querySkuById(skuId);
            SkuInfoEntity skuInfoEntity = skuResp.getData();
            if (skuInfoEntity == null) {
                return itemVo;
            }
            itemVo.setSkuTitle(skuInfoEntity.getSkuTitle());
            itemVo.setSubTitle(skuInfoEntity.getSkuSubtitle());
            itemVo.setPrice(skuInfoEntity.getPrice());
            itemVo.setWeight(skuInfoEntity.getWeight());
            itemVo.setSpuId(skuInfoEntity.getSpuId());
            //获取spuID
            return skuInfoEntity;
        }, threadPoolExecutor);
        skucompletableFuture.thenAcceptAsync(sku -> {
            //根据sku中spuId查询spu
            Resp<SpuInfoEntity> spuResp = this.gmallPmsClient.querySpuById(((SkuInfoEntity) sku).getSpuId());
            SpuInfoEntity spuInfoEntity = spuResp.getData();

            if (spuInfoEntity != null) {
                itemVo.setSpuName(spuInfoEntity.getSpuName());
            }
        }, threadPoolExecutor);


        //根据skuId查询图片列表
        CompletableFuture<Void> imageCompletableFuture = CompletableFuture.runAsync(() -> {
            Resp<List<SkuImagesEntity>> listResp = this.gmallPmsClient.querySkuImagesBySkuId(skuId);
            List<SkuImagesEntity> skuImagesEntities = listResp.getData();
            itemVo.setPice(skuImagesEntities);
        }, threadPoolExecutor);

        //根据sku中的brandid和categoryId查询品牌和分类
        CompletableFuture<Void> brandCompletableFuture = skucompletableFuture.thenAcceptAsync(sku -> {
            Resp<BrandEntity> brandEntityResp = this.gmallPmsClient.queryBrandById(((SkuInfoEntity) sku).getBrandId());
            BrandEntity brandEntity = brandEntityResp.getData();
            itemVo.setBrandEntity(brandEntity);
        }, threadPoolExecutor);

        //设置分类
        CompletableFuture<Void> cateCompletableFuture = skucompletableFuture.thenAcceptAsync(sku -> {
            Resp<CategoryEntity> categoryEntityResp = this.gmallPmsClient.queryCategoryById(((SkuInfoEntity) sku).getCatalogId());
            CategoryEntity categoryEntity = categoryEntityResp.getData();
            itemVo.setCategoryEntity(categoryEntity);
        }, threadPoolExecutor);


        //根据skuid查询营销信息
        CompletableFuture<Void> salesCompletableFuture = CompletableFuture.runAsync(() -> {
            Resp<List<SaleVo>> seleResp = this.gmallSmsClient.querySalesBySkuId(skuId);
            List<SaleVo> saleVoList = seleResp.getData();
            itemVo.setSales(saleVoList);
        }, threadPoolExecutor);


        //根据skuid查询库存信息
        CompletableFuture<Void> wareCompletableFuture = CompletableFuture.runAsync(() -> {
            Resp<List<WareSkuEntity>> wareResp = this.gmallWmsClient.queryWareSkusBySkuId(skuId);
            List<WareSkuEntity> wareSkuEntities = wareResp.getData();
            itemVo.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0));
        }, threadPoolExecutor);


        //根据spuid查询所有skuids,再去查询所有的销售属性
        CompletableFuture<Void> saleAttrsCompletableFuture = skucompletableFuture.thenAcceptAsync(sku -> {
            Resp<List<SkuSaleAttrValueEntity>> skuSaleAttrValues = this.gmallPmsClient.querySkuSaleAttrValuesById(((SkuInfoEntity) sku).getSpuId());
            List<SkuSaleAttrValueEntity> skuSaleAttrValuesData = skuSaleAttrValues.getData();
            itemVo.setSaleAttrs(skuSaleAttrValuesData);
        }, threadPoolExecutor);


        //根据spuId查询商品 的海报信息
        CompletableFuture<Void> spuInfoDescCompletableFuture = skucompletableFuture.thenAcceptAsync(sku -> {
            Resp<SpuInfoDescEntity> spuInfoDescEntityResp = this.gmallPmsClient.querySpuDescBySpuId(((SkuInfoEntity) sku).getSpuId());
            SpuInfoDescEntity spuInfoDescEntityRespData = spuInfoDescEntityResp.getData();
            if (spuInfoDescEntityRespData != null) {
                String decript = spuInfoDescEntityRespData.getDecript();
                String[] split = StringUtils.split(decript, ",");
                itemVo.setImages(Arrays.asList(split));
            }
        }, threadPoolExecutor);


        //根据spuid和categoryId查询组及组下的规格参数
        CompletableFuture<Void> itemGroupCompletableFuture = skucompletableFuture.thenAcceptAsync(sku -> {
            Resp<List<ItemGroupVo>> itemGroupVOByCidAndSpuid = this.gmallPmsClient.queryItemGroupVOByCidAndSpuid(((SkuInfoEntity) sku).getCatalogId(), ((SkuInfoEntity) sku).getSpuId());
            List<ItemGroupVo> itemGroupVOByCidAndSpuidData = itemGroupVOByCidAndSpuid.getData();

            itemVo.setGroups(itemGroupVOByCidAndSpuidData);
        }, threadPoolExecutor);

        CompletableFuture.allOf(
                skucompletableFuture, imageCompletableFuture, brandCompletableFuture,
                cateCompletableFuture, salesCompletableFuture, wareCompletableFuture,
                saleAttrsCompletableFuture, spuInfoDescCompletableFuture,
                itemGroupCompletableFuture).join();
        return itemVo;


    }
}
