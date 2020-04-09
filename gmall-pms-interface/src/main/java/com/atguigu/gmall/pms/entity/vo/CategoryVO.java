package com.atguigu.gmall.pms.entity.vo;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import lombok.Data;

import java.util.List;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/9
 * @Content:
 **/
@Data
public class CategoryVO extends CategoryEntity {
    private List<CategoryEntity> subs;
}
