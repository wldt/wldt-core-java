package it.wldt.management;

import it.wldt.core.engine.DigitalTwin;
import it.wldt.core.engine.DigitalTwinWorker;
import it.wldt.exception.WldtRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project wldt-core
 * @created 30/05/2025 - 22:22
 */
public abstract class ManagementInterface extends DigitalTwinWorker {

    private static final Logger logger = LoggerFactory.getLogger(ManagementInterface.class);

    protected ResourceManager resourceManager;

    public ManagementInterface() {
        super();
    }

    @Override
    public void onWorkerStart() throws WldtRuntimeException {
        try{

            logger.info("Starting Management Interface...");

            if (this.resourceManager == null) {
                logger.error("ResourceManager is not set. Please set it before starting the Management Interface.");
                throw new WldtRuntimeException("ResourceManager is not set. Please set it before starting the Management Interface.");
            }

            onStart(this.resourceManager.getResourceList());
        }catch (Exception e){
            throw new WldtRuntimeException(e.getLocalizedMessage());
        }
    }

    @Override
    public void onWorkerStop() throws WldtRuntimeException {
        try{
            logger.info("Stopping Management Interface...");
            onStop();
        }catch (Exception e){
            throw new WldtRuntimeException(e.getLocalizedMessage());
        }
    }

    /**
     * Sets the ResourceManager for this Management Interface.
     * This method is typically called by Digital Twin before starting the Management Interface.
     *
     * @param resourceManager the ResourceManager instance to set
     */
    public void setResourceManager(ResourceManager resourceManager) {
        this.resourceManager = resourceManager;
    }

    /**
     * Callback when the Management Interface is started.
     * @param resources the list of resources that are being managed and that have been already registered.
     */
    abstract protected void onStart(List<ManagedResource<?, ?, ?>> resources);

    /**
     * Callback when the Management Interface has been stopped.
     */
    abstract protected void onStop();


    /**
     * Callback when a resource is added to the Digital Twin and the Management Interface should handle it
     */
    abstract protected void onResourceAdded(ManagedResource<?, ?, ?> resource);

    /**
     * Callback when a resource has been updated and the Management Interface should handle it
     */
    abstract protected void onResourceUpdated(ManagedResource<?, ?, ?> resource);

    /**
     * Callback when a resource has been removed and the Management Interface should handle it
     */
    abstract protected void onResourceRemoved(ManagedResource<?, ?, ?> resource);
}
