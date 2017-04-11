package com.wxsk.pusher.enums;

public enum ResourceAction {

    /**
     * 添加
     * */
    ADD("add"),

    /**
     * 移除
     * */
    REMOVE("remove");

    public static ResourceAction  getResourceAction(String actionname) {
        for (ResourceAction action: values()) {
            if (action.name().equals(actionname)) {
                return action;
            }
        }
        return null;
    }

    private String value;

    public String getValue() {
        return value;
    }

    public ResourceAction setValue(String value) {
        this.value = value;
        return this;
    }

    ResourceAction(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ResourceAction{" +
                "value='" + value + '\'' +
                "} " + super.toString();
    }
}
