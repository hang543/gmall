package com.atguigu.gmall.search.repository;

import com.atguigu.gmall.search.pojo.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/7
 * @Content:
 **/
public interface GoodRepository extends ElasticsearchRepository<Goods,Long> {
}
