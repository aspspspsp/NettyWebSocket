package com.testcom.chat.entities;

import lombok.Data;

@Data
public class SocketMessage {
    /**
     * 消息類型
     */
    private String messageType;

    /**
     * 消息發送者id
     */
    private Integer userId;

    /**
     * 消息接受者或是群聊id
     */
    private Integer chatId;

    /**
     * 消息內容
     */
    private String message;
}
