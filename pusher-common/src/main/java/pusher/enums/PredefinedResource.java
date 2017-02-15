package pusher.enums;

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

    private String name;

    public String getName() {
        return name;
    }

    public PredefinedResource setName(String name) {
        this.name = name;
        return this;
    }

    PredefinedResource(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "PredefinedResource{" +
                "name='" + name + '\'' +
                "} " + super.toString();
    }
}
