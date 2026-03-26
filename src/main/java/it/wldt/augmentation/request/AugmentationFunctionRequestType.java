package it.wldt.augmentation.request;

/**
 * Enum representing the types of requests that can be made for an augmentation function. This enum defines the possible
 * actions that can be performed on an augmentation function, such as starting the function, stopping it, or executing it.
 * Each enum constant is associated with a string value that represents the type of request, allowing for easy
 * identification and handling of different request types within the system.
 */
public enum AugmentationFunctionRequestType {
    START("START"),
    STOP("STOP"),
    EXECUTE("EXECUTE");

    private String value;

    private AugmentationFunctionRequestType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}