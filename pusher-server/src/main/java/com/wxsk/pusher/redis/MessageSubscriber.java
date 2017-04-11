package com.wxsk.pusher.redis;

import com.wxsk.common.redis.TopicSubscriber;
import com.wxsk.pusher.entity.Message;
import com.wxsk.pusher.enums.PredefinedResource;
import com.wxsk.pusher.enums.ResourceAction;
import com.wxsk.pusher.util.ChannelUtil;
import com.wxsk.pusher.util.JsonUtil;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisCluster;

import java.io.IOException;
import java.util.List;

public class MessageSubscriber extends TopicSubscriber{

    private static final Logger logger = LoggerFactory.getLogger(MessageSubscriber.class);

    public MessageSubscriber(JedisCluster jedisCluster, List<String> topics) {
        super(jedisCluster, topics);
    }

    @Override
    public void onMessage(String channel, String msg) {
        try {
            Message message = JsonUtil.parseObjectFromJson(Message.class, msg);
            logger.info("receive a message from channel: " + channel);
            logger.info("resource: " + message.getResourceName());
            logger.info("action: " + message.getAction());
            logger.info("extensions: " + message.getExtensions());
            logger.info("body: " + message.getBody());

            String resource = message.getResourceName();
            //订阅消息
            if(PredefinedResource.SUBSCRIBE.getValue().equals(resource)) {
                String action = message.getAction();
                String topic = (String)message.getBody();
                //add
                if (ResourceAction.ADD.getValue().equalsIgnoreCase(action)) {
                    if (topic != null && topic.trim().length() > 0) {
                        ChannelUtil.addListenResource(topic, message.getFrom());
                    }
                }
                //remove
                else if (ResourceAction.REMOVE.getValue().equalsIgnoreCase(action)) {
                    if (topic != null && topic.trim().length() > 0) {
                        ChannelUtil.removeListenResource(topic, message.getFrom());
                    }
                }
                else {

                }
            }
            //用户点对点消息
            else if (PredefinedResource.USER_MESSAGE.getValue().equals(resource)) {
                String targetUsernames = message.getTo();
                if (StringUtils.isNotEmpty(targetUsernames)) {
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
                    String[] groupsToUse = targetGroups.split(",");
                    for (String group: groupsToUse) {
                        ChannelUtil.sendChannelGroupTextMessage(group, new TextWebSocketFrame(message.toJsonString()));
                    }
                }
            }
            //资源消息
            else {
                TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(message.toJsonString());
                ChannelUtil.sendChannelGroupResourceTextMessage(message.getResourceName(), textWebSocketFrame);
            }

        } catch (IOException e) {
            logger.error("", e);
        }
    }

}
