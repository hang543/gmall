package com.atguigu.gmall.search.service.impl;

import cn.hutool.json.JSONUtil;
import com.alibaba.nacos.client.utils.JSONUtils;
import com.atguigu.gmall.search.pojo.Goods;
import com.atguigu.gmall.search.pojo.SearchParamVO;
import com.atguigu.gmall.search.pojo.SearchResponseAttrVO;
import com.atguigu.gmall.search.pojo.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.*;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/8
 * @Content:
 **/
@Service
public class SearchServiceImpl implements SearchService {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public SearchResponseVo search(SearchParamVO searchParamVO) {
        SearchResponse response = null;
        try {
            SearchRequest searchRequest = this.buildQueryDsl(searchParamVO);
            response = this.restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            System.out.println(response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        SearchResponseVo responseVo = this.parseSearchResult(response);
        responseVo.setPageNum(searchParamVO.getPageNum());
        responseVo.setPageSize(searchParamVO.getPageSize());

        return responseVo;
    }

    /**
     * 解析查询的值
     *
     * @param response
     * @return
     */
    private SearchResponseVo parseSearchResult(SearchResponse response) {
        SearchResponseVo responseVo = new SearchResponseVo();

        //获取总记录数
        SearchHits hits = response.getHits();
        responseVo.setTotal(hits.getTotalHits());
        /**
         * 设置品牌
         */
        //解析品牌的聚合结果
        SearchResponseAttrVO brand = new SearchResponseAttrVO();
        brand.setName("品牌");
        //获取品牌的聚合结果
        Map<String, Aggregation> aggregationMap = response.getAggregations().asMap();
        ParsedLongTerms brandIdAgg = (ParsedLongTerms) aggregationMap.get("brandIdAgg");
        List<String> branValues = brandIdAgg.getBuckets().stream().map(bucket -> {
            Map<String, String> map = new ConcurrentHashMap<>(16);
            map.put("id", bucket.getKeyAsString());
            //通过自聚合来获取品牌名称
            Map<String, Aggregation> brandIdSubMap = bucket.getAggregations().asMap();
            ParsedStringTerms brandNameAgg = (ParsedStringTerms) brandIdSubMap.get("brandNameAgg");
            String brandName = brandNameAgg.getBuckets().get(0).getKeyAsString();
            map.put("name", brandName);
            String json = JSONUtil.toJsonStr(map);
            System.out.println(json.toString());
            return json;
        }).collect(Collectors.toList());
        brand.setValue(branValues);
        responseVo.setBrand(brand);
        /**
         * 设置分类
         */
        ParsedLongTerms categoryIdAgg = (ParsedLongTerms) aggregationMap.get("categoryIdAgg");
        List<String> cateValues = categoryIdAgg.getBuckets().stream().map(bucket -> {
            Map<String, String> map2 = new ConcurrentHashMap<>(16);
            map2.put("id", bucket.getKeyAsString());
            //通过自聚合来获取品牌名称
            Map<String, Aggregation> categoryNameAggMap = bucket.getAggregations().asMap();
            ParsedStringTerms categoryNameAgg = (ParsedStringTerms) categoryNameAggMap.get("categoryNameAgg");
            String categoryName = categoryNameAgg.getBuckets().get(0).getKeyAsString();
            map2.put("name", categoryName);
            String json = JSONUtil.toJsonStr(map2);
            System.out.println(json.toString());
            return json;
        }).collect(Collectors.toList());
        SearchResponseAttrVO category = new SearchResponseAttrVO();
        category.setName("分类");
        category.setValue(cateValues);
        responseVo.setCatelog(category);
        /**
         * 设置产品集 获取商品
         */
        CopyOnWriteArrayList<Goods> goosList = new CopyOnWriteArrayList<>();
        for (SearchHit hit : hits) {
            Goods goods = JSONUtil.toBean(hit.getSourceAsString(), Goods.class);
            goosList.add(goods);
        }
        responseVo.setProducts(goosList);
        /**
         * 设置规格参数
         */
        //获取嵌套聚合对象
        ParsedNested attrAgg = (ParsedNested) aggregationMap.get("attrAgg");
        //规格参数id聚合对象
        ParsedLongTerms attrIdAgg = (ParsedLongTerms) attrAgg.getAggregations().get("attrIdAgg");
        //
        List<? extends Terms.Bucket> buckets = attrIdAgg.getBuckets();
        if (!CollectionUtils.isEmpty(buckets)) {
            List<SearchResponseAttrVO> searchResponseAttrVOS = buckets.stream().map(bucket -> {
                SearchResponseAttrVO responseAttrVO = new SearchResponseAttrVO();
                responseAttrVO.setProductAttributeId(bucket.getKeyAsNumber().longValue());
                List<? extends Terms.Bucket> nameBuckets = ((ParsedStringTerms) (bucket.getAggregations().get("attrNameAgg"))).getBuckets();
                responseAttrVO.setName(nameBuckets.get(0).getKeyAsString());
                //设置规格参数的值列表
                List<? extends Terms.Bucket> valueBuckets = ((ParsedStringTerms) (bucket.getAggregations().get("attrValueAgg"))).getBuckets();
                List<String> values = valueBuckets.stream().map(Terms.Bucket::getKeyAsString).collect(Collectors.toList());
                responseAttrVO.setValue(values);
                return responseAttrVO;

            }).collect(Collectors.toList());
            responseVo.setAttrs(searchResponseAttrVOS);
        }

        return responseVo;
    }

    /**
     * 构建查询的dsl语句
     *
     * @param searchParamVO
     * @return
     */
    private SearchRequest buildQueryDsl(SearchParamVO searchParamVO) {
        //查询关键字
        String keyword = searchParamVO.getKeyword();
        if (StringUtils.isEmpty(keyword)) {
            return null;
        }
        //查询条件构建器
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //1.构建查询条件和过滤条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //1.1构建查询条件
        boolQueryBuilder.must(QueryBuilders.matchQuery("title", keyword).operator(Operator.AND));
        //1.2构建过滤条件
        //1.2.1构建品牌过滤条件
        String[] brands = searchParamVO.getBrand();
        if (brands != null && brands.length != 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("brandId", brands));
        }
        //1.2.2构建分类过滤条件
        String[] catelog3 = searchParamVO.getCatelog3();
        if (catelog3 != null && catelog3.length != 0) {
            boolQueryBuilder.filter(QueryBuilders.termsQuery("categoryId", catelog3));
        }
        //1.2.3构建规格属性的嵌套过滤
        String[] props = searchParamVO.getProps();
        if (props != null && props.length != 0) {
            for (String prop : props) {
                //进行分割 分割后是两个元素 1-attrId 2-attrValue
                String[] split = StringUtils.split(prop, ":");
                if (split == null || split.length != 2) {
                    continue;
                }
                //以-分割处理attrvalues
                String[] attrValue = StringUtils.split(split[1], "-");
                BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
                //构建嵌套中的子查询
                BoolQueryBuilder subboolQuery = QueryBuilders.boolQuery();
                subboolQuery.must(QueryBuilders.termQuery("attrs.attrId", split[0]));
                //把嵌套查询放入过滤器中
                subboolQuery.must(QueryBuilders.termsQuery("attrs.attrValue", attrValue));

                boolQuery.must(QueryBuilders.nestedQuery("attrs", subboolQuery, ScoreMode.None));
                boolQueryBuilder.filter(boolQuery);
            }
        }
        //1.2.4构建价格过滤
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price");
        Integer priceTo = searchParamVO.getPriceTo();
        Integer priceFrom = searchParamVO.getPriceFrom();
        if (priceFrom != null) {
            rangeQueryBuilder.gte(priceFrom);
        }
        if (priceTo != null) {
            rangeQueryBuilder.lte(priceTo);
        }
        boolQueryBuilder.filter(rangeQueryBuilder);

        searchSourceBuilder.query(boolQueryBuilder);
        //2.分页
        Integer pageSize = searchParamVO.getPageSize();
        Integer pageNum = searchParamVO.getPageNum();
        searchSourceBuilder.from((pageNum - 1) * pageSize);
        searchSourceBuilder.size(pageSize);
        //3.构建排序
        String order = searchParamVO.getOrder();
        if (!StringUtils.isEmpty(order)) {
            String[] split = StringUtils.split(order, ":");
            if (split != null && split.length == 2) {
                String filed = null;
                switch (split[0]) {

                    case "1":
                        filed = "sale";
                        break;
                    case "2":
                        filed = "price";
                        break;
                    default:
                        break;
                }
                searchSourceBuilder.sort(filed, StringUtils.equals("asc", split[1]) ? SortOrder.ASC : SortOrder.DESC);
            }
        }
        //4.构建高亮
        searchSourceBuilder.highlighter(new HighlightBuilder().field("title").preTags("<em>").postTags("</em>"));
        //5构建聚合
        //5.1品牌聚合
        searchSourceBuilder.aggregation(AggregationBuilders.terms("brandIdAgg").field("brandId").
                subAggregation(AggregationBuilders.terms("brandNameAgg").field("brandName")));
        //5.2分类聚合
        searchSourceBuilder.aggregation(AggregationBuilders.terms("categoryIdAgg").field("categoryId").
                subAggregation(AggregationBuilders.terms("categoryNameAgg").field("categoryName")));
        //5.3搜索的规格属性聚合
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrAgg", "attrs").
                subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId")
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));
        System.out.println(searchSourceBuilder.toString());


        //查询参数
        SearchRequest searchRequest = new SearchRequest("goods");
        searchRequest.types("info");
        searchRequest.source(searchSourceBuilder);

        return searchRequest;
    }

}
