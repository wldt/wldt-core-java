package it.wldt.augmentation.request;

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