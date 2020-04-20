package com.atguigu.gmall.pms.mapper;

import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品三级分类
 * 
 * @author dongge
 * @email 517525115@qq.com
 * @date 2020-04-01 22:33:37
 */
@Mapper
public interface CategoryMapper extends BaseMapper<CategoryEntity> {
	
}
