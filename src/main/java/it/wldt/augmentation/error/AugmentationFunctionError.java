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
package it.wldt.augmentation.error;

import java.util.HashMap;
import java.util.UUID;

/**
 * This class represents an error that occurred during the execution of an Augmentation Function.
 * It contains information about the error, including a unique identifier, timestamp, error type, message, and any relevant metadata.
 * The error type can be used to categorize the error and provide more information about the nature of the error.
 * The metadata can include any relevant information that can help with understanding the error and finding a solution.
 * This class can be used to log errors, report errors to a monitoring system, or to provide feedback to the user about the error that occurred.
 * It can also be used to trace back the error to the specific request that was being processed when the error occurred,
 * by including the identifier of the augmentation function request in the error object.
 */
public class AugmentationFunctionError {

    /**
     * Unique identifier for the error, can be generated using UUID or any other method to ensure uniqueness.
     */
    private final String errorId;

    /**
     * Timestamp of when the error occurred, can be generated using System.currentTimeMillis() or any other method to
     * get the current time in milliseconds.
     */
    private final Long timestamp;

    /**
     * Identifier of the augmentation function request that was being processed when the error occurred, this can be
     * used to trace back the error to the specific request that was being processed when the error occurred.
     */
    private String augmentationFunctionRequestId;

    /**
     * Type of the error, this can be used to categorize the error and to provide more information about the nature
     * of the error. The error type can be defined as an enum or as a string, depending on the specific use case
     * and requirements.
     */
    private final AugmentationFunctionErrorType errorType;

    /**
     * Message describing the error, this can be used to provide more information about the error and to help with
     * debugging and troubleshooting. The message can be generated based on the specific error and can include details
     * such as the error type, the function that caused the error, and any other relevant information.
     */
    private final String message;

    /**
     * Metadata related to the error, this can be used to provide additional information about the error and to help
     * with debugging and troubleshooting. The metadata can include any relevant information such as the input
     * parameters of the function, the state of the system when the error occurred, and any other relevant information
     * that can help with understanding the error and finding a solution.
     */
    private HashMap<String, Object> metadata;

    /**
     * Constructor of the AugmentationFunctionError class with all the parameters.
     * @param errorType the type of the error
     * @param message the message describing the error
     */
    public AugmentationFunctionError(AugmentationFunctionErrorType errorType, String message) {
        this(errorType, message, System.currentTimeMillis());
    }

    /**
     * Constructor of the AugmentationFunctionError class with all the parameters.
     * @param errorType the type of the error
     * @param message the message describing the error
     * @param timestamp the timestamp of when the error occurred
     */
    public AugmentationFunctionError(AugmentationFunctionErrorType errorType, String message, Long timestamp) {
        this(errorType, message, System.currentTimeMillis(), new HashMap<>());

    }

    /**
     * Constructor of the AugmentationFunctionError class with all the parameters.
     * @param errorType the type of the error
     * @param message the message describing the error
     * @param timestamp the timestamp of when the error occurred
     * @param metadata the metadata related to the error
     */
    public AugmentationFunctionError(AugmentationFunctionErrorType errorType, String message, Long timestamp, HashMap<String, Object> metadata) {
        this.errorId = UUID.randomUUID().toString();
        this.errorType = errorType;
        this.message = message;
        this.timestamp = timestamp;
        this.metadata = metadata;
    }

    /**
     * Getters and Setters for the class attributes
     * @return the unique identifier for the error
     */
    public String getErrorId() {
        return errorId;
    }

    /**
     * Getters and Setters for the class attributes
     * @return the timestamp of when the error occurred
     */
    public Long getTimestamp() {
        return timestamp;
    }

    /**
     * Getters and Setters for the class attributes
     * @return the identifier of the augmentation function request that was being processed when the error occurred
     */
    public String getAugmentationFunctionRequestId() {
        return augmentationFunctionRequestId;
    }

    /**
     * Getters and Setters for the class attributes
     * @return the type of the error
     */
    public AugmentationFunctionErrorType getErrorType() {
        return errorType;
    }

    /**
     * Getters and Setters for the class attributes
     * @return the message describing the error
     */
    public String getMessage() {
        return message;
    }

    /**
     * Getters and Setters for the class attributes
     * @return the metadata related to the error
     */
    public HashMap<String, Object> getMetadata() {
        return metadata;
    }

    /**
     * Setters for the class attributes
     * @param augmentationFunctionRequestId the identifier of the augmentation function request that was being
     *                                      processed when the error occurred
     */
    public void setAugmentationFunctionRequestId(String augmentationFunctionRequestId) {
        this.augmentationFunctionRequestId = augmentationFunctionRequestId;
    }

    /**
     * Setters for the class attributes
     * @param metadata the metadata related to the error
     */
    public void setMetadata(HashMap<String, Object> metadata) {
        this.metadata = metadata;
    }

    @Override
    public String toString() {
        return "AugmentationFunctionError{" +
                "errorId='" + errorId + '\'' +
                ", timestamp=" + timestamp +
                ", augmentationFunctionRequestId='" + augmentationFunctionRequestId + '\'' +
                ", errorType=" + errorType +
                ", message='" + message + '\'' +
                ", metadata=" + metadata +
                '}';
    }
}
