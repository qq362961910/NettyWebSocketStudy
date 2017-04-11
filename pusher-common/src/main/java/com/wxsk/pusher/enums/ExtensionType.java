package com.wxsk.pusher.enums;

/**
 * 消息扩展类型
 * */
public enum ExtensionType {

    /**
     * 创建时间
     * */
    CREATE_TIME("createTime"),

    /**
     * 接收时间
     * */
    RECEIVE_TIME("receiveTIme"),

    /**
     * DB ID
     * */
    DB_ID("dbId"),

    /**
     * Title
     * */
    TITLE("title");



    public static ExtensionType getExtensionType(String value) {
        for (ExtensionType type: values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }
    private String value;

    public String getValue() {
        return value;
    }

    public ExtensionType setValue(String value) {
        this.value = value;
        return this;
    }

    ExtensionType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ExtensionType{" +
                "value='" + value + '\'' +
                "} " + super.toString();
    }
}
