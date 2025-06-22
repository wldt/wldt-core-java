package it.wldt.management;


/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project wldt-core
 * @created 31/05/2025 - 17:44
 * This interface defines the methods for observing changes in resources.
 */
public interface IResourceObserver {

    /**
     * Called when a resource/subresource is created.
     *
     * @param resourceId the unique identifier of the created resource
     * @param subResourceId the unique identifier of the created subresource
     */
    public void onResourceCreated(String resourceId, String subResourceId);

    /**
     * Called when a resource/subresource is updated.
     *
     * @param resourceId the unique identifier of the updated resource
     * @param subResourceId the unique identifier of the updated subresource
     */
    public void onResourceUpdated(String resourceId, String subResourceId);

    /**
     * Called when a resource/subresource is deleted.
     *
     * @param resourceId the unique identifier of the deleted resource
     * @param subResourceId the unique identifier of the deleted subresource
     */
    public void onResourceDeleted(String resourceId, String subResourceId);

}
