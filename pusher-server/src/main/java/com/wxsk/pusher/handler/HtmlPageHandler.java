package com.wxsk.pusher.handler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.InputStream;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

@Scope("prototype")
@Component
public class HtmlPageHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final String PATH_TAIL = ".html";

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        // Handle a bad request.
        if (!req.decoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        // Allow only GET methods.
        if (req.method() != GET) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }

        // Send html page
        if (req.uri().endsWith(PATH_TAIL)) {
            String uri = req.uri();
            String htmlFile = uri.substring(uri.lastIndexOf("/") + 1);
            InputStream inputStream = HtmlPageHandler.class.getClassLoader().getResourceAsStream(htmlFile);
            //404
            if (inputStream == null) {
                sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND));
            }
            byte[] fileContent = new byte[inputStream.available()];
            inputStream.read(fileContent);
            inputStream.close();
            ByteBuf fileBuf = Unpooled.copiedBuffer(fileContent);
            FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, fileBuf);

            res.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8");
            HttpHeaderUtil.setContentLength(res, fileBuf.readableBytes());

            sendHttpResponse(ctx, req, res);
            ReferenceCountUtil.release(req);
        } else {
            ctx.fireChannelRead(req);
        }
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // Generate an error page if response getStatus code is not OK (200).
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpHeaderUtil.setContentLength(res, res.content().readableBytes());
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpHeaderUtil.isKeepAlive(req) || res.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    public HtmlPageHandler() {
        super(false);
    }
}
