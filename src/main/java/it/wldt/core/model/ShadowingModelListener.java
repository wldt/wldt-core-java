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
package it.wldt.core.model;

import it.wldt.core.state.DigitalTwinState;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 * Listener interface for observing Digital Twin shadowing process synchronization state changes.
 * <p>
 * Notifies observers when the Digital Twin transitions between synchronized and out-of-sync states
 * during the shadowing process (replication and digitalization). Components can implement this
 * interface to react to synchronization status changes between the Digital Twin State and the
 * physical asset.
 *
 * State Transitions
 * <ul>
 *   <li><b>Synchronized</b>: Digital Twin State accurately reflects the physical asset state.
 *       All Physical Adapters are bound and actively providing data.</li>
 *   <li><b>Out-of-Sync</b>: Digital Twin State does not reflect the physical asset state due to
 *       Physical Adapter disconnection, binding failures, or communication issues.</li>
 * </ul>
 */
public interface ShadowingModelListener {

    /**
     * Invoked when the Digital Twin transitions to synchronized state.
     * <p>
     * Called when all Physical Adapters are bound and the Digital Twin State is actively
     * synchronized with the physical asset.
     *
     * @param digitalTwinState the current synchronized Digital Twin State
     */
    public void onShadowingSync(DigitalTwinState digitalTwinState);

    /**
     * Invoked when the Digital Twin transitions to out-of-sync state.
     * <p>
     * Called when the Digital Twin loses synchronization with its physical asset due to
     * Physical Adapter issues or communication failures.
     *
     * @param digitalTwinState the last known Digital Twin State before synchronization was lost
     */
    public void onShadowingOutOfSync(DigitalTwinState digitalTwinState);

}
