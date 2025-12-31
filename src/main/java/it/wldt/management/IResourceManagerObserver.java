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
package it.wldt.management;


/**
 * This interface defines a listener for resource management events
 * allowing for the observation of changes in the list of managed resources.
 *
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
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
     * @param resourceId the unique identifier of the updated resource
     */
    void onManagerResourceUpdated(String resourceId);

    /**
     * This method is called when the resource list is cleared.
     */
    void onManagerResourceListCleared();
}
