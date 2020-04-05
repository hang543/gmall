package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import lombok.Data;

import java.util.List;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/4
 * @Content:
 **/
@Data
public class SpuInfoVO extends SpuInfoEntity {
    private List<String> spuImages;
    private List<BaseAttrVO> baseAttrs;
    private List<SkuinfoVO> skus;

}
