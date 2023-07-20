package it.wldt.core.model;

import it.wldt.core.state.IDigitalTwinState;

public interface ShadowingModelListener {

    public void onShadowingSync(IDigitalTwinState digitalTwinState);

    public void onShadowingOutOfSync(IDigitalTwinState digitalTwinState);

}
