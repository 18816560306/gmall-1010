package com.atguigu.gmall.pms.config;

import com.alibaba.nacos.api.config.filter.IFilterConfig;
import com.sun.org.apache.xml.internal.security.keys.storage.implementations.CertsInFilesystemDirectoryResolver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
@Slf4j
@Configuration
public class RabbitMqConfig implements RabbitTemplate.ConfirmCallback,RabbitTemplate.ReturnCallback {
   @Autowired
   RabbitTemplate rabbitTemplate;
   @PostConstruct
   public void init(){
       rabbitTemplate.setConfirmCallback(this);
       rabbitTemplate.setReturnCallback(this);
   }
    @Override
    public void confirm(CorrelationData correlationData, boolean b, String s) {
        if(b){
            System.out.println("消息已经到达交换机");
        }else {
            log.error("消息没有到达交换机");
        }
    }

    @Override
    public void returnedMessage(Message message, int i, String s, String s1, String s2) {
        log.error("消息发送失败"+message.getBody());
    }
}
