package com.atguigu.gmall.index.controller;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.index.service.IndexService;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("index")
public class IndexController {
    @Autowired
    IndexService indexService;
    @GetMapping("cates")
    public ResponseVo<List<CategoryEntity>> queryLevelOne(){
        List<CategoryEntity>  list=indexService.queryLevelOne();
    return ResponseVo.ok(list);
    }

    @GetMapping("cates/{pid}")
    public ResponseVo getChildrenList(@PathVariable("parentId")Long id){
      // List<> childrenList=indexService.getChildrenList(id);
       return ResponseVo.ok();
    }
}
