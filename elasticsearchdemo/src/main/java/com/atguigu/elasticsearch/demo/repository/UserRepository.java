package com.atguigu.elasticsearch.demo.repository;

import com.atguigu.elasticsearch.demo.pojo.User;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/7
 * @Content:
 **/
public interface UserRepository extends ElasticsearchRepository<User, Long> {
    List<User> findByAgeBetween(Integer age1, Integer age2);

    List<User> findByNameLike(String name);
}
