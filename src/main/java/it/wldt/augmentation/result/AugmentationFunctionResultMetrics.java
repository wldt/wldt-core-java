package it.wldt.augmentation.result;

import it.wldt.exception.AugmentationFunctionException;

public class AugmentationFunctionResultMetrics {

    private Long totalExecutionTimeMs;
    private Long startTimestamp;
    private Long endTimestamp;

    private Integer recordsProcessed;
    private Integer recordsGenerated;

    private String executionId;
    private String nodeId;

    public AugmentationFunctionResultMetrics(Long totalExecutionTimeMs) throws AugmentationFunctionException {
        if (totalExecutionTimeMs == null || totalExecutionTimeMs < 0) {
            throw new AugmentationFunctionException("Total execution time cannot be null or negative.");
        }
        this.totalExecutionTimeMs = totalExecutionTimeMs;
    }

    public AugmentationFunctionResultMetrics(Long startTimestamp, Long endTimestamp) throws AugmentationFunctionException {
        if (startTimestamp == null || endTimestamp == null) {
            throw new AugmentationFunctionException("Start time and end time cannot be null.");
        }
        if (endTimestamp < startTimestamp) {
            throw new AugmentationFunctionException("End time cannot be earlier than start time.");
        }
        this.startTimestamp = startTimestamp;
        this.endTimestamp = endTimestamp;
        this.totalExecutionTimeMs = endTimestamp - startTimestamp;
    }

    public Long getTotalExecutionTimeMs() {
        return totalExecutionTimeMs;
    }

    public void setTotalExecutionTimeMs(Long totalExecutionTimeMs) {
        this.totalExecutionTimeMs = totalExecutionTimeMs;
    }

    public Long getStartTimestamp() {
        return startTimestamp;
    }

    public void setStartTimestamp(Long startTimestamp) {
        this.startTimestamp = startTimestamp;
    }

    public Long getEndTimestamp() {
        return endTimestamp;
    }

    public void setEndTimestamp(Long endTimestamp) {
        this.endTimestamp = endTimestamp;
    }

    public Integer getRecordsProcessed() {
        return recordsProcessed;
    }

    public void setRecordsProcessed(Integer recordsProcessed) {
        this.recordsProcessed = recordsProcessed;
    }

    public Integer getRecordsGenerated() {
        return recordsGenerated;
    }

    public void setRecordsGenerated(Integer recordsGenerated) {
        this.recordsGenerated = recordsGenerated;
    }

    public String getExecutionId() {
        return executionId;
    }

    public void setExecutionId(String executionId) {
        this.executionId = executionId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }
}