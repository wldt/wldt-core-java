package it.wldt.core.worker;

import it.wldt.exception.WldtRuntimeException;
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

    @Override
    public void run() {
        try {
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
}
