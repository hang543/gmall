package com.atguigu.gmall.index.service;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.entity.vo.CategoryVO;

import java.util.List;

/**
 * @Version 1.0
 * @Author:杭利达
 * @Date:2020/4/9
 * @Content:
 **/
public interface IndexService {
    List<CategoryEntity> queryLv1Categories();

    List<CategoryVO> querySubCategories(Long pid);

    void testLock();

    String readLock();

    String writeLock();

    String latch() throws InterruptedException;

    String countDown();
}
