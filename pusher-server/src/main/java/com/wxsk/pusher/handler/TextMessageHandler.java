package com.wxsk.pusher.handler;

import com.wxsk.common.json.JSONUtil;
import com.wxsk.pusher.entity.Message;
import com.wxsk.pusher.enums.PredefinedResource;
import com.wxsk.pusher.enums.ResourceAction;
import com.wxsk.pusher.util.ChannelUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Scope("prototype")
@Component
public class TextMessageHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(TextMessageHandler.class);
    private String username;
    private Long unitId;

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        String content = msg.text();
        logger.info("user: " + username + " send message: " + content);
        Message message = JSONUtil.getObjectByJsonStr( content, Message.class);
        String resource = message.getResourceName();
        //订阅消息
        if(PredefinedResource.SUBSCRIBE.getValue().equals(resource)) {
            String action = message.getAction();
            String topic = (String)message.getBody();
            //add
            if (ResourceAction.ADD.getValue().equalsIgnoreCase(action)) {
                if (topic != null && topic.trim().length() > 0) {
                    ChannelUtil.addListenResource(topic, ctx.channel());
                }
            }
            //remove
            else if (ResourceAction.REMOVE.getValue().equalsIgnoreCase(action)) {
                if (topic != null && topic.trim().length() > 0) {
                    ChannelUtil.removeListenResource(topic, ctx.channel());
                }
            }
            else {

            }
        }
        //用户点对点消息
        else if (PredefinedResource.USER_MESSAGE.getValue().equals(resource)) {
            String targetUsernames = message.getTo();
            if (StringUtils.isNotEmpty(targetUsernames)) {
                message.setFrom(this.username);
                String[] usernamesToUse = targetUsernames.split(",");
                for (String targetUsername: usernamesToUse) {
                    message.setTo(targetUsername);
                    ChannelUtil.sendUserChannelTextMessage(targetUsername.trim(), new TextWebSocketFrame(message.toJsonString()));
                }
            }
        }
        //群组消息
        else if (PredefinedResource.GROUP_MESSAGE.getValue().equals(resource)) {
            String targetGroups = message.getTo();
            if (StringUtils.isNotEmpty(targetGroups)) {
                message.setFrom(this.username);
                String[] groupsToUse = targetGroups.split(",");
                for (String group: groupsToUse) {
                    ChannelUtil.sendChannelGroupTextMessage(group, new TextWebSocketFrame(message.toJsonString()));
                }
            }
        }
        //资源消息
        else {
            ChannelUtil.sendChannelGroupResourceTextMessage(resource, msg.duplicate().retain());
        }
    }

    public TextMessageHandler(String username, Long unitId) {
        this.username = username;
        this.unitId = unitId;
    }

    public TextMessageHandler() {
        super(true);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("Client Connection Exception, cleaning this Connection");
        ctx.close();
    }

}
