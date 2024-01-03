package it.wldt.core.engine;

import it.wldt.exception.WldtConfigurationException;
import it.wldt.exception.WldtEngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project wldt-core
 * @created 29/12/2023 - 15:50
 */
public class DigitalTwinEngine {

    private static final Logger logger = LoggerFactory.getLogger(DigitalTwinEngine.class);

    private Map<String, DigitalTwin> digitalTwinMap;

    public DigitalTwinEngine(){
        this.digitalTwinMap = new HashMap<>();
    }

    public synchronized void addDigitalTwin(DigitalTwin digitalTwin, boolean startDigitalTwin) throws WldtEngineException, WldtConfigurationException {

        addDigitalTwin(digitalTwin);

        if(startDigitalTwin)
            startDigitalTwin(digitalTwin.getDigitalTwinId());
    }

    public synchronized void addDigitalTwin(DigitalTwin digitalTwin) throws WldtEngineException {

        if(this.digitalTwinMap != null && digitalTwin != null && digitalTwin.getId() != null) {
            logger.debug("Adding Digital Twin: {} to the Engine ...", digitalTwin.getId());
            this.digitalTwinMap.put(digitalTwin.getId(), digitalTwin);
            logger.debug("Digital Twin: {} added to the Engine !", digitalTwin.getId());
        }
        else
            throw new WldtEngineException("Error adding new Digital Twin to the Engine ! On value among twinMap, twin or twinId = null");
    }

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

    public synchronized void removeAll() throws WldtEngineException {
        List<String> idList = new ArrayList<>(digitalTwinMap.keySet());
        for (String digitalTwinId : idList)
            removeDigitalTwin(digitalTwinId);
    }

    public synchronized void startAll() throws WldtEngineException, WldtConfigurationException {
        for (Map.Entry<String, DigitalTwin> digitalTwinEntry : this.digitalTwinMap.entrySet())
            startDigitalTwin(digitalTwinEntry.getKey());
    }

    public synchronized void stopAll() throws WldtEngineException {
        for (Map.Entry<String, DigitalTwin> digitalTwinEntry : this.digitalTwinMap.entrySet())
            stopDigitalTwin(digitalTwinEntry.getKey());
    }

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

    public int getDigitalTwinCount(){
        return this.digitalTwinMap.size();
    }

    public synchronized Map<String, DigitalTwin> getDigitalTwinMap() {
        return new HashMap<>(digitalTwinMap);
    }

}
