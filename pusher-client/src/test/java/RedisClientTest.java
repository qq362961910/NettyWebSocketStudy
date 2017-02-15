import pusher.client.RedisClient;
import pusher.entity.Message;
import pusher.enums.ExtensionType;
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
        extensions.put(ExtensionType.SOURCE_USERNAME.getExtensionName(), "Source_A");
        for (int i=0; i<1000; i++) {
            message.setResourceName("yuwen");
            message.setAction("modify");
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
