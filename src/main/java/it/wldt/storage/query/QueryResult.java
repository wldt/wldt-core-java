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

import java.util.List;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 25/07/2024
 * This class represents the Query Result returned by the Query Executor
 * containing the query results and the query status (successful or not) and error message (if any)
 * together with also the original request.
 */
public class QueryResult<T> {

    private QueryRequest originalRequest;

    private boolean isSuccessful;

    private String errorMessage;

    private List<T> results;

    private int totalResults;

    public QueryResult() {
    }

    /**
     * Constructor used to create a Query Result with the query status and error message
     * @param originalRequest the original request
     * @param isSuccessful the query status
     * @param errorMessage the error message (if any)
     */
    public QueryResult(QueryRequest originalRequest, boolean isSuccessful, String errorMessage) {
        this.originalRequest = originalRequest;
        this.isSuccessful = isSuccessful;
        this.errorMessage = errorMessage;
    }

    /**
     * Constructor used to create a Query Result with the query status, error message, results and total results
     * @param originalRequest the original request
     * @param isSuccessful the query status
     * @param errorMessage the error message (if any)
     * @param results the query results
     * @param totalResults the total number of results
     */
    public QueryResult(QueryRequest originalRequest, boolean isSuccessful, String errorMessage, List<T> results, int totalResults) {
        this.originalRequest = originalRequest;
        this.isSuccessful = isSuccessful;
        this.errorMessage = errorMessage;
        this.results = results;
        this.totalResults = totalResults;
    }

    /**
     * Method used to check if the query was successful
     * @return true if the query was successful, false otherwise
     */
    public boolean isSuccessful() {
        return isSuccessful;
    }

    /**
     * Method used to set the query status
     * @param successful the query status
     */
    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }

    /**
     * Method used to get the error message
     * @return the error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Method used to set the error message
     * @param errorMessage the error message
     */
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * Method used to get the query results
     * @return the query results
     */
    public List<T> getResults() {
        return results;
    }

    /**
     * Method used to set the query results
     * @param results the query results
     */
    public void setResults(List<T> results) {
        this.results = results;
    }

    /**
     * Method used to get the total number of results
     * @return the total number of results
     */
    public int getTotalResults() {
        return totalResults;
    }

    /**
     * Method used to set the total number of results
     * @param totalResults the total number of results
     */
    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    /**
     * Method used to get the original request
     * @return the original request
     */
    public QueryRequest getOriginalRequest() {
        return originalRequest;
    }

    /**
     * Method used to set the original request
     * @param originalRequest the original request
     */
    public void setOriginalRequest(QueryRequest originalRequest) {
        this.originalRequest = originalRequest;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("QueryResult{");
        sb.append("originalRequest=").append(originalRequest);
        sb.append(", isSuccessful=").append(isSuccessful);
        sb.append(", errorMessage='").append(errorMessage).append('\'');
        sb.append(", results=").append(results);
        sb.append(", totalResults=").append(totalResults);
        sb.append('}');
        return sb.toString();
    }
}
