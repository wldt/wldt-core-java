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
package it.wldt.storage;

import it.wldt.adapter.digital.DigitalActionRequest;
import it.wldt.adapter.physical.*;
import it.wldt.augmentation.error.AugmentationFunctionError;
import it.wldt.augmentation.function.AugmentationFunction;
import it.wldt.augmentation.function.AugmentationFunctionType;
import it.wldt.augmentation.request.AugmentationFunctionRequest;
import it.wldt.augmentation.result.AugmentationFunctionResult;
import it.wldt.core.engine.LifeCycleStateVariation;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateChange;
import it.wldt.core.state.DigitalTwinStateEventNotification;
import it.wldt.exception.StorageException;
import it.wldt.adapter.physical.PhysicalAssetPropertyVariation;
import it.wldt.monitoring.CoreMonitoringUtils;
import it.wldt.monitoring.MonitoringInterface;
import it.wldt.monitoring.metrics.WldtCounter;
import it.wldt.monitoring.metrics.WldtMetricComponent;
import it.wldt.monitoring.metrics.WldtTimer;
import it.wldt.storage.model.StorageStats;
import it.wldt.storage.query.Query;
import it.wldt.storage.query.QueryResult;
import it.wldt.storage.model.augmentation.*;
import it.wldt.storage.model.digital.DigitalActionRequestRecord;
import it.wldt.storage.model.lifecycle.LifeCycleVariationRecord;
import it.wldt.storage.model.physical.*;
import it.wldt.storage.model.state.DigitalTwinStateEventNotificationRecord;
import it.wldt.storage.model.state.DigitalTwinStateRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 29/03/2024
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 * Defines an abstract class allowing the Digital Twin developer to implement its internal storage system for the Digital Twin instance.
 * The class defines methods for the management of:
 *      - Digital Twin State storage and retrieval with the associated change list;
 *      - Generated State Digital Events;
 *      - Generated State Event Notifications
 *      - Life Cycle State storage and retrieval;
 *      - Physical Asset Description storage and retrieval;
 *      - Physical Asset Property Variation storage and retrieval;
 *      - Physical Asset Relationship Instance storage and retrieval;
 *      - Digital Action Request storage and retrieval;
 *      - Physical Asset Action Request storage and retrieval;
 *      - Physical Asset Event Notification storage and retrieval;
 */
public abstract class WldtStorage {

    // If true the storage manager will observe state events and receive callback to handle the storage
    private boolean observeStateEvents = false;

    // If true the storage manager will observe physical asset events and receive callback to handle the storage
    private boolean observerPhysicalAssetEvents = false;

    // If true the storage manager will observe physical asset action events and receive callback to handle the storage
    private boolean observerPhysicalAssetActionEvents = false;

    // If true the storage manager will observe physical asset description events and receive callback to handle the storage
    private boolean observePhysicalAssetDescriptionEvents = false;

    // If true the storage manager will observe digital action events and receive callback to handle the storage
    private boolean observerDigitalActionEvents = false;

    // If true the storage manager will observe life cycle events and receive callback to handle the storage
    private boolean observeLifeCycleEvents = false;

    // If true the storage manager will observe augmentation function events and receive callback to handle the storage
    private boolean observeAugmentationFunctionEvents = false;

    // Id of the storage instance
    private String storageId = null;

    /** Metadata key identifying this storage instance in monitoring metrics. */
    public static final String METRIC_METADATA_STORAGE_ID_KEY = "storage_id";

    private String digitalTwinId;
    private MonitoringInterface monitoringInterface;
    private String metricsNamespace;
    private String metricsInstanceId;

    /**
     * Default Constructor
     */
    protected WldtStorage(String storageId) {
        this.storageId = storageId;
    }

    /**
     * Constructor with the possibility to enable/disable the observation of all events
     * @param storageId the id of the storage instance
     * @param observeAll if true the storage manager will observe all events and receive callback to handle the storage
     */
    protected WldtStorage(String storageId, boolean observeAll){
        this(storageId);
        this.observeStateEvents = observeAll;
        this.observerPhysicalAssetEvents = observeAll;
        this.observerPhysicalAssetActionEvents = observeAll;
        this.observePhysicalAssetDescriptionEvents = observeAll;
        this.observerDigitalActionEvents = observeAll;
        this.observeLifeCycleEvents = observeAll;
        this.observeAugmentationFunctionEvents = observeAll;
    }

    /**
     * Constructor with the possibility to enable/disable the observation of specific events
     * @param storageId the id of the storage instance
     * @param observeStateEvents if true the storage manager will observe state events and receive callback to handle the storage
     * @param observerPhysicalAssetEvents if true the storage manager will observe physical asset events and receive callback to handle the storage
     * @param observerPhysicalAssetActionEvents if true the storage manager will observe physical asset action events and receive callback to handle the storage
     * @param observePhysicalAssetDescriptionEvents if true the storage manager will observe physical asset description events and receive callback to handle the storage
     * @param observerDigitalActionEvents if true the storage manager will observe digital action events and receive callback to handle the storage
     * @param observeLifeCycleEvents if true the storage manager will observe life cycle events and receive callback to handle the storage
     * @param observeAugmentationFunctionEvents if true the storage manager will observe augmentation function events and receive callback to handle the storage
     */
    protected WldtStorage(String storageId,
                       boolean observeStateEvents,
                       boolean observerPhysicalAssetEvents,
                       boolean observerPhysicalAssetActionEvents,
                       boolean observePhysicalAssetDescriptionEvents,
                       boolean observerDigitalActionEvents,
                       boolean observeLifeCycleEvents,
                       boolean observeAugmentationFunctionEvents){
        this(storageId);

        this.observeStateEvents = observeStateEvents;
        this.observerPhysicalAssetEvents = observerPhysicalAssetEvents;
        this.observerPhysicalAssetActionEvents = observerPhysicalAssetActionEvents;
        this.observePhysicalAssetDescriptionEvents = observePhysicalAssetDescriptionEvents;
        this.observerDigitalActionEvents = observerDigitalActionEvents;
        this.observeLifeCycleEvents = observeLifeCycleEvents;
        this.observeAugmentationFunctionEvents = observeAugmentationFunctionEvents;
    }

    public boolean isObserveStateEvents() {
        return observeStateEvents;
    }

    public void setObserveStateEvents(boolean observeStateEvents) {
        this.observeStateEvents = observeStateEvents;
    }

    public boolean isObserverPhysicalAssetEvents() {
        return observerPhysicalAssetEvents;
    }

    public void setObserverPhysicalAssetEvents(boolean observerPhysicalAssetEvents) {
        this.observerPhysicalAssetEvents = observerPhysicalAssetEvents;
    }

    public boolean isObserverPhysicalAssetActionEvents() {
        return observerPhysicalAssetActionEvents;
    }

    public void setObserverPhysicalAssetActionEvents(boolean observerPhysicalAssetActionEvents) {
        this.observerPhysicalAssetActionEvents = observerPhysicalAssetActionEvents;
    }

    public boolean isObservePhysicalAssetDescriptionEvents() {
        return observePhysicalAssetDescriptionEvents;
    }

    public void setObservePhysicalAssetDescriptionEvents(boolean observePhysicalAssetDescriptionEvents) {
        this.observePhysicalAssetDescriptionEvents = observePhysicalAssetDescriptionEvents;
    }

    public boolean isObserverDigitalActionEvents() {
        return observerDigitalActionEvents;
    }

    public void setObserverDigitalActionEvents(boolean observerDigitalActionEvents) {
        this.observerDigitalActionEvents = observerDigitalActionEvents;
    }

    public boolean isObserveLifeCycleEvents() {
        return observeLifeCycleEvents;
    }

    public void setObserveLifeCycleEvents(boolean observeLifeCycleEvents) {
        this.observeLifeCycleEvents = observeLifeCycleEvents;
    }

    public boolean isObserveAugmentationFunctionEvents() {return observeAugmentationFunctionEvents;}

    public void setObserveAugmentationFunctionEvents(boolean observeAugmentationFunctionEvents) {
        this.observeAugmentationFunctionEvents = observeAugmentationFunctionEvents;
    }

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    /**
     * Sets the monitoring interface for this storage instance.
     * Called by StorageManager when monitoring is configured or when this storage is added.
     */
    public void setMonitoringInterface(String digitalTwinId, MonitoringInterface monitoringInterface) {
        if (monitoringInterface != null) {
            this.digitalTwinId = digitalTwinId;
            this.monitoringInterface = monitoringInterface;
            handleMetricsRegistration();
        }
    }

    private void handleMetricsRegistration() {
        if (this.monitoringInterface == null || !this.monitoringInterface.isActive(WldtMetricComponent.STORAGE)) return;
        if (this.metricsNamespace != null) return;
        this.metricsNamespace = CoreMonitoringUtils.buildCoreNamespace();
        this.metricsInstanceId = getStorageId();
        final Map<String, Object> metadata = new HashMap<String, Object>() {{
            put(METRIC_METADATA_STORAGE_ID_KEY, getStorageId());
        }};
        this.monitoringInterface.registerMetric(new WldtCounter(digitalTwinId, metricsNamespace, CoreMonitoringUtils.STORAGE_WRITE_DIGITAL_TWIN_STATE_SUCCESS_COUNT, WldtMetricComponent.STORAGE, metadata).withInstanceId(metricsInstanceId));
        this.monitoringInterface.registerMetric(new WldtCounter(digitalTwinId, metricsNamespace, CoreMonitoringUtils.STORAGE_WRITE_DIGITAL_TWIN_STATE_ERROR_COUNT, WldtMetricComponent.STORAGE, metadata).withInstanceId(metricsInstanceId));
        this.monitoringInterface.registerMetric(new WldtTimer(digitalTwinId, metricsNamespace, CoreMonitoringUtils.STORAGE_WRITE_DIGITAL_TWIN_STATE_EXEC_TIME, WldtMetricComponent.STORAGE, metadata).withInstanceId(metricsInstanceId));
        this.monitoringInterface.registerMetric(new WldtCounter(digitalTwinId, metricsNamespace, CoreMonitoringUtils.STORAGE_WRITE_DIGITAL_EVENT_SUCCESS_COUNT, WldtMetricComponent.STORAGE, metadata).withInstanceId(metricsInstanceId));
        this.monitoringInterface.registerMetric(new WldtCounter(digitalTwinId, metricsNamespace, CoreMonitoringUtils.STORAGE_WRITE_DIGITAL_EVENT_ERROR_COUNT, WldtMetricComponent.STORAGE, metadata).withInstanceId(metricsInstanceId));
        this.monitoringInterface.registerMetric(new WldtTimer(digitalTwinId, metricsNamespace, CoreMonitoringUtils.STORAGE_WRITE_DIGITAL_EVENT_EXEC_TIME, WldtMetricComponent.STORAGE, metadata).withInstanceId(metricsInstanceId));
        this.monitoringInterface.registerMetric(new WldtCounter(digitalTwinId, metricsNamespace, CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_EVENT_SUCCESS_COUNT, WldtMetricComponent.STORAGE, metadata).withInstanceId(metricsInstanceId));
        this.monitoringInterface.registerMetric(new WldtCounter(digitalTwinId, metricsNamespace, CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_EVENT_ERROR_COUNT, WldtMetricComponent.STORAGE, metadata).withInstanceId(metricsInstanceId));
        this.monitoringInterface.registerMetric(new WldtTimer(digitalTwinId, metricsNamespace, CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_EVENT_EXEC_TIME, WldtMetricComponent.STORAGE, metadata).withInstanceId(metricsInstanceId));
        this.monitoringInterface.registerMetric(new WldtCounter(digitalTwinId, metricsNamespace, CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_ASSET_DESCRIPTION_SUCCESS_COUNT, WldtMetricComponent.STORAGE, metadata).withInstanceId(metricsInstanceId));
        this.monitoringInterface.registerMetric(new WldtCounter(digitalTwinId, metricsNamespace, CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_ASSET_DESCRIPTION_ERROR_COUNT, WldtMetricComponent.STORAGE, metadata).withInstanceId(metricsInstanceId));
        this.monitoringInterface.registerMetric(new WldtTimer(digitalTwinId, metricsNamespace, CoreMonitoringUtils.STORAGE_WRITE_PHYSICAL_ASSET_DESCRIPTION_EXEC_TIME, WldtMetricComponent.STORAGE, metadata).withInstanceId(metricsInstanceId));
        this.monitoringInterface.registerMetric(new WldtCounter(digitalTwinId, metricsNamespace, CoreMonitoringUtils.STORAGE_WRITE_LIFECYCLE_EVENT_SUCCESS_COUNT, WldtMetricComponent.STORAGE, metadata).withInstanceId(metricsInstanceId));
        this.monitoringInterface.registerMetric(new WldtCounter(digitalTwinId, metricsNamespace, CoreMonitoringUtils.STORAGE_WRITE_LIFECYCLE_EVENT_ERROR_COUNT, WldtMetricComponent.STORAGE, metadata).withInstanceId(metricsInstanceId));
        this.monitoringInterface.registerMetric(new WldtTimer(digitalTwinId, metricsNamespace, CoreMonitoringUtils.STORAGE_WRITE_LIFECYCLE_EVENT_EXEC_TIME, WldtMetricComponent.STORAGE, metadata).withInstanceId(metricsInstanceId));
        this.monitoringInterface.registerMetric(new WldtCounter(digitalTwinId, metricsNamespace, CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_SUCCESS_COUNT, WldtMetricComponent.STORAGE, metadata).withInstanceId(metricsInstanceId));
        this.monitoringInterface.registerMetric(new WldtCounter(digitalTwinId, metricsNamespace, CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_ERROR_COUNT, WldtMetricComponent.STORAGE, metadata).withInstanceId(metricsInstanceId));
        this.monitoringInterface.registerMetric(new WldtTimer(digitalTwinId, metricsNamespace, CoreMonitoringUtils.STORAGE_WRITE_AUGMENTATION_FUNCTION_EXEC_TIME, WldtMetricComponent.STORAGE, metadata).withInstanceId(metricsInstanceId));
    }

    void notifyWriteSuccess(String successMetric, String timeMetric, long startMs) {
        if (monitoringInterface != null && monitoringInterface.isActive(WldtMetricComponent.STORAGE)) {
            monitoringInterface.increaseCounter(metricsNamespace, successMetric, metricsInstanceId);
            monitoringInterface.updateTimerSince(metricsNamespace, timeMetric, startMs, metricsInstanceId);
        }
    }

    void notifyWriteError(String errorMetric, String timeMetric, long startMs) {
        if (monitoringInterface != null && monitoringInterface.isActive(WldtMetricComponent.STORAGE)) {
            monitoringInterface.increaseCounter(metricsNamespace, errorMetric, metricsInstanceId);
            monitoringInterface.updateTimerSince(metricsNamespace, timeMetric, startMs, metricsInstanceId);
        }
    }

    /**
     * Save a new computed instance of the DT State in the Storage together with the list of the changes with respect
     * to the previous state
     * @param digitalTwinState associated to the new computed Digital State to be stored
     * @param digitalTwinStateChangeList the list of state changes associated to the new Digital State that has to be stored
     * @throws IllegalArgumentException  if digitalTwinState is null
     */
    public abstract void saveDigitalTwinState(DigitalTwinState digitalTwinState, List<DigitalTwinStateChange> digitalTwinStateChangeList) throws StorageException, IllegalArgumentException;

    /**
     * Save the Digital Twin State Event Notification
     * @param digitalTwinStateEventNotification the Digital Twin State Event Notification to be saved
     */
    public abstract void saveDigitalTwinStateEventNotification(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) throws StorageException;

    /**
     * Save the LifeCycleState of the Digital Twin
     * @param lifeCycleStateVariation the LifeCycleStateVariation to be saved
     */
    public abstract void saveLifeCycleState(LifeCycleStateVariation lifeCycleStateVariation) throws StorageException;

    /**
     * Save the Physical Asset Event Notification
     * @param physicalAssetEventNotification the Physical Asset Event Notification to be saved
     */
    public abstract void savePhysicalAssetEventNotification(PhysicalAssetEventNotification physicalAssetEventNotification) throws StorageException;

    /**
     * Save Physical Asset Action Request
     * @param physicalAssetActionRequest the Physical Asset Action Request to be saved
     */
    public abstract void savePhysicalAssetActionRequest(PhysicalAssetActionRequest physicalAssetActionRequest) throws StorageException;

    /**
     * Save a Digital Action Request
     * @param digitalActionRequest the Digital Action Request to be saved
     */
    public abstract void saveDigitalActionRequest(DigitalActionRequest digitalActionRequest) throws StorageException;

    /**
     * Save a new Physical Asset Description Available
     * @param physicalAssetDescriptionNotification the Physical Asset Description Notification to be saved
     */
    public abstract void saveNewPhysicalAssetDescriptionNotification(PhysicalAssetDescriptionNotification physicalAssetDescriptionNotification) throws StorageException;

    /**
     * Save the updated Physical Asset Description
     */
    public abstract void saveUpdatedPhysicalAssetDescriptionNotification(PhysicalAssetDescriptionNotification physicalAssetDescriptionNotification) throws StorageException;

    /**
     * Save the Physical Asset Property Variation
     * @param physicalAssetPropertyVariation the Physical Asset Property Variation to be saved
     */
    public abstract void savePhysicalAssetPropertyVariation(PhysicalAssetPropertyVariation physicalAssetPropertyVariation) throws StorageException;

    /**
     * Save the Physical Asset Relationship Instance Created Event
     * @param physicalRelationshipInstanceVariation the Physical Relationship Instance Variation to be saved
     */
    public abstract void savePhysicalAssetRelationshipInstanceCreatedNotification(PhysicalRelationshipInstanceVariation physicalRelationshipInstanceVariation) throws StorageException;

    /**
     * Save the Physical Asset Relationship Instance Updated Event
     * @param physicalRelationshipInstanceVariation the Physical Relationship Instance Variation to be saved
     */
    public abstract void savePhysicalAssetRelationshipInstanceDeletedNotification(PhysicalRelationshipInstanceVariation physicalRelationshipInstanceVariation) throws StorageException;

    /**
     * Save the Augmentation Function Error
     * @param augmentationFunctionHandlerId the id of the handler of the Augmentation Function associated to the error
     * @param augmentationFunctionId the id of the Augmentation Function associated to the error
     * @param augmentationFunctionError the Augmentation Function Error to be saved
     * @throws StorageException if any error occurs during the storage of the Augmentation Function Error
     */
    public abstract void saveAugmentationFunctionError(String augmentationFunctionId, String augmentationFunctionHandlerId, AugmentationFunctionError augmentationFunctionError) throws StorageException;

    /**
     * Save the Augmentation Function Request
     * @param augmentationFunctionId the id of the Augmentation Function associated to the request
     * @param augmentationFunctionHandlerId the id of the handler of the Augmentation Function associated to the request
     * @param augmentationFunctionRequest the Augmentation Function Request to be saved
     * @throws StorageException if any error occurs during the storage of the Augmentation Function Request
     */
    public abstract void saveAugmentationFunctionRequest(String augmentationFunctionId, String augmentationFunctionHandlerId, AugmentationFunctionRequest augmentationFunctionRequest) throws StorageException;

    /**
     * Save the Augmentation Function Result
     * @param augmentationFunctionId the id of the Augmentation Function associated to the result
     * @param augmentationFunctionHandlerId the id of the handler of the Augmentation Function associated to the result
     * @param augmentationFunctionResult the Augmentation Function Result to be saved
     * @throws StorageException if any error occurs during the storage of the Augmentation Function Result
     */
    public abstract void saveAugmentationFunctionResult(String augmentationFunctionId, String augmentationFunctionHandlerId, AugmentationFunctionResult<?> augmentationFunctionResult) throws StorageException;

    /**
     * Save the Augmentation Function Registration
     * @param augmentationFunctionId the id of the Augmentation Function associated to the registration
     * @param augmentationFunctionHandlerId the id of the handler of the Augmentation Function associated to the registration
     * @param augmentationFunctionType the type of the Augmentation Function associated to the registration
     * @throws StorageException if any error occurs during the storage of the Augmentation Function Registration
     */
    public abstract void saveAugmentationFunctionRegistration(String augmentationFunctionId, String augmentationFunctionHandlerId, AugmentationFunctionType augmentationFunctionType) throws StorageException;

    /**
     * Save the Augmentation Function Unregistration
     * @param augmentationFunctionId the id of the Augmentation Function associated to the unregistration
     * @param augmentationFunctionHandlerId the id of the handler of the Augmentation Function associated to the unregistration
     * @param augmentationFunctionType the type of the Augmentation Function associated to the unregistration
     * @throws StorageException if any error occurs during the storage of the Augmentation Function Unregistration
     */
    public abstract void saveAugmentationFunctionUnregistration(String augmentationFunctionId, String augmentationFunctionHandlerId, AugmentationFunctionType augmentationFunctionType) throws StorageException;

    /**
     * Single entry point to read data from the storage. The backend must execute the query natively
     * (translating filter, sort, offset/limit and aggregation to its own query language) and return record classes:
     * the records of the queried resource for a plain fetch, {@link it.wldt.storage.model.AggregationRecord} items
     * for an aggregation, a single {@link StorageStats} item for {@link it.wldt.storage.query.QueryResourceType#STORAGE_STATS}.
     * Storages without a query engine can delegate to {@link it.wldt.storage.query.inmemory.InMemoryQueryEvaluator}.
     * Query failures must be reported through {@code QueryResult.failure(...)}, not thrown.
     * <p>
     * Threading: this method is called on the single event thread of the {@code StorageManager}, serialized with all
     * the {@code save*} calls, so a slow or blocking implementation delays the storing of the new data. A storage that
     * needs concurrent or asynchronous reads has to manage its own threading internally (read replica, own executor, ...).
     *
     * @param query the query to execute
     * @return the query result
     * @throws StorageException if an unexpected error occurs while accessing the storage
     */
    public abstract QueryResult<?> execute(Query query) throws StorageException;

    /**
     * Retrieve and returns storage statistics in terms of the number of stored records for each type and the
     * associated time range of the stored records (start and end timestamp)
     * @return the storage statistics
     * @throws StorageException if an error occurs while retrieving the storage statistics
     */
    public abstract StorageStats getStorageStats() throws StorageException;

    /**
     * Initialize the WLDT Storage
     */
    protected abstract void init() throws StorageException;

    /**
     * Clear all the store information in the WLDT Storage
     */
    protected abstract void clear() throws StorageException;

}
