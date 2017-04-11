package com.wxsk.pusher.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wxsk.pusher.entity.Message;
import com.wxsk.pusher.enums.MessageStatus;
import com.wxsk.pusher.handler.WebSocketClientHandler;
import com.wxsk.pusher.util.JsonUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public final class WebSocketClient {


    private Channel channel;
    private String uriPath, ticket;

    public void sendTextMessage(String resourceName, String from, String to, String action) throws JsonProcessingException {
        sendTextMessage(resourceName, from, to, action, null);
    }

    public void sendTextMessage(String resourceName, String from, String to, String action, Object body) throws JsonProcessingException {
        sendTextMessage(resourceName, from, to, action, null, body);
    }

    public void sendTextMessage(String resourceName, String from, String to, String action, Map<String, Object> extensions, Object body) throws JsonProcessingException {
        Message message = new Message(resourceName, from, to, action, extensions, body, MessageStatus.INIT.getValue());
        channel.writeAndFlush(new TextWebSocketFrame(JsonUtil.formatObjectToJson(message)));
    }

    public String getUriPath() {
        return uriPath;
    }

    public WebSocketClient setUriPath(String uriPath) {
        this.uriPath = uriPath;
        return this;
    }

    public String getTicket() {
        return ticket;
    }

    public WebSocketClient setTicket(String ticket) {
        this.ticket = ticket;
        return this;
    }

    public WebSocketClient(String uriPath, String ticket) throws URISyntaxException, InterruptedException {
        this.uriPath = uriPath;
        this.ticket = ticket;

        URI uri = new URI(uriPath);
        EventLoopGroup group = new NioEventLoopGroup();
        // Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or V00.
        // If you change it to V00, ping is not supported and remember to change
        // HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
        HttpHeaders headers = new DefaultHttpHeaders();
        headers.add("Cookie", "ticket=" + ticket);
        final WebSocketClientHandler handler =
                new WebSocketClientHandler(
                        WebSocketClientHandshakerFactory.newHandshaker(
                                uri, WebSocketVersion.V13, null, true, headers));

        Bootstrap b = new Bootstrap();
        b.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ChannelPipeline p = ch.pipeline();
                        p.addLast(
                                new HttpClientCodec(),
                                new HttpObjectAggregator(8192),
                                new WebSocketClientCompressionHandler(),
                                handler);
                    }
                });
        channel = b.connect(uri.getHost(), uri.getPort()).sync().channel();
        while (!handler.getHandshakeFuture().isSuccess()) {
        }
    }


    public static void main(String[] args) throws Exception{

        WebSocketClient client = new WebSocketClient("ws://192.168.4.200:8888/", "45d2cfaa6d1045848008b04bc617be2a");
        client.sendTextMessage("yuwen",null, null,"insert");
        client.sendTextMessage("yuwen", null, null, "update");
        client.sendTextMessage("yuwen", null, null, "delete");
        client.sendTextMessage("yuwen", null, null, "query");
        Thread.sleep(5000);

    }
}
