package com.atguigu.gmall.wms.vo;

import lombok.Data;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/14
 * @Content:
 **/
@Data
public class SkuLockVO {
    private Long skuId;
    private Integer count;

    private Boolean lock;//锁定状态
    private Long wareSkuId; //锁定库存的Id

    private String orderToken;

}
