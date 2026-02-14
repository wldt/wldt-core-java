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
package it.wldt.augmentation;

import it.wldt.core.state.DigitalTwinState;
import it.wldt.storage.query.QueryRequest;
import it.wldt.storage.query.QueryResult;

public class AugmentationFunctionContext {

    /**
     * The Digital Twin State at the time of the augmentation function execution.
     */
    private DigitalTwinState digitalTwinState;

    /**
     * The results of the query on the Digital Twin's storage associated to the {@link QueryRequest}
     * defined in the {@link AugmentationFunctionContextRequest} of the {@link AugmentationFunction}
     */
    private QueryResult<?> queryResult;

    /**
     * Default constructor for AugmentationFunctionContext
     */
    public AugmentationFunctionContext() {
    }

    /**
     * Constructor for AugmentationFunctionContext with both Digital Twin State and Query Result.
     * @param digitalTwinState The Digital Twin State at the time of the augmentation function execution.
     * @param queryResult The results of the query on the Digital Twin's storage associated to the {@link QueryRequest}
     * defined in the {@link AugmentationFunctionContextRequest} of the {@link AugmentationFunction}
     */
    public AugmentationFunctionContext(DigitalTwinState digitalTwinState, QueryResult<?> queryResult) {
        this.digitalTwinState = digitalTwinState;
        this.queryResult = queryResult;
    }

    /**
     * Constructor for AugmentationFunctionContext only with Digital Twin State, for cases where no query result is needed.
     * @param digitalTwinState The Digital Twin State at the time of the augmentation function execution.
     */
    public AugmentationFunctionContext(DigitalTwinState digitalTwinState) {
        this(digitalTwinState, null);
    }

    /**
     * Gets the Digital Twin State at the time of the augmentation function execution.
     * @return The Digital Twin State at the time of the augmentation function execution.
     */
    public DigitalTwinState getDigitalTwinState() {
        return digitalTwinState;
    }

    /**
     * Get the results of the query on the Digital Twin's storage associated to the {@link QueryRequest}
     * defined in the {@link AugmentationFunctionContextRequest} of the {@link AugmentationFunction}
     * @return The results of the query on the Digital Twin's storage
     */
    public QueryResult<?> getQueryResult() {
        return queryResult;
    }

    public void setDigitalTwinState(DigitalTwinState digitalTwinState) {
        this.digitalTwinState = digitalTwinState;
    }

    public void setQueryResult(QueryResult<?> queryResult) {
        this.queryResult = queryResult;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("AugmentationFunctionContext{");
        sb.append("digitalTwinState=").append(digitalTwinState);
        sb.append(", queryResult=").append(queryResult);
        sb.append('}');
        return sb.toString();
    }
}
