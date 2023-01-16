package com.testcom.chat.handlers;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.testcom.chat.component.SpringUtil;
import com.testcom.chat.entities.SocketMessage;
import com.testcom.chat.repositories.UserGroupRepository;
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

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class TestWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final Logger logger = LoggerFactory.getLogger(TestWebSocketHandler.class);

    /**
     * 存儲已經登入用戶的channel對象
     */
    public static ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 存儲用戶id與channelId綁定
     */
    public static ConcurrentHashMap<Integer, ChannelId> userMap = new ConcurrentHashMap<>();


    /**
     * 用於存儲群聊房間號和群聊成員的channel信息
     */
    public static ConcurrentHashMap<Integer, ChannelGroup> groupMap = new ConcurrentHashMap<>();

    /**
     * 獲取用戶擁有的群聊id號
     */
    private UserGroupRepository userGroupRepository = SpringUtil.getBean(UserGroupRepository.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("與客戶端建立連接，通道開啟!");

        // 添加至channelGroup通道組
            channelGroup.add(ctx.channel());
            ctx.channel().id();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("與客戶端斷開連接，通道關閉!");
        // 從通道組移除
        channelGroup.remove(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 首次連接是FullHttpRequest，把用戶id和對應的channel對象存儲起來
        if (null != msg && msg instanceof FullHttpRequest) {
            FullHttpRequest request = (FullHttpRequest) msg;
            String uri = request.uri();
            Integer userId = getUriParams(uri);
            userMap.put(getUriParams(uri), ctx.channel().id());
            logger.info("登入用戶的id是: {}", userId);
            // 第一次登入，需要查詢下當前用戶是否加入過群，加入過群，則放入對應的群聊裡
            List<Integer> groupIds = userGroupRepository.findGroupIdByUserId(userId);
            ChannelGroup cGroup = null;
            // 查詢用戶擁有的組是否已經創建
            for (Integer groupId : groupIds) {
                cGroup = groupMap.get(groupId);
                // 如果群聊管理對象沒有創建
                if (cGroup == null) {
                    // 構建一個channelGroup群聊管理對象然後放入groupMap中
                    cGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
                    groupMap.put(groupId, cGroup);
                }
                // 把用戶放到群聊管理對象裡去
                cGroup.add(ctx.channel());
            }
            // 如果url包含參數，需要處理
            if (uri.contains("?")) {
                String newUri = uri.substring(0, uri.indexOf("?"));
                request.setUri(newUri);
            }
        } else if (msg instanceof TextWebSocketFrame) {
            // 正常的text消息類型
            TextWebSocketFrame frame = (TextWebSocketFrame) msg;
            logger.info("客戶端收到的服務器數據: {}", frame.text());
            SocketMessage socketMessage = JSON.parseObject(frame.text(), SocketMessage.class);
            // 處理群聊任務
            if ("group".equals(socketMessage.getMessageType())) {
                // 推送群聊訊息
                groupMap.get(socketMessage.getChatId())
                        .writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(socketMessage)));
            } else {
                // 處理私聊的任務，如果對方也在線，則推送消息
                ChannelId channelId = userMap.get(socketMessage.getChatId());
                if (channelId != null) {
                    Channel ct = channelGroup.find(channelId);
                    if (ct != null) {
                        ct.writeAndFlush(new TextWebSocketFrame(JSONObject.toJSONString(socketMessage)));
                    }
                }
            }
        }

        super.channelRead(ctx, msg);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {

    }

    private static Integer getUriParams(String url) {
        if (!url.contains("=")) {
            return null;
        }

        String userId = url.substring(url.indexOf("=") + 1);
        return Integer.parseInt(userId);
    }
}
