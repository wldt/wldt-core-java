/*
 * Copyright (c) 2025 - Current Year
 * Marco Picone Ph.D
 * Email: picone.m@gmail.com
 * Website: https://www.marcopicone.net/
 * All rights reserved.
 *
 * This program is provided under a Dual Licensing model:
 * 1) GNU General Public License version 3.0 (GPL-3.0) for open-source, academic,
 *    research, non-profit, and other non-commercial use; or
 * 2) Commercial License, for any commercial use, proprietary development, or
 *    closed-source distribution. To obtain a Commercial License, please contact: Marco Picone (picone.m@gmail.com)
 *
 * By using this software, you agree to comply with the terms of the applicable license.
 * This applies to all forms of the software, including source code and compiled/binary forms.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */
package it.wldt.core.engine;

import it.wldt.exception.WldtRuntimeException;
import it.wldt.exception.WldtWorkerException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;

/**
 * Author:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 27/03/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 *
 * Execution unit on the Digital Twin Engine used to execute adapters and any other active component associate to
 * a single Digital Twin Instance. Each DigitalTwinWorker has a direct reference to the Id of the DT where the worker
 * is executed.
 *
 */
public abstract class DigitalTwinWorker implements Runnable {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(DigitalTwinWorker.class);

    /**
     * The ID of the associated Digital Twin.
     */
    protected String digitalTwinId;

    /**
     * Default constructor for the DigitalTwinWorker class.
     */
    public DigitalTwinWorker(){}

    /**
     * The main execution logic of the worker. Calls the {@code onWorkerStart} method. Handles exceptions by logging
     * errors, calling the {@code onWorkerStop} method, and rethrowing the exception.
     */
    @Override
    public void run() {

        try {

            if(this.digitalTwinId == null)
                throw new WldtWorkerException("Error ! Impossible to start a DigitalTwinWorker with a NULL Digital Twin Id !");

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

    /**
     * Abstract method to be implemented by subclasses.
     * Defines the logic to be executed when the worker stops.
     *
     * @throws WldtRuntimeException If an error occurs during the execution of the worker stop logic.
     */
    abstract public void onWorkerStop() throws WldtRuntimeException;

    /**
     * Abstract method to be implemented by subclasses.
     * Defines the logic to be executed when the worker starts.
     *
     * @throws WldtRuntimeException If an error occurs during the execution of the worker start logic.
     */
    abstract public void onWorkerStart() throws WldtRuntimeException;

    /**
     * Gets the ID of the associated Digital Twin.
     *
     * @return The Digital Twin ID.
     */
    public String getDigitalTwinId() {
        return digitalTwinId;
    }

    /**
     * Sets the ID of the associated Digital Twin.
     * This method is called by the Digital Twin Engine when a DigitalTwinWorker (e.g., a Physical Adapter) is associated
     * to a target DT in the engine.
     *
     * @param digitalTwinId The Digital Twin ID to set.
     * @throws WldtWorkerException If the provided Digital Twin ID is null.
     */
    public void setDigitalTwinId(String digitalTwinId) throws WldtWorkerException {

        if(digitalTwinId == null)
            throw new WldtWorkerException("Error ! Impossible to create a DigitalTwinWorker with a NULL Digital Twin Id !");

        this.digitalTwinId = digitalTwinId;
    }
}
