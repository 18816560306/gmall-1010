package com.atguigu.gmall.index.service;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.feign.GmallPmsClient;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IndexService {
    @Autowired
    GmallPmsClient gmallPmsClient;
    public List<CategoryEntity> queryLevelOne() {
        List<CategoryEntity> categoryEntityList = (List<CategoryEntity>) gmallPmsClient.list(0l).getData();
        return categoryEntityList;
    }
}
