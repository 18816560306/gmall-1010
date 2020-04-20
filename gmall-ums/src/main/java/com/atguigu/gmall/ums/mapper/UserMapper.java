package com.atguigu.gmall.ums.mapper;

import com.atguigu.gmall.ums.entity.UserEntity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户表
 * 
 * @author dongge
 * @email 517525115@qq.com
 * @date 2020-04-20 23:51:15
 */
@Mapper
public interface UserMapper extends BaseMapper<UserEntity> {
	
}
