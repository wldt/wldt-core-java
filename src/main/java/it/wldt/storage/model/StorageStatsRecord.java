/*
 * Copyright [2025] [Marco Picone, Ph.D. - picone.m@gmail.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Marco Picone <picone.m@gmail.com> - https://www.marcopicone.net/
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
