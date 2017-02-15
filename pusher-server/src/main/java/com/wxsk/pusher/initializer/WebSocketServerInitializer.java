package com.wxsk.pusher.initializer;

import com.wxsk.pusher.handler.HtmlPageHandler;
import com.wxsk.pusher.handler.WebSocketHandShakerHandler;
import com.wxsk.pusher.helper.ContextHelper;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
public class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(ContextHelper.getBean(HttpServerCodec.class));
        pipeline.addLast(ContextHelper.getBean(HttpObjectAggregator.class));
        pipeline.addLast(ContextHelper.getBean(WebSocketServerCompressionHandler.class));
        pipeline.addLast(ContextHelper.getBean(HtmlPageHandler.class));
        pipeline.addLast(ContextHelper.getBean(WebSocketHandShakerHandler.class));
    }
}
