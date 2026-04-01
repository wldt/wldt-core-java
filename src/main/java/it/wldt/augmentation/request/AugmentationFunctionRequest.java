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
package it.wldt.augmentation.request;

import it.wldt.augmentation.context.AugmentationFunctionContext;

/**
 * Class representing a request for an augmentation function. This class encapsulates the details of a request made to
 * execute an augmentation function, including the unique request ID, the context of the augmentation function, the
 * type of request, and the timestamp of when the request was made. The AugmentationFunctionRequest class serves as a
 * data structure to hold all relevant information about a specific request for an augmentation function, allowing for
 * better management and tracking of requests within the system.
 */
public class AugmentationFunctionRequest {

    /**
     * The unique identifier for the augmentation function request.
     */
    private final String requestId;

    /**
     * The context of the augmentation function request, containing all necessary information and parameters required
     * for the execution of the augmentation function.
     */
    private final AugmentationFunctionContext context;

    /**
     * The timestamp indicating when the augmentation function request was made, represented as a long value (milliseconds since epoch).
     */
    private final Long timestamp;

    /**
     * The type of the augmentation function request.
     */
    private final AugmentationFunctionRequestType type;

    /**
     * Constructor for the AugmentationFunctionRequest class, initializing all properties of the request.
     * @param requestId The unique identifier for the augmentation function request.
     * @param context The context of the augmentation function request, containing all necessary information and parameters required for the execution of the augmentation function.
     * @param requestType The type of the augmentation function request.
     * @param timestamp The timestamp indicating when the augmentation function request was made, represented as a long value (milliseconds since epoch).
     */
    public AugmentationFunctionRequest(String requestId, AugmentationFunctionContext context, AugmentationFunctionRequestType requestType, Long timestamp) {
        this.requestId = requestId;
        this.context = context;
        this.type = requestType;
        this.timestamp = timestamp;
    }

    /**
     * Constructor for the AugmentationFunctionRequest class, initializing the request with the current timestamp.
     * @param requestId The unique identifier for the augmentation function request.
     * @param context The context of the augmentation function request, containing all necessary information and parameters required for the execution of the augmentation function.
     * @param requestType The type of the augmentation function request.
     */
    public AugmentationFunctionRequest(String requestId, AugmentationFunctionContext context, AugmentationFunctionRequestType requestType) {
        this(requestId, context, requestType, System.currentTimeMillis());
    }

    /**
     * Gets the unique identifier for the augmentation function request.
     * @return The unique identifier for the augmentation function request.
     */
    public String getRequestId() {
        return requestId;
    }

    /**
     * Gets the context of the augmentation function request, containing all necessary information and parameters required for the execution of the augmentation function.
     * @return The context of the augmentation function request.
     */
    public AugmentationFunctionContext getContext() {
        return context;
    }

    /**
     * Gets the timestamp indicating when the augmentation function request was made, represented as a long value (milliseconds since epoch).
     * @return The timestamp of the augmentation function request.
     */
    public Long getTimestamp() {
        return timestamp;
    }

    /**
     * Gets the type of the augmentation function request.
     * @return The type of the augmentation function request.
     */
    public AugmentationFunctionRequestType getType() {
        return type;
    }

    @Override
    public String toString() {
        return "AugmentationFunctionRequest{" +
                "requestId='" + requestId + '\'' +
                ", context=" + context +
                ", timestamp=" + timestamp +
                ", type=" + type +
                '}';
    }
}
