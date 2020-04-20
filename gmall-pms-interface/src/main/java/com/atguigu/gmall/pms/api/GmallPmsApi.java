package com.atguigu.gmall.pms.api;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface GmallPmsApi {
    @PostMapping("pms/spu/page")
    @ApiOperation("分页查询json")
    public ResponseVo<List<SpuEntity>> querySpu(@RequestBody PageParamVo paramVo);
    //根据id查询sku
    @GetMapping("pms/sku/spu/{spuId}")
    public ResponseVo<List<SkuEntity>> querySkusBySpuId(@PathVariable("spuId")Long spuId);
    //根据id查询品牌
    @GetMapping("pms/brand/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<BrandEntity> queryBrandById(@PathVariable("id") Long id);
    //根据id查询分类信息
    @GetMapping("pms/category/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<CategoryEntity> queryCategoryById(@PathVariable("id") Long id);
    @GetMapping("pms/attr/category/{cid}")
    public ResponseVo<List<AttrEntity>>querryAttrByCid(
            @PathVariable("cid")Long cid,
            @RequestParam(value = "type", required = false)Integer type,
            @RequestParam(value = "searchType", required = false) Integer searchType);
    @GetMapping("pms/skuattrvalue/search/attr")
    public ResponseVo<List<SkuAttrValueEntity>> querySkuSearchAttrValue(
            @RequestParam("skuId")Long skuId,
            @RequestParam("attrIds")List<Long> attrIds);
    @GetMapping("pms/spuattrvalue/search/attr")
    public ResponseVo<List<SpuAttrValueEntity>> querySpuSearchAttrValue(
            @RequestParam("spuId")Long spuId,
            @RequestParam("attrIds")List<Long> attrIds);
    @GetMapping("pms/spu/{id}")
    @ApiOperation("详情查询")
    public ResponseVo<SpuEntity> querySpuById(@PathVariable("id") Long id);
    //根据parentId查询商品分类
    @GetMapping("pms/category/parent/{parentId}")
    public ResponseVo list(@PathVariable("parentId")Long parentId);
}
