package it.wldt.core.model;

import it.wldt.core.event.WldtEventBus;
import it.wldt.core.event.WldtEventFilter;
import it.wldt.core.event.WldtEventListener;
import it.wldt.core.event.WldtEvent;
import it.wldt.exception.EventBusException;
import it.wldt.core.state.DefaultDigitalTwinState;
import it.wldt.core.state.DigitalTwinStateProperty;
import it.wldt.core.state.IDigitalTwinState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Objects;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 27/03/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public abstract class StateModelFunction {

    private static final Logger logger = LoggerFactory.getLogger(StateModelFunction.class);

    private String id = null;

    private WldtEventFilter stateWldtEventFilter = null;
    private WldtEventFilter statePropertyEventsFilter = null;

    protected IDigitalTwinState digitalTwinState = null;

    private StateModelFunction(){}

    public StateModelFunction(String id){
        this.id = id;
    }

    protected void observeDigitalTwinState() throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DefaultDigitalTwinState.DT_STATE_PROPERTY_CREATED);
        wldtEventFilter.add(DefaultDigitalTwinState.DT_STATE_PROPERTY_UPDATED);
        wldtEventFilter.add(DefaultDigitalTwinState.DT_STATE_PROPERTY_DELETED);

        //Save the adopted EventFilter
        this.stateWldtEventFilter = wldtEventFilter;

        WldtEventBus.getInstance().subscribe(this.id, wldtEventFilter, new WldtEventListener() {
            @Override
            public void onEventSubscribed(String eventType) {
                //TODO Implement
            }

            @Override
            public void onEventUnSubscribed(String eventType) {
                //TODO Implement
            }

            @Override
            public void onEvent(WldtEvent<?> wldtEvent) {

                if(wldtEvent != null && wldtEvent.getBody() != null && (wldtEvent.getBody() instanceof DigitalTwinStateProperty)){
                    DigitalTwinStateProperty<?> digitalTwinStateProperty = (DigitalTwinStateProperty<?>) wldtEvent.getBody();

                    if(wldtEvent.getType().equals(DefaultDigitalTwinState.DT_STATE_PROPERTY_CREATED))
                        onStateChangePropertyCreated(digitalTwinStateProperty);

                    if(wldtEvent.getType().equals(DefaultDigitalTwinState.DT_STATE_PROPERTY_UPDATED))
                        onStateChangePropertyUpdated(digitalTwinStateProperty);

                    if(wldtEvent.getType().equals(DefaultDigitalTwinState.DT_STATE_PROPERTY_DELETED))
                        onStateChangePropertyDeleted(digitalTwinStateProperty);
                }
            }
        });
    }

    protected void observeDigitalTwinProperties(List<String> propertyList) throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        for(String propertyKey : propertyList) {
            wldtEventFilter.add(digitalTwinState.getPropertyUpdatedWldtEventMessageType(propertyKey));
            wldtEventFilter.add(digitalTwinState.getPropertyDeletedWldtEventMessageType(propertyKey));
        }

        //Save the adopted EventFilter
        this.statePropertyEventsFilter = wldtEventFilter;

        WldtEventBus.getInstance().subscribe(this.id, wldtEventFilter, new WldtEventListener() {
            @Override
            public void onEventSubscribed(String eventType) {
                //TODO Implement
            }

            @Override
            public void onEventUnSubscribed(String eventType) {
                //TODO Implement
            }

            @Override
            public void onEvent(WldtEvent<?> wldtEvent) {
                if(wldtEvent != null && wldtEvent.getBody() != null && (wldtEvent.getBody() instanceof DigitalTwinStateProperty)){
                    DigitalTwinStateProperty<?> digitalTwinStateProperty = (DigitalTwinStateProperty<?>) wldtEvent.getBody();
                    if(wldtEvent.getType().equals(digitalTwinState.getPropertyCreatedWldtEventMessageType(digitalTwinStateProperty.getKey())))
                        onStateChangePropertyCreated(digitalTwinStateProperty);
                    else if(wldtEvent.getType().equals(digitalTwinState.getPropertyUpdatedWldtEventMessageType(digitalTwinStateProperty.getKey())))
                        onStatePropertyUpdated(digitalTwinStateProperty);
                    else if(wldtEvent.getType().equals(digitalTwinState.getPropertyDeletedWldtEventMessageType(digitalTwinStateProperty.getKey())))
                        onStatePropertyDeleted(digitalTwinStateProperty);
                    else
                        logger.error(String.format("ModelFunction(%s) -> observeDigitalTwinProperties: Error received type %s that is not matching", id, wldtEvent.getType()));
                }
            }
        });

    }

    protected void init(IDigitalTwinState digitalTwinState){
        this.digitalTwinState = digitalTwinState;
    }

    abstract protected void onAdded();

    abstract protected void onRemoved();

    abstract protected void onStateChangePropertyCreated(DigitalTwinStateProperty<?> digitalTwinStateProperty);

    abstract protected void onStateChangePropertyUpdated(DigitalTwinStateProperty<?> digitalTwinStateProperty);

    abstract protected void onStateChangePropertyDeleted(DigitalTwinStateProperty<?> digitalTwinStateProperty);

    abstract protected void onStatePropertyUpdated(DigitalTwinStateProperty<?> digitalTwinStateProperty);

    abstract protected void onStatePropertyDeleted(DigitalTwinStateProperty<?> digitalTwinStateProperty);

    abstract protected void onPhysicalEvent();

    abstract protected void onDigitalEvent();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StateModelFunction that = (StateModelFunction) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ModelFunction{");
        sb.append("id='").append(id).append('\'');
        sb.append(", statePropertyEventsFilter=").append(statePropertyEventsFilter);
        sb.append('}');
        return sb.toString();
    }
}
