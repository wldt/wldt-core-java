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
package it.wldt.storage.query;

import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import it.wldt.monitoring.CoreMonitoringUtils;
import it.wldt.monitoring.MonitoringInterface;
import it.wldt.monitoring.metrics.WldtCounter;
import it.wldt.monitoring.metrics.WldtMetricComponent;
import it.wldt.monitoring.metrics.WldtTimer;
import it.wldt.storage.WldtStorage;
import java.util.Map;
import java.util.Optional;


/**
 * Authors:
 *      Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 25/07/2024
 * This class represents the Query Manager responsible to handle the query request and manage the query execution.
 * It selects the target storage and delegates the execution of the {@link Query} to it
 * (see {@link WldtStorage#execute(Query)}).
 * This class is designed to be extended by the user to implement a different storage selection logic.
 */
public class QueryManager {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(QueryManager.class);

    protected String digitalTwinId;
    protected MonitoringInterface monitoringInterface;
    protected String metricsNamespace;

    public QueryManager() {}

    public QueryManager(String digitalTwinId) {
        this.digitalTwinId = digitalTwinId;
    }

    public void setMonitoringInterface(MonitoringInterface monitoringInterface) {
        if (monitoringInterface != null) {
            this.monitoringInterface = monitoringInterface;
            handleMetricsRegistration();
        }
    }

    private void handleMetricsRegistration() {
        if (this.monitoringInterface == null || !this.monitoringInterface.isActive(WldtMetricComponent.STORAGE)) return;
        if (this.metricsNamespace != null) return;
        this.metricsNamespace = CoreMonitoringUtils.buildCoreNamespace();
        this.monitoringInterface.registerMetric(new WldtCounter(this.digitalTwinId, this.metricsNamespace, CoreMonitoringUtils.STORAGE_QUERY_SUCCESS_COUNT, WldtMetricComponent.STORAGE));
        this.monitoringInterface.registerMetric(new WldtCounter(this.digitalTwinId, this.metricsNamespace, CoreMonitoringUtils.STORAGE_QUERY_ERROR_COUNT, WldtMetricComponent.STORAGE));
        this.monitoringInterface.registerMetric(new WldtTimer(this.digitalTwinId, this.metricsNamespace, CoreMonitoringUtils.STORAGE_QUERY_EXEC_TIME, WldtMetricComponent.STORAGE));
    }

    /**
     * Default implementation of the method for the query manager.
     * The method has been designed to return the desired storage object from the storage map to be used for the query management.
     * In the default implementation, the method returns the first storage object available in the storage map.
     * @param query Query Object, that can be used to select different storage according to the query
     * @param storageMap Storage Map containing the storage objects
     * @return Desired Storage Object to be used for the query management
     */
    public Optional<WldtStorage> getTargetStorage(Query query, Map<String, WldtStorage> storageMap) {
        if(!storageMap.isEmpty()) {
            WldtStorage targetStorage = storageMap.entrySet().iterator().next().getValue();
            if(targetStorage != null)
                return Optional.of(targetStorage);
            else
                return Optional.empty();
        }
        else
            return Optional.empty();
    }

    /**
     * Handle a Query allowing its execution on the target storage selected from the storage map
     *
     * @param query Query Object
     * @param storageMap Storage Map containing the storage objects to be used for the query management
     * @return the query result, a failed result if the query cannot be executed
     */
    public QueryResult<?> handleQuery(Query query, Map<String, WldtStorage> storageMap) {

        try {

            logger.info("Handling Query: {}", query);
            Optional<WldtStorage> storageOptional = getTargetStorage(query, storageMap);

            if(!storageOptional.isPresent())
                return QueryResult.failure(query, "No Storage Available for the Query !");

            logger.info("Storage Available for the Query ! Storage Id: {}", storageOptional.get().getStorageId());
            return storageOptional.get().execute(query);

        } catch (Exception e) {
            return QueryResult.failure(query, e.getMessage());
        }
    }
}
