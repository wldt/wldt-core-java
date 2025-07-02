package it.wldt.core.adapter.physical;

import it.wldt.adapter.physical.*;
import it.wldt.core.event.WldtEventBus;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;

import java.util.ArrayList;
import java.util.Random;

public class TestPhysicalAdapter extends ConfigurablePhysicalAdapter<TestPhysicalAdapterConfiguration> {

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(TestPhysicalAdapter.class);

    public static final int TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES = 10;

    public static final int TARGET_PHYSICAL_ASSET_EVENT_UPDATES = 2;

    public static long MESSAGE_SLEEP_PERIOD_MS = 2000;

    public final String DIGITAL_TWIN_ID = "dt00001";

    public static final String ENERGY_PROPERTY_KEY = "energy";

    public static final String SWITCH_PROPERTY_KEY = "switch";

    public static final String SWITCH_OFF_ACTION_KEY = "switch_off";

    public static final String SWITCH_ON_ACTION_KEY = "switch_on";

    public static final String OVERHEATING_EVENT_KEY = "overheating";

    private boolean isTelemetryOn = false;

    private Random random = new Random();

    public TestPhysicalAdapter(String id, TestPhysicalAdapterConfiguration configuration) {
        super(id, configuration);
    }

    public TestPhysicalAdapter(String id, TestPhysicalAdapterConfiguration configuration, boolean isTelemetryOn) {
        super(id, configuration);
        this.isTelemetryOn = isTelemetryOn;
    }

    @Override
    public void onIncomingPhysicalAction(PhysicalAssetActionWldtEvent<?> physicalActionEvent) {
        try{
            logger.info("Received PhysicalActionEventMessage: {}", physicalActionEvent);

            if(physicalActionEvent != null && physicalActionEvent.getActionKey().equals(SWITCH_ON_ACTION_KEY)) {
                logger.info("{} Received ! Switching ON the device ...", physicalActionEvent.getType());
                Thread.sleep(MESSAGE_SLEEP_PERIOD_MS);
                WldtEventBus.getInstance().publishEvent(DIGITAL_TWIN_ID, getId(), new PhysicalAssetPropertyWldtEvent<>(SWITCH_PROPERTY_KEY, "ON"));
            } else if(physicalActionEvent != null && physicalActionEvent.getActionKey().equals(SWITCH_OFF_ACTION_KEY)){
                logger.info("{} Received ! Switching OFF the device ...", physicalActionEvent.getType());
                Thread.sleep(MESSAGE_SLEEP_PERIOD_MS);
                WldtEventBus.getInstance().publishEvent(DIGITAL_TWIN_ID, getId(), new PhysicalAssetPropertyWldtEvent<>(SWITCH_PROPERTY_KEY, "OFF"));
            } else
                logger.error("WRONG OR NULL ACTION RECEIVED !");

        }catch (Exception e){
           e.printStackTrace();
        }
    }

    @Override
    public void onAdapterStart() {

        try{

            PhysicalAssetDescription physicalAssetDescription = new PhysicalAssetDescription();

            physicalAssetDescription.setActions(new ArrayList<PhysicalAssetAction>() {{
                add(new PhysicalAssetAction(SWITCH_OFF_ACTION_KEY, "demo.actuation", "application/json"));
                add(new PhysicalAssetAction(SWITCH_ON_ACTION_KEY, "demo.actuation", "application/json"));
            }});

            physicalAssetDescription.setProperties(new ArrayList<PhysicalAssetProperty<?>>() {{
                add(new PhysicalAssetProperty<String>(SWITCH_PROPERTY_KEY, "OFF"));
                add(new PhysicalAssetProperty<Double>(ENERGY_PROPERTY_KEY, 0.0));
            }});

            physicalAssetDescription.setEvents(new ArrayList<PhysicalAssetEvent>() {{
                add(new PhysicalAssetEvent(OVERHEATING_EVENT_KEY,"text/plain"));
            }});

            this.notifyPhysicalAdapterBound(physicalAssetDescription);

        }catch (Exception e){
            e.printStackTrace();
        }

        //Emulate the real device on a different Thread and then send the PhysicalEvent
        if(isTelemetryOn)
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        //Before the telemetry messages send an overheating event
                        Thread.sleep(MESSAGE_SLEEP_PERIOD_MS);
                        publishPhysicalAssetEventWldtEvent(new PhysicalAssetEventWldtEvent<String>(OVERHEATING_EVENT_KEY, "overheating-low"));

                        for(int i = 0; i< TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES; i++){

                            Thread.sleep(MESSAGE_SLEEP_PERIOD_MS);
                            double randomEnergyValue = 10 + (100 - 10) * random.nextDouble();
                            publishPhysicalAssetPropertyWldtEvent(new PhysicalAssetPropertyWldtEvent<>(ENERGY_PROPERTY_KEY, randomEnergyValue));
                        }

                        //At the end of telemetry messages send an overheating event
                        publishPhysicalAssetEventWldtEvent(new PhysicalAssetEventWldtEvent<String>(OVERHEATING_EVENT_KEY, "overheating-critical"));

                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }).start();
    }

    @Override
    public void onAdapterStop() {
        logger.info("DemoPhysicalAdapter Stopped !");
    }
}
