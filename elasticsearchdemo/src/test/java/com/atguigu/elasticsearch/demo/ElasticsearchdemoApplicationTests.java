package com.atguigu.elasticsearch.demo;

import com.atguigu.elasticsearch.demo.pojo.User;
import com.atguigu.elasticsearch.demo.repository.UserRepository;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SpringBootTest
class ElasticsearchdemoApplicationTests {
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;
    @Autowired
    private UserRepository  userRepository;
    @Test
    void contextLoads() {
       this.elasticsearchRestTemplate.createIndex(User.class);
       this.elasticsearchRestTemplate.putMapping(User.class);
    }
    @Test
    void testAdd(){
//        this.userRepository.save(new User(1L,"刘安，胸有成竹",20,"123456"));
        List<User>users= Arrays.asList(
                this.userRepository.save(new User(1L,"刘安，胸有成竹",20,"123456")),
                this.userRepository.save(new User(2L,"刘瑞，发挥好扫地恢复",21,"123456")),
                this.userRepository.save(new User(3L,"刘比，糟糕的时光",22,"123456")),
                this.userRepository.save(new User(4L,"刘强，陈国华算法哈",23,"123456")),
                this.userRepository.save(new User(5L,"刘东，这不是孤",24,"123456")),
                this.userRepository.save(new User(6L,"刘西，寻找是多少",25,"123456"))
        );
        this.userRepository.saveAll(users);
    }
    @Test
    void testfindByAgeBetween(){
//        this.userRepository.findByAgeBetween(20,23).forEach(System.out::println);
        this.userRepository.findByNameLike("糟糕").forEach(System.out::println);
    }
    @Test
    void testNativeQuery(){
        //自定义查询构建起
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //构件查询
        queryBuilder.withQuery(QueryBuilders.matchQuery("name","糟糕"));
        //构建分页条件
        queryBuilder.withPageable(PageRequest.of(0,2));
        queryBuilder.withHighlightBuilder(new HighlightBuilder().field("name").preTags("<em>").postTags("</em>"));
        //构建排序条件
        queryBuilder.withSort(SortBuilders.fieldSort("age").order(SortOrder.DESC));
        //执行查询
        Page<User> userPage = this.userRepository.search(queryBuilder.build());
        System.out.println("命中数"+userPage.getTotalElements());
        System.out.println("页数"+userPage.getTotalPages());
        userPage.getContent().forEach(System.out::println);
    }
}
