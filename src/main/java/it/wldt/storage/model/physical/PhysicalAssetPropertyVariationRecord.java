package it.wldt.storage.model.physical;

import it.wldt.storage.model.StorageRecord;

import java.util.Map;

/**
 * Author: Marco Picone (picone.m@gmail.com)
 * Date: 01/08/2024
 * Physical Asset Property Variation Class
 * Represents a Physical Asset Property Variation in the storage
 */
public class PhysicalAssetPropertyVariationRecord extends StorageRecord {

    // Timestamp of the variation
    private long timestamp;

    // Property Key associated to the variation
    private String propertykey;

    // Body of the variation
    private Object body;

    // Metadata associated to the variation
    private Map<String, Object> variationMetadata;

    /**
     * Default Constructor
     */
    private PhysicalAssetPropertyVariationRecord() {
    }

    /**
     * Constructor
     *
     * @param timestamp Timestamp of the variation
     * @param propertykey Property Key associated to the variation
     * @param body Body of the variation
     * @param variationMetadata Metadata associated to the variation
     */
    public PhysicalAssetPropertyVariationRecord(long timestamp, String propertykey, Object body, Map<String, Object> variationMetadata) {
        this.timestamp = timestamp;
        this.propertykey = propertykey;
        this.body = body;
        this.variationMetadata = variationMetadata;
    }

    /**
     * Get the timestamp of the variation
     * @return timestamp of the variation
     */
    public long getTimestamp() {
        return timestamp;
    }

    /**
     * Set the timestamp of the variation
     * @param timestamp timestamp of the variation
     */
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Get the Property Key associated to the variation
     * @return Property Key associated to the variation
     */
    public String getPropertykey() {
        return propertykey;
    }

    /**
     * Set the Property Key associated to the variation
     * @param propertykey Property Key associated to the variation
     */
    public void setPropertykey(String propertykey) {
        this.propertykey = propertykey;
    }

    /**
     * Get the Body of the variation
     * @return Body of the variation
     */
    public Object getBody() {
        return body;
    }

    /**
     * Set the Body of the variation
     * @param body Body of the variation
     */
    public void setBody(Object body) {
        this.body = body;
    }

    /**
     * Get the Metadata associated to the variation
     * @return Metadata associated to the variation
     */
    public Map<String, Object> getVariationMetadata() {
        return variationMetadata;
    }

    /**
     * Set the Metadata associated to the variation
     * @param variationMetadata Metadata associated to the variation
     */
    public void setVariationMetadata(Map<String, Object> variationMetadata) {
        this.variationMetadata = variationMetadata;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PhysicalAssetPropertyVariation{");
        sb.append("recordId=").append(getId());
        sb.append("timestamp=").append(timestamp);
        sb.append(", propertykey='").append(propertykey).append('\'');
        sb.append(", body=").append(body);
        sb.append(", variationMetadata=").append(variationMetadata);
        sb.append('}');
        return sb.toString();
    }
}
