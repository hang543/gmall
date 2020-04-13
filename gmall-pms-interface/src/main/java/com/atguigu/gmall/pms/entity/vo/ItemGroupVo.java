package com.atguigu.gmall.pms.entity.vo;

import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import lombok.Data;

import java.util.List;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/10
 * @Content:
 **/
@Data
public class ItemGroupVo {
    private String name;
    private List<ProductAttrValueEntity> baseAttrs;
}
