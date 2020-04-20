package com.atguigu.gmall.search;

import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.search.bean.Goods;
import com.atguigu.gmall.search.bean.SearchAttrValue;
import com.atguigu.gmall.search.feign.GmallPmsClient;
import com.atguigu.gmall.search.feign.GmallWmsClient;
import com.atguigu.gmall.search.repository.GoodsRepository;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
class GmallSearchApplicationTests {
@Autowired
ElasticsearchRestTemplate elasticsearchRestTemplate;
@Autowired
GmallPmsClient gmallPmsClient;
@Autowired
GmallWmsClient wmsClient;
@Autowired
GoodsRepository goodsRepository;
    @Test
    void contextLoads() {
    elasticsearchRestTemplate.createIndex(Goods.class);
    elasticsearchRestTemplate.putMapping(Goods.class);
    }
    @Test
    void importData(){
        Integer pageNum=1;
        Integer pageSize=100;


        do {
            PageParamVo pageParamVo = new PageParamVo();
            pageParamVo.setPageNum(pageNum);
            pageParamVo.setPageSize(pageSize);
            List<SpuEntity> listEntity = gmallPmsClient.querySpu(pageParamVo).getData();
            if (CollectionUtils.isEmpty(listEntity)){
                continue;
            }
            listEntity .forEach(spuEntity -> {
                List<SkuEntity> skuEntitys = gmallPmsClient.querySkusBySpuId(spuEntity.getId()).getData();
                if (!CollectionUtils.isEmpty(skuEntitys)){
                    List<Goods> listGoods=  skuEntitys.stream().map(sku -> {
                        Goods goods = new Goods();
                        goods.setSkuId(sku.getId());
                        goods.setTitle(sku.getTitle());
                        goods.setPrice(sku.getPrice().doubleValue());
                        goods.setDefaultImage(sku.getDefaultImage());
                        // 查询品牌相关数据
                        ResponseVo<BrandEntity> brandEntityResponseVo = this.gmallPmsClient.queryBrandById(sku.getBrandId());
                        BrandEntity brandEntity = brandEntityResponseVo.getData();
                        if (brandEntity != null) {
                            goods.setBrandId(sku.getBrandId());
                            goods.setBrandName(brandEntity.getName());
                            goods.setLogo(brandEntity.getLogo());
                        }

                        // 查询分类相关数据
                        ResponseVo<CategoryEntity> categoryEntityResponseVo = this.gmallPmsClient.queryCategoryById(sku.getCategoryId());
                        CategoryEntity categoryEntity = categoryEntityResponseVo.getData();
                        if (categoryEntity != null) {
                            goods.setCategoryId(sku.getCategoryId());
                            goods.setCategoryName(categoryEntity.getName());
                        }

                        goods.setCreateTime(spuEntity.getCreateTime());

                        // 查询sku对应商品库存的相关信息
                        ResponseVo<List<WareSkuEntity>> wareSkuListResponseVo = this.wmsClient.queryWareSkuBySkuId(sku.getId());
                        List<WareSkuEntity> wareSkuEntities = wareSkuListResponseVo.getData();
                        if (!CollectionUtils.isEmpty(wareSkuEntities)) {
                            goods.setSales(wareSkuEntities.stream().map(WareSkuEntity::getSales).reduce((a, b)-> a+b).get());
                            goods.setStore(wareSkuEntities.stream().anyMatch(wareSkuEntity -> wareSkuEntity.getStock()>0));
                        }
                        // 先查询出检索属性
                        ResponseVo<List<AttrEntity>> attrResponseVo = this.gmallPmsClient.querryAttrByCid(sku.getCategoryId(), null, 1);
                        List<AttrEntity> attrEntities = attrResponseVo.getData();
                        if (!CollectionUtils.isEmpty(attrEntities)) {
                            List<Long> attrIds = attrEntities.stream().map(AttrEntity::getId).collect(Collectors.toList());

                            List<SearchAttrValue> searchAttrValues = new ArrayList<>();
                            ResponseVo<List<SkuAttrValueEntity>> skuAttrValuesResponseVo = this.gmallPmsClient.querySkuSearchAttrValue(sku.getId(), attrIds);
                            ResponseVo<List<SpuAttrValueEntity>> spuAttrValuesResponseVo = this.gmallPmsClient.querySpuSearchAttrValue(spuEntity.getId(), attrIds);
                            List<SkuAttrValueEntity> skuAttrValueEntities = skuAttrValuesResponseVo.getData();
                            List<SpuAttrValueEntity> spuAttrValueEntities = spuAttrValuesResponseVo.getData();
                            if (!CollectionUtils.isEmpty(skuAttrValueEntities)) {
                                searchAttrValues.addAll(skuAttrValueEntities.stream().map(skuAttrValueEntity -> {
                                    SearchAttrValue searchAttrValue = new SearchAttrValue();
                                    BeanUtils.copyProperties(skuAttrValueEntity, searchAttrValue);
                                    return searchAttrValue;
                                }).collect(Collectors.toList()));
                            }
                            if (!CollectionUtils.isEmpty(spuAttrValueEntities)) {
                                searchAttrValues.addAll(spuAttrValueEntities.stream().map(spuAttrValueEntity -> {
                                    SearchAttrValue searchAttrValue = new SearchAttrValue();
                                    BeanUtils.copyProperties(spuAttrValueEntity, searchAttrValue);
                                    return searchAttrValue;
                                }).collect(Collectors.toList()));
                            }
                            goods.setSearchAttrs(searchAttrValues);
                        }
                        return goods;
                    }).collect(Collectors.toList());
                    this.goodsRepository.saveAll(listGoods);
                    }
                });

                //goodsRepository.saveAll();
            pageNum++;
            //List<SpuEntity> data = listResponseVo.getData();
            pageSize=listEntity.size();
        }while (pageSize==100);
    }
}