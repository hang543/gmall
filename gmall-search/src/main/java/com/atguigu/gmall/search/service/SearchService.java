package com.atguigu.gmall.search.service;

import com.atguigu.gmall.search.pojo.SearchParamVO;
import com.atguigu.gmall.search.pojo.SearchResponseVo;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/8
 * @Content:
 **/
public interface SearchService {
    public SearchResponseVo search(SearchParamVO searchParamVO) ;
}
