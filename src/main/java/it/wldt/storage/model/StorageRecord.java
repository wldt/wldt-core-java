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
