package it.wldt.adapter.physical;

public abstract class ConfigurablePhysicalAdapter<C> extends PhysicalAdapter {

    private C configuration;

    public ConfigurablePhysicalAdapter(String id, C configuration){
        super(id);
        this.configuration = configuration;
    }

    public C getConfiguration() {
        return configuration;
    }
}
