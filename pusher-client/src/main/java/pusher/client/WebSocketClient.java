package pusher.client;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import pusher.entity.Message;
import pusher.handler.WebSocketClientHandler;
import pusher.util.JsonUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

public final class WebSocketClient {


    private Channel channel;
    private String uriPath, ticket;

    public void sendTextMessage(String resourceName,String action) throws JsonProcessingException {
        sendTextMessage(resourceName, action, null);
    }

    public void sendTextMessage(String resourceName,String action, Object body) throws JsonProcessingException {
        sendTextMessage(resourceName, action, null, body);
    }

    public void sendTextMessage(String resourceName,String action, Map<String, Object> extensions, Object body) throws JsonProcessingException {
        Message message = new Message(resourceName, action, extensions, body);
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
        WebSocketClient client = new WebSocketClient("ws://192.168.4.200:8080/", "a2f51349a9624e1f8d4a595bc83a3166");
        client.sendTextMessage("a", "insert");
        client.sendTextMessage("b", "update");
        client.sendTextMessage("c", "delete");
        client.sendTextMessage("d", "query");
        Thread.sleep(5000);
    }
}
