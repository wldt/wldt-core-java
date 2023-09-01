package it.wldt.adapter.digital;

import it.wldt.adapter.physical.PhysicalAssetDescription;

import java.util.Map;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * Internal Listener to monitor the DT Life Cycle
 */
public interface DigitalAdapterLifeCycleListener {
    void onPhysicalAdapterBound(String adapterId, PhysicalAssetDescription physicalAssetDescription);

    void onPhysicalAdapterBindingUpdate(String adapterId, PhysicalAssetDescription physicalAssetDescription);

    void onPhysicalAdapterUnBound(String adapterId, PhysicalAssetDescription physicalAssetDescription, String errorMessage);

    void onDigitalAdapterBound(String adapterId);

    void onDigitalAdapterUnBound(String adapterId, String errorMessage);

    void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap);

    void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap, String errorMessage);
}
