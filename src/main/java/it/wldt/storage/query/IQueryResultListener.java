package it.wldt.storage.query;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 25/07/2024
 * This interface represents the Query Result Listener used to receive the query results
 */
public interface IQueryResultListener {

    public void onQueryResult(QueryResult<?> queryResult);

}
