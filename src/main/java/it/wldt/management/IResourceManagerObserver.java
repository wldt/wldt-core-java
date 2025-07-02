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
 * This interface defines a listener for resource management events
 * allowing for the observation of changes in the list of managed resources.
 *
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @created 23/06/2025 - 15:56
 */
public interface IResourceManagerObserver {

    /**
     * This method is called when a new resource is added to the management interface.
     *
     * @param resourceId the unique identifier of the added resource
     */
    void onManagerResourceAdded(String resourceId);

    /**
     * This method is called when a resource is removed from the management interface.
     *
     * @param resourceId the unique identifier of the removed resource
     */
    void onManagerResourceRemoved(String resourceId);

    /**
     * This method is called when a resource is updated on the resource manager
     */
    void onManagerResourceUpdated(String resourceId);

    /**
     * This method is called when the resource list is cleared.
     */
    void onManagerResourceListCleared();
}
