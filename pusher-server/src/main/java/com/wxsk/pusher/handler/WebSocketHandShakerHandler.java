package com.wxsk.pusher.handler;

import com.alibaba.dubbo.common.logger.Logger;
import com.alibaba.dubbo.common.logger.LoggerFactory;
import com.wxsk.pusher.helper.ContextHelper;
import com.wxsk.pusher.listener.UserConnectWebsocketSuccessListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.AttributeKey;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import static io.netty.handler.codec.http.HttpHeaderNames.HOST;

@Scope("prototype")
@Component
public class WebSocketHandShakerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static Logger logger = LoggerFactory.getLogger(WebSocketHandShakerHandler.class);
    private static final AttributeKey<WebSocketServerHandshaker> HANDSHAKER_ATTR_KEY =
            AttributeKey.valueOf(WebSocketServerHandshaker.class, "HANDSHAKER");
    public static final String WEBSOCKET_PATH = "/websocket";

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                getWebSocketLocation(msg), null, true);
        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(msg);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            try {
                handshaker.handshake(ctx.channel(), msg).addListener(ContextHelper.getBean(UserConnectWebsocketSuccessListener.class, msg, ctx));
                ctx.attr(HANDSHAKER_ATTR_KEY).set(handshaker);
            } catch (Exception e) {
                logger.error("request: " + msg.uri(), e);
            }
        }
    }

    private static String getWebSocketLocation(FullHttpRequest req) {
        String location = req.headers().get(HOST) + WEBSOCKET_PATH;
        return "ws://" + location;
    }
}
