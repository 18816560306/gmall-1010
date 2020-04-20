package com.atguigu.gmall.ums.service;

import com.atguigu.gmall.ums.entity.GrowthHistoryEntity;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * 成长积分记录表
 *
 * @author dongge
 * @email 517525115@qq.com
 * @date 2020-04-01 22:39:07
 */
public interface GrowthHistoryService extends IService<GrowthHistoryEntity> {

    PageResultVo queryPage(PageParamVo paramVo);
}

