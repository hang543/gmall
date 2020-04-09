package com.atguigu.gmall.search.listener;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchAttr;
import com.atguigu.gmall.search.repository.GoodRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/9
 * @Content:
 **/
@Component
public class GoodsListener {

    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private GmallWmsClient wmsClient;
    @Autowired
    private GoodRepository goodRepository;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "gmall-seatch-queue", durable = "ture"),
            exchange = @Exchange(value = "GMALL-PMS-EXCHANGE", type = ExchangeTypes.TOPIC, ignoreDeclarationExceptions = "ture"),
            key = {"item.insert", "item.update"}
    ))
    private void listner(Long spuId) {
        Resp<List<SkuInfoEntity>> skuResp = this.pmsClient.querySkuBySpuId(spuId);
        List<SkuInfoEntity> skuInfoEntities = skuResp.getData();
        if (!CollectionUtils.isEmpty(skuInfoEntities)) {
            //把sku对象转化成goods对象
            List<Goods> goodsList = skuInfoEntities.stream().map(skuInfoEntity -> {
                Goods goods = new Goods();
                //查询搜索属性及值
                Resp<List<ProductAttrValueEntity>> attrValuResp = this.pmsClient.querySearchAttrValueBySpuId(spuId);
                List<ProductAttrValueEntity> attrValueEntities = attrValuResp.getData();
                if (!CollectionUtils.isEmpty(attrValueEntities)) {
                    List<SearchAttr> searchAttrs = attrValueEntities.stream().map(productAttrValueEntity -> {
                        SearchAttr searchAttr = new SearchAttr();
                        searchAttr.setAttrId(productAttrValueEntity.getAttrId());
                        searchAttr.setAttrName(productAttrValueEntity.getAttrName());
                        searchAttr.setAttrValue(productAttrValueEntity.getAttrValue());
                        return searchAttr;
                    }).collect(Collectors.toList());
                    goods.setAttrs(searchAttrs);
                }

//                        //查询品牌
//
                Resp<BrandEntity> brandEntityResp = this.pmsClient.queryBrandById(skuInfoEntity.getBrandId());
                BrandEntity brandEntity = brandEntityResp.getData();
                if (brandEntity != null) {
                    goods.setBrandName(brandEntity.getName());
                    goods.setBrandId(skuInfoEntity.getBrandId());
                }
                //设置分类
                Resp<CategoryEntity> categoryEntityResp = this.pmsClient.queryCategoryById(skuInfoEntity.getCatalogId());
                CategoryEntity categoryEntity = categoryEntityResp.getData();
                if (categoryEntityResp != null) {
                    goods.setCategoryId(skuInfoEntity.getCatalogId());
                    goods.setCategoryName(categoryEntity.getName());
                }
                Resp<SpuInfoEntity> spuInfoEntityResp = this.pmsClient.querySpuById(spuId);
                if (spuInfoEntityResp!=null) {
                    SpuInfoEntity spuInfoEntity = spuInfoEntityResp.getData();
                    goods.setCreateTime(spuInfoEntity.getCreateTime());
                }
                goods.setPic(skuInfoEntity.getSkuDefaultImg());
                goods.setPrice(skuInfoEntity.getPrice().doubleValue());

                goods.setSale(0L);
                goods.setSkuId(skuInfoEntity.getSkuId());

                /**
                 * 查询库存
                 */
                Resp<List<WareSkuEntity>> listResp = this.wmsClient.queryWareSkusBySkuId(skuInfoEntity.getSkuId());
                List<WareSkuEntity> wareSkuEntities = listResp.getData();
                if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                    boolean flag = wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock() > 0);

                    goods.setStore(flag);
                }
                goods.setTitle(skuInfoEntity.getSkuTitle());
                return goods;
            }).collect(Collectors.toList());
            this.goodRepository.saveAll(goodsList);
        }
    }
}
