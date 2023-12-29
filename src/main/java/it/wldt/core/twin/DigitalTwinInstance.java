package it.wldt.core.twin;

import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.adapter.physical.PhysicalAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * Instance of the DT with its adapters, id and digitalized assets.
 * This class is used in the Digital Twin Class for coordination and execution purposes.
 */
public class DigitalTwinInstance {

    private final String digitalTwinId;
    private final List<String> digitalizedPhysicalAssets;
    private final List<PhysicalAdapter> physicalAdapterList;
    private final List<DigitalAdapter<?>> digitalAdapterList;

    public DigitalTwinInstance(String digitalTwinId, List<String> digitalizedPhysicalAssets) {
        this.digitalTwinId = digitalTwinId;
        this.digitalizedPhysicalAssets = digitalizedPhysicalAssets;
        this.physicalAdapterList = new ArrayList<>();
        this.digitalAdapterList = new ArrayList<>();
    }

    public DigitalTwinInstance(String digitalTwinId) {
        this(digitalTwinId, new ArrayList<>());
    }

    public String getDigitalTwinId() {
        return digitalTwinId;
    }

    public List<String> getDigitalizedPhysicalAssets() {
        return Collections.unmodifiableList(this.digitalizedPhysicalAssets);
    }

    public List<String> getPhysicalAdapterIds(){
        return Collections.unmodifiableList(this.physicalAdapterList.stream().map(PhysicalAdapter::getId).collect(Collectors.toList()));
    }

    public List<String> getDigitalAdapterIds(){
        return Collections.unmodifiableList(this.digitalAdapterList.stream().map(DigitalAdapter::getId).collect(Collectors.toList()));
    }

    protected List<PhysicalAdapter> getPhysicalAdapterList() {
        return physicalAdapterList;
    }

    protected List<DigitalAdapter<?>> getDigitalAdapterList() {
        return digitalAdapterList;
    }


    @Override
    public String toString() {
        return "DigitalTwinInstance{" +
                "digitalTwinId='" + digitalTwinId + '\'' +
                ", digitalizedPhysicalAssets=" + digitalizedPhysicalAssets +
                ", physicalAdapterList=" + physicalAdapterList +
                ", digitalAdapterList=" + digitalAdapterList +
                '}';
    }
}
