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
package it.wldt.storage.query;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 25/07/2024
 * This Enum represents the Query Request Type used to specify the type of query to be performed
 * on the storage system (e.g., Time Range Query, Sample Range Query, Last Value Query, Count Query)
 */
public enum QueryRequestType {

    TIME_RANGE("TIME_RANGE"),
    SAMPLE_RANGE("SAMPLE_RANGE"),
    LAST_VALUE("LAST_VALUE"),
    COUNT("COUNT");

    private String value;

    private QueryRequestType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
