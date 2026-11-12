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

import it.wldt.core.event.*;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 25/07/2024
 * This class represents the Query Executor used to execute queries on the storage system
 * supporting both synchronous and asynchronous query execution. Internally is implemented through
 * an event-based mechanism to handle the query and its result: a single long-lived subscription receives all the
 * query results and each one completes the pending request with the same request id.
 */
public class QueryExecutor implements WldtEventListener {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(QueryExecutor.class);

    /** Default time (ms) to wait for a query result */
    public static final long DEFAULT_QUERY_TIMEOUT_MS = 5000;

    // Shared scheduler used to fail the queries that do not receive a result in time
    private static final ScheduledExecutorService TIMEOUT_SCHEDULER = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "wldt-query-timeout");
        thread.setDaemon(true);
        return thread;
    });

    // Digital Twin Id
    private final String digitalTwinId;

    // Query Executor Id to identify the Query Executor on the Event Bus. A unique suffix is added because the event bus
    // keeps a single listener per subscriber id, and several executors can be created with the same configured id.
    private final String queryExecutorId;

    // Time (ms) to wait for a result when the query does not define its own timeout
    private final long defaultTimeoutMs;

    // Pending queries: request id -> future completed when the result arrives (or the timeout expires)
    private final Map<String, CompletableFuture<QueryResult<?>>> pendingQueries = new ConcurrentHashMap<>();

    private boolean subscribed = false;

    /**
     * Default Constructor
     * @param digitalTwinId Digital Twin Id
     * @param queryExecutorId Query Executor Id
     */
    public QueryExecutor(String digitalTwinId, String queryExecutorId) {
        this(digitalTwinId, queryExecutorId, DEFAULT_QUERY_TIMEOUT_MS);
    }

    /**
     * Constructor with a custom default timeout, useful when heavy queries are expected.
     * A query can still override it with {@link Query.Builder#timeout(long)}.
     * @param digitalTwinId Digital Twin Id
     * @param queryExecutorId Query Executor Id
     * @param defaultTimeoutMs time (ms) to wait for a query result, must be positive
     */
    public QueryExecutor(String digitalTwinId, String queryExecutorId, long defaultTimeoutMs) {
        if (defaultTimeoutMs <= 0)
            throw new IllegalArgumentException("Query timeout must be positive");
        this.defaultTimeoutMs = defaultTimeoutMs;
        this.digitalTwinId = digitalTwinId;
        this.queryExecutorId = queryExecutorId + "-" + UUID.randomUUID();
    }

    /**
     * Subscribe once to all the query result events
     */
    private synchronized void ensureSubscribed() throws Exception {
        if (subscribed)
            return;
        WldtEventFilter filter = new WldtEventFilter();
        filter.add(WldtEventTypes.ALL_STORAGE_QUERY_RESULT_EVENT_TYPE);
        WldtEventBus.getInstance().subscribe(this.digitalTwinId, this.queryExecutorId, filter, this);
        subscribed = true;
    }

    /**
     * Send the query and return the future that completes with its result. The future always completes normally:
     * errors and timeouts are reported as failed query results.
     */
    private CompletableFuture<QueryResult<?>> submit(Query originalQuery) {

        // Each execution gets its own request id, so the same query can be executed many times
        final Query query = originalQuery.refresh(defaultTimeoutMs);

        CompletableFuture<QueryResult<?>> future = new CompletableFuture<>();

        try {
            ensureSubscribed();

            pendingQueries.put(query.getRequestId(), future);

            ScheduledFuture<?> timeout = TIMEOUT_SCHEDULER.schedule(
                    () -> future.complete(QueryResult.failure(query, "Timeout waiting for the query result")),
                    query.getTimeoutMs(),
                    TimeUnit.MILLISECONDS);

            future.whenComplete((result, error) -> {
                pendingQueries.remove(query.getRequestId());
                timeout.cancel(false);
            });

            EventManager.publishStorageQueryRequest(this.digitalTwinId, this.queryExecutorId, query);

        } catch (Exception e) {
            logger.error("Error sending the Query ! Error: {}", e.getMessage());
            future.complete(QueryResult.failure(query, e.getMessage()));
        }

        return future;
    }

    /**
     * Synchronous Query Execution
     * @param query Query Object
     * @return Query Result Object containing the query result, a failed result in case of error or timeout
     */
    public QueryResult<?> syncQueryExecute(Query query) {
        return submit(query).join();
    }

    /**
     * Asynchronous Query Execution
     * @param query Query Object
     * @param queryResultListener Query Result Listener to be used to receive the query result, a failed result
     *                            is notified in case of error or timeout
     */
    public void asyncQueryExecute(Query query, IQueryResultListener queryResultListener) {
        submit(query).thenAccept(queryResultListener::onQueryResult);
    }

    @Override
    public void onEventSubscribed(String eventType) {
        logger.info("QueryExecutor - Correctly Subscribed to the Event Type: {}", eventType);
    }

    @Override
    public void onEventUnSubscribed(String eventType) {
        logger.info("QueryExecutor - Correctly UnSubscribed from the Event Type: {}", eventType);
    }

    @Override
    public void onEvent(WldtEvent<?> wldtEvent) {

        try {
            if (wldtEvent == null || !(wldtEvent.getBody() instanceof QueryResult<?>)) {
                logger.error("QueryExecutor - Error handling the Query Result Event ! The event body is not a QueryResult !");
                return;
            }

            QueryResult<?> queryResult = (QueryResult<?>) wldtEvent.getBody();

            // Results of queries sent by other executors are ignored
            CompletableFuture<QueryResult<?>> pending = pendingQueries.get(queryResult.getOriginalQuery().getRequestId());
            if (pending != null)
                pending.complete(queryResult);

        } catch (Exception e) {
            logger.error("QueryExecutor - Error handling the Query Result Event ! Error: {}", e.getMessage());
        }
    }
}
