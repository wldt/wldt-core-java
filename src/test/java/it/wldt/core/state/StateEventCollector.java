package it.wldt.core.state;

import it.wldt.core.event.WldtEvent;
import it.wldt.core.event.WldtEventBus;
import it.wldt.core.event.WldtEventFilter;
import it.wldt.core.event.WldtEventListener;
import it.wldt.core.event.WldtEventTypes;
import it.wldt.exception.EventBusException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Test utility that subscribes to the topics of the Digital Twin State on the event bus (description updates,
 * values updates and event notifications) and keeps the received events, separated by kind, with accessors
 * to the body and to the metadata of each one.
 */
public class StateEventCollector implements WldtEventListener {

    private final String digitalTwinId;
    private final String subscriberId;
    private final WldtEventFilter filter = new WldtEventFilter();

    private final List<WldtEvent<?>> descriptionEvents = new CopyOnWriteArrayList<>();
    private final List<WldtEvent<?>> valuesEvents = new CopyOnWriteArrayList<>();
    private final List<WldtEvent<?>> notificationEvents = new CopyOnWriteArrayList<>();

    public StateEventCollector(String digitalTwinId, String subscriberId) {
        this.digitalTwinId = digitalTwinId;
        this.subscriberId = subscriberId;
        this.filter.add(WldtEventTypes.DT_STATE_DESCRIPTION_UPDATE_MESSAGE_EVENT_TYPE);
        this.filter.add(WldtEventTypes.DT_STATE_VALUES_UPDATE_MESSAGE_EVENT_TYPE);
        this.filter.add(WldtEventTypes.ALL_DT_STATE_EVENT_NOTIFICATION_EVENT_TYPE);
    }

    /**
     * Subscribes the collector. It first lets the events published before (e.g. by the preparation of the test) be
     * delivered, so they are not mixed with the ones the test is interested in.
     */
    public void start() throws EventBusException {
        settle();
        WldtEventBus.getInstance().subscribe(digitalTwinId, subscriberId, filter, this);
    }

    public void stop() throws EventBusException {
        WldtEventBus.getInstance().unSubscribe(digitalTwinId, subscriberId, filter, this);
    }

    @Override
    public void onEventSubscribed(String eventType) {
    }

    @Override
    public void onEventUnSubscribed(String eventType) {
    }

    @Override
    public void onEvent(WldtEvent<?> wldtEvent) {
        if (wldtEvent == null || wldtEvent.getType() == null)
            return;

        if (wldtEvent.getType().equals(WldtEventTypes.DT_STATE_DESCRIPTION_UPDATE_MESSAGE_EVENT_TYPE))
            descriptionEvents.add(wldtEvent);
        else if (wldtEvent.getType().equals(WldtEventTypes.DT_STATE_VALUES_UPDATE_MESSAGE_EVENT_TYPE))
            valuesEvents.add(wldtEvent);
        else
            notificationEvents.add(wldtEvent);
    }

    private static boolean await(List<?> list, int count, long timeoutMs) {
        long end = System.currentTimeMillis() + timeoutMs;
        while (System.currentTimeMillis() < end) {
            if (list.size() >= count)
                return true;
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
        return list.size() >= count;
    }

    public boolean awaitDescriptionEvents(int count, long timeoutMs) {
        return await(descriptionEvents, count, timeoutMs);
    }

    public boolean awaitValuesEvents(int count, long timeoutMs) {
        return await(valuesEvents, count, timeoutMs);
    }

    public boolean awaitNotificationEvents(int count, long timeoutMs) {
        return await(notificationEvents, count, timeoutMs);
    }

    /**
     * Gives the time to the events already published to be delivered, to check that something has NOT been published
     */
    public void settle() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public List<WldtEvent<?>> getDescriptionEvents() {
        return new ArrayList<>(descriptionEvents);
    }

    public List<WldtEvent<?>> getValuesEvents() {
        return new ArrayList<>(valuesEvents);
    }

    public List<WldtEvent<?>> getNotificationEvents() {
        return new ArrayList<>(notificationEvents);
    }

    public DigitalTwinStateDescription description(int index) {
        return (DigitalTwinStateDescription) descriptionEvents.get(index).getBody();
    }

    public DigitalTwinStateDescription previousDescription(int index) {
        return (DigitalTwinStateDescription) descriptionEvents.get(index).getMetadata(DigitalTwinStateManager.DT_STATE_DESCRIPTION_UPDATE_METADATA_PREVIOUS_DESCRIPTION).get();
    }

    @SuppressWarnings("unchecked")
    public List<DigitalTwinStateChange> descriptionChanges(int index) {
        return (List<DigitalTwinStateChange>) descriptionEvents.get(index).getMetadata(DigitalTwinStateManager.DT_STATE_DESCRIPTION_UPDATE_METADATA_CHANGE_LIST).get();
    }

    public DigitalTwinStateValues delta(int index) {
        return (DigitalTwinStateValues) valuesEvents.get(index).getBody();
    }

    public DigitalTwinStateValues previousValues(int index) {
        return (DigitalTwinStateValues) valuesEvents.get(index).getMetadata(DigitalTwinStateManager.DT_STATE_VALUES_UPDATE_METADATA_PREVIOUS_VALUES).get();
    }

    @SuppressWarnings("unchecked")
    public List<DigitalTwinStateChange> valuesChanges(int index) {
        return (List<DigitalTwinStateChange>) valuesEvents.get(index).getMetadata(DigitalTwinStateManager.DT_STATE_VALUES_UPDATE_METADATA_CHANGE_LIST).get();
    }
}
