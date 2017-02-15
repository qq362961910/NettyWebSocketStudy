package pusher.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wxsk.common.redis.TopicPublisher;
import pusher.entity.Message;
import pusher.util.JsonUtil;
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
