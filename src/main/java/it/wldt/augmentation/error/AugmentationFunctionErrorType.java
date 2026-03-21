package it.wldt.augmentation.error;

public enum AugmentationFunctionErrorType {

    INFO("info"),
    WARNING("warning"),
    ERROR("error"),
    CRITICAL("critical");

    private String value;

    private AugmentationFunctionErrorType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
