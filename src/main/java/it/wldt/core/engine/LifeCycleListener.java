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
