/*
 * Copyright (c) 2025 - Current Year
 * Marco Picone Ph.D
 * Email: picone.m@gmail.com
 * Website: https://www.marcopicone.net/
 * All rights reserved.
 *
 * This program is provided under a Dual Licensing model:
 * 1) GNU General Public License version 3.0 (GPL-3.0) for open-source, academic,
 *    research, non-profit, and other non-commercial use; or
 * 2) Commercial License, for any commercial use, proprietary development, or
 *    closed-source distribution. To obtain a Commercial License, please contact: Marco Picone (picone.m@gmail.com)
 *
 * By using this software, you agree to comply with the terms of the applicable license.
 * This applies to all forms of the software, including source code and compiled/binary forms.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package it.wldt.adapter.physical;

import it.wldt.core.engine.LifeCycleListener;
import it.wldt.core.event.*;
import it.wldt.core.engine.DigitalTwinWorker;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.exception.EventBusException;
import it.wldt.exception.PhysicalAdapterException;
import it.wldt.exception.WldtRuntimeException;
import it.wldt.adapter.physical.event.*;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.monitoring.CoreMonitoringUtils;
import it.wldt.monitoring.MonitoringInterface;
import it.wldt.monitoring.metrics.WldtCounter;
import it.wldt.monitoring.metrics.WldtMetricComponent;
import it.wldt.monitoring.metrics.WldtTimer;

import java.util.Map;
import java.util.Objects;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 *          Marta Spadoni (marta.spadoni2@studio.unibo.it)
 *          Samuele Burattini (samuele.burattini@unibo.it)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 * This class defines the core capabilities and responsibilities of a Physical Adapter in the Digital Twin's Instance
 * A Digital Adapter is the architectural component in charge of handling the interaction of the Digital Twin
 * with the external physical world exposing information and feed the Shadowing Function and in generale the digitalization
 * of the associated physical counterpart.
 * This class can be extended in order to create custom Physical Adapters shaping specific behaviours of the twin and
 * allowing a simplified interaction with the external physical world. Multiple Physical Adapters can be active at the
 * same time on the Digital Twin with the aim to handle different interaction with the physical layer according to the
 * nature of the twin and the associated physical device.
 */
public abstract class PhysicalAdapter extends DigitalTwinWorker implements WldtEventListener, LifeCycleListener {

    /** Logger */
    private static final WldtLogger logger = WldtLoggerProvider.getLogger(ConfigurablePhysicalAdapter.class);

    /** Physical Adapter Identifier */
    private String id;

    /** Event Filter used to handle Physical Action Events */
    private WldtEventFilter physicalActionEventsFilter;

    /** Physical Adapter Listener used to notify binding/unbinding and update events */
    private PhysicalAdapterListener physicalAdapterListener;

    /** Physical Asset Description handled by the Physical Adapter */
    private PhysicalAssetDescription adapterPhysicalAssetDescription;

    /**
     * Reference to the Monitoring Interface in order to add metrics
     * related to computation of the Digital Twin State
     */
    private MonitoringInterface monitoringInterface;

    /**
     * State Manager Metrics Namespace
     */
    private String metricsNamespace;

    /**
     * Default private constructor.
     */
    private PhysicalAdapter(){

    }

    /**
     * Public constructor allowing to set the Physical Adapter Identifier and initialize the Physical Adapter instance.
     * @param id Physical Adapter Identifier
     */
    public PhysicalAdapter(String id) {
        super();
        this.id = id;
    }

    /**
     * TODO ...
     */
    private void handleMetricsRegistration() {

        // Check if the Monitoring Interface is configured and has the handler configured
        if(this.monitoringInterface != null && this.monitoringInterface.isActive(WldtMetricComponent.PHYSICAL_ADAPTER)) {

            // Build metric namespace
            this.metricsNamespace = CoreMonitoringUtils.buildCoreNamespace();

            // Register Counter Metric(s) - Physical Property Event Pub
            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinId,
                    this.metricsNamespace,
                    CoreMonitoringUtils.PHYSICAL_ADAPTER_PROPERTY_EVENT_PUB_SUCCESS_COUNT,
                    WldtMetricComponent.PHYSICAL_ADAPTER));

            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinId,
                    this.metricsNamespace,
                    CoreMonitoringUtils.PHYSICAL_ADAPTER_PROPERTY_EVENT_PUB_ERROR_COUNT,
                    WldtMetricComponent.PHYSICAL_ADAPTER));

            // Register Counter Metric(s) - Physical Property Event Pub
            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinId,
                    this.metricsNamespace,
                    CoreMonitoringUtils.PHYSICAL_ADAPTER_EVENT_NOTIFICATION_EVENT_PUB_SUCCESS_COUNT,
                    WldtMetricComponent.PHYSICAL_ADAPTER));

            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinId,
                    this.metricsNamespace,
                    CoreMonitoringUtils.PHYSICAL_ADAPTER_EVENT_NOTIFICATION_EVENT_PUB_ERROR_COUNT,
                    WldtMetricComponent.PHYSICAL_ADAPTER));

            // Register Counter Metric(s) - Physical Relationship Created Pub
            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinId,
                    this.metricsNamespace,
                    CoreMonitoringUtils.PHYSICAL_ADAPTER_REL_CREATED_EVENT_PUB_SUCCESS_COUNT,
                    WldtMetricComponent.PHYSICAL_ADAPTER));

            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinId,
                    this.metricsNamespace,
                    CoreMonitoringUtils.PHYSICAL_ADAPTER_REL_CREATED_EVENT_PUB_ERROR_COUNT,
                    WldtMetricComponent.PHYSICAL_ADAPTER));

            // Register Counter Metric(s) - Physical Relationship Deleted Pub
            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinId,
                    this.metricsNamespace,
                    CoreMonitoringUtils.PHYSICAL_ADAPTER_REL_DELETED_EVENT_PUB_SUCCESS_COUNT,
                    WldtMetricComponent.PHYSICAL_ADAPTER));

            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinId,
                    this.metricsNamespace,
                    CoreMonitoringUtils.PHYSICAL_ADAPTER_REL_DELETED_EVENT_PUB_ERROR_COUNT,
                    WldtMetricComponent.PHYSICAL_ADAPTER));

            // Register Counter Metric(s) - Action Request
            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinId,
                    this.metricsNamespace,
                    CoreMonitoringUtils.PHYSICAL_ADAPTER_ACTION_COMPUTATION_SUCCESS_COUNT,
                    WldtMetricComponent.PHYSICAL_ADAPTER));

            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinId,
                    this.metricsNamespace,
                    CoreMonitoringUtils.PHYSICAL_ADAPTER_ACTION_COMPUTATION_ERROR_COUNT,
                    WldtMetricComponent.PHYSICAL_ADAPTER));

            // Register Execution Time Metrics - Action Request
            this.monitoringInterface.registerMetric(new WldtTimer(
                    this.digitalTwinId,
                    this.metricsNamespace,
                    CoreMonitoringUtils.PHYSICAL_ADAPTER_ACTION_COMPUTATION_EXEC_TIME,
                    WldtMetricComponent.PHYSICAL_ADAPTER));
        }

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
        try{
            // Before publishing the event, set the physical adapter id
            targetPhysicalPropertyEventMessage.setPhysicalAdapterId(this.id);
            WldtEventBus.getInstance().publishEvent(this.digitalTwinId, getId(), targetPhysicalPropertyEventMessage);

            // Increase Success Counter
            if(this.monitoringInterface != null && this.monitoringInterface.isActive(WldtMetricComponent.PHYSICAL_ADAPTER))
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.PHYSICAL_ADAPTER_PROPERTY_EVENT_PUB_SUCCESS_COUNT);
        } catch (Exception e){

            // Build the error message
            String errorMessage = String.format("onDigitalActionEvent Function Error: %s", e.getLocalizedMessage());
            logger.error(errorMessage);

            // Increate Metrics Count
            if(this.monitoringInterface != null && this.monitoringInterface.isActive(WldtMetricComponent.PHYSICAL_ADAPTER))
               this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.PHYSICAL_ADAPTER_PROPERTY_EVENT_PUB_ERROR_COUNT);

            // Propagate the Exception
            throw new EventBusException(errorMessage);
        }
    }

    protected void publishPhysicalAssetEventWldtEvent(PhysicalAssetEventWldtEvent<?> targetPhysicalAssetEventWldtEvent) throws EventBusException {

        try{
            // Before publishing the event, set the physical adapter id
            targetPhysicalAssetEventWldtEvent.setPhysicalAdapterId(this.id);
            WldtEventBus.getInstance().publishEvent(this.digitalTwinId, getId(), targetPhysicalAssetEventWldtEvent);

            // Increase Success Counter
            if(this.monitoringInterface != null && this.monitoringInterface.isActive(WldtMetricComponent.PHYSICAL_ADAPTER))
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.PHYSICAL_ADAPTER_EVENT_NOTIFICATION_EVENT_PUB_SUCCESS_COUNT);
        } catch (Exception e){

            // Build the error message
            String errorMessage = String.format("onDigitalActionEvent Function Error: %s", e.getLocalizedMessage());
            logger.error(errorMessage);

            // Increate Metrics Count
            if(this.monitoringInterface != null && this.monitoringInterface.isActive(WldtMetricComponent.PHYSICAL_ADAPTER))
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.PHYSICAL_ADAPTER_EVENT_NOTIFICATION_EVENT_PUB_ERROR_COUNT);

            // Propagate the Exception
            throw new EventBusException(errorMessage);
        }
    }

    protected void publishPhysicalAssetRelationshipCreatedWldtEvent(PhysicalAssetRelationshipInstanceCreatedWldtEvent<?> targetPhysicalAssetRelationshipWldtEvent) throws EventBusException {

        try{

            // Before publishing the event, set the physical adapter id
            targetPhysicalAssetRelationshipWldtEvent.setPhysicalAdapterId(this.id);
            WldtEventBus.getInstance().publishEvent(this.digitalTwinId, getId(), targetPhysicalAssetRelationshipWldtEvent);

            // Increase Success Counter
            if(this.monitoringInterface != null && this.monitoringInterface.isActive(WldtMetricComponent.PHYSICAL_ADAPTER))
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.PHYSICAL_ADAPTER_REL_CREATED_EVENT_PUB_SUCCESS_COUNT);
        } catch (Exception e){

            // Build the error message
            String errorMessage = String.format("onDigitalActionEvent Function Error: %s", e.getLocalizedMessage());
            logger.error(errorMessage);

            // Increate Metrics Count
            if(this.monitoringInterface != null && this.monitoringInterface.isActive(WldtMetricComponent.PHYSICAL_ADAPTER))
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.PHYSICAL_ADAPTER_REL_CREATED_EVENT_PUB_ERROR_COUNT);

            // Propagate the Exception
            throw new EventBusException(errorMessage);
        }
    }

    protected void publishPhysicalAssetRelationshipDeletedWldtEvent(PhysicalAssetRelationshipInstanceDeletedWldtEvent<?> targetPhysicalAssetRelationshipWldtEvent) throws EventBusException {

        try{

            // Before publishing the event, set the physical adapter id
            targetPhysicalAssetRelationshipWldtEvent.setPhysicalAdapterId(this.id);
            WldtEventBus.getInstance().publishEvent(this.digitalTwinId, getId(), targetPhysicalAssetRelationshipWldtEvent);

            // Increase Success Counter
            if(this.monitoringInterface != null && this.monitoringInterface.isActive(WldtMetricComponent.PHYSICAL_ADAPTER))
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.PHYSICAL_ADAPTER_REL_DELETED_EVENT_PUB_SUCCESS_COUNT);
        } catch (Exception e){

            // Build the error message
            String errorMessage = String.format("onDigitalActionEvent Function Error: %s", e.getLocalizedMessage());
            logger.error(errorMessage);

            // Increate Metrics Count
            if(this.monitoringInterface != null && this.monitoringInterface.isActive(WldtMetricComponent.PHYSICAL_ADAPTER))
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.PHYSICAL_ADAPTER_REL_DELETED_EVENT_PUB_ERROR_COUNT);

            // Propagate the Exception
            throw new EventBusException(errorMessage);
        }
    }

    public PhysicalAssetDescription getPhysicalAssetDescription() {
        return adapterPhysicalAssetDescription;
    }

    /**
     * This method allows an implementation of a PhysicalAdapter to update the representation
     * of the PhysicalAssetState and to notify the DigitalTwin that the PhysicalAssetDescription
     * is changed and should be potentially handled by other modules and core components.
     *
     * @param physicalAssetDescription Physical Asset Description to be updated
     * @throws PhysicalAdapterException Exception thrown if there is an error in the Physical Adapter
     * @throws EventBusException Exception thrown if there is an error in the Event Bus
     */
    protected void notifyPhysicalAssetBindingUpdate(PhysicalAssetDescription physicalAssetDescription) throws PhysicalAdapterException, EventBusException {

        if(physicalAssetDescription == null)
            throw new PhysicalAdapterException("Error updating AdapterPhysicalAssetState ! Provided State = Null.");

        updatePhysicalAssetDescription(physicalAssetDescription);

        //Notify Listeners
        if(getPhysicalAdapterListener() != null)
            getPhysicalAdapterListener().onPhysicalBindingUpdate(getId(), this.adapterPhysicalAssetDescription);

        // Notify with an Event that a new PAD for the target Physical Adapter has been updated
        EventManager.notifyPhysicalAdapterPadUpdated(this.digitalTwinId,
                id,
                id,
                this.adapterPhysicalAssetDescription);
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

        // Notify with an Event that a new PAD for the target Physical Adapter has been updated
        EventManager.notifyPhysicalAdapterPadUpdated(this.digitalTwinId,
                id,
                id,
                this.adapterPhysicalAssetDescription);
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

        // Notify with an Event that a new PAD for the target Physical Adapter is Available
        EventManager.notifyPhysicalAdapterPadAvailable(this.digitalTwinId,
                id,
                id,
                this.adapterPhysicalAssetDescription);
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

    /**
     * TODO ...
     * @param wldtEvent
     */
    private void handleIncomingPhysicalAction(PhysicalAssetActionWldtEvent<?> wldtEvent){

        // Check if the Monitoring Interface is configured and has the handler configured
        if(this.monitoringInterface == null || !this.monitoringInterface.isActive(WldtMetricComponent.PHYSICAL_ADAPTER)){
            try {
                onIncomingPhysicalAction(wldtEvent);
            }
            catch (Exception e){
                String errorMessage = String.format("onPhysicalAssetPropertyVariation Function Error Observing Physical Asset Property Variation Event: %s", e.getLocalizedMessage());
                logger.error(errorMessage);
            }
        }
        else {

            long startMs = System.currentTimeMillis();
            try {

                // Call the actual function to handle the event (implemented by the developer)
                onIncomingPhysicalAction(wldtEvent);

                // Increase Success Counter
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.PHYSICAL_ADAPTER_ACTION_COMPUTATION_SUCCESS_COUNT);
            }
            catch (Exception e){
                String errorMessage = String.format("onDigitalActionEvent Function Error: %s", e.getLocalizedMessage());
                logger.error(errorMessage);
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.PHYSICAL_ADAPTER_ACTION_COMPUTATION_ERROR_COUNT);
            }
            finally {
                // Update new Timer Value for the execution of the Physical Property Variation
                this.monitoringInterface.updateTimerSince(
                        this.metricsNamespace,
                        CoreMonitoringUtils.PHYSICAL_ADAPTER_ACTION_COMPUTATION_EXEC_TIME,
                        startMs);
            }
        }
    }

    @Override
    public void onEvent(WldtEvent<?> wldtEvent) {
        logger.debug("{} -> Received Event: {}", id, wldtEvent);
        if (wldtEvent != null && wldtEvent instanceof PhysicalAssetActionWldtEvent) {
            //onIncomingPhysicalAction((PhysicalAssetActionWldtEvent<?>) wldtEvent);
            handleIncomingPhysicalAction((PhysicalAssetActionWldtEvent<?>) wldtEvent);
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

    //////////////////////// DT Life Cycle Callback methods ////////////////////////////////////////////////////////

    /**
     * Callback method invoked when the Digital Twin is synchronized.
     * In this case since it is a Physical Adapter the information about the Digital Twin State is not relevant, and
     * it is removed from the method signature and the original received callback from
     * the LifeCycleListener interface.
     * By default, this method does nothing. It can be optionally overridden by subclasses.
     */
    public void onDigitalTwinSync(){
        // Method intentionally left blank, it can be overridden by subclasses when needed to handle the callback
        // associated to the Digital Twin synchronization event of the LifeCycleListener interface.
    }

    /**
     * Callback method invoked when the Digital Twin is out of synchronization.
     * In this case since it is a Physical Adapter the information about the Digital Twin State is not relevant, and
     * it is removed from the method signature and the original received callback from
     * the LifeCycleListener interface.
     * By default, this method does nothing. It can be optionally overridden by subclasses.
     */
    public void onDigitalTwinUnSync(){
        // Method intentionally left blank, it can be overridden by subclasses when needed to handle the callback
        // associated to the Digital Twin out of synchronization event of the LifeCycleListener interface.
    }

    @Override
    public void onCreate() {
        // Nothing to do in a Physical Adapter
    }

    @Override
    public void onStart() {
        // Nothing to do in a Physical Adapter
    }

    @Override
    public void onPhysicalAdapterBound(String adapterId, PhysicalAssetDescription physicalAssetDescription) {
        // Nothing to do in a Physical Adapter
    }

    @Override
    public void onPhysicalAdapterBindingUpdate(String adapterId, PhysicalAssetDescription physicalAssetDescription) {
        // Nothing to do in a Physical Adapter
    }

    @Override
    public void onPhysicalAdapterUnBound(String adapterId, PhysicalAssetDescription physicalAssetDescription, String errorMessage) {
        // Nothing to do in a Physical Adapter
    }

    @Override
    public void onDigitalAdapterBound(String adapterId) {
        // Nothing to do in a Physical Adapter
    }

    @Override
    public void onDigitalAdapterUnBound(String adapterId, String errorMessage) {
        // Nothing to do in a Physical Adapter
    }

    @Override
    public void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap) {
        // Nothing to do in a Physical Adapter
    }

    @Override
    public void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap, String errorMessage) {
        // Nothing to do in a Physical Adapter
    }

    @Override
    public void onSync(DigitalTwinState digitalTwinState) {
        // Invoke the specialized method for Physical Adapters
        onDigitalTwinSync();
    }

    @Override
    public void onUnSync(DigitalTwinState digitalTwinState) {
        // Invoke the specialized method for Physical Adapters
        onDigitalTwinUnSync();
    }

    @Override
    public void onStop() {
        // Nothing to do in a Physical Adapter
    }

    @Override
    public void onDestroy() {
        // Nothing to do in a Physical Adapter
    }

    public MonitoringInterface getMonitoringInterface() {
        return monitoringInterface;
    }

    public void setMonitoringInterface(MonitoringInterface monitoringInterface) {
        // Check if the monitoring interface is not null
        if(monitoringInterface != null) {

            // Set the Monitoring Interface Reference to the DT Model
            this.monitoringInterface = monitoringInterface;

            // Handle Metrics Registration
            this.handleMetricsRegistration();
        }
        else
            logger.error("Cannot set Monitoring Interface to null for Digital Twin State Manager (DT Id: {}) !", this.digitalTwinId);
    }
}
