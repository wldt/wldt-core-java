package it.wldt.adapter.physical;

import it.wldt.core.event.WldtEvent;
import it.wldt.core.event.WldtEventBus;
import it.wldt.core.event.WldtEventFilter;
import it.wldt.core.event.WldtEventListener;
import it.wldt.core.engine.DigitalTwinWorker;
import it.wldt.exception.EventBusException;
import it.wldt.exception.PhysicalAdapterException;
import it.wldt.exception.WldtRuntimeException;
import it.wldt.adapter.physical.event.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 *          Marta Spadoni (marta.spadoni2@studio.unibo.it)
 *          Samuele Burattini (samuele.burattini@unibo.it)
 *
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * This class defines the core capabilities and responsibilities of a Physical Adapter in the Digital Twin's Instance
 * A Digital Adapter is the architectural component in charge of handling the interaction of the Digital Twin
 * with the external physical world exposing information and feed the Shadowing Function and in generale the digitalization
 * of the associated physical counterpart.
 *
 * This class can be extended in order to create custom Physical Adapters shaping specific behaviours of the twin and
 * allowing a simplified interaction with the external physical world. Multiple Physical Adapters can be active at the
 * same time on the Digital Twin with the aim to handle different interaction with the physical layer according to the
 * nature of the twin and the associated physical device.
 */
public abstract class PhysicalAdapter extends DigitalTwinWorker implements WldtEventListener {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurablePhysicalAdapter.class);

    private String id;

    private WldtEventFilter physicalActionEventsFilter;

    private PhysicalAdapterListener physicalAdapterListener;

    private PhysicalAssetDescription adapterPhysicalAssetDescription;

    private PhysicalAdapter(){

    }

    public PhysicalAdapter(String id) {
        super();
        this.id = id;
    }

    @Override
    public void onWorkerStop() throws WldtRuntimeException {
        try{

            onAdapterStop();

            if(getPhysicalAdapterListener() != null)
                getPhysicalAdapterListener().onPhysicalAdapterUnBound(this.id, this.adapterPhysicalAssetDescription, null);

        }catch (Exception e){

            //Notify Listeners
            if(getPhysicalAdapterListener() != null)
                getPhysicalAdapterListener().onPhysicalAdapterUnBound(this.id, this.adapterPhysicalAssetDescription, e.getLocalizedMessage());

            throw new WldtRuntimeException(e.getLocalizedMessage());
        }
    }

    @Override
    public void onWorkerStart() throws WldtRuntimeException {
        try{
            onAdapterStart();
        }catch (Exception e){

            //Notify Listeners
            if(getPhysicalAdapterListener() != null)
                getPhysicalAdapterListener().onPhysicalAdapterUnBound(this.id, this.adapterPhysicalAssetDescription, e.getLocalizedMessage());

            throw new WldtRuntimeException(e.getLocalizedMessage());
        }
    }

    public String getId() {
        return id;
    }

    public PhysicalAdapterListener getPhysicalAdapterListener() {
        return physicalAdapterListener;
    }

    public void setPhysicalAdapterListener(PhysicalAdapterListener physicalAdapterListener) {
        this.physicalAdapterListener = physicalAdapterListener;
    }

    public abstract void onIncomingPhysicalAction(PhysicalAssetActionWldtEvent<?> physicalActionEvent);

    public abstract void onAdapterStart();

    public abstract void onAdapterStop();

    protected void publishPhysicalAssetPropertyWldtEvent(PhysicalAssetPropertyWldtEvent<?> targetPhysicalPropertyEventMessage) throws EventBusException {
        WldtEventBus.getInstance().publishEvent(this.digitalTwinId, getId(), targetPhysicalPropertyEventMessage);
    }

    protected void publishPhysicalAssetEventWldtEvent(PhysicalAssetEventWldtEvent<?> targetPhysicalAssetEventWldtEvent) throws EventBusException {
        WldtEventBus.getInstance().publishEvent(this.digitalTwinId, getId(), targetPhysicalAssetEventWldtEvent);
    }

    protected void publishPhysicalAssetRelationshipCreatedWldtEvent(PhysicalAssetRelationshipInstanceCreatedWldtEvent<?> targetPhysicalAssetRelationshipWldtEvent) throws EventBusException {
        WldtEventBus.getInstance().publishEvent(this.digitalTwinId, getId(), targetPhysicalAssetRelationshipWldtEvent);
    }

    protected void publishPhysicalAssetRelationshipDeletedWldtEvent(PhysicalAssetRelationshipInstanceDeletedWldtEvent<?> targetPhysicalAssetRelationshipWldtEvent) throws EventBusException {
        WldtEventBus.getInstance().publishEvent(this.digitalTwinId, getId(), targetPhysicalAssetRelationshipWldtEvent);
    }

    public PhysicalAssetDescription getPhysicalAssetDescription() {
        return adapterPhysicalAssetDescription;
    }

    /**
     * This method allows an implementation of a PhysicalAdapter to update the representation
     * of the PhysicalAssetState and to notify the DigitalTwin that the PhysicalAssetDescription is changed and should be potentially handled by other modules and core components.
     *
     * @param physicalAssetDescription
     * @throws PhysicalAdapterException
     * @throws EventBusException
     */
    protected void notifyPhysicalAssetBindingUpdate(PhysicalAssetDescription physicalAssetDescription) throws PhysicalAdapterException, EventBusException {

        if(physicalAssetDescription == null)
            throw new PhysicalAdapterException("Error updating AdapterPhysicalAssetState ! Provided State = Null.");

        updatePhysicalAssetDescription(physicalAssetDescription);

        //Notify Listeners
        if(getPhysicalAdapterListener() != null)
            getPhysicalAdapterListener().onPhysicalBindingUpdate(getId(), this.adapterPhysicalAssetDescription);
    }

    /**
     * This method allows an implementation of a PhysicalAdapter to notify the DigitalTwin that the representation
     * of the PhysicalAssetState is changed and should be potentially handled by other modules and core components.
     *
     * This method is meant to be used after a previous calling of the method {@link #updatePhysicalAssetDescription(PhysicalAssetDescription) updatePhysicalAssetDescription}
     *
     *
     * @throws PhysicalAdapterException
     * @throws EventBusException
     */
    protected void notifyPhysicalAssetBindingUpdate() throws PhysicalAdapterException, EventBusException {
        if(this.adapterPhysicalAssetDescription == null)
            throw new PhysicalAdapterException("Error notifying update of PhysicalAssetDescription ! Updated Description = Null.");

        if(getPhysicalAdapterListener() != null)
            getPhysicalAdapterListener().onPhysicalBindingUpdate(getId(), this.adapterPhysicalAssetDescription);
    }

    /**
     * This method allows an implementation of a Physical Adapter to notify active listeners
     * when there is an issue in the binding with the Physical Asset. If the binding is restored
     * the adapter can use notifyPhysicalAssetBindingUpdate to notify the binding update.
     *
     * @param errorMessage
     */
    protected void notifyPhysicalAdapterUnBound(String errorMessage){
        //Notify Listeners
        if(getPhysicalAdapterListener() != null)
            getPhysicalAdapterListener().onPhysicalAdapterUnBound(getId(), this.adapterPhysicalAssetDescription, errorMessage);
    }

    protected void notifyPhysicalAdapterBound(PhysicalAssetDescription physicalAssetDescription) throws PhysicalAdapterException, EventBusException {

        if(physicalAssetDescription == null)
            throw new PhysicalAdapterException("Error updating AdapterPhysicalAssetState ! Provided Description = Null.");

        updatePhysicalAssetDescription(physicalAssetDescription);

        //Notify Listeners
        if(getPhysicalAdapterListener() != null)
            getPhysicalAdapterListener().onPhysicalAdapterBound(getId(), this.adapterPhysicalAssetDescription);
    }

    /**
     * Handle and update of the Physical Asset State.
     * This method is automatically internally called by the PhysicalAdapter basic class after the adapter startup and
     * can be manually called by an adapter implementation to update the representation of the PhysicalAssetState without notifying the DigitalTwin
     *
     * This method can also be called by an adapter implementation through {@link #notifyPhysicalAssetBindingUpdate(PhysicalAssetDescription) notifyPhysicalAssetBindingUpdate} method
     * if it detects a variation in the state of the Physical Asset and wants to notify the DigitalTwin.
     *
     * This method manages the proper subscription to receive action events from other DT's modules according to the
     * exposed actions in the PhysicalAssetState.
     *
     * @param physicalAssetDescription
     * @throws PhysicalAdapterException
     * @throws EventBusException
     */
    protected void updatePhysicalAssetDescription(PhysicalAssetDescription physicalAssetDescription) throws PhysicalAdapterException, EventBusException {

        if(physicalAssetDescription == null)
            throw new PhysicalAdapterException("Error updating AdapterPhysicalAssetState ! Provided State = Null.");

        this.adapterPhysicalAssetDescription = physicalAssetDescription;

        if(physicalAssetDescription.getActions() != null && physicalAssetDescription.getActions().size() > 0) {

            //Handle PhysicalActionEvent EventFilter
            if(this.physicalActionEventsFilter == null)
                this.physicalActionEventsFilter = new WldtEventFilter();
            else {
                //Clean existing subscriptions and the local event filter
                WldtEventBus.getInstance().unSubscribe(this.digitalTwinId, this.id, this.physicalActionEventsFilter, this);
                this.physicalActionEventsFilter.clear();
            }

            //Create/Update the event filter and handle subscription
            for(PhysicalAssetAction physicalAssetAction : physicalAssetDescription.getActions())
                this.physicalActionEventsFilter.add(PhysicalAssetActionWldtEvent
                        .buildEventType(PhysicalAssetActionWldtEvent.EVENT_BASIC_TYPE, physicalAssetAction.getKey()));

            WldtEventBus.getInstance().subscribe(this.digitalTwinId, this.id, this.physicalActionEventsFilter, this);

        }
        else
            logger.info("No Supported Action Exposed. Subscription to PhysicalActionEvents not required !");
    }

    @Override
    public void onEventSubscribed(String eventType) {
        logger.debug("{} -> Subscribed to: {}", id, eventType);
    }

    @Override
    public void onEventUnSubscribed(String eventType) {
        logger.debug("{} -> Unsubscribed from: {}", id, eventType);
    }

    @Override
    public void onEvent(WldtEvent<?> wldtEvent) {
        logger.debug("{} -> Received Event: {}", id, wldtEvent);
        if (wldtEvent != null && wldtEvent instanceof PhysicalAssetActionWldtEvent) {
            onIncomingPhysicalAction((PhysicalAssetActionWldtEvent<?>) wldtEvent);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhysicalAdapter that = (PhysicalAdapter) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
