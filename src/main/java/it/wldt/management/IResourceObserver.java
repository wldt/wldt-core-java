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
