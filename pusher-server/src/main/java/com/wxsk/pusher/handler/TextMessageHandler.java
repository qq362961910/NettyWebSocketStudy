package com.wxsk.pusher.handler;

import com.wxsk.common.json.JSONUtil;
import com.wxsk.pusher.redis.MessageSubscriber;
import com.wxsk.pusher.util.ChannelUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import pusher.entity.Message;
import pusher.enums.ExtensionType;
import pusher.enums.PredefinedResource;
import pusher.enums.ResourceAction;

import java.util.Map;

@Scope("prototype")
@Component
public class TextMessageHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    private static final Logger logger = LoggerFactory.getLogger(TextMessageHandler.class);
    private String username;
    private Long unitId;

    @Autowired
    private MessageSubscriber messageSubscriber;

    @Override
    protected void messageReceived(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        String content = msg.text();
        logger.info("user: " + username + " send message: " + content);
        Message message = JSONUtil.getObjectByJsonStr( content, Message.class);
        String resource = message.getResourceName();
        //订阅消息
        if(PredefinedResource.SUBSCRIBE.getName().equals(resource)) {
            String action = message.getAction();
            String topic = (String)message.getBody();
            //add
            if (ResourceAction.ADD.getName().equalsIgnoreCase(action)) {
                if (topic != null && topic.trim().length() > 0) {
                    ChannelUtil.addListenResource(topic, ctx.channel());
                }
            }
            //remove
            else if (ResourceAction.REMOVE.getName().equalsIgnoreCase(action)) {
                if (topic != null && topic.trim().length() > 0) {
                    ChannelUtil.removeListenResource(topic, ctx.channel());
                }
            }
            else {

            }
        }
        //用户点对点消息
        else if (PredefinedResource.USER_MESSAGE.getName().equals(resource)) {
            Map<String,Object> extensions = message.getExtensions();
            if (extensions != null) {
                String username = (String)extensions.get(ExtensionType.TARGET_USERNAME.getExtensionName());
                ChannelUtil.sendUserChannelTextMessage(username, msg.duplicate().retain());
            }
        }
        //群组消息
        else if (PredefinedResource.GROUP_MESSAGE.getName().equals(resource)) {
            Map<String,Object> extensions = message.getExtensions();
            Object groupId;
            if (extensions != null) {
                groupId = extensions.get(ExtensionType.GROUP_ID.getExtensionName());
            }
            else {
                groupId = null;
            }
            ChannelUtil.sendChannelGroupTextMessage(groupId, msg.duplicate().retain());
        }
        //资源消息
        else {
            ChannelUtil.sendChannelGroupTextMessage(resource, msg.duplicate().retain());
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
        ChannelUtil.cleanAllUserChannel(username);
    }
}
