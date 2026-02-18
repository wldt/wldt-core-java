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
package it.wldt.augmentation;

import it.wldt.core.engine.LifeCycleListener;
import it.wldt.core.state.DigitalTwinState;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 18/02/2026
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 * Since Augmentation Function are not interested in the full granularity of the Digital Twin Life Cycle Evolution,
 * but only in some specific events, this interface models the callbacks associated to the Digital Twin Life Cycle Evolution
 * specifically for the Augmentation Function, so that the Augmentation Function can implement only the callbacks of interest.
 * This Interface is an adaptation of the {@link LifeCycleListener} used by core modules to track the Digital Twin Life Cycle Evolution,
 * but with a more specific granularity for the Augmentation Function.
 */
public interface AugmentationLifeCycleListener {

    public void onCreate();

    public void onStart();

    public void onDigitalTwinBound();

    public void onDigitalTwinUnBound();

    public void onSync(DigitalTwinState digitalTwinState);

    public void onUnSync(DigitalTwinState digitalTwinState);

    public void onStop();

    public void onDestroy();

}
