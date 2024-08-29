package it.wldt.storage.query;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 25/07/2024
 * This Enum represents the Query Request Type used to specify the type of query to be performed
 * on the storage system (e.g., Time Range Query, Sample Range Query, Last Value Query, Count Query)
 */
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
