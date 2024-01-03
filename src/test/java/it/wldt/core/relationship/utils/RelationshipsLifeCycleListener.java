package it.wldt.core.relationship.utils;

import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.core.engine.LifeCycleListener;
import it.wldt.core.state.DigitalTwinState;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class RelationshipsLifeCycleListener implements LifeCycleListener {
    private DigitalTwinState digitalTwinState;
    private PhysicalAssetDescription physicalAssetDescription;
    private final CountDownLatch syncLatch;

    public RelationshipsLifeCycleListener(CountDownLatch syncLatch) {
        this.syncLatch = syncLatch;
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onStart() {

    }

    @Override
    public void onPhysicalAdapterBound(String adapterId, PhysicalAssetDescription physicalAssetDescription) {
        this.physicalAssetDescription = physicalAssetDescription;
    }

    @Override
    public void onPhysicalAdapterBindingUpdate(String adapterId, PhysicalAssetDescription physicalAssetDescription) {

    }

    @Override
    public void onPhysicalAdapterUnBound(String adapterId, PhysicalAssetDescription physicalAssetDescription, String errorMessage) {

    }

    @Override
    public void onDigitalAdapterBound(String adapterId) {

    }

    @Override
    public void onDigitalAdapterUnBound(String adapterId, String errorMessage) {

    }

    @Override
    public void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap) {

    }

    @Override
    public void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap, String errorMessage) {

    }

    @Override
    public void onSync(DigitalTwinState digitalTwinState) {
        this.digitalTwinState = digitalTwinState;
        this.syncLatch.countDown();
    }

    @Override
    public void onUnSync(DigitalTwinState digitalTwinState) {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {

    }

    public DigitalTwinState getDigitalTwinState() {
        return digitalTwinState;
    }

    public void setDigitalTwinState(DigitalTwinState digitalTwinState) {
        this.digitalTwinState = digitalTwinState;
    }

    public PhysicalAssetDescription getPhysicalAssetDescription() {
        return physicalAssetDescription;
    }
}
