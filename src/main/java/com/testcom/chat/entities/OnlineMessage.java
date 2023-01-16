package com.testcom.chat.entities;

import lombok.Data;

@Data
public class OnlineMessage {
    /**
     * 消息發送者id
     */
    private String sendId;
    /**
     * 消息接受者id
     */
    private String acceptId;
    /**
     * 消息内容
     */
    private String message;

    /**
     * 頭像
     */
    private String headImg;

    /**
     * 消息類型
     */
    private String messageType;
}
