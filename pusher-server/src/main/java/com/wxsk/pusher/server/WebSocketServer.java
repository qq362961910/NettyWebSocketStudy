package com.wxsk.pusher.server;

import com.wxsk.pusher.initializer.WebSocketServerInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class WebSocketServer implements Runnable{

    private static final Logger logger = LoggerFactory.getLogger(WebSocketServer.class);
    public int port = 8080;

    @Autowired
    private WebSocketServerInitializer webSocketServerInitializer;

    @Override
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(webSocketServerInitializer);

            Channel ch = b.bind(port).sync().channel();
            logger.info("Open your web browser and navigate to http://localhost:" + port + "/static/client.html");
            ch.closeFuture().sync();
        }catch (Exception e) {
            logger.error(e.toString());
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public int getPort() {
        return port;
    }

    public WebSocketServer setPort(int port) {
        this.port = port;
        return this;
    }
}
