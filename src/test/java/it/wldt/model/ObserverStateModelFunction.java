package it.wldt.model;

import it.wldt.core.model.StateModelFunction;
import it.wldt.core.state.DigitalTwinStateProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ObserverStateModelFunction extends StateModelFunction {

    private static final Logger logger = LoggerFactory.getLogger(ObserverStateModelFunction.class);

    public ObserverStateModelFunction(String id) {
        super(id);
    }

    @Override
    protected void onAdded() {
        logger.debug("onAdded()");
    }

    @Override
    protected void onRemoved() {
        logger.debug("onRemoved()");
    }

    @Override
    protected void onStateChangePropertyCreated(DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        logger.debug("onStateChangePropertyCreated()-> {}", digitalTwinStateProperty);
    }

    @Override
    protected void onStateChangePropertyUpdated(DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        logger.debug("onStateChangePropertyUpdated()-> {}", digitalTwinStateProperty);
    }

    @Override
    protected void onStateChangePropertyDeleted(DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        logger.debug("onStateChangePropertyDeleted()-> {}", digitalTwinStateProperty);
    }

    @Override
    protected void onStatePropertyUpdated(DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        logger.debug("onStatePropertyUpdated()-> {}", digitalTwinStateProperty);
    }

    @Override
    protected void onStatePropertyDeleted(DigitalTwinStateProperty<?> digitalTwinStateProperty) {
        logger.debug("onStatePropertyDeleted()-> {}", digitalTwinStateProperty);
    }

    @Override
    protected void onPhysicalEvent() {

    }

    @Override
    protected void onDigitalEvent() {

    }
}
