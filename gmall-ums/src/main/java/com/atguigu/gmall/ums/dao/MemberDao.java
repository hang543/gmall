package com.atguigu.gmall.ums.dao;

import com.atguigu.gmall.ums.entity.MemberEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员
 * 
 * @author hang3
 * @email hangsansan@aliyun.com
 * @date 2020-04-11 23:05:40
 */
@Mapper
public interface MemberDao extends BaseMapper<MemberEntity> {
	
}
