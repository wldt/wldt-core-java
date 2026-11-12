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

/**
 * Field names usable in filters, sorting and aggregations. They match the property names of the record classes
 * in {@code it.wldt.storage.model}, so a backend can map them directly to columns / document fields.
 * Nested values can be addressed with a dotted path (e.g. {@code "variationMetadata.unit"}).
 * <p>
 * {@link #TIMESTAMP} is the storage time of the record and is available on every resource.
 */
public final class QueryFieldNames {

    private QueryFieldNames() {}

    /** Storage timestamp (epoch ms) of the record, available on all the resources */
    public static final String TIMESTAMP = "timestamp";

    /** Record id */
    public static final String ID = "id";

    // Physical asset / digital twin state records
    public static final String PROPERTY_KEY = "propertykey";
    public static final String EVENT_KEY = "eventkey";
    public static final String STATE_EVENT_KEY = "eventKey";
    public static final String ACTION_KEY = "actionkey";
    public static final String BODY = "body";
    public static final String REQUEST_BODY = "requestBody";
    public static final String METADATA = "metadata";
    public static final String VARIATION_METADATA = "variationMetadata";
    public static final String REQUEST_METADATA = "requestMetadata";
    public static final String ADAPTER_ID = "adapterId";
    public static final String LIFE_CYCLE_STATE = "lifeCycleState";
    public static final String INSTANCE_KEY = "instanceKey";
    public static final String RELATIONSHIP_NAME = "relationshipName";
    public static final String RELATIONSHIP_TYPE = "relationshipType";

    // Augmentation function records
    public static final String FUNCTION_ID = "augmentationFunctionId";
    public static final String FUNCTION_HANDLER_ID = "augmentationFunctionHandlerId";
    public static final String REQUEST_ID = "requestId";
    public static final String ERROR_ID = "errorId";
    public static final String ERROR_TYPE = "errorType";
    public static final String TYPE = "type";
    public static final String KEY = "key";
    public static final String VALUE = "value";
}
