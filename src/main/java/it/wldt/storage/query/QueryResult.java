package it.wldt.storage.query;

import java.util.List;

public class QueryResult<T> {

    private QueryRequest originalRequest;

    private boolean isSuccessful;

    private String errorMessage;

    private List<T> results;

    private int totalResults;

    public QueryResult() {
    }

    public QueryResult(QueryRequest originalRequest, boolean isSuccessful, String errorMessage) {
        this.originalRequest = originalRequest;
        this.isSuccessful = isSuccessful;
        this.errorMessage = errorMessage;
    }

    public QueryResult(QueryRequest originalRequest, boolean isSuccessful, String errorMessage, List<T> results, int totalResults) {
        this.originalRequest = originalRequest;
        this.isSuccessful = isSuccessful;
        this.errorMessage = errorMessage;
        this.results = results;
        this.totalResults = totalResults;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public List<T> getResults() {
        return results;
    }

    public void setResults(List<T> results) {
        this.results = results;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public QueryRequest getOriginalRequest() {
        return originalRequest;
    }

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
