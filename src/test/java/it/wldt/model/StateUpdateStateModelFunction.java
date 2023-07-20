package it.wldt.model;

import it.wldt.core.model.StateModelFunction;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.exception.WldtDigitalTwinStateException;
import it.wldt.exception.WldtDigitalTwinStatePropertyBadRequestException;
import it.wldt.exception.WldtDigitalTwinStatePropertyConflictException;
import it.wldt.exception.WldtDigitalTwinStatePropertyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StateUpdateStateModelFunction extends StateModelFunction {

    private static final Logger logger = LoggerFactory.getLogger(StateUpdateStateModelFunction.class);

    public static final String TEST_PROPERTY_KEY_0001 = "testKey0001";
    public static final String TEST_PROPERTY_VALUE_0001 = "TEST-STRING";
    public static final String TEST_PROPERTY_VALUE_0001_UPDATED = "TEST-STRING-UPDATED";

    public StateUpdateStateModelFunction(String id) {
        super(id);
    }

    @Override
    protected void onAdded() {
        try {
            logger.debug("onAdded()");
            createTestStateProperty();
        }catch (Exception e){
            logger.error("Error Creating new Property ! Msg: {}", e.getLocalizedMessage());
        }
    }

    @Override
    protected void onRemoved() {
        logger.debug("onRemoved()");
    }

    private void createTestStateProperty() throws WldtDigitalTwinStateException, WldtDigitalTwinStatePropertyException, WldtDigitalTwinStatePropertyBadRequestException, WldtDigitalTwinStatePropertyConflictException {

        if(this.digitalTwinState != null){
            DigitalTwinStateProperty<Object> newStateProperty = new DigitalTwinStateProperty<>(TEST_PROPERTY_KEY_0001, TEST_PROPERTY_VALUE_0001);
            newStateProperty.setReadable(true);
            newStateProperty.setWritable(true);
            digitalTwinState.createProperty(newStateProperty);
        }
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
