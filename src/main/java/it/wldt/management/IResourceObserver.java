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
package it.wldt.management;


/**
 *  This interface defines the methods for observing changes in resources that is managed by the
 *  Management Interface of the Digital Twin.
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 */
public interface IResourceObserver {

    /**
     * Called when a resource/subresource is created.
     *
     * @param resourceId the unique identifier of the created resource
     * @param subResourceId the unique identifier of the created subresource
     */
    public void onCreate(String resourceId, String subResourceId);

    /**
     * Called when a resource/subresource is updated.
     *
     * @param resourceId the unique identifier of the updated resource
     * @param subResourceId the unique identifier of the updated subresource
     */
    public void onUpdate(String resourceId, String subResourceId);

    /**
     * Called when a resource/subresource is deleted.
     *
     * @param resourceId the unique identifier of the deleted resource
     * @param subResourceId the unique identifier of the deleted subresource
     */
    public void onDelete(String resourceId, String subResourceId);

}
