package it.wldt.management;

import it.wldt.exception.WldtManagedResourceException;
import it.wldt.exception.WldtManagementInterfaceException;
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

    // Flag to indicate if the management interface has been started
    private boolean isStarted = false;

    // Constants for request types
    public static final String READ_REQUEST = "READ";
    public static final String CREATE_REQUEST = "CREATE";
    public static final String UPDATE_REQUEST = "UPDATE";
    public static final String DELETE_REQUEST = "DELETE";

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
     * Validates the ResourceRequest and retrieves the associated ManagedResource
     * form the ResourceManager.
     *
     * @param resourceRequest the resource request
     * @return an Optional containing the ManagedResource if found, or empty if not found
     * @throws WldtManagementInterfaceException if the management interface is not started or the request is invalid
     * @throws WldtManagedResourceException if the resource is not found
     */
    private Optional<DictionaryManagedResource> validateAndLoadRequest(ResourceRequest<?> resourceRequest) throws WldtManagementInterfaceException, WldtManagedResourceException {

        if (!this.isStarted) {
            logger.error("DemoManagementInterface is not started. Cannot access resource.");
            throw new WldtManagementInterfaceException("Management interface not started.");
        }
        if (resourceRequest == null || resourceRequest.getResourceId() == null || resourceRequest.getResourceId().isEmpty()) {
            logger.error("Error handling Null Request! Received request: {}", resourceRequest);
            throw new WldtManagedResourceException("Invalid resource request.");
        }

        // Get the resource ID from the ResourceRequest
        String resourceId = resourceRequest.getResourceId();
        logger.info("Accessing resource with ID: {}", resourceId);

        // Retrieve the ManagedResource from the ResourceManager using the resource ID
        Optional<ManagedResource<?, ?, ?>> resourceOpt = this.resourceManager.getResourceById(resourceId);

        // Check if the ManagedResource is present
        if(resourceOpt.isPresent()) {

            // Retrieve the ManagedResource from the Optional
            ManagedResource<?, ?, ?> retrievedManagedResource = resourceOpt.get();

            // Check the instance of ManagedResource to ensure it is compatible with the request
            if (!(retrievedManagedResource instanceof DictionaryManagedResource))
                throw new WldtManagedResourceException("Loaded resource has a wrong type: " + retrievedManagedResource.getClass().getName());

            // Cast and return the ManagedResource to DictionaryManagedResource
            return Optional.of((DictionaryManagedResource) retrievedManagedResource);
        }
        else
            // Resource not found, return an empty Optional
            return Optional.empty();
    }

    /**
     * Emulates an incoming request to the management interface.
     * This method validates the request, retrieves the ManagedResource, and performs the requested operation (read, create, update, delete).
     * It returns an Optional containing the ResourceResponse if successful, or an error response if the request fails or the resource is not found.
     * @param requestType the type of request (READ, CREATE, UPDATE, DELETE)
     * @param resourceRequest the resource request containing the resource ID and sub-resource ID
     * @return an Optional containing the ResourceResponse if successful, or an error response if the request fails or the resource is not found
     */
    public Optional<ResourceResponse<?>> emulateIncomingRequest(String requestType, ResourceRequest<?> resourceRequest) {

        try{

            // Validate the ResourceRequest and retrieve the ManagedResource
            Optional<DictionaryManagedResource> optionalManagedResource = validateAndLoadRequest(resourceRequest);

            // Check if the ManagedResource is present
            if(optionalManagedResource.isPresent()) {

                // Cast the ManagedResource to DictionaryManagedResource
                DictionaryManagedResource managedResource = optionalManagedResource.get();

                // Prepare the request
                ResourceRequest<Object> newRequest = new ResourceRequest<>(
                        resourceRequest.getResourceId(),
                        resourceRequest.getSubResourceId(),
                        resourceRequest.getContent(),
                        resourceRequest.getMetadata());

                if(requestType.equals(READ_REQUEST))
                    // Read the resource using the ManagedResource's read method and return the response
                    return Optional.ofNullable(managedResource.read(newRequest));
                else if(requestType.equals(CREATE_REQUEST))
                    // Create the resource using the ManagedResource's create method and return the response
                    return Optional.ofNullable(managedResource.create(newRequest)); // Assuming getData() returns the resource data
                else if(requestType.equals(UPDATE_REQUEST))
                    // Update the resource using the ManagedResource's update method and return the response
                    return Optional.ofNullable(managedResource.update(newRequest));
                else if(requestType.equals(DELETE_REQUEST))
                    // Delete the resource using the ManagedResource's delete method and return the response
                    return Optional.ofNullable(managedResource.delete(newRequest));
                else {
                    logger.error("Unknown request type: {}", requestType);
                    return Optional.of(new ResourceResponse<>(400, "Unknown request type: " + requestType));
                }

            } else {
                // Resource not found, return a 404 response
                return Optional.of(new ResourceResponse<>(404, "Resource not found with ID: " + resourceRequest.getResourceId()));
            }

        }catch (Exception e){
            e.printStackTrace();
            logger.error("Error reading resource: {}", e.getMessage());
            return Optional.of(new ResourceResponse<>(400, "Error Managing the Resource Request: " + e.getMessage()));
        }
    }

}
