package com.testcom.chat.listener;

import com.testcom.chat.handlers.OnlineWebSocketHandler;
import io.netty.channel.ChannelId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;


/**
 * session超時，移除websocket對應的channel
 */
public class MySessionListener implements HttpSessionListener {
    private final Logger logger = LoggerFactory.getLogger(MySessionListener.class);

    @Override
    public void sessionCreated(HttpSessionEvent httpSessionEvent) {
        logger.info("sessionCreated sessionId={}", httpSessionEvent.getSession().getId());
        MySessionContext.addSession(httpSessionEvent.getSession());
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent httpSessionEvent) {
        HttpSession session = httpSessionEvent.getSession();
        Integer userId = session.getAttribute("userId") == null ? null : Integer.parseInt(session.getAttribute("userId").toString());
        // 銷毀時從websocket channel中移除
        if (userId != null) {
            ChannelId channelId = OnlineWebSocketHandler.userMap.get(userId);
            if (channelId != null) {
                // 移除了私聊的channel對象，群聊的還未移除
                OnlineWebSocketHandler.userMap.remove(userId);
                OnlineWebSocketHandler.channelGroup.remove(channelId);
                logger.info("session timeout, remove channel, userId={}", userId);
            }
        }
        MySessionContext.delSession(session);
        logger.info("session destroyed ....");
    }
}
