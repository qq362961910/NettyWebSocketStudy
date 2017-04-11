import com.wxsk.pusher.client.RedisClient;
import com.wxsk.pusher.entity.Message;
import com.wxsk.pusher.enums.ResourceAction;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RedisClientTest {
    public static void main(String[] args) throws Exception{

        String topic = "xf_message";
        RedisClient redisClient = new RedisClient(getJedisCluster(), topic);
        Message message = new Message();
        Map<String, Object> extensions = new HashMap<>();
//        message.setFrom("Source_A");
//        message.setTo("yangjian,zhangpengfei");
        for (int i=0; i<20; i++) {
//            message.setResourceName(PredefinedResource.USER_MESSAGE.getValue());
            message.setResourceName("shuxue");
            message.setAction(ResourceAction.ADD.getValue());
            message.setBody(i);
            message.setExtensions(extensions);
            redisClient.sendMessage(message);
            Thread.sleep(100);
        }
    }
    public static JedisCluster getJedisCluster(){
        Set<HostAndPort> jedisClusterNodes = new HashSet<HostAndPort>();
        jedisClusterNodes.add(new HostAndPort("redis.cloud.server1", 6381));
        jedisClusterNodes.add(new HostAndPort("redis.cloud.server2", 6381));
        jedisClusterNodes.add(new HostAndPort("redis.cloud.server3", 6381));
        jedisClusterNodes.add(new HostAndPort("redis.cloud.server4", 6382));
        jedisClusterNodes.add(new HostAndPort("redis.cloud.server5", 6382));
        jedisClusterNodes.add(new HostAndPort("redis.cloud.server6", 6382));
        // 3个master 节点
        JedisCluster jc = new JedisCluster(jedisClusterNodes);
        return jc;
    }
}
