package it.wldt.adapter.physical;

import java.util.Map;

public class PhysicalAssetPropertyVariation {

    private long timestamp;

    private String propertykey;

    private Object body;

    private Map<String, Object> variationMetadata;

    private PhysicalAssetPropertyVariation() {
    }

    public PhysicalAssetPropertyVariation(long timestamp, String propertykey, Object body, Map<String, Object> variationMetadata) {
        this.timestamp = timestamp;
        this.propertykey = propertykey;
        this.body = body;
        this.variationMetadata = variationMetadata;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPropertykey() {
        return propertykey;
    }

    public void setPropertykey(String propertykey) {
        this.propertykey = propertykey;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
    }

    public Map<String, Object> getVariationMetadata() {
        return variationMetadata;
    }

    public void setVariationMetadata(Map<String, Object> variationMetadata) {
        this.variationMetadata = variationMetadata;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("PhysicalAssetPropertyVariation{");
        sb.append("timestamp=").append(timestamp);
        sb.append(", propertykey='").append(propertykey).append('\'');
        sb.append(", body=").append(body);
        sb.append(", variationMetadata=").append(variationMetadata);
        sb.append('}');
        return sb.toString();
    }
}
