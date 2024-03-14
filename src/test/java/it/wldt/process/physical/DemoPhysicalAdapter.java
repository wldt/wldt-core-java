package it.wldt.process.physical;

import it.wldt.adapter.physical.*;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.core.event.WldtEventBus;
import it.wldt.process.metrics.SharedTestMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Random;

public class DemoPhysicalAdapter extends ConfigurablePhysicalAdapter<DemoPhysicalAdapterConfiguration> {

    private static final Logger logger = LoggerFactory.getLogger(DemoPhysicalAdapter.class);

    public static final int DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES = 10;

    public static final int DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES = 2;

    public static long DEFAULT_MESSAGE_SLEEP_PERIOD_MS = 1000;

    public static final String ENERGY_PROPERTY_KEY = "energy";

    public static final String SWITCH_PROPERTY_KEY = "switch";

    public static final String SWITCH_OFF_ACTION_KEY = "switch_off";

    public static final String SWITCH_ON_ACTION_KEY = "switch_on";

    public static final String OVERHEATING_EVENT_KEY = "overheating";

    private int propertyUpdateMessageLimit = DEFAULT_TARGET_PHYSICAL_ASSET_PROPERTY_UPDATE_MESSAGES;

    private int eventUpdateMessageLimit = DEFAULT_TARGET_PHYSICAL_ASSET_EVENT_UPDATES;

    private long messageSleepPeriodMs = DEFAULT_MESSAGE_SLEEP_PERIOD_MS;

    private boolean isTelemetryOn = false;

    private Random random = new Random();

    public DemoPhysicalAdapter(String id, DemoPhysicalAdapterConfiguration configuration) {
        super(id, configuration);
    }

    public DemoPhysicalAdapter(String id, DemoPhysicalAdapterConfiguration configuration, boolean isTelemetryOn) {
        super(id, configuration);
        this.isTelemetryOn = isTelemetryOn;
    }

    public DemoPhysicalAdapter(String id,
                               DemoPhysicalAdapterConfiguration configuration,
                               boolean isTelemetryOn,
                               int targetPropertyUpdateMessageLimit,
                               int targetEventUpdateMessageLimit,
                               long targetMessageSleepPeriodMs) {
        super(id, configuration);
        this.isTelemetryOn = isTelemetryOn;
        this.propertyUpdateMessageLimit = targetPropertyUpdateMessageLimit;
        this.eventUpdateMessageLimit = targetEventUpdateMessageLimit;
        this.messageSleepPeriodMs = targetMessageSleepPeriodMs;
    }

    @Override
    public void onIncomingPhysicalAction(PhysicalAssetActionWldtEvent<?> physicalActionEvent) {
        try{
            logger.info("Received PhysicalActionEventMessage: {}", physicalActionEvent);

            if(physicalActionEvent != null && physicalActionEvent.getActionKey().equals(SWITCH_ON_ACTION_KEY)) {
                logger.info("{} Received ! Switching ON the device ...", physicalActionEvent.getType());
                Thread.sleep(this.messageSleepPeriodMs);
                PhysicalAssetPropertyWldtEvent<String> event = new PhysicalAssetPropertyWldtEvent<>(SWITCH_PROPERTY_KEY, "ON");
                WldtEventBus.getInstance().publishEvent(digitalTwinId, getId(), event);
                SharedTestMetrics.getInstance().addPhysicalAdapterPropertyEvent(digitalTwinId, event);
            } else if(physicalActionEvent != null && physicalActionEvent.getActionKey().equals(SWITCH_OFF_ACTION_KEY)){
                logger.info("{} Received ! Switching OFF the device ...", physicalActionEvent.getType());
                Thread.sleep(this.messageSleepPeriodMs);
                PhysicalAssetPropertyWldtEvent<String> event = new PhysicalAssetPropertyWldtEvent<>(SWITCH_PROPERTY_KEY, "OFF");
                WldtEventBus.getInstance().publishEvent(digitalTwinId, getId(), event);
                SharedTestMetrics.getInstance().addPhysicalAdapterPropertyEvent(digitalTwinId, event);
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
                        Thread.sleep(messageSleepPeriodMs);
                        publishPhysicalAssetEventWldtEvent(new PhysicalAssetEventWldtEvent<String>(OVERHEATING_EVENT_KEY, "overheating-low"));

                        for(int i = 0; i< propertyUpdateMessageLimit; i++){

                            Thread.sleep(messageSleepPeriodMs);
                            double randomEnergyValue = 10 + (100 - 10) * random.nextDouble();
                            PhysicalAssetPropertyWldtEvent<Double> event = new PhysicalAssetPropertyWldtEvent<>(ENERGY_PROPERTY_KEY, randomEnergyValue);
                            publishPhysicalAssetPropertyWldtEvent(event);
                            SharedTestMetrics.getInstance().addPhysicalAdapterPropertyEvent(digitalTwinId, event);
                        }

                        //At the end of telemetry messages send an overheating event
                        PhysicalAssetEventWldtEvent<String> event = new PhysicalAssetEventWldtEvent<String>(OVERHEATING_EVENT_KEY, "overheating-critical");
                        publishPhysicalAssetEventWldtEvent(event);
                        SharedTestMetrics.getInstance().addPhysicalAdapterEventNotification(digitalTwinId, event);

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
