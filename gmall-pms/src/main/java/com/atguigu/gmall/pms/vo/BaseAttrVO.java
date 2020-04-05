package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.entity.ProductAttrValueEntity;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/4
 * @Content:
 **/
@Data
public class BaseAttrVO extends ProductAttrValueEntity {
    public void setValueSelected(List<String> selected){
        if (CollectionUtils.isEmpty(selected)){
            return ;
        }
        this.setAttrValue(StringUtils.join(selected,","));
    }
}
