package com.testcom.chat.servers;

import com.testcom.chat.handlers.OnlineWebSocketHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class NettyServer {
    private final int port;

    public NettyServer(int port) {
        this.port = port;
    }

    public void start() throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap sb = new ServerBootstrap();
            sb.option(ChannelOption.SO_BACKLOG, 1024);
            // 綁定線程池
            sb.group(group, bossGroup)
                    // 指定使用的channel
                    .channel(NioServerSocketChannel.class)
                    // 綁定監聽端口
                    .localAddress(this.port)
                    // 綁定客戶端連線時候觸發操作
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            System.out.println("收到新連接");

                            // websocket協議本身是基於http協議的，所以這邊也要使用http解編碼器
                            ch.pipeline().addLast(new HttpServerCodec());
                            // 以塊的方式來寫的處理器
                            ch.pipeline().addLast(new ChunkedWriteHandler());
                            ch.pipeline().addLast(new HttpObjectAggregator(8192));
                            // ch.pipeline().addLast(new OnlineWebSocketHandler()); // 添加在線客服聊天消息處理類
                            ch.pipeline().addLast(new OnlineWebSocketHandler()); // 添加測試客服聊天消息處理類
                            ch.pipeline().addLast(new WebSocketServerProtocolHandler("/ws", null, true, 65536 * 10));

                            // 避免跨域
                            CorsConfig corsConfig = CorsConfigBuilder.forAnyOrigin().allowNullOrigin().allowCredentials().build();
                            ch.pipeline().addLast(new CorsHandler(corsConfig));

                        }
                    });
            // 服務器異步創建綁定
            ChannelFuture cf = sb.bind().sync();
            System.out.println(NettyServer.class + " 啟動正在監聽: " + cf.channel().localAddress());
            // 關閉服務器通道
            cf.channel().closeFuture().sync();
        } finally {
            // 釋放線程池資源
            group.shutdownGracefully().sync();
            bossGroup.shutdownGracefully().sync();
        }
    }
}
