package it.wldt.core.engine;

public class LifeCycleStateVariation {

    private long timestamp;

    private LifeCycleState lifeCycleState = LifeCycleState.NONE;

    public LifeCycleStateVariation() {
    }

    public LifeCycleStateVariation(long timestamp, LifeCycleState lifeCycleState) {
        this.timestamp = timestamp;
        this.lifeCycleState = lifeCycleState;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public LifeCycleState getLifeCycleState() {
        return lifeCycleState;
    }

    public void setLifeCycleState(LifeCycleState lifeCycleState) {
        this.lifeCycleState = lifeCycleState;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("LifeCycleStateVariation{");
        sb.append("timestamp=").append(timestamp);
        sb.append(", lifeCycleState=").append(lifeCycleState);
        sb.append('}');
        return sb.toString();
    }
}
