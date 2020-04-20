package com.atguigu.gmall.ums.service;

import com.atguigu.gmall.ums.entity.UserCollectShopEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * 
 *
 * @author dongge
 * @email 517525115@qq.com
 * @date 2020-04-01 22:39:07
 */
public interface UserCollectShopService extends IService<UserCollectShopEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

