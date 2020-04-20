package com.atguigu.gmall.ums.controller;

import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.atguigu.gmall.ums.entity.UserLoginLogEntity;
import com.atguigu.gmall.ums.service.UserLoginLogService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * 用户登陆记录表
 *
 * @author dongge
 * @email 517525115@qq.com
 * @date 2020-04-20 23:51:15
 */
@Api(tags = "用户登陆记录表 管理")
@RestController
@RequestMapping("ums/userloginlog")
public class UserLoginLogController {

    @Autowired
    private UserLoginLogService userLoginLogService;

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> list(PageParamVo paramVo){
        PageResultVo pageResultVo = userLoginLogService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<UserLoginLogEntity> queryUserLoginLogById(@PathVariable("id") Long id){
		UserLoginLogEntity userLoginLog = userLoginLogService.getById(id);

        return ResponseVo.ok(userLoginLog);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody UserLoginLogEntity userLoginLog){
		userLoginLogService.save(userLoginLog);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody UserLoginLogEntity userLoginLog){
		userLoginLogService.updateById(userLoginLog);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids){
		userLoginLogService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
