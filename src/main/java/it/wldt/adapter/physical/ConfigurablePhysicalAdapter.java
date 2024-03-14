package it.wldt.adapter.physical;

import it.wldt.exception.WldtWorkerException;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * A variation of the PhysicalAdapter with the possibility to specify a target Configuration (with a specific class)
 * that can be used to customize/adapt the behaviour of the adapter
 */
public abstract class ConfigurablePhysicalAdapter<C> extends PhysicalAdapter {

    private C configuration;

    private ConfigurablePhysicalAdapter() {
        super("");
    }

    public ConfigurablePhysicalAdapter(String id, C configuration) {
        super(id);
        this.configuration = configuration;
    }

    public C getConfiguration() {
        return configuration;
    }
}
