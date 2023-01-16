package com.testcom.chat.component;

import com.alibaba.fastjson.JSON;
import com.testcom.chat.entities.OnlineMessage;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import static com.testcom.chat.config.RabbitMqConfig.EXCHANGE_NAME;

@Component
public class ChatMsgMqSender {

    @Resource
    private AmqpTemplate amqpTemplate;


    public void send(OnlineMessage onlineMessage) throws Exception {
        String msg = JSON.toJSONString(onlineMessage);
        amqpTemplate.convertAndSend(EXCHANGE_NAME, "", msg);
    }
}
