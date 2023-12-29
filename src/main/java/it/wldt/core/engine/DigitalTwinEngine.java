package it.wldt.core.engine;

import it.wldt.core.twin.DigitalTwin;
import it.wldt.core.twin.LifeCycleState;
import it.wldt.exception.WldtConfigurationException;
import it.wldt.exception.WldtEngineException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
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

    public void addDigitalTwin(DigitalTwin digitalTwin) throws WldtEngineException {

        if(this.digitalTwinMap != null && digitalTwin != null && digitalTwin.getId() != null) {
            logger.debug("Adding Digital Twin: {} to the Engine ...", digitalTwin.getId());
            this.digitalTwinMap.put(digitalTwin.getId(), digitalTwin);
            logger.debug("Digital Twin: {} added to the Engine !", digitalTwin.getId());
        }
        else
            throw new WldtEngineException("Error adding new Digital Twin to the Engine ! On value among twinMap, twin or twinId = null");
    }

    public void startAll() throws WldtEngineException, WldtConfigurationException {
        for (Map.Entry<String, DigitalTwin> digitalTwinEntry : this.digitalTwinMap.entrySet())
            startDigitalTwin(digitalTwinEntry.getKey());
    }

    public void stopAll() throws WldtEngineException {
        for (Map.Entry<String, DigitalTwin> digitalTwinEntry : this.digitalTwinMap.entrySet())
            stopDigitalTwin(digitalTwinEntry.getKey());
    }

    public void startDigitalTwin(String digitalTwinId) throws WldtEngineException, WldtConfigurationException {

        logger.debug("Starting Digital Twin: {} ...", digitalTwinId);

        if(digitalTwinId == null)
            throw new WldtEngineException(String.format("Error starting the target DT with id: %s -> Provided Id is null !", digitalTwinId));

        if(!this.digitalTwinMap.containsKey(digitalTwinId))
            throw new WldtEngineException(String.format("Error starting the target DT with id: %s -> DT not available in the Engine !", digitalTwinId));

        DigitalTwin targetDigitalTwin = this.digitalTwinMap.get(digitalTwinId);

        if(targetDigitalTwin.getCurrentLifeCycleState().equals(LifeCycleState.NONE) ||
                targetDigitalTwin.getCurrentLifeCycleState().equals(LifeCycleState.STOPPED) ||
                targetDigitalTwin.getCurrentLifeCycleState().equals(LifeCycleState.DESTROYED))
            targetDigitalTwin.startLifeCycle();
        else
            throw new WldtEngineException(
                    String.format("Error starting the target DT with id: %s -> DT already started ! LifeCycle State: %s !",
                            digitalTwinId,
                            targetDigitalTwin.getCurrentLifeCycleState().getValue()
                    )
            );

        logger.debug("Digital Twin: {} STARTED !", digitalTwinId);
    }

    public void stopDigitalTwin(String digitalTwinId) throws WldtEngineException {

        logger.debug("Stopping Digital Twin: {} ...", digitalTwinId);

        if(digitalTwinId == null)
            throw new WldtEngineException(String.format("Error starting the target DT with id: %s -> Provided Id is null !", digitalTwinId));

        if(!this.digitalTwinMap.containsKey(digitalTwinId))
            throw new WldtEngineException(String.format("Error starting the target DT with id: %s -> DT not available in the Engine !", digitalTwinId));

        DigitalTwin targetDigitalTwin = this.digitalTwinMap.get(digitalTwinId);

        if(!targetDigitalTwin.getCurrentLifeCycleState().equals(LifeCycleState.NONE) &&
                !targetDigitalTwin.getCurrentLifeCycleState().equals(LifeCycleState.STOPPED) &&
                !targetDigitalTwin.getCurrentLifeCycleState().equals(LifeCycleState.DESTROYED))
            targetDigitalTwin.stopLifeCycle();
        else
            throw new WldtEngineException(
                    String.format("Error starting the target DT with id: %s -> DT already started ! LifeCycle State: %s !",
                            digitalTwinId,
                            targetDigitalTwin.getCurrentLifeCycleState().getValue()
                    )
            );

        logger.debug("Digital Twin: {} STOPPED !", digitalTwinId);
    }

    public Map<String, DigitalTwin> getDigitalTwinMap() {
        return digitalTwinMap;
    }
}
