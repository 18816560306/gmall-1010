package com.atguigu.gmall.oms.mapper;

import com.atguigu.gmall.oms.entity.OrderEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单
 * 
 * @author dongge
 * @email 517525115@qq.com
 * @date 2020-04-01 22:30:24
 */
@Mapper
public interface OrderMapper extends BaseMapper<OrderEntity> {
	
}
