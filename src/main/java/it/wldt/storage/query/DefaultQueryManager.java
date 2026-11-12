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

import it.wldt.monitoring.CoreMonitoringUtils;
import it.wldt.monitoring.metrics.WldtMetricComponent;
import it.wldt.storage.WldtStorage;
import java.util.Map;
import java.util.Optional;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 25/07/2024
 * Default Query Manager Class
 * This class is responsible to manage the query received by the Query Engine.
 * It extends the QueryManager class adding the monitoring metrics (STORAGE_QUERY_*) around the query execution.
 */
public class DefaultQueryManager extends QueryManager{

    public DefaultQueryManager() {
        super();
    }

    public DefaultQueryManager(String digitalTwinId) {
        super(digitalTwinId);
    }

    /**
     * The method has been designed to return the desired storage object from the storage map to be used for the query management.
     * Recall the default implementation of the method for the query manager.
     * In the default implementation, the method returns the first storage object available in the storage map.
     *
     * The behavior can be changed by overriding the method in the custom query manager implementation.
     * In the custom implementation, the method can be used to select different storage according to the query.
     *
     * @param query Query Object, that can be used to select different storage according to the query
     * @param storageMap   Storage Map containing the storage objects
     * @return Desired Storage Object to be used for the query management
     */
    @Override
    public Optional<WldtStorage> getTargetStorage(Query query, Map<String, WldtStorage> storageMap) {
        return super.getTargetStorage(query, storageMap);
    }

    /**
     * Override of handleQuery to add monitoring metrics (STORAGE_QUERY_*).
     * Delegates actual execution to the parent implementation.
     */
    @Override
    public QueryResult<?> handleQuery(Query query, Map<String, WldtStorage> storageMap) {
        if (monitoringInterface == null || !monitoringInterface.isActive(WldtMetricComponent.STORAGE))
            return super.handleQuery(query, storageMap);
        long startMs = System.currentTimeMillis();
        QueryResult<?> result = super.handleQuery(query, storageMap);
        monitoringInterface.updateTimerSince(metricsNamespace, CoreMonitoringUtils.STORAGE_QUERY_EXEC_TIME, startMs);
        if (result != null && result.isSuccessful())
            monitoringInterface.increaseCounter(metricsNamespace, CoreMonitoringUtils.STORAGE_QUERY_SUCCESS_COUNT);
        else
            monitoringInterface.increaseCounter(metricsNamespace, CoreMonitoringUtils.STORAGE_QUERY_ERROR_COUNT);
        return result;
    }
}
