package it.wldt.core.state.event;

import it.wldt.core.state.*;
import it.wldt.exception.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests the events published on the event bus when the events of the Digital Twin State change. The definition of the
 * events is part of the structure (description): changes produce a description update event and no values event;
 * the notification of an event produces a values update event with its last notification and, after it,
 * the event notification itself.
 */
public class DigitalTwinStateWldtEventObserverTester {

    public final String DIGITAL_TWIN_ID = "dt00001";

    private final static String EVENT_KEY_1 = "testEventKey1";

    private final static String EVENT_KEY_2 = "testEventKey2";

    private final static String EVENT_TYPE = "testEventType";

    private final static String EVENT_TYPE_2 = "testEventType2";

    public static final String SUBSCRIBER_ID_1 = "testModuleSubscriber1";

    public DigitalTwinStateManager digitalTwinStateManager = null;

    private DigitalTwinStateEvent testNewEvent1;

    private void initTestDtState() throws WldtDigitalTwinStateException {
        if(digitalTwinStateManager == null) {
            //Init DigitaTwin State Manager
            digitalTwinStateManager = new DigitalTwinStateManager(DIGITAL_TWIN_ID);
        }
    }

    private void registerEvent() throws WldtDigitalTwinStateException {

        initTestDtState();

        testNewEvent1 = new DigitalTwinStateEvent(EVENT_KEY_1, EVENT_TYPE, EVENT_TYPE);

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.registerEvent(testNewEvent1);
        digitalTwinStateManager.commitStateTransaction();
    }

    @Test
    public void observeStateChanges() throws WldtDigitalTwinStateException, InterruptedException, EventBusException {

        initTestDtState();
        DigitalTwinStateDescription originalDescription = digitalTwinStateManager.getDigitalTwinStateDescription();

        StateEventCollector collector = new StateEventCollector(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1);
        collector.start();

        testNewEvent1 = new DigitalTwinStateEvent(EVENT_KEY_1, EVENT_TYPE, EVENT_TYPE);
        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.registerEvent(testNewEvent1);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(collector.awaitDescriptionEvents(1, 2000));
        collector.settle();

        assertEquals(digitalTwinStateManager.getDigitalTwinStateDescription(), collector.description(0));
        assertEquals(1, collector.description(0).getEvents().size());
        assertEquals(EVENT_KEY_1, collector.description(0).getEvents().get(0).getKey());
        assertEquals(EVENT_TYPE, collector.description(0).getEvents().get(0).getType());
        assertNotEquals(originalDescription.getId(), collector.description(0).getId());
        assertEquals(originalDescription, collector.previousDescription(0));

        assertEquals(1, collector.descriptionChanges(0).size());
        assertEquals(DigitalTwinStateChange.Operation.OPERATION_ADD, collector.descriptionChanges(0).get(0).getOperation());
        assertEquals(DigitalTwinStateChange.ResourceType.EVENT, collector.descriptionChanges(0).get(0).getResourceType());
        assertEquals(testNewEvent1, collector.descriptionChanges(0).get(0).getResource());

        assertTrue(collector.getValuesEvents().isEmpty());

        collector.stop();
    }

    @Test
    public void observeStateUpdatedEvent() throws WldtDigitalTwinStateException, InterruptedException, EventBusException {

        registerEvent();
        DigitalTwinStateDescription descriptionBefore = digitalTwinStateManager.getDigitalTwinStateDescription();

        StateEventCollector collector = new StateEventCollector(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1);
        collector.start();

        DigitalTwinStateEvent updatedEvent = new DigitalTwinStateEvent(EVENT_KEY_1, EVENT_TYPE_2, EVENT_TYPE_2);
        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.updateRegisteredEvent(updatedEvent);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(collector.awaitDescriptionEvents(1, 2000));
        collector.settle();

        assertEquals(1, collector.description(0).getEvents().size());
        assertEquals(EVENT_TYPE_2, collector.description(0).getEvents().get(0).getType());
        assertNotEquals(descriptionBefore.getId(), collector.description(0).getId());
        assertEquals(EVENT_TYPE, collector.previousDescription(0).getEvents().get(0).getType());

        assertEquals(1, collector.descriptionChanges(0).size());
        assertEquals(DigitalTwinStateChange.Operation.OPERATION_UPDATE, collector.descriptionChanges(0).get(0).getOperation());
        assertEquals(DigitalTwinStateChange.ResourceType.EVENT, collector.descriptionChanges(0).get(0).getResourceType());
        assertEquals(updatedEvent, collector.descriptionChanges(0).get(0).getResource());

        assertTrue(collector.getValuesEvents().isEmpty());

        collector.stop();
    }

    @Test
    public void observeStateDeletedEvent() throws WldtDigitalTwinStateException, InterruptedException, EventBusException {

        registerEvent();
        DigitalTwinStateDescription descriptionBefore = digitalTwinStateManager.getDigitalTwinStateDescription();

        StateEventCollector collector = new StateEventCollector(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1);
        collector.start();

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.unRegisterEvent(EVENT_KEY_1);
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(collector.awaitDescriptionEvents(1, 2000));
        collector.settle();

        assertTrue(collector.description(0).getEvents().isEmpty());
        assertNotEquals(descriptionBefore.getId(), collector.description(0).getId());
        assertEquals(descriptionBefore, collector.previousDescription(0));

        assertEquals(1, collector.descriptionChanges(0).size());
        assertEquals(DigitalTwinStateChange.Operation.OPERATION_REMOVE, collector.descriptionChanges(0).get(0).getOperation());
        assertEquals(DigitalTwinStateChange.ResourceType.EVENT, collector.descriptionChanges(0).get(0).getResourceType());
        assertEquals(testNewEvent1, collector.descriptionChanges(0).get(0).getResource());

        assertTrue(collector.getValuesEvents().isEmpty());

        collector.stop();
    }

    @Test
    public void observeEventNotification() throws WldtDigitalTwinStateException, WldtDigitalTwinStateEventNotificationException, InterruptedException, EventBusException {

        registerEvent();
        String descriptionIdBefore = digitalTwinStateManager.getDigitalTwinStateDescription().getId();

        StateEventCollector collector = new StateEventCollector(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1);
        collector.start();

        DigitalTwinStateEventNotification<String> notification = new DigitalTwinStateEventNotification<>(EVENT_KEY_1, "body", System.currentTimeMillis());

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.notifyDigitalTwinStateEvent(notification);

        // Nothing is published before the commit
        collector.settle();
        assertTrue(collector.getValuesEvents().isEmpty());
        assertTrue(collector.getNotificationEvents().isEmpty());

        digitalTwinStateManager.commitStateTransaction();

        assertTrue(collector.awaitValuesEvents(1, 2000));
        assertTrue(collector.awaitNotificationEvents(1, 2000));
        collector.settle();

        // The last notification is a value of the state, the structure does not change
        assertTrue(collector.getDescriptionEvents().isEmpty());
        assertEquals(descriptionIdBefore, collector.delta(0).getDescriptionId());
        assertNotNull(collector.delta(0).getLastEventNotifications().get(EVENT_KEY_1));
        assertEquals(DigitalTwinStateChange.Operation.OPERATION_UPDATE_VALUE, collector.valuesChanges(0).get(0).getOperation());
        assertEquals(DigitalTwinStateChange.ResourceType.EVENT, collector.valuesChanges(0).get(0).getResourceType());
        assertNotNull(digitalTwinStateManager.getDigitalTwinStateValues().getLastEventNotifications().get(EVENT_KEY_1));

        // The notification itself
        assertEquals(1, collector.getNotificationEvents().size());
        assertEquals(notification, collector.getNotificationEvents().get(0).getBody());
        assertEquals(DigitalTwinStateManager.getEventNotificationWldtEventMessageType(EVENT_KEY_1), collector.getNotificationEvents().get(0).getType());

        collector.stop();
    }

    @Test
    public void rollbackDiscardsEventNotification() throws WldtDigitalTwinStateException, WldtDigitalTwinStateEventNotificationException, InterruptedException, EventBusException {

        registerEvent();

        StateEventCollector collector = new StateEventCollector(DIGITAL_TWIN_ID, SUBSCRIBER_ID_1);
        collector.start();

        digitalTwinStateManager.startStateTransaction();
        digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(EVENT_KEY_1, "body", System.currentTimeMillis()));
        digitalTwinStateManager.rollbackTransaction();

        // A new committed transaction (a different change) must not publish the discarded notification
        digitalTwinStateManager.createProperty(new DigitalTwinStateProperty<>("p", 1));
        digitalTwinStateManager.commitStateTransaction();

        assertTrue(collector.awaitValuesEvents(1, 2000));
        collector.settle();

        assertTrue(collector.getNotificationEvents().isEmpty());
        assertTrue(digitalTwinStateManager.getDigitalTwinStateValues().getLastEventNotifications().isEmpty());

        collector.stop();
    }

    @Test
    public void notifyEventRequiresTransactionAndExistingEvent() throws WldtDigitalTwinStateException {

        registerEvent();

        // Outside of a transaction
        assertThrows(WldtDigitalTwinStateEventNotificationException.class,
                () -> digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(EVENT_KEY_1, "body", 0L)));

        // Event not defined in the state
        digitalTwinStateManager.startStateTransaction();
        assertThrows(WldtDigitalTwinStateEventNotificationException.class,
                () -> digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(EVENT_KEY_2, "body", 0L)));
        digitalTwinStateManager.rollbackTransaction();
    }
}
