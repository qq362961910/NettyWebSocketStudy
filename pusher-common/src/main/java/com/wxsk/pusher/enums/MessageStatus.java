package com.wxsk.pusher.enums;

public enum MessageStatus {

    /**
     * 初始状态
     * */
    INIT(0),

    /**
     * 已发送
     * */
    SENT(1);


    public static MessageStatus getMessageStatus(int value) {
        for (MessageStatus status: values()) {
            if (status.value == value) {
                return status;
            }
        }
        return null;
    }

    private int value;

    public int getValue() {
        return value;
    }

    public MessageStatus setValue(int value) {
        this.value = value;
        return this;
    }

    MessageStatus(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "MessageStatus{" +
                "value=" + value +
                "} " + super.toString();
    }
}
