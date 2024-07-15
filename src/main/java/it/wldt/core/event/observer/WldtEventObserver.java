package it.wldt.core.event.observer;

import it.wldt.core.event.*;
import it.wldt.exception.EventBusException;
import it.wldt.exception.WldtRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Arrays;

/**
 *
 */
public class WldtEventObserver implements WldtEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WldtEventObserver.class);

    private WldtEventFilter dtStateEventFilter = null;

    private WldtEventFilter physicalAssetEventFilter = null;

    private WldtEventFilter physicalAssetActionEventFilter = null;

    private WldtEventFilter digitalActionEventFilter = null;

    private String observerId;

    private String digitalTwinId;

    private IWldtEventObserverListener observerListener;

    private WldtEventObserver() {
    }

    public WldtEventObserver(String digitalTwinId, String observerId, IWldtEventObserverListener observerListener) throws WldtRuntimeException {

        this.digitalTwinId = digitalTwinId;
        this.observerId = observerId;

        if(digitalTwinId == null)
            throw new WldtRuntimeException("Observer digitalTwinId = Null in WldtEventObserver !");

        if(observerId == null)
            throw new WldtRuntimeException("Observer Id = Null in WldtEventObserver !");

        if(observerListener == null)
            throw new WldtRuntimeException("Observer Listener = Null in WldtEventObserver !");

        // Init Wldt Event Filters:
        this.dtStateEventFilter = new WldtEventFilter();
        this.physicalAssetEventFilter = new WldtEventFilter();
        this.physicalAssetActionEventFilter = new WldtEventFilter();
        this.digitalActionEventFilter = new WldtEventFilter();

        this.observerListener = observerListener;
    }

    private void observeEventsWithFilter(WldtEventFilter targetFilter, String... eventTypeList){
        try{
            if(eventTypeList == null || eventTypeList.length == 0)
                logger.error("Error Observing Events ! Error: Empty/Null Filter or List ...");
            else {

                // If the filter has been already used cancel observation and clean the filter
                if(targetFilter != null)
                    unObserveEventsWithFilter(targetFilter);

                // Add target Event Types to the Filter
                if(targetFilter.addAll(Arrays.asList(eventTypeList)))
                    WldtEventBus.getInstance().subscribe(this.digitalTwinId, this.observerId, targetFilter, this);
                else
                    logger.error("Error Observing Events ! Impossible to add event to the Filter ...");
            }

        }catch (Exception e){
            logger.error("Error Observing Events ! Error: {}, EventTypeList: {}", e.getLocalizedMessage(), eventTypeList);
        }
    }

    public void unObserveEventsWithFilter(WldtEventFilter targetFilter) throws EventBusException {
        try{
            if (targetFilter != null && !targetFilter.isEmpty()) {
                WldtEventBus.getInstance().unSubscribe(this.digitalTwinId, this.observerId, targetFilter, this);
                targetFilter.clear();
                targetFilter = new WldtEventFilter();
            }
            else
                logger.warn("Warning Canceling Observation Observing Events ! Empty/Null Filter or List ...");
        }catch (Exception e){
            logger.error("Error Canceling Observation for Events ! Error: {}, Filter: {}", e.getLocalizedMessage(), targetFilter);
        }
    }

    /**
     * Trigger the observation of events generated from the Digital Twin Model related to State Variations and Event
     * Notifications
     * @throws EventBusException
     */
    public void observeStateEvents() throws EventBusException {
        observeEventsWithFilter(this.dtStateEventFilter,
                WldtEventTypes.DT_STATE_UPDATE_MESSAGE_EVENT_TYPE,
                WldtEventTypes.ALL_DT_STATE_EVENT_NOTIFICATION_EVENT_TYPE);
    }

    /**
     * Cancel the observation of events generated from the Digital Twin Model related to State Variations and Event
     * Notifications
     * @throws EventBusException
     */
    public void unObserveStateEvents() throws EventBusException {
        unObserveEventsWithFilter(this.dtStateEventFilter);
    }


    /**
     * Trigger the observation of Physical Assets events generated from the active Physical Adapters
     * @throws EventBusException
     */
    public void observePhysicalAssetEvents() throws EventBusException {
        observeEventsWithFilter(this.physicalAssetEventFilter,
                WldtEventTypes.ALL_PHYSICAL_PROPERTY_VARIATION_EVENT_TYPE,
                WldtEventTypes.ALL_PHYSICAL_EVENT_NOTIFICATION_EVENT_TYPE,
                WldtEventTypes.ALL_PHYSICAL_RELATIONSHIP_INSTANCE_CREATION_EVENT_TYPE,
                WldtEventTypes.ALL_PHYSICAL_RELATIONSHIP_INSTANCE_DELETED_EVENT_TYPE);
    }

    /**
     * Cancel the observation of Physical Assets events generated from the active Physical Adapters
     * Notifications
     * @throws EventBusException
     */
    public void unObservePhysicalAssetEvents() throws EventBusException {
        unObserveEventsWithFilter(this.physicalAssetEventFilter);
    }

    /**
     * Trigger the observation of Physical Asset Action events generated from the Model for the Physical Adapters
     * @throws EventBusException
     */
    public void observePhysicalAssetActionEvents() throws EventBusException {
        observeEventsWithFilter(this.physicalAssetActionEventFilter,
                WldtEventTypes.ALL_PHYSICAL_ACTION_TRIGGER_EVENT_TYPE);
    }

    /**
     * Cancel the observation of Physical Asset Action events generated from the Model for the Physical Adapters
     * Notifications
     * @throws EventBusException
     */
    public void unObservePhysicalAssetActionEvents() throws EventBusException {
        unObserveEventsWithFilter(this.physicalAssetActionEventFilter);
    }

    /**
     * Trigger the observation of Digital Action events generated from Digital Adapter for the Model and the Shadowing
     * Function
     * @throws EventBusException
     */
    public void observeDigitalActionEvents() throws EventBusException {
        observeEventsWithFilter(this.digitalActionEventFilter,
                WldtEventTypes.ALL_DIGITAL_ACTION_EVENT_TYPE);
    }

    /**
     * Cancel the observation of Digital Action events generated from Digital Adapter for the Model and the Shadowing
     * Function
     * @throws EventBusException
     */
    public void unObserveDigitalActionEvents() throws EventBusException {
        unObserveEventsWithFilter(this.digitalActionEventFilter);
    }

    @Override
    public void onEventSubscribed(String eventType) {
        if(this.observerListener != null)
            this.observerListener.onEventSubscribed(eventType);
        else
            logger.error("WldtEventObserver({}) onEventSubscribed - Null Listener Error !", this.observerId);
    }

    @Override
    public void onEventUnSubscribed(String eventType) {
        if(this.observerListener != null)
            this.observerListener.onEventUnSubscribed(eventType);
        else
            logger.error("WldtEventObserver({}) onEventUnSubscribed - Null Listener Error !", this.observerId);
    }

    @Override
    public void onEvent(WldtEvent<?> wldtEvent) {

        if(this.observerListener == null)
            logger.error("WldtEventObserver({}) onEvent - Null Listener Error !", this.observerId);

        if(wldtEvent != null && wldtEvent.getType() != null){

            // Check State Events
            if(this.dtStateEventFilter != null && this.dtStateEventFilter.matchEventType(wldtEvent.getType()))
                this.observerListener.onStateEvent(wldtEvent);

            // Check Physical Asset Events
            if(this.physicalAssetEventFilter != null && this.physicalAssetEventFilter.matchEventType(wldtEvent.getType()))
                this.observerListener.onPhysicalAssetEvent(wldtEvent);

            // Check Physical Asset Action Events
            if(this.physicalAssetActionEventFilter != null && this.physicalAssetActionEventFilter.matchEventType(wldtEvent.getType()))
                this.observerListener.onPhysicalAssetActionEvent(wldtEvent);

            // Check Digital Action Events
            if(this.digitalActionEventFilter != null && this.digitalActionEventFilter.matchEventType(wldtEvent.getType()))
                this.observerListener.onDigitalActionEvent(wldtEvent);
        }
        else
            logger.error("WldtEventObserver({}) onEvent - Wrong or Null WldtEvent: {}", this.observerId, wldtEvent);
    }

    public WldtEventFilter getDtStateEventFilter() {
        return dtStateEventFilter;
    }

    public WldtEventFilter getPhysicalAssetEventFilter() {
        return physicalAssetEventFilter;
    }

    public WldtEventFilter getPhysicalAssetActionEventFilter() {
        return physicalAssetActionEventFilter;
    }

    public WldtEventFilter getDigitalActionEventFilter() {
        return digitalActionEventFilter;
    }

    public String getObserverId() {
        return observerId;
    }

    public String getDigitalTwinId() {
        return digitalTwinId;
    }

    public IWldtEventObserverListener getObserverListener() {
        return observerListener;
    }

    public void setDtStateEventFilter(WldtEventFilter dtStateEventFilter) {
        this.dtStateEventFilter = dtStateEventFilter;
    }

    public void setPhysicalAssetEventFilter(WldtEventFilter physicalAssetEventFilter) {
        this.physicalAssetEventFilter = physicalAssetEventFilter;
    }

    public void setPhysicalAssetActionEventFilter(WldtEventFilter physicalAssetActionEventFilter) {
        this.physicalAssetActionEventFilter = physicalAssetActionEventFilter;
    }

    public void setDigitalActionEventFilter(WldtEventFilter digitalActionEventFilter) {
        this.digitalActionEventFilter = digitalActionEventFilter;
    }

    public void setObserverId(String observerId) {
        this.observerId = observerId;
    }

    public void setDigitalTwinId(String digitalTwinId) {
        this.digitalTwinId = digitalTwinId;
    }

    public void setObserverListener(IWldtEventObserverListener observerListener) {
        this.observerListener = observerListener;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WldtEventObserver{");
        sb.append("dtStateEventFilter=").append(dtStateEventFilter);
        sb.append(", physicalAssetEventFilter=").append(physicalAssetEventFilter);
        sb.append(", physicalAssetActionEventFilter=").append(physicalAssetActionEventFilter);
        sb.append(", digitalActionEventFilter=").append(digitalActionEventFilter);
        sb.append(", observerId='").append(observerId).append('\'');
        sb.append(", digitalTwinId='").append(digitalTwinId).append('\'');
        sb.append(", observerListener=").append(observerListener);
        sb.append('}');
        return sb.toString();
    }
}