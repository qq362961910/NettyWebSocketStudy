package com.wxsk.pusher.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.wxsk.pusher.enums.ExtensionType;
import com.wxsk.pusher.util.JsonUtil;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息
 * */
public class Message {

    @Id
    private ObjectId id;

    /**
     * 资源名称
     * */
    private String resourceName;

    /**
     * from
     * */
    private String from;

    /**
     * to
     * */
    private String to;

    /**
     * 动作
     * */
    private String action;

    /**
     * 扩展
     * */
    private Map<String, Object> extensions = new HashMap<>();

    /**
     * body
     * */
    private Object body;

    /**
     * {@link com.wxsk.pusher.enums.MessageStatus}
     * */
    public int status;

    public String toJsonString() throws JsonProcessingException {
        return JsonUtil.formatObjectToJson(this);
    }

    public ObjectId getId() {
        return id;
    }

    public Message setId(ObjectId id) {
        this.id = id;
        return this;
    }

    public String getResourceName() {
        return resourceName;
    }

    public Message setResourceName(String resourceName) {
        this.resourceName = resourceName;
        return this;
    }

    public String getFrom() {
        return from;
    }

    public Message setFrom(String from) {
        this.from = from;
        return this;
    }

    public String getTo() {
        return to;
    }

    public Message setTo(String to) {
        this.to = to;
        return this;
    }

    public String getAction() {
        return action;
    }

    public Message setAction(String action) {
        this.action = action;
        return this;
    }

    public Object getBody() {
        return body;
    }

    public Message setBody(Object body) {
        this.body = body;
        return this;
    }

    public int getStatus() {
        return status;
    }

    public Message setStatus(int status) {
        this.status = status;
        return this;
    }

    public Object getExtensionValue(ExtensionType type) {
        return extensions.get(type.getValue());
    }

    public Message setExtensionValue(ExtensionType type, Object value) {
        extensions.put(type.getValue(), value);
        return this;
    }

    public Message setExtensionValue(String type, Object value) {
        this.extensions.put(type, value);
        return this;
    }

    public Message setExtensions(Map<String, Object> extensions) {
        if (extensions != null) {
            this.extensions.putAll(extensions);
        }
        return this;
    }

    public Map<String, Object> getExtensions() {
        return extensions;
    }

    public Message() {
    }

    public Message(String resourceName, String from, String to, String action, Map<String, Object> extensions, Object body, int status) {
        this.resourceName = resourceName;
        this.from = from;
        this.to = to;
        this.action = action;
        this.extensions = extensions;
        this.body = body;
        this.status = status;
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", resourceName='" + resourceName + '\'' +
                ", from=" + from +
                ", to=" + to +
                ", action='" + action + '\'' +
                ", extensions=" + extensions +
                ", body=" + body +
                ", status=" + status +
                '}';
    }
}
