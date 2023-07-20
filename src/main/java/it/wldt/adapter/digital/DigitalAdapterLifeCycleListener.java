package it.wldt.adapter.digital;

import it.wldt.adapter.physical.PhysicalAssetDescription;

import java.util.Map;

public interface DigitalAdapterLifeCycleListener {
    void onPhysicalAdapterBound(String adapterId, PhysicalAssetDescription physicalAssetDescription);

    void onPhysicalAdapterBindingUpdate(String adapterId, PhysicalAssetDescription physicalAssetDescription);

    void onPhysicalAdapterUnBound(String adapterId, PhysicalAssetDescription physicalAssetDescription, String errorMessage);

    void onDigitalAdapterBound(String adapterId);

    void onDigitalAdapterUnBound(String adapterId, String errorMessage);

    void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap);

    void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap, String errorMessage);
}
