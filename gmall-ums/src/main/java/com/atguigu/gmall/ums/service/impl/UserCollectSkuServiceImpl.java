package com.atguigu.gmall.ums.service.impl;

import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.ums.mapper.UserCollectSkuMapper;
import com.atguigu.gmall.ums.service.UserCollectSkuService;


@Service("userCollectSkuService")
public class UserCollectSkuServiceImpl extends ServiceImpl<UserCollectSkuMapper, UserCollectSkuEntity> implements UserCollectSkuService {

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserCollectSkuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserCollectSkuEntity>()
        );

        return new PageResultVo(page);
    }

}