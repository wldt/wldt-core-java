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

import it.wldt.exception.WldtConfigurationException;
import it.wldt.exception.WldtEngineException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The DigitalTwinEngine class manages a collection of DigitalTwin instances.
 *
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 */
public class DigitalTwinEngine {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(DigitalTwinEngine.class);

    private Map<String, DigitalTwin> digitalTwinMap;

    /**
     * Constructs a DigitalTwinEngine with an empty map of DigitalTwins.
     */
    public DigitalTwinEngine(){
        this.digitalTwinMap = new HashMap<>();
    }

    /**
     * Adds a DigitalTwin to the engine. Optionally starts the DigitalTwin if specified.
     *
     * @param digitalTwin The DigitalTwin instance to add.
     * @param startDigitalTwin Whether to start the DigitalTwin after adding.
     * @throws WldtEngineException If an error occurs while adding the DigitalTwin.
     * @throws WldtConfigurationException If there is a configuration error in the DigitalTwin.
     */
    public synchronized void addDigitalTwin(DigitalTwin digitalTwin, boolean startDigitalTwin) throws WldtEngineException, WldtConfigurationException {

        addDigitalTwin(digitalTwin);

        if(startDigitalTwin)
            startDigitalTwin(digitalTwin.getDigitalTwinId());
    }

    /**
     * Adds a DigitalTwin to the engine.
     *
     * @param digitalTwin The DigitalTwin instance to add.
     * @throws WldtEngineException If an error occurs while adding the DigitalTwin.
     */
    public synchronized void addDigitalTwin(DigitalTwin digitalTwin) throws WldtEngineException {

        if(this.digitalTwinMap != null && digitalTwin != null && digitalTwin.getId() != null) {
            logger.debug("Adding Digital Twin: {} to the Engine ...", digitalTwin.getId());
            this.digitalTwinMap.put(digitalTwin.getId(), digitalTwin);
            logger.debug("Digital Twin: {} added to the Engine !", digitalTwin.getId());
        }
        else
            throw new WldtEngineException("Error adding new Digital Twin to the Engine ! On value among twinMap, twin or twinId = null");
    }

    /**
     * Removes a DigitalTwin from the engine.
     *
     * @param digitalTwinId The ID of the DigitalTwin to remove.
     * @throws WldtEngineException If an error occurs while removing the DigitalTwin.
     */
    public synchronized void removeDigitalTwin(String digitalTwinId) throws WldtEngineException {

        if(this.digitalTwinMap != null && digitalTwinId != null) {
            logger.debug("Removing Digital Twin: {} from the Engine ...", digitalTwinId);
            stopDigitalTwin(digitalTwinId);
            this.digitalTwinMap.remove(digitalTwinId);
            logger.debug("Digital Twin: {} removed from the Engine !", digitalTwinId);
        }
        else
            throw new WldtEngineException("Error removing new Digital Twin to the Engine ! On value among twinMap, twin or twinId = null");
    }

    /**
     * Removes all DigitalTwins from the engine.
     *
     * @throws WldtEngineException If an error occurs while removing DigitalTwins.
     */
    public synchronized void removeAll() throws WldtEngineException {
        List<String> idList = new ArrayList<>(digitalTwinMap.keySet());
        for (String digitalTwinId : idList)
            removeDigitalTwin(digitalTwinId);
    }

    /**
     * Starts all DigitalTwins in the engine.
     *
     * @throws WldtEngineException If an error occurs while starting DigitalTwins.
     * @throws WldtConfigurationException If there is a configuration error in a DigitalTwin.
     */
    public synchronized void startAll() throws WldtEngineException, WldtConfigurationException {
        for (Map.Entry<String, DigitalTwin> digitalTwinEntry : this.digitalTwinMap.entrySet())
            startDigitalTwin(digitalTwinEntry.getKey());
    }

    /**
     * Stops all DigitalTwins in the engine.
     *
     * @throws WldtEngineException If an error occurs while stopping DigitalTwins.
     */
    public synchronized void stopAll() throws WldtEngineException {
        for (Map.Entry<String, DigitalTwin> digitalTwinEntry : this.digitalTwinMap.entrySet())
            stopDigitalTwin(digitalTwinEntry.getKey());
    }

    /**
     * Starts a specific DigitalTwin in the engine.
     *
     * @param digitalTwinId The ID of the DigitalTwin to start.
     * @throws WldtEngineException If an error occurs while starting the DigitalTwin.
     * @throws WldtConfigurationException If there is a configuration error in the DigitalTwin.
     */
    public synchronized void startDigitalTwin(String digitalTwinId) throws WldtEngineException, WldtConfigurationException {

        logger.debug("Starting Digital Twin: {} ...", digitalTwinId);

        if(digitalTwinId == null)
            throw new WldtEngineException(String.format("Error starting the target DT with id: %s -> Provided Id is null !", digitalTwinId));

        if(!this.digitalTwinMap.containsKey(digitalTwinId))
            throw new WldtEngineException(String.format("Error starting the target DT with id: %s -> DT not available in the Engine !", digitalTwinId));

        DigitalTwin targetDigitalTwin = this.digitalTwinMap.get(digitalTwinId);

        // Check the current DT Life Cycle State
        if(targetDigitalTwin.getCurrentLifeCycleState().equals(LifeCycleState.NONE) ||
                targetDigitalTwin.getCurrentLifeCycleState().equals(LifeCycleState.STOPPED) ||
                targetDigitalTwin.getCurrentLifeCycleState().equals(LifeCycleState.DESTROYED)) {
            targetDigitalTwin.startLifeCycle();
            logger.debug("Digital Twin: {} STARTED !", digitalTwinId);
        }
        else
            logger.warn(String.format("Warning starting the target DT with id: %s -> DT already started ! LifeCycle State: %s !",
                    digitalTwinId,
                    targetDigitalTwin.getCurrentLifeCycleState().getValue()
            ));
            /*
            throw new WldtEngineException(
                    String.format("Error starting the target DT with id: %s -> DT already started ! LifeCycle State: %s !",
                            digitalTwinId,
                            targetDigitalTwin.getCurrentLifeCycleState().getValue()
                    )
            );
            */
    }

    /**
     * Stops a specific DigitalTwin in the engine.
     *
     * @param digitalTwinId
     * @throws WldtEngineException
     */
    public synchronized void stopDigitalTwin(String digitalTwinId) throws WldtEngineException {

        logger.debug("Stopping Digital Twin: {} ...", digitalTwinId);

        if(digitalTwinId == null)
            throw new WldtEngineException(String.format("Error starting the target DT with id: %s -> Provided Id is null !", digitalTwinId));

        if(!this.digitalTwinMap.containsKey(digitalTwinId))
            throw new WldtEngineException(String.format("Error starting the target DT with id: %s -> DT not available in the Engine !", digitalTwinId));

        DigitalTwin targetDigitalTwin = this.digitalTwinMap.get(digitalTwinId);

        // Check the current DT Life Cycle State
        if(!targetDigitalTwin.getCurrentLifeCycleState().equals(LifeCycleState.NONE) &&
                !targetDigitalTwin.getCurrentLifeCycleState().equals(LifeCycleState.STOPPED) &&
                !targetDigitalTwin.getCurrentLifeCycleState().equals(LifeCycleState.DESTROYED)) {
            targetDigitalTwin.stopLifeCycle();
            logger.debug("Digital Twin: {} STOPPED !", digitalTwinId);
        }
        else
            logger.warn(String.format("Error starting the target DT with id: %s -> DT already stopped ! ! LifeCycle State: %s !",
                    digitalTwinId,
                    targetDigitalTwin.getCurrentLifeCycleState().getValue()
            ));
            /*
            throw new WldtEngineException(
                    String.format("Error starting the target DT with id: %s -> DT already stopped ! ! LifeCycle State: %s !",
                            digitalTwinId,
                            targetDigitalTwin.getCurrentLifeCycleState().getValue()
                    )
            );
            */
    }

    /**
     * Returns the number of DigitalTwins in the engine.
     *
     * @return The number of DigitalTwins in the engine.
     */
    public int getDigitalTwinCount(){
        return this.digitalTwinMap.size();
    }

    /**
     * Returns a synchronized copy of the map containing DigitalTwin instances.
     *
     * @return A synchronized copy of the map containing DigitalTwin instances.
     */
    public synchronized Map<String, DigitalTwin> getDigitalTwinMap() {
        return new HashMap<>(digitalTwinMap);
    }

}
