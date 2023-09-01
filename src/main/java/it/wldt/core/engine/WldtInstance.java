package it.wldt.core.engine;

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
 * This class is used in the WLDT Engine for coordination and execution purposes.
 */
public class WldtInstance {

    private final String wldtId;
    private final List<String> digitalizedPhysicalAssets;
    private final List<PhysicalAdapter> physicalAdapterList;
    private final List<DigitalAdapter<?>> digitalAdapterList;

    public WldtInstance(String wldtId, List<String> digitalizedPhysicalAssets) {
        this.wldtId = wldtId;
        this.digitalizedPhysicalAssets = digitalizedPhysicalAssets;
        this.physicalAdapterList = new ArrayList<>();
        this.digitalAdapterList = new ArrayList<>();
    }

    public WldtInstance(String wldtId) {
        this(wldtId, new ArrayList<>());
    }

    public String getWldtId() {
        return wldtId;
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
        return "WldtInstance{" +
                "wldtId='" + wldtId + '\'' +
                ", digitalizedPhysicalAssets=" + digitalizedPhysicalAssets +
                ", physicalAdapterList=" + physicalAdapterList +
                ", digitalAdapterList=" + digitalAdapterList +
                '}';
    }
}
