package it.wldt.storage.model;

/**
 * Author: Marco Picone (picone.m@gmail.com)
 * Date: 01/08/2024
 * Storage Record Class
 * Represents a single record in the storage with a unique id
 */
public class StorageRecord {

    // Storage Record Id
    private String id;

    /**
     * Default Constructor
     */
    public StorageRecord() {
    }

    /**
     * Constructor
     *
     * @param id Storage Record Id
     */
    public StorageRecord(String id) {
        this.id = id;
    }

    /**
     * Get the Storage Record Id
     * @return Storage Record Id
     */
    public String getId() {
        return id;
    }

    /**
     * Set the Storage Record Id
     * @param id Storage Record Id
     */
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "StorageRecord{" +
                "id='" + id + '\'' +
                '}';
    }
}
