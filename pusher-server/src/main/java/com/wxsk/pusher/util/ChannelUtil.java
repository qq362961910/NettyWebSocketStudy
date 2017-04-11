package com.wxsk.pusher.util;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelUtil {

    private static final Logger logger = LoggerFactory.getLogger(ChannelUtil.class);
    private static final Map<Long, ChannelGroup> USER_GROUP_MAPPING = new ConcurrentHashMap<>();
    private static final ChannelGroup ALL_CHANNELS = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static final Map<String, ChannelId> USER_CHANNEL_MAPPING = new HashMap<>();
    private static final Map<String, ChannelGroup> GROUP_RESOURCE_CHANNEL_MAPPING = new HashMap<>();


    /**
     * 添加用户连接映射
     */
    public static final void addUserChannel(String username, Channel channel) {
        USER_CHANNEL_MAPPING.put(username, channel.id());
        ALL_CHANNELS.add(channel);
    }

    /**
     * 添加用户组映射
     */
    public static synchronized void addUserGroupMapping(Long groupId, Channel channel) {
        ChannelGroup channelGroup = USER_GROUP_MAPPING.get(groupId);
        if (channelGroup == null) {
            channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            USER_GROUP_MAPPING.put(groupId, channelGroup);
        }
        channelGroup.add(channel);
    }

    /**
     * 发送指定群组消息(All Channel)
     */
    public static void sendChannelGroupTextMessage(Long groupId, TextWebSocketFrame message) {
        ChannelGroup channelGroup = USER_GROUP_MAPPING.get(groupId);
        if (channelGroup != null) {
            channelGroup.writeAndFlush(message);
        }
    }

    public static void sendChannelGroupTextMessage(Object groupId, TextWebSocketFrame message) {
        //默认发送所有用户小修
        if (groupId == null) {
            sendAllUserTextMessage(message);
        }
        else if (groupId instanceof Integer) {
            sendChannelGroupTextMessage(((Integer)groupId).longValue(), message);
        }
        else if (groupId instanceof Long) {
            sendChannelGroupTextMessage((Long)groupId, message);
        }
        else {
            String groupIdStr = groupId.toString();
            if ("".equals(groupIdStr.trim())) {
                sendAllUserTextMessage(message);
            }
            else {
                sendChannelGroupTextMessage(Long.parseLong(groupIdStr), message);
            }
        }
    }


    /**
     * 发送所有在线用户消息
     * */
    public static void sendAllUserTextMessage(TextWebSocketFrame message) {
        ALL_CHANNELS.writeAndFlush(message);
    }

    /**
     * 发送群组消息(用户监听资源)
     * */
    public static void sendChannelGroupResourceTextMessage(String resourceName, TextWebSocketFrame message) {
        ChannelGroup channelGroup = GROUP_RESOURCE_CHANNEL_MAPPING.get(resourceName);
        if (channelGroup != null) {
            channelGroup.writeAndFlush(message);
        }
    }
    /**
     * 发送用户消息
     * */
    public static void sendUserChannelTextMessage(String username, TextWebSocketFrame message) {
        ChannelId channelId = USER_CHANNEL_MAPPING.get(username);
        if (channelId != null) {
            Channel channel = ALL_CHANNELS.find(channelId);
            if (channel != null) {
                channel.writeAndFlush(message);
            }
        }
    }

    /**
     * 移除用户组Channel
     */
    public static void cleanAllUserChannel(String username) {
        //用户消息集合
        ChannelId channelId = USER_CHANNEL_MAPPING.get(username);
        if (channelId != null) {
            USER_CHANNEL_MAPPING.remove(username);
            //群组消息集合
            Channel channel = ALL_CHANNELS.find(channelId);
            if (channel != null) {
                ALL_CHANNELS.remove(channel);
            }
            //资源消息集合
            for (Map.Entry<Long, ChannelGroup> entry : USER_GROUP_MAPPING.entrySet()) {
                Channel groupChannel = entry.getValue().find(channelId);
                if (groupChannel != null) {
                    logger.info("clean user channel, group: " + entry.getKey());
                    entry.getValue().remove(groupChannel);
                }
            }
        }
    }
    /**
     * 获取所有在线单位
     * */
    public static Set<Long> getAllUnitIds() {
        return USER_GROUP_MAPPING.keySet();
    }

    /**
     * 监听资源
     * */
    public static synchronized boolean addListenResource(String resource, Channel channel) {
        ChannelGroup channelGroup = GROUP_RESOURCE_CHANNEL_MAPPING.get(resource);
        if (channelGroup == null) {
            channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            GROUP_RESOURCE_CHANNEL_MAPPING.put(resource, channelGroup);
        }
        channelGroup.add(channel);
        return channelGroup.size() == 1;
    }
    /**
     * 监听资源
     * */
    public static synchronized boolean addListenResource(String resource, String username) {
        ChannelGroup channelGroup = GROUP_RESOURCE_CHANNEL_MAPPING.get(resource);
        if (channelGroup == null) {
            channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            GROUP_RESOURCE_CHANNEL_MAPPING.put(resource, channelGroup);
        }
        ChannelId channelId = USER_CHANNEL_MAPPING.get(username);
        if (channelId != null) {
            Channel channel = ALL_CHANNELS.find(channelId);
            if (channel != null) {
                channelGroup.add(channel);
            }
        }
        return channelGroup.size() == 1;
    }

    /**
     * 取消监听资源
     * */
    public static synchronized boolean removeListenResource(String resource, Channel channel) {
        ChannelGroup channelGroup = GROUP_RESOURCE_CHANNEL_MAPPING.get(resource);
        if (channelGroup != null) {
            channelGroup.remove(channel);
        }
        return channelGroup.size() == 0;
    }

    /**
     * 取消监听资源
     * */
    public static synchronized boolean removeListenResource(String resource, String username) {
        ChannelGroup channelGroup = GROUP_RESOURCE_CHANNEL_MAPPING.get(resource);
        if (channelGroup != null) {
            ChannelId channelId = USER_CHANNEL_MAPPING.get(username);
            if (channelId != null) {
                Channel channel = ALL_CHANNELS.find(channelId);
                if (channel != null) {
                    channelGroup.remove(channel);
                }
            }
        }
        return channelGroup.size() == 0;
    }

}
