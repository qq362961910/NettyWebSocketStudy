package com.wxsk.pusher.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wxsk.common.redis.TopicPublisher;
import com.wxsk.pusher.entity.Message;
import com.wxsk.pusher.util.JsonUtil;
import redis.clients.jedis.JedisCluster;

public class RedisClient extends TopicPublisher{

    private String[] topics;

    public RedisClient(JedisCluster jedisCluster, String... topics) {
        super(jedisCluster);
        this.topics = topics;
    }

    public void sendMessage(Message message) throws JsonProcessingException {
        String json = JsonUtil.formatObjectToJson(message);
        publish(json, topics);
    }
}
