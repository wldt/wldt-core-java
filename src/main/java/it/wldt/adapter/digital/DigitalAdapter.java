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
package it.wldt.adapter.digital;

import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.core.engine.LifeCycleListener;
import it.wldt.core.event.WldtEvent;
import it.wldt.core.event.WldtEventBus;
import it.wldt.core.event.WldtEventFilter;
import it.wldt.core.event.WldtEventListener;
import it.wldt.core.state.*;
import it.wldt.core.engine.DigitalTwinWorker;
import it.wldt.exception.EventBusException;
import it.wldt.exception.WldtDigitalTwinStateEventException;
import it.wldt.exception.WldtRuntimeException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.monitoring.CoreMonitoringUtils;
import it.wldt.monitoring.MonitoringInterface;
import it.wldt.monitoring.metrics.WldtCounter;
import it.wldt.monitoring.metrics.WldtMetricComponent;
import it.wldt.monitoring.metrics.WldtTimer;
import it.wldt.storage.query.QueryExecutor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 *          Marta Spadoni (marta.spadoni2@studio.unibo.it)
 * Date: 05/08/2024
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 * This class defines the core capabilities and responsibilities of a Digital Adapter in the Digital Twin's Instance
 * A Digital Adapter is the architectural component in charge of handling the interaction of the Digital Twin
 * with the external digital world exposing information coming from the Digital Twin State and receiving information and
 * action from other digital applications or twins.
 * This class can be extended in order to create custom Digital Adapter shaping specific behaviours of the twin and
 * allowing a simplified interaction with the external digital world.
 * Multiple Digital Adapters can be active at the same time on the Digital Twin with the aim to handle different
 * interaction with the digital layer according to the nature of the twin and its operation context.
 * The class integrates a Query Executor to send query to the storage layer in both synchronous and asynchronous way.
 */
public abstract class DigitalAdapter<C> extends DigitalTwinWorker implements WldtEventListener, LifeCycleListener {

    //public static final String DIGITAL_ACTION_EVENT = "da.digital.action.event";

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(DigitalAdapter.class);

    public static final String METRIC_METADATA_DIGITAL_ADAPTER_ID_KEY = "da_id";

    private String id = null;

    private C configuration;

    private WldtEventFilter statePropertiesWldtEventFilter = null;

    private WldtEventFilter stateActionsWldtEventFilter = null;

    private WldtEventFilter stateEventsAvailabilityWldtEventFilter = null;

    private WldtEventFilter stateTargetPropertyWldtEventsFilter = null;

    private WldtEventFilter stateTargetEventNotificationWldtEventsFilter = null;

    private WldtEventFilter stateRelationshipsWldtEventFilter;

    private final Map<String, WldtEventFilter> stateTargetRelationshipWldtEventFilter = new HashMap<>();

    // Local, always complete and unfiltered copy of the Digital Twin State (description and values) kept up to date
    // with the events received on the two state topics of the event bus
    private final DigitalTwinStateReplica stateReplica = new DigitalTwinStateReplica();

    // Content filter applied to the Digital Twin State Values delivered to onStateValuesUpdate(...)
    // (the description is never filtered)
    private volatile DigitalTwinStateFilter stateFilter = DigitalTwinStateFilter.all();

    // Trigger filter: if defined, onStateValuesUpdate(...) is invoked only for the changes that match it
    private volatile DigitalTwinStateChangeFilter stateChangeFilter = null;

    // True if the adapter is currently subscribed to the Digital Twin State updates
    private volatile boolean stateObserved = false;

    private DigitalAdapterListener digitalAdapterListener;

    private DigitalAdapterLifeCycleListener digitalAdapterLifeCycleListener;

    // Query Executor to send query to the storage layer in both synchronous and asynchronous way
    protected QueryExecutor queryExecutor = null;

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
     * Instance id used as inner registry key for this adapter's metrics.
     */
    private String metricsInstanceId;


    private DigitalAdapter() {
        super();
    }

    public DigitalAdapter(String id, C configuration) {
        this(id);
        this.id = id;
        this.configuration = configuration;
    }

    public DigitalAdapter(String id) {
        this();
        this.id = id;
    }

    /**
     * This method allows to handle the registration of the Metrics related to the Digital Adapter component in the Monitoring Interface.
     */
    private void handleMetricsRegistration() {

        // Check if the Monitoring Interface is configured and has the handler configured
        if(this.monitoringInterface != null && this.monitoringInterface.isActive(WldtMetricComponent.DIGITAL_ADAPTER)) {

            // Build metric namespace and instance id
            this.metricsNamespace = CoreMonitoringUtils.buildCoreNamespace();
            this.metricsInstanceId = getId();

            // Create Additional Metric Metadata to keep track of the adapter Id
            Map<String, Object> metricMetadata = new HashMap<String, Object>() {{
                put(METRIC_METADATA_DIGITAL_ADAPTER_ID_KEY, getId());
            }};

            // Register Counter Metric(s) - Digital Action
            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinId,
                    this.metricsNamespace,
                    CoreMonitoringUtils.DIGITAL_ADAPTER_ACTION_EVENT_PUB_SUCCESS_COUNT,
                    WldtMetricComponent.DIGITAL_ADAPTER, metricMetadata).withInstanceId(metricsInstanceId));

            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinId,
                    this.metricsNamespace,
                    CoreMonitoringUtils.DIGITAL_ADAPTER_ACTION_EVENT_PUB_ERROR_COUNT,
                    WldtMetricComponent.DIGITAL_ADAPTER, metricMetadata).withInstanceId(metricsInstanceId));

            // Register Counter Metric(s) - Digital Twin State Processing
            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinId,
                    this.metricsNamespace,
                    CoreMonitoringUtils.DIGITAL_ADAPTER_STATE_UPDATE_SUCCESS_COUNT,
                    WldtMetricComponent.DIGITAL_ADAPTER, metricMetadata).withInstanceId(metricsInstanceId));

            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinId,
                    this.metricsNamespace,
                    CoreMonitoringUtils.DIGITAL_ADAPTER_STATE_UPDATE_ERROR_COUNT,
                    WldtMetricComponent.DIGITAL_ADAPTER, metricMetadata).withInstanceId(metricsInstanceId));

            // Register Execution Time Metrics - Digital Twin State Processing
            this.monitoringInterface.registerMetric(new WldtTimer(
                    this.digitalTwinId,
                    this.metricsNamespace,
                    CoreMonitoringUtils.DIGITAL_ADAPTER_STATE_UPDATE_EXEC_TIME,
                    WldtMetricComponent.DIGITAL_ADAPTER, metricMetadata).withInstanceId(metricsInstanceId));

            // Register Counter Metric(s) - Digital Twin Event Processing
            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinId,
                    this.metricsNamespace,
                    CoreMonitoringUtils.DIGITAL_EVENT_NOTIFICATION_SUCCESS_COUNT,
                    WldtMetricComponent.DIGITAL_ADAPTER, metricMetadata).withInstanceId(metricsInstanceId));

            this.monitoringInterface.registerMetric(new WldtCounter(
                    this.digitalTwinId,
                    this.metricsNamespace,
                    CoreMonitoringUtils.DIGITAL_EVENT_NOTIFICATION_ERROR_COUNT,
                    WldtMetricComponent.DIGITAL_ADAPTER, metricMetadata).withInstanceId(metricsInstanceId));

            // Register Execution Time Metrics - Digital Twin Event Processing
            this.monitoringInterface.registerMetric(new WldtTimer(
                    this.digitalTwinId,
                    this.metricsNamespace,
                    CoreMonitoringUtils.DIGITAL_EVENT_NOTIFICATION_EXEC_TIME,
                    WldtMetricComponent.DIGITAL_ADAPTER, metricMetadata).withInstanceId(metricsInstanceId));

        }

    }

    //////////////////////// PROPERTIES OBSERVATION ///////////////////////////////////////////////////////////////////
    /**
     * Enable the observation of all the Digital Twin State and any of its variations in terms of Properties, Actions,
     * Events and Relationships. The adapter subscribes to the two state topics of the event bus: the description
     * updates (structure) and the values updates (only the changed values). The adapter keeps a local, always
     * complete copy of both, available through {@link #getDigitalTwinStateDescription()} and
     * {@link #getDigitalTwinStateValues()}.
     * @throws EventBusException Thrown if there is an error in the EventBus subscription
     */
    protected void observeDigitalTwinState() throws EventBusException {

        //Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DigitalTwinStateManager.getStateDescriptionUpdateWldtEventMessageType());
        wldtEventFilter.add(DigitalTwinStateManager.getStateValuesUpdateWldtEventMessageType());

        //Save the adopted EventFilter
        this.statePropertiesWldtEventFilter = wldtEventFilter;

        WldtEventBus.getInstance().subscribe(this.digitalTwinId, this.id, wldtEventFilter, this);
        this.stateObserved = true;
    }

    /**
     * Enable the observation of the Digital Twin State applying a {@link DigitalTwinStateFilter} to the values
     * delivered to the adapter. The filter is applied by the adapter on its complete local copy of the state:
     * <ul>
     *   <li>{@link #onStateValuesUpdate(DigitalTwinStateValues, List)} receives the values of the accepted resources,
     *   if at least one accepted value changed;</li>
     *   <li>{@link #onStateDescriptionUpdate(DigitalTwinStateDescription, List)} is <b>never</b> filtered: it receives the
     *   complete description every time the structure of the state changes;</li>
     *   <li>the event names of the filter also select the event notifications delivered to
     *   {@link #onEventNotificationReceived(DigitalTwinStateEventNotification)}: the notifications of the events that
     *   the filter does not accept are discarded (the subscription to the notifications is still defined with the
     *   {@code observe*EventNotification(s)} methods).</li>
     * </ul>
     * If the adapter is already observing the state (e.g. automatically after the DT synchronization) no additional
     * subscription is created: only the filter is replaced. A new call replaces the previous filter.
     * The natural place to call it is {@link #onDigitalTwinSync(DigitalTwinStateDescription, DigitalTwinStateValues)},
     * where the description of the state is available: the filter is then active from the first update.
     * @param filter the filter, null is equivalent to {@link DigitalTwinStateFilter#all()}
     * @throws EventBusException Thrown if there is an error in the EventBus subscription
     */
    protected void observeDigitalTwinState(DigitalTwinStateFilter filter) throws EventBusException {
        observeDigitalTwinState(filter, this.stateChangeFilter);
    }

    /**
     * Enable the observation of the Digital Twin State applying a content filter ({@link DigitalTwinStateFilter})
     * and a trigger filter ({@link DigitalTwinStateChangeFilter}) to the values updates. See
     * {@link #observeDigitalTwinState(DigitalTwinStateFilter)}. The values update is notified only if at least one
     * change passes the content filter and matches the trigger filter. The description is never filtered.
     * @param filter the content filter, null is equivalent to {@link DigitalTwinStateFilter#all()}
     * @param changeFilter the trigger filter, null = no trigger filter
     * @throws EventBusException Thrown if there is an error in the EventBus subscription
     */
    protected void observeDigitalTwinState(DigitalTwinStateFilter filter, DigitalTwinStateChangeFilter changeFilter) throws EventBusException {

        this.stateFilter = filter != null ? filter : DigitalTwinStateFilter.all();
        this.stateChangeFilter = changeFilter;

        if(!this.stateObserved)
            observeDigitalTwinState();
    }

    /**
     * Remove the filters configured on the values updates (content filter and trigger filter): from now on the
     * adapter receives every values update, complete.
     */
    protected void clearStateFilter() {
        this.stateFilter = DigitalTwinStateFilter.all();
        this.stateChangeFilter = null;
    }

    /**
     * Cancel the observation of all the Digital Twin State variations and updates.
     * @throws EventBusException Thrown if there is an error in the EventBus unsubscription
     */
    protected void unObserveDigitalTwinState() throws EventBusException {

        //Define EventFilter and add the target topics
        WldtEventFilter wldtEventFilter = new WldtEventFilter();
        wldtEventFilter.add(DigitalTwinStateManager.getStateDescriptionUpdateWldtEventMessageType());
        wldtEventFilter.add(DigitalTwinStateManager.getStateValuesUpdateWldtEventMessageType());

        WldtEventBus.getInstance().unSubscribe(this.digitalTwinId, this.id, wldtEventFilter, this);
        this.stateObserved = false;
    }

    /**
     * @return the current complete (never filtered) Digital Twin State Description known by the adapter, or null if
     * the Digital Twin has not been synchronized yet
     */
    protected DigitalTwinStateDescription getDigitalTwinStateDescription() {
        return this.stateReplica.getDescription();
    }

    /**
     * @return the current complete (never filtered) Digital Twin State Values known by the adapter, or null if
     * the Digital Twin has not been synchronized yet
     */
    protected DigitalTwinStateValues getDigitalTwinStateValues() {
        return this.stateReplica.getValues();
    }


    /////////////////////////////// DIGITAL ACTION EVENT MANAGEMENT ///////////////////////////////////////////////////

    /**
     *
     * Publish an event associated to a Digital Action performed on the Digital Adapter and that should be forwarded
     * to the DT's core
     *
     * @param actionKey the key of the action to be performed
     * @param body the body of the action, which can be any type of object
     * @param <T> the type of the body object
     * @throws EventBusException Thrown if there is an error in the EventBus publishing
     */
    protected <T> void publishDigitalActionWldtEvent(String actionKey, T body) throws EventBusException {
        //WldtEvent<DigitalActionWldtEvent<T>> notification = new WldtEvent<>(DIGITAL_ACTION_EVENT, new DigitalActionWldtEvent<>(actionKey, body));
        //WldtEventBus.getInstance().publishEvent(this.digitalTwinId, this.id, new DigitalActionWldtEvent<>(actionKey, body));

        try{
            // Send the Event
            WldtEventBus.getInstance().publishEvent(this.digitalTwinId, this.id, new DigitalActionWldtEvent<>(actionKey, body));

            // Increase Success Counter
            if(this.monitoringInterface != null && this.monitoringInterface.isActive(WldtMetricComponent.DIGITAL_ADAPTER))
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.DIGITAL_ADAPTER_ACTION_EVENT_PUB_SUCCESS_COUNT, metricsInstanceId);
        } catch (Exception e){

            // Build the error message
            String errorMessage = String.format("publishDigitalActionWldtEvent Function Error: %s", e.getLocalizedMessage());
            logger.error(errorMessage);

            // Increate Metrics Count
            if(this.monitoringInterface != null && this.monitoringInterface.isActive(WldtMetricComponent.DIGITAL_ADAPTER))
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.DIGITAL_ADAPTER_ACTION_EVENT_PUB_ERROR_COUNT, metricsInstanceId);

            // Propagate the Exception
            throw new EventBusException(errorMessage);
        }
    }

    /**
     * Publish an event associated to a Digital Action performed on the Digital Adapter and that should be forwarded
     * to the DT's core
     * @param actionWldtEvent the DigitalActionWldtEvent to be published
     * @throws EventBusException Thrown if there is an error in the EventBus publishing
     */
    protected void publishDigitalActionWldtEvent(DigitalActionWldtEvent<?> actionWldtEvent) throws EventBusException {
        //WldtEvent<DigitalActionWldtEvent<?>> notification = new WldtEvent<>(DIGITAL_ACTION_EVENT, actionWldtEvent);
        //WldtEventBus.getInstance().publishEvent(this.digitalTwinId, this.id, actionWldtEvent);

        try{
            // Send the Event
            WldtEventBus.getInstance().publishEvent(this.digitalTwinId, this.id, actionWldtEvent);

            // Increase Success Counter
            if(this.monitoringInterface != null && this.monitoringInterface.isActive(WldtMetricComponent.DIGITAL_ADAPTER))
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.DIGITAL_ADAPTER_ACTION_EVENT_PUB_SUCCESS_COUNT, metricsInstanceId);
        } catch (Exception e){

            // Build the error message
            String errorMessage = String.format("publishDigitalActionWldtEvent Function Error: %s", e.getLocalizedMessage());
            logger.error(errorMessage);

            // Increate Metrics Count
            if(this.monitoringInterface != null && this.monitoringInterface.isActive(WldtMetricComponent.DIGITAL_ADAPTER))
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.DIGITAL_ADAPTER_ACTION_EVENT_PUB_ERROR_COUNT, metricsInstanceId);

            // Propagate the Exception
            throw new EventBusException(errorMessage);
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //////////////////////// EVENTS OBSERVATION //////////////////////////////////////////////////////////
    /**
     * Enable the observation of available Digital Twin State Events Notifications.
     * @param digitalTwinStateDescription the Digital Twin State Description listing the events to observe
     * @throws EventBusException Thrown if there is an error in the EventBus subscription
     */
    protected void observeAllDigitalTwinEventsNotifications(DigitalTwinStateDescription digitalTwinStateDescription) throws EventBusException, WldtDigitalTwinStateEventException {

        if(digitalTwinStateDescription != null && !digitalTwinStateDescription.getEvents().isEmpty()){
            this.observeDigitalTwinEventsNotifications(digitalTwinStateDescription.getEvents().stream().map(DigitalTwinStateDescription.EventDescription::getKey).collect(Collectors.toList()));
        }
        else
            throw new WldtDigitalTwinStateEventException("Error observing All DT Event Notifications ! Provided DT State Description = null or without events !");
    }

    /**
     * Cancel the observation of Digital Twin State Events Notifications
     * @param digitalTwinStateDescription the Digital Twin State Description listing the events to unobserve
     * @throws EventBusException Thrown if there is an error in the EventBus unsubscription
     */
    protected void unObserveAllDigitalTwinEventsNotifications(DigitalTwinStateDescription digitalTwinStateDescription) throws EventBusException, WldtDigitalTwinStateEventException {

        if(digitalTwinStateDescription != null && !digitalTwinStateDescription.getEvents().isEmpty()){
            this.unObserveDigitalTwinEventsNotifications(digitalTwinStateDescription.getEvents().stream().map(DigitalTwinStateDescription.EventDescription::getKey).collect(Collectors.toList()));
        }
        else
            throw new WldtDigitalTwinStateEventException("Error observing All DT Event Notifications ! Provided DT State Description = null or without events !");
    }

    /**
     * Enable the observation of the notification associated to a specific list of Digital Twin State events.
     * With respect to event a notification contains the new associated value
     * @param eventsList the list of events to observe
     * @throws EventBusException Thrown if there is an error in the EventBus subscription
     */
    protected void observeDigitalTwinEventsNotifications(List<String> eventsList) throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        for(String eventKey : eventsList)
            wldtEventFilter.add(DigitalTwinStateManager.getEventNotificationWldtEventMessageType(eventKey));

        if(this.stateTargetEventNotificationWldtEventsFilter == null)
            this.stateTargetEventNotificationWldtEventsFilter = new WldtEventFilter();

        this.stateTargetEventNotificationWldtEventsFilter.addAll(wldtEventFilter);

        WldtEventBus.getInstance().subscribe(this.digitalTwinId, this.id, wldtEventFilter, this);
    }

    /**
     * Cancel the observation of a target list of properties
     * @param eventsList the list of events to unobserve
     * @throws EventBusException Thrown if there is an error in the EventBus unsubscription
     */
    protected void unObserveDigitalTwinEventsNotifications(List<String> eventsList) throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        for(String eventKey : eventsList)
            wldtEventFilter.add(DigitalTwinStateManager.getEventNotificationWldtEventMessageType(eventKey));

        if(this.stateTargetEventNotificationWldtEventsFilter == null)
            this.stateTargetEventNotificationWldtEventsFilter= new WldtEventFilter();

        this.stateTargetEventNotificationWldtEventsFilter.removeAll(wldtEventFilter);

        WldtEventBus.getInstance().unSubscribe(this.digitalTwinId, this.id, wldtEventFilter, this);
    }

    /**
     * Enable the observation of the notification associated to a single Digital Twin State event.
     * With respect to event a notification contains the new associated value
     * @param eventKey the key of the event to observe
     * @throws EventBusException Thrown if there is an error in the EventBus subscription
     */
    protected void observeDigitalTwinEventNotification(String eventKey) throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        wldtEventFilter.add(DigitalTwinStateManager.getEventNotificationWldtEventMessageType(eventKey));

        if(this.stateTargetEventNotificationWldtEventsFilter == null)
            this.stateTargetEventNotificationWldtEventsFilter= new WldtEventFilter();

        this.stateTargetEventNotificationWldtEventsFilter.addAll(wldtEventFilter);

        WldtEventBus.getInstance().subscribe(this.digitalTwinId, this.id, wldtEventFilter, this);
    }

    /**
     * Cancel the observation of a single target event
     * @param eventKey the key of the event to unobserve
     * @throws EventBusException Thrown if there is an error in the EventBus unsubscription
     */
    protected void unObserveDigitalTwinEventNotification(String eventKey) throws EventBusException {

        //Define EventFilter and add the target topic
        WldtEventFilter wldtEventFilter = new WldtEventFilter();

        wldtEventFilter.add(DigitalTwinStateManager.getEventNotificationWldtEventMessageType(eventKey));

        if(this.stateTargetEventNotificationWldtEventsFilter == null)
            this.stateTargetEventNotificationWldtEventsFilter= new WldtEventFilter();

        this.stateTargetEventNotificationWldtEventsFilter.removeAll(wldtEventFilter);

        WldtEventBus.getInstance().unSubscribe(this.digitalTwinId, this.id, wldtEventFilter, this);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * This method allows an implementation of a Digital Adapter to notify active listeners
     * when there is an issue in the binding with the Digital Asset.
     *
     * @param errorMessage the error message to be notified to the listeners
     */
    protected void notifyDigitalAdapterUnBound(String errorMessage){
        //Notify Listeners
        if(getDigitalAdapterListener() != null)
            getDigitalAdapterListener().onDigitalAdapterUnBound(getId(), errorMessage);
    }

    /**
     * This method allows an implementation of a Digital Adapter to notify active listeners when
     * the adapter is ready to work and correctly bound to the associated external digital services.
     *
     */
    protected void notifyDigitalAdapterBound() {
        //Notify Listeners
        if(getDigitalAdapterListener() != null)
            getDigitalAdapterListener().onDigitalAdapterBound(getId());
    }

    //////////////////////// DIGITAL TWIN STATE UPDATE  //////////////////////////////////////////////////////////
    /**
     * Called every time the <b>description</b> (structure) of the Digital Twin State changes: a property, action,
     * event or relationship has been added, updated or removed. It is never filtered, neither for the triggering nor
     * for the content: the adapter always receives the complete new description, even if filters are configured on
     * the values.
     * <p>
     * <b>Threading:</b> the callback runs on the event bus thread of the adapter while holding the monitor of the
     * local copy of the state. A callback that takes long, or that keeps a lock needed by other threads, blocks the
     * delivery of the following state events (and the access to {@link #getDigitalTwinStateValues()}).
     * Failures are logged and counted in the monitoring metrics, and do not stop the following callbacks.
     * @param description the complete current description of the state (it has a unique id)
     * @param descriptionChanges the changes of the update that altered the description
     */
    abstract protected void onStateDescriptionUpdate(DigitalTwinStateDescription description,
                                            List<DigitalTwinStateChange> descriptionChanges);

    /**
     * Called when the <b>values</b> of the Digital Twin State change: a property value, a relationship instance or
     * the last invocation/notification of an action/event has changed. The adapter receives the complete values
     * (its local copy, updated with the delta received on the event bus) associated to the id of the current
     * description. If a content filter ({@link DigitalTwinStateFilter}) and/or a trigger filter
     * ({@link DigitalTwinStateChangeFilter}) is configured, only the changes that pass them trigger the method and
     * the values are limited to the accepted resources.
     * <p>
     * <b>Threading:</b> the callback runs on the event bus thread of the adapter while holding the monitor of the
     * local copy of the state. A callback that takes long, or that keeps a lock needed by other threads, blocks the
     * delivery of the following state events (and the access to {@link #getDigitalTwinStateValues()}).
     * Failures are logged and counted in the monitoring metrics, and do not stop the following callbacks.
     * @param values the current values (limited to the accepted resources if a filter is configured)
     * @param relevantChanges the changes of the update that altered the values (and pass the configured filters)
     */
    abstract protected void onStateValuesUpdate(DigitalTwinStateValues values,
                                       List<DigitalTwinStateChange> relevantChanges);


    //////////////////////// EVENTS NOTIFICATION CALLBACK /////////////////////////////////////////////////////
    abstract protected void onEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification);


    //////////////////////// ADAPTER CALLBACKS /////////////////////////////////////////////////////
    public abstract void onAdapterStart();

    public abstract void onAdapterStop();


    //////////////////////// DT CALLBACKS /////////////////////////////////////////////////////
    public abstract void onDigitalTwinSync(DigitalTwinStateDescription digitalTwinStateDescription, DigitalTwinStateValues digitalTwinStateValues);

    public abstract void onDigitalTwinUnSync(DigitalTwinStateDescription digitalTwinStateDescription, DigitalTwinStateValues digitalTwinStateValues);

    public abstract void onDigitalTwinCreate();

    /**
     * Called when the DT moves to the STARTED state. It is invoked BEFORE the adapter is executed
     * (before {@link #onAdapterStart()}), so it must not be used to initialize adapter resources.
     * A way to exploit this callback will be defined in the future.
     */
    public abstract void onDigitalTwinStart();

    public abstract void onDigitalTwinStop();

    public abstract void onDigitalTwinDestroy();

    @Override
    public void onWorkerStart() throws WldtRuntimeException {
        try{

            // Init the Query Executor
            if(this.queryExecutor == null)
                this.queryExecutor = new QueryExecutor(this.digitalTwinId, String.format("query-executor-%s", this.id));

            onAdapterStart();
        }catch (Exception e){
            throw new WldtRuntimeException(e.getLocalizedMessage());
        }
    }

    @Override
    public void onWorkerStop() throws WldtRuntimeException {
        try{
            unObserveDigitalTwinState();
            onAdapterStop();
            if(getDigitalAdapterListener() != null)
                getDigitalAdapterListener().onDigitalAdapterUnBound(getId(), null);
        }catch (Exception e){
            if(getDigitalAdapterListener() != null)
                getDigitalAdapterListener().onDigitalAdapterUnBound(getId(), e.getLocalizedMessage());
            throw new WldtRuntimeException(e.getLocalizedMessage());
        } finally {
            // Release the subscription of the Query Executor to the event bus
            if(this.queryExecutor != null)
                this.queryExecutor.close();
        }
    }

    public C getConfiguration() {
        return configuration;
    }

    public void setConfiguration(C configuration) {
        this.configuration = configuration;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public DigitalAdapterListener getDigitalAdapterListener() {
        return digitalAdapterListener;
    }

    public void setDigitalAdapterListener(DigitalAdapterListener digitalAdapterListener) {
        this.digitalAdapterListener = digitalAdapterListener;
    }

    public DigitalAdapterLifeCycleListener getDigitalAdapterLifeCycleListener() {
        return digitalAdapterLifeCycleListener;
    }

    public void setDigitalAdapterLifeCycleListener(DigitalAdapterLifeCycleListener digitalAdapterLifeCycleListener) {
        this.digitalAdapterLifeCycleListener = digitalAdapterLifeCycleListener;
    }

    public void removeDigitalAdapterLifeCycleListener(){
        this.digitalAdapterLifeCycleListener = null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DigitalAdapter that = (DigitalAdapter) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public void onEventSubscribed(String eventType) {
        logger.info("Subscribed to: {}", eventType);
    }

    @Override
    public void onEventUnSubscribed(String eventType) {
        logger.info("UnSubscribed from: {}", eventType);
    }

    /**
     * This method is the main entry point for the handling of the Digital Twin State Update Events and is in charge of
     * managing the monitoring of the processing of the events and the related metrics in the Monitoring Interface.
     * @param wldtEvent the event associated to the Digital Twin State Update to be processed
     */
    private void handleDigitalTwinStateEvent(WldtEvent<?> wldtEvent){

        // Check if the Monitoring Interface is configured and has the handler configured
        if(this.monitoringInterface == null || !this.monitoringInterface.isActive(WldtMetricComponent.DIGITAL_ADAPTER)){
            try {
                processDigitalTwinStateUpdateEvent(wldtEvent);
            }
            catch (Exception e){
                String errorMessage = String.format("handleDigitalTwinStateEvent Error Processing DT State Update Event : %s", e.getLocalizedMessage());
                logger.error(errorMessage);
            }
        }
        else {

            long startMs = System.currentTimeMillis();
            try {

                // Call the actual function to handle the event (the callbacks implemented by the developer are
                // notified inside: their failures are already counted as errors there)
                boolean allCallbacksSucceeded = processDigitalTwinStateUpdateEvent(wldtEvent);

                // Increase Success Counter
                if(allCallbacksSucceeded)
                    this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.DIGITAL_ADAPTER_STATE_UPDATE_SUCCESS_COUNT, metricsInstanceId);
            }
            catch (Exception e){
                String errorMessage = String.format("handleDigitalTwinStateEvent Error Processing DT State Update Event: %s", e.getLocalizedMessage());
                logger.error(errorMessage);
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.DIGITAL_ADAPTER_STATE_UPDATE_ERROR_COUNT, metricsInstanceId);
            }
            finally {
                // Update new Timer Value for the execution of the Physical Property Variation
                this.monitoringInterface.updateTimerSince(
                        this.metricsNamespace,
                        CoreMonitoringUtils.DIGITAL_ADAPTER_STATE_UPDATE_EXEC_TIME,
                        startMs,
                        metricsInstanceId);
            }
        }
    }

    /**
     * This method is the main entry point for the handling of the Digital Twin State Event Notifications and is in charge of
     * managing the monitoring of the processing of the events and the related metrics in the Monitoring Interface.
     * @param digitalTwinStateEventNotification the notification associated to the Digital Twin State Event to be processed
     *                                         the notification associated to the Digital Twin State Event to be processed
     */
    private void handleEventNotification(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification){

        // The event names of the content filter select the notifications delivered to the adapter
        if(!this.stateFilter.acceptsEvent(digitalTwinStateEventNotification.getDigitalEventKey()))
            return;

        // Check if the Monitoring Interface is configured and has the handler configured
        if(this.monitoringInterface == null || !this.monitoringInterface.isActive(WldtMetricComponent.DIGITAL_ADAPTER)){
            try {
                onEventNotificationReceived(digitalTwinStateEventNotification);
            }
            catch (Exception e){
                String errorMessage = String.format("handleEventNotification Error Processing DT Event Notification : %s", e.getLocalizedMessage());
                logger.error(errorMessage);
            }
        }
        else {

            long startMs = System.currentTimeMillis();
            try {

                // Call the actual function to handle the event (implemented by the developer)
                onEventNotificationReceived(digitalTwinStateEventNotification);

                // Increase Success Counter
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.DIGITAL_EVENT_NOTIFICATION_SUCCESS_COUNT, metricsInstanceId);
            }
            catch (Exception e){
                String errorMessage = String.format("handleEventNotification Error Processing DT Event Notification: %s", e.getLocalizedMessage());
                logger.error(errorMessage);
                this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.DIGITAL_EVENT_NOTIFICATION_ERROR_COUNT, metricsInstanceId);
            }
            finally {
                // Update new Timer Value for the execution of the Physical Property Variation
                this.monitoringInterface.updateTimerSince(
                        this.metricsNamespace,
                        CoreMonitoringUtils.DIGITAL_EVENT_NOTIFICATION_EXEC_TIME,
                        startMs,
                        metricsInstanceId);
            }
        }
    }

    /**
     * Applies a state update event (description or values) to the local copy of the state and notifies the
     * description and values callbacks, in order. The description is never filtered, the values are limited by the
     * configured filters. A failure in one callback does not prevent the following ones from being notified.
     * @param wldtEvent the description update event or the values update event
     * @return true if every notified callback completed without errors
     */
    private boolean processDigitalTwinStateUpdateEvent(WldtEvent<?> wldtEvent){

        boolean allCallbacksSucceeded = true;

        // The monitor of the replica keeps the callbacks ordered even if the events are delivered by different threads
        synchronized (this.stateReplica) {

            if(wldtEvent.getBody() instanceof DigitalTwinStateDescription) {

                if(this.stateReplica.applyDescriptionEvent(wldtEvent)) {
                    allCallbacksSucceeded &= notifyDescriptionUpdate(this.stateReplica.getDescription(), DigitalTwinStateManager.getDescriptionChanges(wldtEvent));

                    // Values events that were waiting for this description, one at a time, in arrival order
                    WldtEvent<?> pendingEvent;
                    while((pendingEvent = this.stateReplica.applyNextPendingValues()) != null)
                        allCallbacksSucceeded &= notifyValuesUpdate(this.stateReplica.getValues(), DigitalTwinStateManager.getValuesChanges(pendingEvent));
                }

            } else if(this.stateReplica.applyValuesEvent(wldtEvent)) {
                allCallbacksSucceeded &= notifyValuesUpdate(this.stateReplica.getValues(), DigitalTwinStateManager.getValuesChanges(wldtEvent));
            }
        }

        return allCallbacksSucceeded;
    }

    /**
     * Counts a failure of a state callback in the monitoring metrics (if the monitoring is active)
     */
    private void increaseStateUpdateErrorCount() {
        if(this.monitoringInterface != null && this.monitoringInterface.isActive(WldtMetricComponent.DIGITAL_ADAPTER))
            this.monitoringInterface.increaseCounter(this.metricsNamespace, CoreMonitoringUtils.DIGITAL_ADAPTER_STATE_UPDATE_ERROR_COUNT, metricsInstanceId);
    }

    /**
     * Notifies the description callback (the description is never filtered)
     * @param newDescription the complete description after the update
     * @param changes the description changes
     * @return true if the callback completed without errors
     */
    private boolean notifyDescriptionUpdate(DigitalTwinStateDescription newDescription, List<DigitalTwinStateChange> changes) {
        try {
            onStateDescriptionUpdate(newDescription, changes);
            return true;
        } catch (Exception e) {
            logger.error("Digital Adapter ({}) -> Error in onStateDescriptionUpdate: {}", this.id, e.getLocalizedMessage());
            increaseStateUpdateErrorCount();
            return false;
        }
    }

    /**
     * Notifies the values callback if at least one change passes the content and trigger filters
     * @param newValues the complete (unfiltered) values after the update
     * @param changes the changes of the update
     * @return true if the callback completed without errors (or was not notified)
     */
    private boolean notifyValuesUpdate(DigitalTwinStateValues newValues, List<DigitalTwinStateChange> changes) {

        DigitalTwinStateFilter filter = this.stateFilter;
        DigitalTwinStateChangeFilter changeFilter = this.stateChangeFilter;

        List<DigitalTwinStateChange> relevantChanges = new ArrayList<>();
        for(DigitalTwinStateChange change : changes)
            if(change.isValueChange()
                    && filter.accepts(change)
                    && (changeFilter == null || changeFilter.matches(change)))
                relevantChanges.add(change);

        if(!relevantChanges.isEmpty()) {
            try {
                onStateValuesUpdate(newValues.filter(filter), relevantChanges);
            } catch (Exception e) {
                logger.error("Digital Adapter ({}) -> Error in onStateValuesUpdate: {}", this.id, e.getLocalizedMessage());
                increaseStateUpdateErrorCount();
                return false;
            }
        }

        return true;
    }

    @Override
    public void onEvent(WldtEvent<?> wldtEvent) {

        logger.debug("{} - Digital Adapter - Received Event: {}", getId(), wldtEvent);

        //DT State Events Management (description updates and values updates)
        if(wldtEvent != null
                && wldtEvent.getBody() != null
                && ((wldtEvent.getType().equals(DigitalTwinStateManager.getStateDescriptionUpdateWldtEventMessageType()) && wldtEvent.getBody() instanceof DigitalTwinStateDescription)
                    || (wldtEvent.getType().equals(DigitalTwinStateManager.getStateValuesUpdateWldtEventMessageType()) && wldtEvent.getBody() instanceof DigitalTwinStateValues))){

            handleDigitalTwinStateEvent(wldtEvent);
        }

        ///////// DT STATE EVENTS NOTIFICATION MANAGEMENT ///////////
        if(wldtEvent != null && wldtEvent.getBody() != null && (wldtEvent.getBody() instanceof DigitalTwinStateEventNotification)) {
            logger.debug("Received Event Notification: {}",  wldtEvent.getBody());
            handleEventNotification((DigitalTwinStateEventNotification<?>) wldtEvent.getBody());

            //DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification = (DigitalTwinStateEventNotification<?>) wldtEvent.getBody();
            //logger.debug("Received Event Notification: {}", digitalTwinStateEventNotification);
            //onEventNotificationReceived(digitalTwinStateEventNotification);
        }

    }

    @Override
    public void onSync(DigitalTwinStateDescription digitalTwinStateDescription, DigitalTwinStateValues digitalTwinStateValues) {

        try{

            logger.info("Digital Adapter ({}) Received DT onSync callback ! Ready to start ...", this.id);

            synchronized (this.stateReplica) {
                // Initialize the local copy of the state with the first available description and values
                this.stateReplica.seed(digitalTwinStateDescription, digitalTwinStateValues);
            }

            //Notify about the first available Digital Twin State
            onDigitalTwinSync(digitalTwinStateDescription, digitalTwinStateValues);

            //By default, the Digital Adapter observer all the variation on the DT State
            //(unless the developer already enabled the observation, e.g. with a DigitalTwinStateFilter)
            if(!this.stateObserved)
                observeDigitalTwinState();

        }catch (Exception e){
            logger.error(String.format("Digital Adapter (%s) -> observe DigitalTwin State: Error: %s", id, e.getLocalizedMessage()));
        }
    }

    @Override
    public void onUnSync(DigitalTwinStateDescription digitalTwinStateDescription, DigitalTwinStateValues digitalTwinStateValues) {
        logger.debug("Digital Adapter ({}) Received DT unSync callback ...", this.id);
        onDigitalTwinUnSync(digitalTwinStateDescription, digitalTwinStateValues);

        // While the DT is not synchronized the adapter does not follow the state: the subscription to the state topics
        // is released and the local copy dropped. The next sync seeds the copy again and subscribes again.
        try {
            unObserveDigitalTwinState();
        } catch (Exception e) {
            logger.error("Digital Adapter ({}) -> Error unobserving the DT State at unSync: {}", this.id, e.getLocalizedMessage());
        }

        synchronized (this.stateReplica) {
            this.stateReplica.clear();
        }
    }

    @Override
    public void onCreate() {
        onDigitalTwinCreate();
    }

    @Override
    public void onStart() {
        onDigitalTwinStart();
    }

    @Override
    public void onPhysicalAdapterBound(String adapterId, PhysicalAssetDescription physicalAssetDescription) {
        if(getDigitalAdapterLifeCycleListener() != null)
            getDigitalAdapterLifeCycleListener().onPhysicalAdapterBound(adapterId, physicalAssetDescription);
    }

    @Override
    public void onPhysicalAdapterBindingUpdate(String adapterId, PhysicalAssetDescription physicalAssetDescription) {
        if(getDigitalAdapterLifeCycleListener() != null)
            getDigitalAdapterLifeCycleListener().onPhysicalAdapterBindingUpdate(adapterId, physicalAssetDescription);
    }

    @Override
    public void onPhysicalAdapterUnBound(String adapterId, PhysicalAssetDescription physicalAssetDescription, String errorMessage) {
        if(getDigitalAdapterLifeCycleListener() != null)
            getDigitalAdapterLifeCycleListener().onPhysicalAdapterUnBound(adapterId, physicalAssetDescription, errorMessage);
    }

    @Override
    public void onDigitalAdapterBound(String adapterId) {
        if(getDigitalAdapterLifeCycleListener() != null)
            getDigitalAdapterLifeCycleListener().onDigitalAdapterBound(adapterId);
    }

    @Override
    public void onDigitalAdapterUnBound(String adapterId, String errorMessage) {
        if(getDigitalAdapterLifeCycleListener() != null)
            getDigitalAdapterLifeCycleListener().onDigitalAdapterUnBound(adapterId, errorMessage);
    }

    @Override
    public void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap) {
        if(getDigitalAdapterLifeCycleListener() != null)
            getDigitalAdapterLifeCycleListener().onDigitalTwinBound(adaptersPhysicalAssetDescriptionMap);
    }

    @Override
    public void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap, String errorMessage) {
        if(getDigitalAdapterLifeCycleListener() != null)
            getDigitalAdapterLifeCycleListener().onDigitalTwinUnBound(adaptersPhysicalAssetDescriptionMap,errorMessage);
    }

    @Override
    public void onStop() {
        onDigitalTwinStop();
    }

    @Override
    public void onDestroy() {
        onDigitalTwinDestroy();
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
