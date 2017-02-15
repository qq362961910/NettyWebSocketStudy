package com.wxsk.pusher.redis;

import com.wxsk.common.redis.TopicSubscriber;
import com.wxsk.pusher.util.ChannelUtil;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pusher.entity.Message;
import pusher.util.JsonUtil;
import redis.clients.jedis.JedisCluster;

import java.io.IOException;
import java.util.List;

public class MessageSubscriber extends TopicSubscriber{

    private static final Logger logger = LoggerFactory.getLogger(MessageSubscriber.class);

    public MessageSubscriber(JedisCluster jedisCluster, List<String> topics) {
        super(jedisCluster, topics);
    }

    @Override
    public void onMessage(String channel, String message) {
        try {
            Message msg = JsonUtil.parseObjectFromJson(Message.class, message);
            logger.info("receive a message from channel: " + channel);
            logger.info("resource: " + msg.getResourceName());
            logger.info("action: " + msg.getAction());
            logger.info("extensions: " + msg.getExtensions());
            logger.info("body: " + msg.getBody());
            TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(message);
            ChannelUtil.sendChannelGroupTextMessage(msg.getResourceName(), textWebSocketFrame);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
