package com.wxsk.pusher.listener;

import com.wxsk.passport.model.User;
import com.wxsk.pusher.handler.HtmlPageHandler;
import com.wxsk.pusher.handler.TextMessageHandler;
import com.wxsk.pusher.handler.WebSocketHandShakerHandler;
import com.wxsk.pusher.helper.ContextHelper;
import com.wxsk.pusher.helper.UserHelper;
import com.wxsk.pusher.util.ChannelUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.AsciiString;
import io.netty.handler.codec.Headers;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pusher.enums.PredefinedResource;

@Scope("prototype")
@Component
public class UserConnectWebsocketSuccessListener implements ChannelFutureListener {

    private static final Logger logger = LoggerFactory.getLogger(UserConnectWebsocketSuccessListener.class);
    private static final AsciiString COOKIE_NAME = HttpHeaderNames.COOKIE.toUpperCase();
    private static final String TICKET_KEY = "ticket";

    @Autowired
    private UserHelper userHelper;
    private FullHttpRequest msg;
    private ChannelHandlerContext ctx;

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        Headers headers = msg.headers();
        String cookie = (String) headers.get(COOKIE_NAME);
        String ticket = null;
        if (cookie != null && cookie.trim().length() > 0) {
            String[] eles = cookie.split(";");
            for (String ele : eles) {
                String[] kv = ele.split("=");
                if (TICKET_KEY.equals(kv[0].trim())) {
                    if (kv.length > 1) {
                        ticket = kv[1].trim();
                    }
                }
            }
        }
        User user = userHelper.getUserByTicket(ticket);
        if (user == null) {
            logger.info("invalid, no username found!");
            ctx.close();
        }
        else {
            ctx.pipeline().remove(HtmlPageHandler.class);
            ctx.pipeline().remove(WebSocketHandShakerHandler.class);
            ctx.pipeline().addLast(ContextHelper.getBean(TextMessageHandler.class, user.getUsername(), user.getUnitId()));
            logger.info("websocket 建立完成, username: " + user.getUsername());
            ChannelUtil.addUserChannel(user.getUsername(), ctx.channel());
            ChannelUtil.addUserGroupMapping(user.getUnitId(), ctx.channel());
            ChannelUtil.addListenResource(PredefinedResource.USER_MESSAGE.getName(), ctx.channel());
            ChannelUtil.addListenResource(PredefinedResource.GROUP_MESSAGE.getName(), ctx.channel());
            logger.info("初始化完毕，建立用户连接映射，用户单位映射，添加默认监听资源:1." + PredefinedResource.USER_MESSAGE.getName() +", 2:" + PredefinedResource.GROUP_MESSAGE.getName());
        }
    }

    public UserConnectWebsocketSuccessListener(FullHttpRequest msg, ChannelHandlerContext ctx) {
        this.msg = msg;
        this.ctx = ctx;
    }
}
