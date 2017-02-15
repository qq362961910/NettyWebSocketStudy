package pusher.entity;

import com.fasterxml.jackson.core.JsonProcessingException;
import pusher.util.JsonUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * 消息
 * */
public class Message {

    /**
     * 资源名称
     * */
    private String resourceName;

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


    public String getResourceName() {
        return resourceName;
    }

    public Message setResourceName(String resourceName) {
        this.resourceName = resourceName;
        return this;
    }

    public String getAction() {
        return action;
    }

    public Message setAction(String action) {
        this.action = action;
        return this;
    }

    public Map<String, Object> getExtensions() {
        return extensions;
    }

    public Message setExtensions(Map<String, Object> extensions) {
        this.extensions = extensions;
        return this;
    }

    public Object getBody() {
        return body;
    }

    public Message setBody(Object body) {
        this.body = body;
        return this;
    }

    public Message() {
    }

    public Message(String resourceName, String action, Map<String, Object> extensions, Object body) {
        this.resourceName = resourceName;
        this.action = action;
        this.extensions = extensions;
        this.body = body;
    }

    public String toJsonString() throws JsonProcessingException {
        return JsonUtil.formatObjectToJson(this);
    }

    @Override
    public String toString() {
        return "Message{" +
                "resourceName='" + resourceName + '\'' +
                ", action='" + action + '\'' +
                ", extensions=" + extensions +
                ", body=" + body +
                '}';
    }
}
