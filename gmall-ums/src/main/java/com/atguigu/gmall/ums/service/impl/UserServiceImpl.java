package com.atguigu.gmall.ums.service.impl;

import com.atguigu.gmall.ums.entity.UserEntity;
import com.google.gson.Gson;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpResponse;
import com.atguigu.gmall.common.exception.GmallException;
import com.atguigu.gmall.common.utils.HttpUtils;
import com.atguigu.gmall.common.utils.ScwAppUtils;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.ums.mapper.UserMapper;
import com.atguigu.gmall.ums.service.UserService;
import org.springframework.util.StringUtils;
@Data
@Slf4j
@ConfigurationProperties(prefix = "sms")
@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {
    private String host;
    private String path;
    private String method;
    private String appcode;
    @Autowired
    UserMapper userMapper;
    @Autowired
    UserService userService;
    @Autowired
    StringRedisTemplate stringRedisTemplate;
    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public Boolean checkData(String data, Integer type) {
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
        switch (type) {
            case 1:
                wrapper.eq("username", data);
                break;
            case 2:
                wrapper.eq("phone", data);
                break;
            case 3:
                wrapper.eq("email", data);
                break;
            default:
                return null;
        }

        return this.userMapper.selectCount(wrapper) == 0;
        /*QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
        switch (type){
            case 1: wrapper.eq("username",data);
            case 2: wrapper.eq("phone",data);
            case 3: wrapper.eq("email",data);
            default: return null ;
        }
        return this.userMapper.selectCount(wrapper) == 0;*/
    }

    @Override
    public Boolean sendMessage(String phone) {
        boolean b = ScwAppUtils.isMobilePhone(phone);
        if(!b) {
            //手机号码格式错误
            throw new GmallException("手机格式错误");
        }
        // 带时效性的不需要持久化的数据 一般都使用redis保存
        //1.2 判断手机号码获取验证码次数是否超过范围 24小时内   3次
        // 在redis中该手机号码可能保存多条对应的记录，每一种数据都需要设计一个唯一的key
        //拼接保存手机号码获取验证码次数的唯一的key
        String codeCountKey = "code:"+phone+":count";
        String codeCount = stringRedisTemplate.opsForValue().get(codeCountKey);
        int count = 0;
        if(!StringUtils.isEmpty(codeCount)) {
            //该手机号码不是第一次获取验证码
            count = Integer.parseInt(codeCount);
            if(count>=3) {
                //获取验证码次数超过3次
                throw new GmallException("该手机号码今天获取验证码异常");

            }
        }
        //count=0代表第一次获取，!=0代表第N次获取验证码
        //1.3 判断手机号码是否有未过期的验证码
        //在redis中判断存储手机验证码的key是否存在
        String codeKey = "code:"+phone+":code";
        Boolean flag = stringRedisTemplate.hasKey(codeKey);
        if(flag) {
            //手机号码验证码存在
            throw new GmallException("该手机号码获取验证码过于频繁,请10分钟后再试");

        }
        //1.4 生成验证码：6位
        String code = UUID.randomUUID().toString()
                .replace("-", "").substring(0, 6);
        //1.5 调用smsTemplate的方法发送短信
        Map<String, String> querys = new HashMap<String, String>();
//		"19821269297"
        querys.put("phone", phone);
        querys.put("variable", "code:"+code);
        querys.put("templateId", "TP18040314");
        //flag = true;
        flag=sendMessage(querys);
        if(!flag) {
            //短信发送失败
            throw new GmallException("该手机号码获取验证码失败");

        }
        //1.6 如果成功 将手机号码和验证码保存在服务器中并设置过期时间    10分钟
        stringRedisTemplate.opsForValue().set(codeKey, code, 10, TimeUnit.MINUTES);
        //1.7 更新该手机号码获取验证码的次数  24小时有效
        //incr在键原有的基础上+1 ，   decr在原有基础上自减1
        // redis自增时，如果键没有值，默认在0的基础上+1，如果存在则在之前的基础上+1
        if(count==0) {
            //第一次获取验证码手动保存次数
            stringRedisTemplate.opsForValue().set(codeCountKey, "1", 24, TimeUnit.HOURS);
        }else {
            //以后获取验证码通过自增的方法
            stringRedisTemplate.opsForValue().increment(codeCountKey);
        }

        //1.8 给访问者相应结果
        return true;

    }

    @Override
    public void register(UserEntity userEntity, String code) {
        String cacheCode=stringRedisTemplate.opsForValue().get("code:"+userEntity.getPhone()+":code");
        if (!StringUtils.pathEquals(cacheCode,code)){
            return ;
        }
        String salt=StringUtils.replace(UUID.randomUUID().toString().substring(0,6),"-","");
        userEntity.setPassword(DigestUtils.md5Hex(userEntity.getPassword()+salt));
        userEntity.setSalt(salt);
        // 设置创建时间等
        userEntity.setCreateTime(new Date());
        userEntity.setLevelId(1l);
        userEntity.setStatus(1);
        userEntity.setIntegration(0);
        userEntity.setGrowth(0);
        boolean b=this.save(userEntity);
        if (b){
            stringRedisTemplate.delete("code:"+userEntity.getPhone()+":code");
        }
    }

    @Override
    public UserEntity queryUser(String username, String password) {
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
        wrapper.eq("username",username);
        UserEntity userEntity = userService.getOne(wrapper);
        if (userEntity==null){
            return null;
        }
        if (!userEntity.getPassword().equals(DigestUtils.md5Hex(password+userEntity.getSalt()))){
            return null;
        }
        return userEntity;
    }

    public boolean sendMessage(Map<String, String> querys) {

        //可以修改的配置建议提取到配置文件中
        //由用户动态传入的参数 应该提取为形参：手机号码、动态生成的验证码、短信模板
        boolean flag = false;
        Map<String, String> headers = new HashMap<String, String>();
        //最后在header中的格式(中间是英文空格)为Authorization:APPCODE 83359fd73fe94948385f570e3c139105
        headers.put("Authorization", "APPCODE " + appcode);
        log.info("APPCODE " + appcode);
        log.info(querys.toString());
        Map<String, String> bodys = new HashMap<String, String>();
        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            String bodyStr = EntityUtils.toString(response.getEntity());
            log.warn("发送短信的响应体内容：{}", bodyStr);//发送短信的响应体内容：{"ReturnStatus":"Success","Message":"ok","RemainPoint":1823731,"TaskID":50918100,"SuccessCounts":1}
            //解析bodyStrjson字符串  判断短信发送成功与否
            Gson gson = new Gson();
            Map map = gson.fromJson(bodyStr, Map.class);
            log.info("解析响应体json字符串的结果为：{}",map);
            //解析响应体json字符串的结果为：{ReturnStatus=Success, Message=ok, RemainPoint=1823731.0, TaskID=5.09181E7, SuccessCounts=1.0
            log.info((String) map.get("return_code"));
            //根据map中的字段给调用方法的请求相应
            flag = "00000".equals(map.get("return_code"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        //true代表短信发送成功
        if (flag){
            log.error("成功");
        }else {
            log.error("失败");
        }
        return flag;
    }


}