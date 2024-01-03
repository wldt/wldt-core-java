package it.wldt.core.worker;

import it.wldt.exception.WldtRuntimeException;
import it.wldt.exception.WldtWorkerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Author:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 27/03/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * Execution unit on the WLDT Engine used to execute adapters and any other active component in the
 * WLDT instance.
 *
 */
public abstract class WldtWorker implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(WldtWorker.class);

    protected String digitalTwinId;

    public WldtWorker(){}

    @Override
    public void run() {

        try {

            if(this.digitalTwinId == null)
                throw new WldtWorkerException("Error ! Impossible to start a WldtWorker with a NULL Digital Twin Id !");

            onWorkerStart();

        } catch (Exception e) {
            logger.error("WLDT WORKER onWorkerStart ERROR: {}", e.getLocalizedMessage());
            try{
                onWorkerStop();
            }catch (Exception stopException){
                logger.error("WLDT WORKER ERROR onWorkerStop() ERROR: {}", stopException.getLocalizedMessage());
            }
        }
    }

    abstract public void onWorkerStop() throws WldtRuntimeException;

    abstract public void onWorkerStart() throws WldtRuntimeException;

    public String getDigitalTwinId() {
        return digitalTwinId;
    }

    public void setDigitalTwinId(String digitalTwinId) throws WldtWorkerException {

        if(digitalTwinId == null)
            throw new WldtWorkerException("Error ! Impossible to create a WldtWorker with a NULL Digital Twin Id !");

        this.digitalTwinId = digitalTwinId;
    }
}
