package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;
import com.atguigu.gmall.pms.fegin.GmallSmsClient;
import com.atguigu.gmall.pms.mapper.SkuMapper;
import com.atguigu.gmall.pms.mapper.SpuDescMapper;
import com.atguigu.gmall.pms.service.SkuAttrValueService;
import com.atguigu.gmall.pms.service.SkuImagesService;
import com.atguigu.gmall.pms.service.SpuAttrValueService;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import com.atguigu.gmall.sms.vo.SaleVo;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.pms.mapper.SpuMapper;
import com.atguigu.gmall.pms.service.SpuService;
import org.springframework.util.CollectionUtils;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {
    @Autowired
    SpuDescMapper spuDescMapper;
    @Autowired
    SpuAttrValueService spuAttrValueService;
    @Autowired
    SkuMapper skuMapper;
    @Autowired
    SkuImagesService skuImagesService;
    @Autowired
    SkuAttrValueService skuAttrValueService;
    @Autowired
    GmallSmsClient gmallSmsClient;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo querySpuInfo(PageParamVo pageParamVo, Long categoryId) {
        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();
        if (categoryId!=0){
            wrapper.eq("category_id",categoryId);
        }
        // 如果用户输入了检索条件，根据检索条件查
        String key = pageParamVo.getKey();
        if (StringUtils.isNotBlank(key)){
            wrapper.and(t -> t.eq("id", key).or().like("name", key));
        }
        return new PageResultVo(this.page(pageParamVo.getPage(), wrapper));
    }
    @GlobalTransactional
    @Override
    public void bigSave(SpuVo spuVo) {
        spuVo.setPublishStatus(1); // 默认是已上架
        spuVo.setCreateTime(new Date());
        spuVo.setUpdateTime(spuVo.getCreateTime()); // 新增时，更新时间和创建时间一致
        this.save(spuVo);
        Long spuId = spuVo.getId(); // 获取新增后的spuId
        SpuDescEntity descEntity = new SpuDescEntity();
        descEntity.setSpuId(spuId);
        descEntity.setDecript(StringUtils.join(spuVo.getSpuImages(), ","));
        this.spuDescMapper.insert(descEntity);
        // 1.3. 保存spu的规格参数信息
        List<SpuAttrValueVo> baseAttrs = spuVo.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)) {
            List<SpuAttrValueEntity> spuAttrValueEntities = baseAttrs.stream().map(spuAttrValueVO -> {
                spuAttrValueVO.setSpuId(spuId);
                spuAttrValueVO.setSort(0);
                return spuAttrValueVO;
            }).collect(Collectors.toList());
            spuAttrValueService.saveBatch(spuAttrValueEntities);
        }
        //保存sku相关信息
        List<SkuVo> skuVoList = spuVo.getSkus();
        if (!CollectionUtils.isEmpty(skuVoList)) {
            skuVoList.forEach(skuVo -> {
                SkuEntity skuEntity = new SkuEntity();
                BeanUtils.copyProperties(skuVo, skuEntity);
                skuEntity.setSpuId(spuId);
                skuEntity.setBrandId(spuVo.getBrandId());
                skuEntity.setCategoryId(spuVo.getCategoryId());
                List<String> images = skuVo.getImages();
                // 如果图片列表不为null，则设置默认图片
                if (!CollectionUtils.isEmpty(images)) {
                    // 设置第一张图片作为默认图片
                    skuEntity.setDefaultImage(skuEntity.getDefaultImage() == null ? images.get(0) : skuEntity.getDefaultImage());
                }
                skuMapper.insert(skuEntity);
                Long skuId = skuEntity.getId();
                // 2.2. 保存sku图片信息
                if (!CollectionUtils.isEmpty(images)) {
                    String defaultImage = images.get(0);
                    List<SkuImagesEntity> skuImageses = images.stream().map(image -> {
                        SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                        skuImagesEntity.setDefaultStatus(StringUtils.equals(defaultImage, image) ? 1 : 0);
                        skuImagesEntity.setSkuId(skuId);
                        skuImagesEntity.setSort(0);
                        skuImagesEntity.setUrl(image);
                        return skuImagesEntity;
                    }).collect(Collectors.toList());
                    this.skuImagesService.saveBatch(skuImageses);
                }
                //int i=1/0;
                // 2.3. 保存sku的规格参数（销售属性）
                List<SkuAttrValueEntity> saleAttrs = skuVo.getSaleAttrs();
                saleAttrs.forEach(saleAttr -> {
                    // 设置属性名，需要根据id查询AttrEntity
                    saleAttr.setSort(0);
                    saleAttr.setSkuId(skuId);
                });
                this.skuAttrValueService.saveBatch(saleAttrs);
                SaleVo saleVo = new SaleVo();
                BeanUtils.copyProperties(skuVo,saleVo);
                saleVo.setSkuId(skuId);
                gmallSmsClient.saveSales(saleVo);

            });


        }
        this.rabbitTemplate.convertAndSend("pms-item-exch-insertange","item",spuVo.getId());


    }

}