package it.wldt.augmentation.error;

/**
 * Enum representing the type of an error that can occur during the execution of an augmentation function.
 * <p>
 * This enum defines different levels of error severity, which can be used to categorize and handle errors
 * appropriately based on their type. The error types can include informational messages, warnings, errors,
 * and critical errors, allowing for a structured approach to error handling and logging in the context of augmentation functions.
 * </p>
 */
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
