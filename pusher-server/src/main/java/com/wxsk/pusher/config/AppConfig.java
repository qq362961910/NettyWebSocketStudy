package com.wxsk.pusher.config;

import com.alibaba.dubbo.config.*;
import com.alibaba.dubbo.config.spring.AnnotationBean;
import com.mongodb.Mongo;
import com.wxsk.cas.client.interceptor.AccessRequiredAdminInteceptor;
import com.wxsk.common.redis.StringRedisClusterUtil;
import com.wxsk.passport.service.remote.ISourceServiceRemote;
import com.wxsk.pusher.redis.MessageSubscriber;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.core.MongoClientFactoryBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Configuration
@ComponentScan(
        basePackages = "com.wxsk.pusher"
) //包扫描配置
@PropertySource({"dubbo.properties", "netty.properties", "redis.properties", "mongodb.properties"})   //配置文件配置
public class AppConfig {

    @Autowired
    private Environment environment; //读取配置

    /*与<dubbo:annotation/>相当.提供方扫描带有@com.alibaba.dubbo.config.annotation.Service的注解类*/
    @Bean
    public static AnnotationBean annotationBean() {
        AnnotationBean annotationBean = new AnnotationBean();
        annotationBean.setPackage("com.wxsk.helper");//所以含有@com.alibaba.dubbo.config.annotation.Service的注解类都应在此包中,多个包名可以使用英文逗号分隔.
        return annotationBean;
    }

    /*与<dubbo:application/>相当.*/
    @Bean
    public ApplicationConfig applicationConfig() {
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setLogger("slf4j");
        applicationConfig.setName(environment.getProperty("dubbo.applicationName"));
        return applicationConfig;
    }

    @Bean
    public ConsumerConfig consumerConfig() {
        ConsumerConfig consumerConfig = new ConsumerConfig();
        consumerConfig.setDefault(true);
        consumerConfig.setCheck(false);
        return consumerConfig;
    }

    /*与<dubbo:registry/>相当*/
    @Bean
    public RegistryConfig registryConfig() {
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(environment.getProperty("dubbo.provider.registryAddress"));
        return registryConfig;
    }

    /*与<dubbo:protocol/>相当*/
    @Bean
    public ProtocolConfig protocolConfig() {
        ProtocolConfig protocolConfig = new ProtocolConfig("dubbo", environment.getProperty("dubbo.provider.port", Integer.class));
        protocolConfig.setSerialization("java");//默认为hessian2,但不支持无参构造函数类,而这种方式的效率很低
        return protocolConfig;
    }

    @Bean
    @Autowired
    public ISourceServiceRemote iSourceServiceRemote(ApplicationConfig applicationConfig, RegistryConfig registryConfig) {
        // 引用远程服务
        ReferenceConfig<ISourceServiceRemote> reference = new ReferenceConfig<ISourceServiceRemote>(); // 此实例很重，封装了与注册中心的连接以及与提供者的连接，请自行缓存，否则可能造成内存和连接泄漏
        reference.setApplication(applicationConfig);
        reference.setRegistry(registryConfig); // 多个注册中心可以用setRegistries()
        reference.setInterface(ISourceServiceRemote.class);
        reference.setVersion("1.0.0");
        reference.setLazy(true);
        reference.setCheck(false);
        reference.setTimeout(5000);
        // 和本地bean一样使用xxxService
        ISourceServiceRemote iSourceServiceRemote = reference.get();// 注意：此代理对象内部封装了所有通讯细节，对象较重，请缓存复用
        return iSourceServiceRemote;
    }

    @Bean
    public JedisCluster jedisCluster() {
        Set<HostAndPort> hosts = new HashSet<>();
        String[] hostStr = environment.getProperty("redis.cluster.hosts", "localhost:6379").split(",");
        for (String host : hostStr) {
            String[] hostElements = host.split(":");
            hosts.add(new HostAndPort(hostElements[0], Integer.parseInt(hostElements[1])));
        }
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(environment.getProperty("redis.maxTotal", Integer.class));
        config.setTestOnBorrow(environment.getProperty("redis.testOnBorrow", Boolean.class));
        config.setTestWhileIdle(environment.getProperty("redis.testWhileIdle", Boolean.class));
        config.setMinIdle(environment.getProperty("redis.minIdle", Integer.class));
        config.setMaxIdle(environment.getProperty("redis.maxIdle", Integer.class));
        config.setMaxWaitMillis(environment.getProperty("redis.maxWaitMillis", Long.class));
        return new JedisCluster(hosts, config);
    }

    @Bean
    @Autowired
    public StringRedisClusterUtil stringRedisClusterUtil(JedisCluster jedisCluster) {
        StringRedisClusterUtil stringRedisClusterUtil = new StringRedisClusterUtil();
        stringRedisClusterUtil.setJedisCluster(jedisCluster);
        return stringRedisClusterUtil;
    }

    @Autowired
    @Bean
    public AccessRequiredAdminInteceptor AccessRequiredAdminInteceptor(ISourceServiceRemote iSourceServiceRemote, StringRedisClusterUtil stringRedisClusterUtil) {
        AccessRequiredAdminInteceptor accessRequiredAdminInteceptor = new AccessRequiredAdminInteceptor();
        accessRequiredAdminInteceptor.setSourceServiceRemote(iSourceServiceRemote);
        accessRequiredAdminInteceptor.setStringRedisClusterUtil(stringRedisClusterUtil);
        return accessRequiredAdminInteceptor;
    }

    @Bean
    @Autowired
    public MessageSubscriber messageSubscriber(JedisCluster jedisCluster) {
        String topicPro = environment.getProperty("redis.topics","default");
        String[] topics = topicPro.split(",");
        MessageSubscriber messageSubscriber = new MessageSubscriber(jedisCluster, Arrays.asList(topics));
        return messageSubscriber;
    }


    @Scope("prototype")
    @Bean
    public HttpServerCodec httpServerCodec() {
        return new HttpServerCodec();
    }

    @Scope("prototype")
    @Bean
    public HttpObjectAggregator httpObjectAggregator() {
        return new HttpObjectAggregator(1024 * 1024);
    }

    @Scope("prototype")
    @Bean
    public WebSocketServerCompressionHandler webSocketServerCompressionHandler() {
        return new WebSocketServerCompressionHandler();
    }

    @Bean
    public Mongo mongo() throws Exception {
        MongoClientFactoryBean mongoClientFactoryBean = new MongoClientFactoryBean();
        mongoClientFactoryBean.setHost(environment.getProperty("mongodb.host"));
        mongoClientFactoryBean.setPort(environment.getProperty("mongodb.port", Integer.class));
        mongoClientFactoryBean.afterPropertiesSet();
        return mongoClientFactoryBean.getObject();
    }

    @Bean
    @Autowired
    public MongoTemplate mongoTemplate(Mongo mongo) {
        String database = environment.getProperty("mongodb.database");
        MongoTemplate mongoTemplate = new MongoTemplate(mongo, database);
        return mongoTemplate;
    }


}
