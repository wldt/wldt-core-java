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
 * Date: 03/09/2024
 * Represents a Storage Stats Record containing information about the number of records stored in a specific time interval
 */
public class StorageStatsRecord {

    /**
     * Number of records stored in the specific time interval
     */
    private int recordCount;

    /**
     * Start timestamp of the record interval
     */
    private long recordStartTimestampMs;

    /**
     * End timestamp of the record interval
     */
    private long recordEndTimestampMs;

    /**
     * Default Constructor
     */
    public StorageStatsRecord() {
    }

    /**
     * Constructor
     * @param recordCount Number of records stored in the specific time interval
     * @param recordStartTimestampMs Start timestamp of the record interval
     * @param recordEndTimestampMs End timestamp of the record interval
     */
    public StorageStatsRecord(int recordCount, long recordStartTimestampMs, long recordEndTimestampMs) {
        this.recordCount = recordCount;
        this.recordStartTimestampMs = recordStartTimestampMs;
        this.recordEndTimestampMs = recordEndTimestampMs;
    }

    /**
     * Get the number of records stored in the specific time interval
     * @return Number of records stored in the specific time interval
     */
    public int getRecordCount() {
        return recordCount;
    }

    /**
     * Set the number of records stored in the specific time interval
     * @param recordCount Number of records stored in the specific time interval
     */
    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    /**
     * Get the start timestamp of the record interval
     * @return Start timestamp of the record interval
     */
    public long getRecordStartTimestampMs() {
        return recordStartTimestampMs;
    }

    /**
     * Set the start timestamp of the record interval
     * @param recordStartTimestampMs Start timestamp of the record interval
     */
    public void setRecordStartTimestampMs(long recordStartTimestampMs) {
        this.recordStartTimestampMs = recordStartTimestampMs;
    }

    /**
     * Get the end timestamp of the record interval
     * @return End timestamp of the record interval
     */
    public long getRecordEndTimestampMs() {
        return recordEndTimestampMs;
    }

    /**
     * Set the end timestamp of the record interval
     * @param recordEndTimestampMs End timestamp of the record interval
     */
    public void setRecordEndTimestampMs(long recordEndTimestampMs) {
        this.recordEndTimestampMs = recordEndTimestampMs;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("StorageStatsRecord{");
        sb.append("recordCount=").append(recordCount);
        sb.append(", recordStartTimestampMs=").append(recordStartTimestampMs);
        sb.append(", recordEndTimestampMs=").append(recordEndTimestampMs);
        sb.append('}');
        return sb.toString();
    }
}
