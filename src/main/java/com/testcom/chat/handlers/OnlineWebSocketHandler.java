package com.testcom.chat.handlers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.testcom.chat.component.ChatMsgMqSender;
import com.testcom.chat.component.SpringUtil;
import com.testcom.chat.entities.OnlineMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class OnlineWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {
    private final Logger logger = LoggerFactory.getLogger(OnlineWebSocketHandler.class);

    private final ChatMsgMqSender mqChatSender = SpringUtil.getBean(ChatMsgMqSender.class);

    /**
     * 存儲已經登入用戶的channel對象
     */
    public static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 存儲用戶id和用戶的channelId綁定
     */
    public static ConcurrentHashMap<String, ChannelId> userMap = new ConcurrentHashMap<>();

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info("與客戶端建立連接，通道開啟！");
        // 添加到channelGroup通道組
        channelGroup.add(ctx.channel());
    }

    @Override
        public void channelInactive(ChannelHandlerContext ctx) {
        logger.info("與客戶端斷開连接，通道關閉！");
        // 添加到channelGroup 通道组
        channelGroup.remove(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 首次連接是FullHttpRequest，把用戶id和對應的channel對象存儲起来
        if (msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            String uri = request.uri();
            String userId = getUrlParams(uri);
            // 登入後把用戶id和channel關聯上
            userMap.put(userId, ctx.channel().id());
            logger.info("登录的用户id是：{}", userId);
            // 如果url包含參數，需要處理
            if (uri.contains("?")) {
                String newUri = uri.substring(0, uri.indexOf("?"));
                request.setUri(newUri);
            }
        } else if (msg instanceof TextWebSocketFrame) {
            // 正常的text消息類型
            TextWebSocketFrame frame = (TextWebSocketFrame) msg;
            logger.info("客戶端收到服務器數據：{}", frame.text());

            OnlineMessage onlineMessage = JSON.parseObject(frame.text(), OnlineMessage.class);

            // 推送信息至MQ給其他聊天室實例
            mqChatSender.send(onlineMessage);
        }
        super.channelRead(ctx, msg);
    }

    /**
     * 推送信息
     * @param message
     */
    public static void sendMsg(OnlineMessage message) {
        String msgType = message.getMessageType();
        if (msgType != null && msgType.equals("chat")) {
            sendMsgToChat(message);
            return;
        }

        sendMsgToUser(message);
    }

    /**
     * 處理私聊的任務，如果對方也在線，則推送消息
     */
    private static void sendMsgToUser(OnlineMessage message) {
        ChannelId channelId = userMap.get(message.getAcceptId());
        if (channelId != null) {
            Channel ct = channelGroup.find(channelId);
            if (ct != null) {
                ct.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(message)));
            }
        }
    }

    private static void sendMsgToChat(OnlineMessage message) {

    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
    }

    /**
     * 解析url中的參數
     * @return 獲取用戶的id
     */
    private String getUrlParams(String url) {
        if (!url.contains("=")) {
            return null;
        }
        String userId = url.substring(url.indexOf("=") + 1);
        return userId;
    }
}
