package com.atguigu.gmall.item.vo;

import com.atguigu.gmall.pms.entity.BrandEntity;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.SkuImagesEntity;
import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.pms.entity.vo.ItemGroupVo;
import com.atguigu.gmall.vo.SaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/10
 * @Content: 商品详细页信息
 **/
@Data
public class ItemVo {
    private long skuId;
    private CategoryEntity categoryEntity;
    private BrandEntity brandEntity;
    private Long spuId;
    private String spuName;

    private String skuTitle;
    private String subTitle;
    private BigDecimal price;
    private BigDecimal weight;

    private List<SkuImagesEntity> pice;
    private List<SaleVo> sales;

    private Boolean store;

    private List<SkuSaleAttrValueEntity> saleAttrs;

    private List<String> images;//spu的海报

    private List<ItemGroupVo> groups;//规格参数组及组下的规格参数（带值）
}
