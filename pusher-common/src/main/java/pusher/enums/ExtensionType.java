package pusher.enums;

public enum ExtensionType {

    /**
     * 目标用户
     * */
    TARGET_USERNAME("target_username"),

    /**
     * 来源用户
     * */
    SOURCE_USERNAME("source_username"),

    /**
     * 群组ID
     * */
    GROUP_ID("group_id");


    public static ExtensionType getExtensionType(String extensionName) {
        for (ExtensionType type: values()) {
            if (type.extensionName.equals(extensionName)) {
                return type;
            }
        }
        return null;
    }
    private String extensionName;

    public String getExtensionName() {
        return extensionName;
    }

    public ExtensionType setExtensionName(String extensionName) {
        this.extensionName = extensionName;
        return this;
    }

    ExtensionType(String extensionName) {
        this.extensionName = extensionName;
    }

    @Override
    public String toString() {
        return "ExtensionType{" +
                "extensionName='" + extensionName + '\'' +
                "} " + super.toString();
    }
}
