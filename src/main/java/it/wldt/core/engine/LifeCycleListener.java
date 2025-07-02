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
package it.wldt.core.engine;

import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.core.state.DigitalTwinState;
import java.util.Map;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * Interface modeling the callbacks associated to the Digital Twin Life Cycle Evolution.
 */
public interface LifeCycleListener {

    public void onCreate();

    public void onStart();

    public void onPhysicalAdapterBound(String adapterId, PhysicalAssetDescription physicalAssetDescription);

    public void onPhysicalAdapterBindingUpdate(String adapterId, PhysicalAssetDescription physicalAssetDescription);

    public void onPhysicalAdapterUnBound(String adapterId, PhysicalAssetDescription physicalAssetDescription, String errorMessage);

    public void onDigitalAdapterBound(String adapterId);

    public void onDigitalAdapterUnBound(String adapterId, String errorMessage);

    public void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap);

    public void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap, String errorMessage);

    public void onSync(DigitalTwinState digitalTwinState);

    public void onUnSync(DigitalTwinState digitalTwinState);

    public void onStop();

    public void onDestroy();

}
