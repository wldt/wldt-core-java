package it.wldt.management;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 30/05/2025
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 * This class defines a managed resource in the Digital Twin framework with Input and Output types.
 */
public abstract class ManagedResource<R, I, O> {

    // Unique identifier of the resource
    protected String id;

    // Type of the resource
    protected String type;

    // Display Name of the resource
    protected String name;

    // The resource itself, which can be of any type.
    protected R resource;

    /**
     * Default constructor for ManagedResource.
     * It is private to prevent instantiation without parameters.
     */
    private ManagedResource() {
    }

    /**
     * Constructs a ManagedResource with the specified id, type, name, and resource.
     *
     * @param id       the unique identifier of the resource
     * @param type     the type of the resource
     * @param name     the display name of the resource
     * @param resource the actual resource object
     */
    public ManagedResource(String id, String type, String name, R resource) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.resource = resource;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract ResourceResponse<O> create(ResourceRequest<I> resourceRequest);

    public abstract ResourceResponse<O> read(ResourceRequest<I> resourceRequest);

    public abstract ResourceResponse<O> delete(ResourceRequest<I> resourceRequest);

    public abstract ResourceResponse<O> update(ResourceRequest<I> resourceRequest);

    public R getResource() {
        return resource;
    }

    public void setResource(R resource) {
        this.resource = resource;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("ManagedResource{");
        sb.append("id='").append(id).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", resource=").append(resource);
        sb.append('}');
        return sb.toString();
    }
}
