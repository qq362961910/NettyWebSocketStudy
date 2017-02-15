package pusher.enums;

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

    private String name;

    public String getName() {
        return name;
    }

    public ResourceAction setName(String name) {
        this.name = name;
        return this;
    }

    ResourceAction(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ResourceAction{" +
                "name='" + name + '\'' +
                "} " + super.toString();
    }
}
