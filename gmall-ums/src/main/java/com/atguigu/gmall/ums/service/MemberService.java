package com.atguigu.gmall.ums.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 会员
 *
 * @author hang3
 * @email hangsansan@aliyun.com
 * @date 2020-04-11 23:05:40
 */
public interface MemberService extends IService<MemberEntity> {

    PageVo queryPage(QueryCondition params);

    Boolean cheackData(String data, Integer type);

    void register(MemberEntity memberEntity, String code);


    MemberEntity queryUser(String username, String password);
}

