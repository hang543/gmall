package com.atguigu.gmall.index.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.vo.CategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/9
 * @Content:
 **/
@RestController
@RequestMapping("index")
public class IndexController {
    @Autowired
    private IndexService indexService;

    @GetMapping("/cates")
    public Resp<List<CategoryEntity>> queryLv1Categories() {
        List<CategoryEntity> categoryEntities = this.indexService.queryLv1Categories();
        return Resp.ok(categoryEntities);
    }
    @GetMapping("/cates/{pid}")
    public Resp<List<CategoryVO>> querySubCategories(@PathVariable("pid") Long pid){
        List<CategoryVO> categoryVOList=this.indexService.querySubCategories(pid);
        return Resp.ok(categoryVOList);
    }
    @GetMapping("testlock")
    public Resp<Object> testLock(){
        indexService.testLock();

        return Resp.ok(null);
    }
    @GetMapping("read")
    public Resp<String> read(){
        String msg = indexService.readLock();

        return Resp.ok(msg);
    }

    @GetMapping("write")
    public Resp<String> write(){
        String msg = indexService.writeLock();

        return Resp.ok(msg);
    }

    /**
     * 等待
     * @return
     */
    @GetMapping("latch")
    public Resp<Object> countDownLatch() throws InterruptedException {

        String msg = indexService.latch();

        return Resp.ok(msg);
    }

    /**
     * 计数
     * @return
     */
    @GetMapping("out")
    public Resp<Object> out(){

        String msg = indexService.countDown();

        return Resp.ok(msg);
    }
}
