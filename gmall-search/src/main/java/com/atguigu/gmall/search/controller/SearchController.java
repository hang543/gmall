package com.atguigu.gmall.search.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.search.pojo.SearchParamVO;

import com.atguigu.gmall.search.pojo.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/8
 * @Content:
 **/
@RestController
@RequestMapping("search")
public class SearchController {
    @Autowired
    private SearchService searchService;
    @GetMapping
    public Resp<SearchResponseVo> search(SearchParamVO searchParamVO) {
        SearchResponseVo search = this.searchService.search(searchParamVO);
        return Resp.ok(search);
    }

}
