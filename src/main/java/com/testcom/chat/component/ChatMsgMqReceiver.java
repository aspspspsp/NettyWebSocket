package com.testcom.chat.component;

import com.alibaba.fastjson.JSON;
import com.testcom.chat.entities.OnlineMessage;
import com.testcom.chat.handlers.OnlineWebSocketHandler;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "#{queueChat.name}")
public class ChatMsgMqReceiver {

    @RabbitHandler
    public void receive(String message) {
        OnlineMessage onlineMessage = JSON.parseObject(message, OnlineMessage.class);

        OnlineWebSocketHandler.sendMsg(onlineMessage);
    }
}
