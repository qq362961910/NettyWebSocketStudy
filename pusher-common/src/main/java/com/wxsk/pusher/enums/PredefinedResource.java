package com.wxsk.pusher.enums;

public enum PredefinedResource {

    /**
     * 订阅
     * */
    SUBSCRIBE("subscribe"),

    /**
     * 用户点对点消息
     * */
    USER_MESSAGE("user_message"),

    /**
     * 群组消息
     * */
    GROUP_MESSAGE("group_message");

    public static PredefinedResource  getPredefinedResource(String resourceName) {
        for (PredefinedResource resource: values()) {
            if (resource.name().equals(resourceName)) {
                return resource;
            }
        }
        return null;
    }

    private String value;

    public String getValue() {
        return value;
    }

    public PredefinedResource setValue(String value) {
        this.value = value;
        return this;
    }

    PredefinedResource(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "PredefinedResource{" +
                "value='" + value + '\'' +
                "} " + super.toString();
    }
}
