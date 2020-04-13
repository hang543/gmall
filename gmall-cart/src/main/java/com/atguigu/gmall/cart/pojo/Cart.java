package com.atguigu.gmall.cart.pojo;

import com.atguigu.gmall.pms.entity.SkuSaleAttrValueEntity;
import com.atguigu.gmall.vo.SaleVo;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/13
 * @Content:
 **/
@Data
public class Cart {
    private Long skuId;
    private String title;
    private String defaultImage;
    private BigDecimal price;
    private Integer count;
    private Boolean store;
    private List<SkuSaleAttrValueEntity> saleAttrValues;
    private List<SaleVo> sales;
    private Boolean check;
}
