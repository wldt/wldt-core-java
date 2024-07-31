package it.wldt.storage.query;

public enum QueryRequestType {

    TIME_RANGE("TIME_RANGE"),
    SAMPLE_RANGE("SAMPLE_RANGE"),
    LAST_VALUE("LAST_VALUE"),
    COUNT("COUNT");

    private String value;

    private QueryRequestType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
