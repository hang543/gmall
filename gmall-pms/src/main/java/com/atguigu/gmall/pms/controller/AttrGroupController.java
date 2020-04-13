package com.atguigu.gmall.pms.controller;

import java.util.Arrays;
import java.util.List;


import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.vo.ItemGroupVo;
import com.atguigu.gmall.pms.vo.GroupVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;


/**
 * 属性分组
 *
 * @author hang3
 * @email hangsansan@aliyun.com
 * @date 2020-04-02 15:42:52
 */
@Api(tags = "属性分组 管理")
@RestController
@RequestMapping("pms/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;
    @GetMapping("withattr/{gid}")
    public Resp<GroupVO> queryGroupWithAttrsByGid(@PathVariable Long gid){
        GroupVO groupVO = this.attrGroupService.queryGroupWithAttrsByGid(gid);
        return Resp.ok(groupVO);
    }
    @GetMapping("withattrs/cat/{catId}")
    public Resp<List<GroupVO>>queryGroupAttrsByCid(@PathVariable("catId") Long cid){
           List<GroupVO> groupVOS  = this.attrGroupService.queryGroupAttrsByCid(cid);
        return Resp.ok(groupVOS);
    }
    @GetMapping("item/group/{cid}/{spuId}")
    public Resp<List<ItemGroupVo>> queryItemGroupVOByCidAndSpuid(@PathVariable("cid")Long cid,@PathVariable("spuId") Long spuId){
        List<ItemGroupVo> itemGroupVoList=  this.attrGroupService.queryItemGroupVOByCidAndSpuid(cid,spuId);
        return Resp.ok(itemGroupVoList);
    }


    /**
     *  分页查询分组信息
     * @param queryCondition
     * @param catId
     * @return
     */
    @GetMapping("{catId}")
    public Resp<PageVo> queryGroupByPage(QueryCondition queryCondition, @PathVariable("catId") Long catId) {
        PageVo page = attrGroupService.queryGroupByPage(queryCondition,catId);
        return Resp.ok(page);
    }

    /**
     * 列表
     */
    @ApiOperation("分页查询(排序)")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('pms:attrgroup:list')")
    public Resp<PageVo> list(QueryCondition queryCondition) {
        PageVo page = attrGroupService.queryPage(queryCondition);

        return Resp.ok(page);
    }


    /**
     * 信息
     */
    @ApiOperation("详情查询")
    @GetMapping("/info/{attrGroupId}")
    @PreAuthorize("hasAuthority('pms:attrgroup:info')")
    public Resp<AttrGroupEntity> info(@PathVariable("attrGroupId") Long attrGroupId) {
        AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        return Resp.ok(attrGroup);
    }

    /**
     * 保存
     */
    @ApiOperation("保存")
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('pms:attrgroup:save')")
    public Resp<Object> save(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.save(attrGroup);

        return Resp.ok(null);
    }

    /**
     * 修改
     */
    @ApiOperation("修改")
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('pms:attrgroup:update')")
    public Resp<Object> update(@RequestBody AttrGroupEntity attrGroup) {
        attrGroupService.updateById(attrGroup);

        return Resp.ok(null);
    }

    /**
     * 删除
     */
    @ApiOperation("删除")
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('pms:attrgroup:delete')")
    public Resp<Object> delete(@RequestBody Long[] attrGroupIds) {
        attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return Resp.ok(null);
    }

}
