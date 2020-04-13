package com.atguigu.gmall.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/4
 * @Content:
 **/
@Data
public class SkuSaleVO {

    private Long skuId;
    /**
     * 积分营销相关
     */

    private BigDecimal growBounds;
    private BigDecimal buyBounds;
    private List<Integer> work;

    /**
     * 打折相关字段
     */

    private Integer fullCount;

    private BigDecimal discount;

    private Integer ladderAddOther;

    /**
     * 满减的相关字段
     */

    private BigDecimal fullPrice;

    private BigDecimal reducePrice;

    private Integer fullAddOther;
}
