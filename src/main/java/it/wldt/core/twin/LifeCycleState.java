package it.wldt.core.twin;

public enum LifeCycleState {

        NONE("dt_none"),
        CREATED("dt_created"),
        STARTED("dt_started"),
        BOUND("dt_bound"),
        UN_BOUND("dt_un_bound"),
        SYNCHRONIZED("dt_synchronized"),
        NOT_SYNCHRONIZED("dt_not_synchronized"),
        STOPPED("dt_stopped"),
        DESTROYED("dt_destroyed");

        private String value;

        private LifeCycleState(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }