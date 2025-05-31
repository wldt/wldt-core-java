package it.wldt.management;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Optional;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project wldt-core
 * @created 30/05/2025 - 10:47
 */
public class DemoManagementInterface extends ManagementInterface {

    private static final Logger logger = LoggerFactory.getLogger(DemoManagementInterface.class);

    private boolean isStarted = false;

    @Override
    protected void onStart(List<ManagedResource<?, ?, ?>> resources) {

        // Set the started flag to true
        this.isStarted = true;

        logger.info("DemoManagementInterface started with {} resources:", resources.size());

        for (ManagedResource<?, ?, ?> resource : resources) {
            logger.info("Managed Resource: {}", resource.getId());
        }
    }

    @Override
    protected void onStop() {
        logger.info("DemoManagementInterface stopped.");
    }

    @Override
    protected void onResourceAdded(ManagedResource<?, ?, ?> resource) {
        logger.info("Resource added: {}", resource.getId());
    }

    @Override
    protected void onResourceUpdated(ManagedResource<?, ?, ?> resource) {
        logger.info("Resource updated: {}", resource.getId());
    }

    @Override
    protected void onResourceRemoved(ManagedResource<?, ?, ?> resource) {
        logger.info("Resource removed: {}", resource.getId());
    }

    /**
     * Example method to read a resource by its ID.
     * This is just a placeholder for demonstration purposes.
     *
     * @param resourceRequest the ResourceRequest that should be handled by the interface
     */
    public Optional<ResourceResponse<?>> readResource(ResourceRequest<?> resourceRequest) {

        // Check if the Interface is started otherwise return an empty Optional
        if(!this.isStarted) {
            logger.error("DemoManagementInterface is not started. Cannot read resource.");
            return Optional.empty();
        }

        if(resourceRequest == null || resourceRequest.getResourceId() == null || resourceRequest.getResourceId().isEmpty()){
            logger.error("Error handling Null Request ! Received request: {}", resourceRequest);
            return Optional.empty();
        }

        // Retrieve the target Resource Id
        String resourceId = resourceRequest.getResourceId();

        logger.info("Reading resource with ID: {}", resourceId);

        // Implement the logic to read the resource
        // For example, you could retrieve it from the ResourceManager
        ManagedResource<?, ?, ?> managedResource = this.resourceManager.getResourceById(resourceId).orElse(null);

        if(managedResource != null) {
            logger.info("Resource found: {}", managedResource.getId());
            return Optional.ofNullable(managedResource.read(new ResourceRequest<>(resourceId, resourceRequest.getSubResourceId()))); // Assuming getData() returns the resource data
        } else {
            logger.warn("Resource with ID {} not found.", resourceId);
            return Optional.empty();
        }
    }
}
