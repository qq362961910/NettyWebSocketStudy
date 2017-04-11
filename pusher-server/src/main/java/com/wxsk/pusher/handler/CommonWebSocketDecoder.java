package com.wxsk.pusher.handler;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.AttributeKey;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Scope("prototype")
@Component
public class CommonWebSocketDecoder extends MessageToMessageDecoder<WebSocketFrame>{

    private static final AttributeKey<WebSocketServerHandshaker> HANDSHAKER_ATTR_KEY =
            AttributeKey.valueOf(WebSocketServerHandshaker.class, "HANDSHAKER");

    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame frame, List<Object> out) throws Exception {

        if (frame instanceof CloseWebSocketFrame) {
            WebSocketServerHandshaker handshaker = ctx.attr(HANDSHAKER_ATTR_KEY).get();
            if (handshaker != null) {
                frame.retain();
                handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame);
            } else {
                ctx.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
            }
            return;
        }
        else if (frame instanceof PingWebSocketFrame) {
            frame.content().retain();
            ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content()));
            return;
        }
        else if (frame instanceof PongWebSocketFrame) {
            // Pong frames need to get ignored
            return;
        }
        else {
            out.add(frame.retain());
        }
    }
}
