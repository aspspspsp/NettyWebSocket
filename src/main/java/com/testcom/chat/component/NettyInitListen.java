package com.testcom.chat.component;

import com.testcom.chat.servers.NettyServer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class NettyInitListen implements CommandLineRunner {

    @Value("${netty.port}")
    private Integer nettyPort;

    @Value("${server.port}")
    private Integer serverPort;

    @Override
    public void run (String ... args) throws Exception {
        try {
            System.out.println("NettyServer starting ...");
            System.out.println("http://127.0.0.1:" + serverPort + "/login");
            new NettyServer(nettyPort).start();
        } catch (Exception e) {
            System.out.println("NettyServerError:" + e.getMessage());
        }
    }
}
