package com.atguigu.gmall.sms.service.impl;

import com.atguigu.gmall.sms.entity.SkuFullReductionEntity;
import com.atguigu.gmall.sms.entity.SkuLadderEntity;
import com.atguigu.gmall.sms.service.SkuFullReductionService;
import com.atguigu.gmall.sms.service.SkuLadderService;
import com.atguigu.gmall.sms.vo.SaleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.sms.mapper.SkuBoundsMapper;
import com.atguigu.gmall.sms.entity.SkuBoundsEntity;
import com.atguigu.gmall.sms.service.SkuBoundsService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service("skuBoundsService")
public class SkuBoundsServiceImpl extends ServiceImpl<SkuBoundsMapper, SkuBoundsEntity> implements SkuBoundsService {
    @Autowired
    SkuFullReductionService skuFullReductionService;
    @Autowired
    SkuLadderService skuLadderService;
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SkuBoundsEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SkuBoundsEntity>()
        );

        return new PageResultVo(page);
    }
    @Transactional
    @Override
    public void saveSales(SaleVo saleVo) {
        SkuBoundsEntity skuBoundsEntity = new SkuBoundsEntity();
        skuBoundsEntity.setBuyBounds(saleVo.getBuyBounds());
        skuBoundsEntity.setGrowBounds(saleVo.getGrowBounds());
        skuBoundsEntity.setSkuId(saleVo.getSkuId());
        List<Integer> works=saleVo.getWork();
        skuBoundsEntity.setWork(works.get(3)*8+works.get(2)*4+works.get(1)*2+works.get(0));
        this.save(skuBoundsEntity);
        SkuFullReductionEntity reductionEntity = new SkuFullReductionEntity();
        reductionEntity.setAddOther(saleVo.getFullAddOther());
        reductionEntity.setFullPrice(saleVo.getFullPrice());
        reductionEntity.setSkuId(saleVo.getSkuId());
        reductionEntity.setReducePrice(saleVo.getReducePrice());
        skuFullReductionService.save(reductionEntity);
        SkuLadderEntity ladderEntity = new SkuLadderEntity();
        ladderEntity.setAddOther(saleVo.getLadderAddOther());
        ladderEntity.setDiscount(saleVo.getDiscount());
        ladderEntity.setFullCount(saleVo.getFullCount());
        ladderEntity.setSkuId(saleVo.getSkuId());
        skuLadderService.save(ladderEntity);

    }

}