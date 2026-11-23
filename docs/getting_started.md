# Getting started

This section is dedicated to the creation of a super simple Digital Twin using the WLDT library.
Following the steps described in this section, the developer will be able to create a simple Digital Twin
The steps that we have to follow in order to create our first (and super simple) Digital Twin are the following:

- [Physical Adapter](#physical-adapter)
- [`DigitalTwinModel`](#shadowing-function)
- [Digital Adapter](#digital-adapter)
- [Digital Twin Engine & Process](#digital-twin-process)
- [Digital Action Management](#digital-action-management)

## Physical Adapter

The developer can use an existing Physical Adapter or create a new one to handle the communication with a specific physical twin.
In this documentation we focus on the creation of a new Physical Adapter in order to explain library core functionalities.
However, existing Physical Adapters can be found on the official repository and linked in the core documentation and webpage ([WLDT-GitHub](https://github.com/wldt)).

In general WLDT Physical Adapter extends the class ``PhysicalAdapter`` and it is responsible to talk with the physical world and handling the following main tasks:
- Generate a PAD describing the properties, events, actions and relationships available on the physical twin using the class ``PhysicalAssetDescription``
- Generate Physical Event using the class ``PhysicalAssetEventWldtEvent`` associated to the variation of any aspect of the physical state (properties, events, and relationships)
- Handle action request coming from the Digital World through the DT `DigitalTwinModel` by implementing the method ``onIncomingPhysicalAction`` and processing events modeled through the class ``PhysicalAssetActionWldtEvent``

Create a new class called ``TestPhysicalAdapter`` extending the library class ``PhysicalAdapter`` and implement the following methods:
- ``onAdapterStart``: A callback method used to notify when the adapter has been effectively started withing the DT's life cycle
- ``onAdapterStop``: A call method invoked when the adapter has been stopped and will be dismissed by the core
- ``onIncomingPhysicalAction``: The callback method called when a new ``PhysicalAssetActionWldtEvent`` is sent by the `DigitalTwinModel` upon the receiving of a valid Digital Action through a Digital Adapter

Then you have to create a constructor for your Physical Adapter with a single String parameter representing the id of the adapter.
This id will be used internally by the library to handle and coordinate multiple adapters, adapts logs and execute functions upon the arrival of a new event.
The resulting empty class will the following:

```java
public class TestPhysicalAdapter extends PhysicalAdapter {

    public TestPhysicalAdapter(String id) {
        super(id);
    }

    @Override
    public void onIncomingPhysicalAction(PhysicalAssetActionWldtEvent<?> physicalAssetActionWldtEvent) {
        
    }

    @Override
    public void onAdapterStart() {

    }

    @Override
    public void onAdapterStop() {

    }
}
```
In our test Physical Adapter example we are going to emulate the communication with an Internet of Things device with the following sensing and actuation characteristics:

- A Temperature Sensor generating data about new measurements
- The possibility to generate OVER-HEATING events
- An action to set the target desired temperature resource

The first step will be to generate and publish the ``PhysicalAssetDescription`` (PAD) to describe the capabilities and the characteristics of our object allowing
the `DigitalTwinModel` to decide how to digitalize its physical counterpart.
***Of course in our case the PAD is generated manually but according to the nature of the
connected physical twin it can be automatically generated starting from a discovery or a configuration passed to the adapter.***

The generation of the PAD for each active Physical Adapter is the fundamental DT process to handle the binding procedure
and to allow the `DigitalTwinModel` and consequently the core of the twin to be aware of what is available in the physical world and
consequently decide what to observe and digitalize.

In order to publish the PAD we can update the onAdapterStart method with the following lines of code:

```java
private final static String TEMPERATURE_PROPERTY_KEY = "temperature-property-key";
private final static String OVERHEATING_EVENT_KEY = "overheating-event-key";
private final static String SET_TEMPERATURE_ACTION_KEY = "set-temperatura-action-key";

@Override
public void onAdapterStart() {
    try {
        //Create an empty PAD
        PhysicalAssetDescription pad = new PhysicalAssetDescription();
        
        //Add a new Property associated to the target PAD with a key and a default resource
        //By default 'type' is set equal to the key and 'contentType' is derived from the value's Class (e.g. java.lang.Double);
        //use the (key, type, contentType, initialValue[, immutable, writable]) constructors to set them explicitly
        PhysicalAssetProperty<Double> temperatureProperty = new PhysicalAssetProperty<Double>(TEMPERATURE_PROPERTY_KEY, 0.0);
        pad.getProperties().add(temperatureProperty);
        
        //Add the declaration of a new type of generated event associated to an event key, a domain-specific type
        // and the content-type of the generated payload
        PhysicalAssetEvent overheatingEvent = new PhysicalAssetEvent(OVERHEATING_EVENT_KEY, "temperature.overheating", "text/plain");
        pad.getEvents().add(overheatingEvent);
        
        //Declare the availability of a target action characterized by a Key, an action type
        // and the expected content type and the request body
        PhysicalAssetAction setTemperatureAction = new PhysicalAssetAction(SET_TEMPERATURE_ACTION_KEY, "temperature.actuation", "text/plain");
        pad.getActions().add(setTemperatureAction);
        
        //Notify the new PAD to the DT's `DigitalTwinModel`
        this.notifyPhysicalAdapterBound(pad);
        
        //TODO add here the Device Emulatio method 
        
    } catch (PhysicalAdapterException | EventBusException e) {
        e.printStackTrace();
    }
}
```

Now we need a simple code to emulate the generation of new temperature measurements and over-heating events.
In a real Physical Adapter implementation we have to implement the real communication with the physical twin in
order to read its state variation over time according to the supported protocols.
In our simplified Physical Adapter we can the following function:

```java
private final static int MESSAGE_UPDATE_TIME = 1000;
private final static int MESSAGE_UPDATE_NUMBER = 10;
private final static double TEMPERATURE_MIN_VALUE = 20;
private final static double TEMPERATURE_MAX_VALUE = 30;

private Runnable deviceEmulation(){
    return () -> {
        try {

            //Sleep 5 seconds to emulate device startup
            Thread.sleep(5000);
            
            //Create a new random object to emulate temperature variations
            Random r = new Random();
            
            //Publish an initial Event for a normal condition
            publishPhysicalAssetEventWldtEvent(new PhysicalAssetEventWldtEvent<>(OVERHEATING_EVENT_KEY, "normal"));
            
            //Emulate the generation on 'n' temperature measurements
            for(int i = 0; i < MESSAGE_UPDATE_NUMBER; i++){

                //Sleep to emulate sensor measurement
                Thread.sleep(MESSAGE_UPDATE_TIME);
                
                //Update the 
                double randomTemperature = TEMPERATURE_MIN_VALUE + (TEMPERATURE_MAX_VALUE - TEMPERATURE_MIN_VALUE) * r.nextDouble(); 
                
                //Create a new event to notify the variation of a Physical Property
                PhysicalAssetPropertyWldtEvent<Double> newPhysicalPropertyEvent = new PhysicalAssetPropertyWldtEvent<>(TEMPERATURE_PROPERTY_KEY, randomTemperature);
                
                //Publish the WLDTEvent associated to the Physical Property Variation
                publishPhysicalAssetPropertyWldtEvent(newPhysicalPropertyEvent);
            }
            
            //Publish a demo Physical Event associated to a 'critical' overheating condition
            publishPhysicalAssetEventWldtEvent(new PhysicalAssetEventWldtEvent<>(OVERHEATING_EVENT_KEY, "critical"));

        } catch (EventBusException | InterruptedException e) {
            e.printStackTrace();
        }
    };
}
```

Now we have to call the ``deviceEmulationFunction()`` inside the ``onAdapterStart()`` triggering its execution and emulating the physical counterpart of our DT.
To do that add the following line at the end of the ``onAdapterStart()`` method after the ``this.notifyPhysicalAdapterBound(pad);``.

The last step will be to handle an incoming action trying to set a new temperature on the device by implementing the method ``onIncomingPhysicalAction()``.
This method will receive a ``PhysicalAssetActionWldtEvent<?> physicalAssetActionWldtEvent`` associated to the action request generated by the `DigitalTwinModel`.
Since a Physical Adapter can handle multiple action we have to check both ``action-key`` and ``body`` type in order to properly process the action (in our case just logging the request).
The new update method will result like this:

```java
@Override
public void onIncomingPhysicalAction(PhysicalAssetActionWldtEvent<?> physicalAssetActionWldtEvent) {
    try{

        if(physicalAssetActionWldtEvent != null
                && physicalAssetActionWldtEvent.getActionKey().equals(SET_TEMPERATURE_ACTION_KEY)
                && physicalAssetActionWldtEvent.getBody() instanceof String) {
            System.out.println("Received Action Request: " + physicalAssetActionWldtEvent.getActionKey()
                    + " with Body: " + physicalAssetActionWldtEvent.getBody());
        }
        else
            System.err.println("Wrong Action Received !");

    }catch (Exception e){
        e.printStackTrace();
    }
}
```

The overall class will result as following:

```java
import it.wldt.adapter.physical.*;
import it.wldt.adapter.physical.event.PhysicalAssetActionWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.exception.EventBusException;
import it.wldt.exception.PhysicalAdapterException;

import java.util.Random;

public class TestPhysicalAdapter extends PhysicalAdapter {

    private final static String TEMPERATURE_PROPERTY_KEY = "temperature-property-key";
    private final static String OVERHEATING_EVENT_KEY = "overheating-event-key";
    private final static String SET_TEMPERATURE_ACTION_KEY = "set-temperature-action-key";

    private final static int MESSAGE_UPDATE_TIME = 1000;
    private final static int MESSAGE_UPDATE_NUMBER = 10;
    private final static double TEMPERATURE_MIN_VALUE = 20;
    private final static double TEMPERATURE_MAX_VALUE = 30;

    public TestPhysicalAdapter(String id) {
        super(id);
    }

    @Override
    public void onIncomingPhysicalAction(PhysicalAssetActionWldtEvent<?> physicalAssetActionWldtEvent) {
        try{

            if(physicalAssetActionWldtEvent != null
                    && physicalAssetActionWldtEvent.getActionKey().equals(SET_TEMPERATURE_ACTION_KEY)
                    && physicalAssetActionWldtEvent.getBody() instanceof Double) {
                System.out.println("Received Action Request: " + physicalAssetActionWldtEvent.getActionKey()
                        + " with Body: " + physicalAssetActionWldtEvent.getBody());
            }
            else
                System.err.println("Wrong Action Received !");

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onAdapterStart() {
        try {
            //Create an empty PAD
            PhysicalAssetDescription pad = new PhysicalAssetDescription();

            //Add a new Property associated to the target PAD with a key and a default resource
            //By default 'type' is set equal to the key and 'contentType' is derived from the value's Class (e.g. java.lang.Double);
            //use the (key, type, contentType, initialValue[, immutable, writable]) constructors to set them explicitly
            PhysicalAssetProperty<Double> temperatureProperty = new PhysicalAssetProperty<Double>(TEMPERATURE_PROPERTY_KEY, 0.0);
            pad.getProperties().add(temperatureProperty);

            //Add the declaration of a new type of generated event associated to an event key, a domain-specific type
            // and the content-type of the generated payload
            PhysicalAssetEvent overheatingEvent = new PhysicalAssetEvent(OVERHEATING_EVENT_KEY, "temperature.overheating", "text/plain");
            pad.getEvents().add(overheatingEvent);

            //Declare the availability of a target action characterized by a Key, an action type
            // and the expected content type and the request body
            PhysicalAssetAction setTemperatureAction = new PhysicalAssetAction(SET_TEMPERATURE_ACTION_KEY, "temperature.actuation", "text/plain");
            pad.getActions().add(setTemperatureAction);

            //Notify the new PAD to the DT's `DigitalTwinModel`
            this.notifyPhysicalAdapterBound(pad);

            //Start Device Emulation
            new Thread(deviceEmulation()).start();

        } catch (PhysicalAdapterException | EventBusException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAdapterStop() {

    }

    private Runnable deviceEmulation(){
        return () -> {
            try {

                //Sleep 5 seconds to emulate device startup
                Thread.sleep(5000);

                //Create a new random object to emulate temperature variations
                Random r = new Random();

                //Publish an initial Event for a normal condition
                publishPhysicalAssetEventWldtEvent(new PhysicalAssetEventWldtEvent<>(OVERHEATING_EVENT_KEY, "normal"));

                //Emulate the generation on 'n' temperature measurements
                for(int i = 0; i < MESSAGE_UPDATE_NUMBER; i++){

                    //Sleep to emulate sensor measurement
                    Thread.sleep(MESSAGE_UPDATE_TIME);

                    //Update the
                    double randomTemperature = TEMPERATURE_MIN_VALUE + (TEMPERATURE_MAX_VALUE - TEMPERATURE_MIN_VALUE) * r.nextDouble();

                    //Create a new event to notify the variation of a Physical Property
                    PhysicalAssetPropertyWldtEvent<Double> newPhysicalPropertyEvent = new PhysicalAssetPropertyWldtEvent<>(TEMPERATURE_PROPERTY_KEY, randomTemperature);

                    //Publish the WLDTEvent associated to the Physical Property Variation
                    publishPhysicalAssetPropertyWldtEvent(newPhysicalPropertyEvent);
                }

                //Publish a demo Physical Event associated to a 'critical' overheating condition
                publishPhysicalAssetEventWldtEvent(new PhysicalAssetEventWldtEvent<>(OVERHEATING_EVENT_KEY, "critical"));

            } catch (EventBusException | InterruptedException e) {
                e.printStackTrace();
            }
        };
    }
}
```

Both Physical Adapters and Digital Adapters can be defined natively with a custom configuration provided by the developer
as illustrated in the dedicated Section: [Configurable Physical & Digital Adapters](#configurable-physical-and-digital-adapters).

## DigitalTwinModel - Handling the Shadowing Process

After the definition of the Physical Adapter it is time to start implementing the core of our DT through the definition of
the functions characterizing the **shadowing process** through the class `DigitalTwinModel` function in charge of:

- Handle received PAD from Physical Adapters in order to device which properties, events, relationships or actions available on connected physical twins should be mapped and managed into the DT State
- Manage incoming notifications/callbacks associated to the variation of physical properties (e.g, temperature variation) or the generation of physical event (e.g., overheating)
- Process action requests from the digital world that should be validated and forward to the correct Physical Adapter in order to trigger the associated actions on the physical world

The `DigitalTwinModel` has the responsibility to build and maintain the updated state of the Digital Twin.
The state is owned by a ```DigitalTwinStateManager```, that the `DigitalTwinModel` can access through the variable called: ```digitalTwinStateManager```.

The Digital Twin State is made of two distinct parts, that travel and are read **separately** (the complete state is internal to the manager and is never exposed):

- The **description** (```DigitalTwinStateDescription```): the *structure* of the state, i.e., which properties, actions, events and relationships are defined and how (key/name, type, content type, access flags), without any value.
  Every description has a unique **id** with the format ```<digitalTwinCreationTimestamp>_<numSeq>```: the creation timestamp (epoch milliseconds) of the state manager followed by a sequence number that starts from 0
  and grows every time the structure of the state *really* changes (an update that leaves the structure identical does not change it).
- The **values** (```DigitalTwinStateValues```): the current value of each property, the instances of each relationship, and the instant of the last invocation of each action and of the last notification of each event.
  The values always carry the **id of the description** they belong to (```getDescriptionId()```).

- **Reading** the state: ```this.digitalTwinStateManager.getDigitalTwinStateDescription()``` returns the current description and ```this.digitalTwinStateManager.getDigitalTwinStateValues()``` the current (complete) values.
  No method of the manager returns the complete ```DigitalTwinState```.
- **Writing** the state: all the edits are performed on the manager through a **transaction**. After calling ```startStateTransaction()```
  the edit methods collect the changes, and ```commitStateTransaction()``` applies all of them together and publishes the resulting state updates on the event bus
  (see [Digital Twin State events](#digital-twin-state-events-on-the-event-bus)). ```rollbackTransaction()``` discards the collected changes (and the pending event notifications).
  Calling an edit method without an open transaction throws a ```WldtDigitalTwinStateException```.

Available main read methods on the ```DigitalTwinStateDescription``` are:

- ```getId()```: The unique id of the description
- ```getProperties()```, ```getActions()```, ```getEvents()```, ```getRelationships()```: The (unmodifiable) lists of ```PropertyDescription``` (key, type, content type, readable, writable, exposed), ```ActionDescription```, ```EventDescription``` and ```RelationshipDescription``` (name, type)
- ```isEmpty()``` and ```isSameStructureAs(DigitalTwinStateDescription other)``` (descriptions are equal if they have the same structure, regardless of id and evaluation instant)

Available main read methods on the ```DigitalTwinStateValues``` are:

- ```getDescriptionId()```: The id of the description these values are associated to
- ```getPropertyValues()``` / ```getPropertyValue(String key)```: The property values by key
- ```getRelationshipInstances()```: The instances of each relationship by relationship name (a relationship without instances has an empty list)
- ```getLastActionInvocations()``` / ```getLastEventNotifications()```: The instant of the last invocation of each action / last notification of each event (only the ones that happened)
- ```getEvaluationInstant()``` and ```isEmpty()```

Available main edit methods on the ```DigitalTwinStateManager``` (to be called between ```startStateTransaction()``` and ```commitStateTransaction()```) are:

- Properties:
    - ```createProperty(DigitalTwinStateProperty<?> dtStateProperty)```: Allows the creation of a new Property on the Digital Twin's State through the class DigitalTwinStateProperty
    - ```updateProperty(DigitalTwinStateProperty<?> dtStateProperty)```: Replaces the whole target property (value, type and content type) using the DigitalTwinStateProperty and the associated Property Key field
    - ```updatePropertyValue(DigitalTwinStateProperty<?> dtStateProperty)```: Updates only the value of the target property, keeping its type and content type
    - ```deleteProperty(String propertyKey)```: Deletes the target property identified by the specified key
- Actions:
    - ```enableAction(DigitalTwinStateAction digitalTwinStateAction)```: Enables and registers the target Action described through an instance of the DigitalTwinStateAction class
    - ```updateAction(DigitalTwinStateAction digitalTwinStateAction)```: Update the already registered target Action described through an instance of the DigitalTwinStateAction class
    - ```disableAction(String actionKey)```: Disables and unregisters the target Action described through an instance of the DigitalTwinStateAction class
- Events:
    - ```registerEvent(DigitalTwinStateEvent digitalTwinStateEvent)```: Register a new Digital Twin State Event
    - ```updateRegisteredEvent(DigitalTwinStateEvent digitalTwinStateEvent)```: Update the registration and signature of an existing Digital Twin State Event
    - ```unRegisterEvent(String eventKey)```: Un-register a Digital Twin State Event
- Relationships:
    - ```createRelationship(DigitalTwinStateRelationship<?> relationship)```: Creates a new Relationships (described by the class DigitalTwinStateRelationship) in the Digital Twin's State
    - ```addRelationshipInstance(DigitalTwinStateRelationshipInstance<?> instance)```: Adds a new Relationship instance described through the class DigitalTwinStateRelationshipInstance (the relationship name is part of the instance)
    - ```deleteRelationship(String name)```: Deletes a target Relationship identified through its name
    - ```deleteRelationshipInstance(String relationshipName, String instanceKey)```: Deletes the target Relationship Instance using relationship name and instance Key

Two more methods of the manager must be called **inside a transaction** and update the *last triggered* information, that is part of the state values:

- ```notifyDigitalTwinStateEvent(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification)```: Notifies the occurrence of the target Digital Twin State Event (its key must be defined in the state). The instant of the last notification of the event becomes a value of the state
  (a change with ```OPERATION_UPDATE_VALUE``` on the ```EVENT``` resource) and the notification itself is published on the event bus **at commit time**, after the description and values updates. A rolled back transaction publishes nothing.
- ```notifyActionExecuted(String actionKey)``` / ```notifyActionExecuted(String actionKey, Instant executionInstant)```: Notifies that an action has been executed (by default now). The instant of the last invocation of the action becomes a value of the state
  (a change with ```OPERATION_UPDATE_VALUE``` on the ```ACTION``` resource). The invocation of a Digital Action does **not** update it automatically: the Shadowing Function calls this method when it considers the action executed, for example after publishing the physical action.

The basic library class that we are going to extend is called ```DigitalTwinModel``` and creating a new class named ```TestDigitalTwinModel``` the resulting
code is the same after implementing required methods the basic constructor with the id String parameter.

```java
import it.wldt.adapter.digital.event.DigitalActionWldtEvent;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.adapter.physical.event.PhysicalAssetEventWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetPropertyWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceCreatedWldtEvent;
import it.wldt.adapter.physical.event.PhysicalAssetRelationshipInstanceDeletedWldtEvent;
import it.wldt.core.model.DigitalTwinModel;
import java.util.Map;

public class TestDigitalTwinModel extends DigitalTwinModel {

    public TestDigitalTwinModel(String id) {
        super(id);
    }

    //// `DigitalTwinModel` Management Callbacks ////

    @Override
    protected void onCreate() {

    }

    @Override
    protected void onStart() {

    }

    @Override
    protected void onStop() {

    }

    //// Bound LifeCycle State Management Callbacks ////

    @Override
    protected void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap) {

    }

    @Override
    protected void onDigitalTwinUnBound(Map<String, PhysicalAssetDescription> map, String s) {

    }

    @Override
    protected void onPhysicalAdapterBidingUpdate(String s, PhysicalAssetDescription physicalAssetDescription) {

    }

    //// Physical Property Variation Callback ////

    @Override
    protected void onPhysicalAssetPropertyVariation(PhysicalAssetPropertyWldtEvent<?> physicalAssetPropertyWldtEvent) {

    }

    //// Physical Event Notification Callback ////

    @Override
    protected void onPhysicalAssetEventNotification(PhysicalAssetEventWldtEvent<?> physicalAssetEventWldtEvent) {

    }

    //// Physical Relationships Notification Callbacks ////

    @Override
    protected void onPhysicalAssetRelationshipEstablished(PhysicalAssetRelationshipInstanceCreatedWldtEvent<?> physicalAssetRelationshipInstanceCreatedWldtEvent) {

    }

    @Override
    protected void onPhysicalAssetRelationshipDeleted(PhysicalAssetRelationshipInstanceDeletedWldtEvent<?> physicalAssetRelationshipInstanceDeletedWldtEvent) {

    }

    //// Digital Action Received Callbacks ////

    @Override
    protected void onDigitalActionEvent(DigitalActionWldtEvent<?> digitalActionWldtEvent) {

    }
}
```

The methods ```onCreate()```, ```onStart()``` and ```onStop()``` are used to receive callbacks from the DT's core when the `DigitalTwinModel` has been effectively created within the twin, is started or stopped
according to the evolution of its life cycle. In our initial implementation we are not implementing any of them but they can be useful to trigger specific behaviours according to the different phases.

The first method that we have to implement in order to analyze received PAD and build the Digital Twin State in terms of properties, events, relationships and available actions is
the ```onDigitalTwinBound(Map<String, PhysicalAssetDescription> map)``` method. In our initial implementation we just pass through all the received characteristics recevied from each connected
Physical Adapter mapping every physical entity into the DT's state without any change or adaptation (Of course complex behaviour can be implemented to customized the digitalization process).

Through the following method we implement the following behaviour:

- Analyze each received PAD from each connected and active Physical Adapter (in our case we will have just 1 Physical Adapter)
- Iterate over all the received Properties for each PAD and create the same Property on the Digital Twin State
- Start observing target Physical Properties in order to receive notification callback about physical variation through the method ```observePhysicalAssetProperty(property);```
- Analyze received PAD's Events declaration and recreates them also on the DT's State
- Start observing target Physical Event in order to receive notification callback about physical event generation through the method ```observePhysicalAssetEvent(event);```
- Check available Physical Action and enable them on the DT's State. Enabled Digital Action are automatically observed by the `DigitalTwinModel` in order to receive action requests from active Digital Adapters

**`type` and `contentType`.** Properties, events and actions are described by two separate fields, both in the
Physical Asset Description (`PhysicalAssetProperty`, `PhysicalAssetEvent`, `PhysicalAssetAction`) and in the Digital Twin State
(`DigitalTwinStateProperty`, `DigitalTwinStateEvent`, `DigitalTwinStateAction`):

| Field | Meaning | Example |
|-------|---------|---------|
| `type` | **Semantic** type: what the resource represents (ontology / application domain) | `iot.demo.accelerometer` |
| `contentType` | **Format** used to represent the value or the payload | `string`, `application/json`, `java.lang.Double` |

For example, a string coming from the accelerometer of a specific device has `type = "iot.demo.accelerometer"` and `contentType = "string"`.
When they are not provided, the `contentType` of a property defaults to the name of the Class of its value, while the
`type` of a State Property keeps defaulting to the same Class name for backward compatibility. From version 0.8.1 the
constructors that set only the `type` were deprecated and have been **removed in version 0.9.0**: always provide both
`type` and `contentType` (see `docs/changelogs/v0.8.1.md` and `docs/changelogs/v0.9.0.md`).

The possibility to manually observe Physical Properties and Event has been introduced to allow the `DigitalTwinModel` to decide what
to do according to the nature of the property or of the target event. For example in some cases with static properties it will not be necessary
to observe any variation and it will be enough to read the initial resource to build the digital replica of that specific property.

```java
@Override
protected void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap) {

    try{

        //Iterate over all the received PAD from connected Physical Adapters
        adaptersPhysicalAssetDescriptionMap.values().forEach(pad -> {

            //Iterate over available declared Physical Properties for the target Physical Adapter's PAD
            pad.getProperties().forEach(property -> {
                try {

                    //Create and write the property on the DT's State keeping the semantic type (e.g., "iot.demo.temperature")
                    //and the content type (the format of the value) declared by the Physical Adapter.
                    //Every edit of the State is performed inside a transaction
                    this.digitalTwinStateManager.startStateTransaction();
                    this.digitalTwinStateManager.createProperty(new DigitalTwinStateProperty<>(property.getKey(), (Double) property.getInitialValue(), property.getType(), property.getContentType()));
                    this.digitalTwinStateManager.commitStateTransaction();

                    //Start observing the variation of the physical property in order to receive notifications
                    //Without this call the `DigitalTwinModel` will not receive any notifications or callback about
                    //incoming physical property of the target type and with the target key
                    this.observePhysicalAssetProperty(property);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            //Iterate over available declared Physical Events for the target Physical Adapter's PAD
            pad.getEvents().forEach(event -> {
                try {

                    //Instantiate a new DT State Event with the same key, semantic type and content type
                    DigitalTwinStateEvent dtStateEvent = new DigitalTwinStateEvent(event.getKey(), event.getType(), event.getContentType());

                    //Create and write the event on the DT's State
                    this.digitalTwinStateManager.startStateTransaction();
                    this.digitalTwinStateManager.registerEvent(dtStateEvent);
                    this.digitalTwinStateManager.commitStateTransaction();

                    //Start observing the variation of the physical event in order to receive notifications
                    //Without this call the `DigitalTwinModel` will not receive any notifications or callback about
                    //incoming physical events of the target type and with the target key
                    this.observePhysicalAssetEvent(event);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            //Iterate over available declared Physical Actions for the target Physical Adapter's PAD
            pad.getActions().forEach(action -> {
                try {

                    //Instantiate a new DT State Action with the same key, semantic type and content type
                    DigitalTwinStateAction dtStateAction = new DigitalTwinStateAction(action.getKey(), action.getType(), action.getContentType());

                    //Enable the action on the DT's State
                    this.digitalTwinStateManager.startStateTransaction();
                    this.digitalTwinStateManager.enableAction(dtStateAction);
                    this.digitalTwinStateManager.commitStateTransaction();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

        });

        //Start observation to receive all incoming Digital Action through active Digital Adapter
        //Without this call the `DigitalTwinModel` will not receive any notifications or callback about
        //incoming request to execute an exposed DT's Action
        observeDigitalActionEvents();

        //Notify the DT Core that the Bounding phase has been correctly completed and the DT has evaluated its
        //internal status according to what is available and declared through the Physical Adapters
        notifyShadowingSync();

    }catch (Exception e){
        e.printStackTrace();
    }
}
```

In particular the method ```observeDigitalActionEvents()``` should be called start the observation of digital actions and
to receive all incoming Digital Action through active Digital Adapters.
Without this call the `DigitalTwinModel` will not receive any notifications or callback about
incoming request to execute an exposed DT's Action. Of course, we have to call this method if we are mapping any digital
action in our DT.

Another fundamental method is ```notifyShadowingSync()``` used to notify the DT Core that
the Bounding phase has been correctly completed and the DT has evaluated its  internal status according
to what is available and declared through the Physical Adapters.

As mentioned, in the previous example the `DigitalTwinModel` does not apply any control or check on the nature of declared
physical property. Of course in order to have a more granular control, it will be possible to use property ``Key`` or any other field or even
the type of the instance through an ```instanceof``` check to implement different controls and behaviours.

A variation (only for the property management code) to the previous method can be the following:

```java
//Iterate over available declared Physical Property for the target Physical Adapter's PAD 
pad.getProperties().forEach(property -> {
    try {

        //Check property Key and Instance of to validate that is a Double
        if(property.getKey().equals("temperature-property-key")
                && property.getInitialValue() != null
                &&  property.getInitialValue() instanceof Double) {

            //Instantiate a new DT State Property with the same key, initial resource, semantic type and content type
            DigitalTwinStateProperty<Double> dtStateProperty = new DigitalTwinStateProperty<Double>(property.getKey(), (Double) property.getInitialValue(), property.getType(), property.getContentType());

            //Create and write the property on the DT's State
            this.digitalTwinStateManager.startStateTransaction();
            this.digitalTwinStateManager.createProperty(dtStateProperty);
            this.digitalTwinStateManager.commitStateTransaction();

            //Start observing the variation of the physical property in order to receive notifications
            //Without this call the `DigitalTwinModel` will not receive any notifications or callback about
            //incoming physical property of the target type and with the target key
            this.observePhysicalAssetProperty(property);
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
});
```

The next method that we have to implement in order to properly define and implement the behaviour of our DT through its
DigitalTwinModel are:

- ```onPhysicalAssetPropertyVariation```: Method called when a new variation for a specific Physical Property has been detected
  by the associated Physical Adapter. The method receive as parameter a specific WLDT Event called ```PhysicalAssetPropertyWldtEvent<?> physicalPropertyEventMessage```
  containing all the information generated by the Physical Adapter upon the variation of the monitored physical counterpart.
- ```onPhysicalAssetEventNotification```: Callback method used to be notified by a PhysicalAdapter about the generation of a Physical Event.
  As for the previous method, also this function receive a WLDT Event parameter of type ```onPhysicalAssetEventNotification(PhysicalAssetEventWldtEvent<?> physicalAssetEventWldtEvent)```)
  containing all the field of the generated physical event.
- ```onDigitalActionEvent```: On the opposite this method is triggered from one of the active Digital Adapter when an Action request has been received on the Digital Interface.
  The method receive as parameter an instance of the WLDT Event class ```DigitalActionWldtEvent<?> digitalActionWldtEvent``` describing the target digital action request and the associated
  body.

For the ```onPhysicalAssetPropertyVariation``` a simple implementation in charge ONLY of mapping the new Physical Property resource
into the corresponding DT'State property can be implemented as follows:

```java
@Override
protected void onPhysicalAssetPropertyVariation(PhysicalAssetPropertyWldtEvent<?> physicalPropertyEventMessage) {
    try {
        //Only the value changes: updatePropertyValue() keeps the type and the contentType of the existing State Property
        //(updateProperty() would replace the whole property, including its type and contentType)
        this.digitalTwinStateManager.startStateTransaction();
        this.digitalTwinStateManager.updatePropertyValue(new DigitalTwinStateProperty<>(physicalPropertyEventMessage.getPhysicalPropertyId(), physicalPropertyEventMessage.getBody()));
        this.digitalTwinStateManager.commitStateTransaction();
    } catch (WldtDigitalTwinStateException e) {
        e.printStackTrace();
    }
}
```

In this case as reported in the code, we call the method ```this.digitalTwinStateManager.updatePropertyValue``` inside a state transaction
in order to update the value of an existing DT'State property (previously created in the ```onDigitalTwinBound``` method).
To update the resource we directly use the received data on the ```PhysicalAssetPropertyWldtEvent``` without any additional check
or change that might be instead needed in advanced examples.

Following the same principle, a simplified digital mapping between physical and digital state upon the receving of a physical event variation can be the following:

```java
@Override
protected void onPhysicalAssetEventNotification(PhysicalAssetEventWldtEvent<?> physicalAssetEventWldtEvent) {
    try {
        // The notification of an event must be done inside a state transaction
        this.digitalTwinStateManager.startStateTransaction();
        this.digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(physicalAssetEventWldtEvent.getPhysicalEventKey(), physicalAssetEventWldtEvent.getBody(), physicalAssetEventWldtEvent.getCreationTimestamp()));
        this.digitalTwinStateManager.commitStateTransaction();
    } catch (WldtDigitalTwinStateEventNotificationException | WldtDigitalTwinStateException e) {
        e.printStackTrace();
    }
}
```

With respect to events management, we use the Shadowing Function method ```this.digitalTwinStateManager.notifyDigitalTwinStateEvent``` (inside a state transaction) to notify
the other DT Components (e.g., Digital Adapters) the incoming Physical Event by creating a new instance of a ```DigitalTwinStateEventNotification``` class
containing all the information associated to the event. At commit time the instant of the notification becomes the *last notification* of the event in the state values and the notification is published on the event bus. Of course, additional controls and checks can be introduced in this
method validating and processing the incoming physical message to define complex behaviours.

The last method that we are going to implement is the ```onDigitalActionEvent``` one where we have to handle an incoming
Digital Action request associated to an Action declared on the DT's State in the ```onDigitalTwinBound``` method.
In that case the Digital Action should be forwarded to the Physical Interface in order to be sent to the physical counterpart
for the effective execution.

```java
@Override
protected void onDigitalActionEvent(DigitalActionWldtEvent<?> digitalActionWldtEvent) {
    try {
        this.publishPhysicalAssetActionWldtEvent(digitalActionWldtEvent.getActionKey(), digitalActionWldtEvent.getBody());
    } catch (EventBusException e) {
        e.printStackTrace();
    }
}
```

Also in that case we are forwarding the incoming Digital Action request described through the class ```DigitalActionWldtEvent```
to the Physical Adapter with the method of the `DigitalTwinModel` denoted as ```this.publishPhysicalAssetActionWldtEvent``` and
passing directly the action key and the target Body. No additional processing or validation have been introduced here, but they might
be required in advanced scenario in order to properly adapt incoming digital action request to what is effectively expected on the
physical counterpart.

The invocation of a Digital Action does not update by itself the *last invocation* of the action in the state values.
When the Shadowing Function considers the action executed (for example after publishing the physical action, or when the Physical Adapter confirms it)
it records it, inside a state transaction, with ```notifyActionExecuted```, optionally passing the instant of the execution:

```java
this.digitalTwinStateManager.startStateTransaction();
this.digitalTwinStateManager.notifyActionExecuted(digitalActionWldtEvent.getActionKey());   // or (actionKey, executionInstant)
this.digitalTwinStateManager.commitStateTransaction();
```

## Digital Twin State events on the event bus

Every committed state transaction publishes, on the WLDT event bus, up to **two** events (plus the notifications of the events notified in the transaction), always in this order.
The complete state never travels on the bus: only what changed.

| Event | Topic | Published when | Body | Metadata |
|---|---|---|---|---|
| Description update | `dt.state.description.update` (`WldtEventTypes.DT_STATE_DESCRIPTION_UPDATE_MESSAGE_EVENT_TYPE`) | The structure of the state **really** changed (compared structurally with the previous description) | The **complete** new `DigitalTwinStateDescription` (with its new id) | `DT_STATE_DESCRIPTION_UPDATE_METADATA_PREVIOUS_DESCRIPTION`: the previous description; `DT_STATE_DESCRIPTION_UPDATE_METADATA_CHANGE_LIST`: the `DigitalTwinStateChange` that alter the structure |
| Values update | `dt.state.values.update` (`WldtEventTypes.DT_STATE_VALUES_UPDATE_MESSAGE_EVENT_TYPE`) | The transaction contains at least one change of the values | A `DigitalTwinStateValues` **delta** with only the values that changed, associated to the id of the current description | `DT_STATE_VALUES_UPDATE_METADATA_PREVIOUS_VALUES`: the previous values of the same resources; `DT_STATE_VALUES_UPDATE_METADATA_CHANGE_LIST`: the `DigitalTwinStateChange` that alter the values |
| Event notification | `dt.state.event.notification.<eventKey>` | At commit time, for each `notifyDigitalTwinStateEvent` of the transaction | The `DigitalTwinStateEventNotification` | The event key |

The topics are available through `DigitalTwinStateManager.getStateDescriptionUpdateWldtEventMessageType()`, `getStateValuesUpdateWldtEventMessageType()` and `getEventNotificationWldtEventMessageType(eventKey)`.
The old topic `dt.state.update` (that carried the complete state) does not exist anymore.

What goes in the delta of the values update event:

- a property added or whose value changed: its new value. A property whose value is **unchanged** is not in the body (an update of a property with the same value is still in the change list)
- a relationship added, or whose instances changed: the **complete** list of the instances of that relationship
- the last invocation of an action / last notification of an event that has been triggered in the transaction
- nothing for a property, relationship, action or event that has been **removed**: the removal is not a values update, it travels only in the description update (the change list of the description event contains it) and the local copies drop the value when they apply the new description. No values callback is invoked for it
- a relationship instance added or removed: the complete, updated list of the instances of that relationship (the structure does not change, so this is the only event that carries the removal of an instance)

Rules to keep in mind:

- A transaction that does not change the structure does **not** publish a description update, and a transaction that changes only the structure (for example it enables an action) does **not** publish a values update.
- The values update is published also when the delta is empty but the change list is not (for example a property updated with the same value): consumers refresh the evaluation instant of their copy and filters can still be evaluated on the changes.
- The description update is always published **before** the values update of the same transaction, and the values update refers to the id of the new description. Delivery to the subscribers is asynchronous: a subscriber can receive the values before the description.
  Digital Adapters, Augmentation Function Handlers and the Storage Manager handle it for you (see below); a custom subscriber should do the same.
- A *state change* (`DigitalTwinStateChange`) is described by an operation (`OPERATION_ADD`, `OPERATION_UPDATE`, `OPERATION_UPDATE_VALUE`, `OPERATION_REMOVE`), a resource type and the resource.
  The trigger of an action or event is a change with `OPERATION_UPDATE_VALUE` on an `ACTION` / `EVENT` resource carrying the instant (`getValueInstant()`).

### Keeping a local copy of the state

Components that observe the state (Digital Adapters, the Storage Manager and the Augmentation Function Handlers) keep a **local, complete and never filtered** copy of the description and of the values,
updated at every event: the delta of the values is merged with the previous complete values, and a new description realigns the values to its id (dropping the values of the resources it does not define anymore).
The class that does it, ```DigitalTwinStateReplica```, is available for custom components too:

```java
DigitalTwinStateReplica replica = new DigitalTwinStateReplica();
replica.seed(description, values);                    // at synchronization time

synchronized (replica) {
    // Values event: returns true if it has been applied
    if (replica.applyValuesEvent(wldtEvent)) {
        replica.getValues();                                          // complete values after the update
        DigitalTwinStateManager.getValuesChanges(wldtEvent);          // the changes carried by the event
    }

    // Description event: after it, the values events that were waiting for it must be processed
    if (replica.applyDescriptionEvent(wldtEvent)) {
        replica.getDescription();                                     // complete description after the update
        DigitalTwinStateManager.getDescriptionChanges(wldtEvent);     // the changes carried by the event

        WldtEvent<?> pendingEvent;
        while ((pendingEvent = replica.applyNextPendingValues()) != null) {
            replica.getValues();                                      // values after each pending update, in arrival order
            DigitalTwinStateManager.getValuesChanges(pendingEvent);
        }
    }
}
```

Values that refer to a description that is not known yet are kept in a list, in the order they arrive. When the description arrives it is applied first and then the waiting values are processed with `applyNextPendingValues()`, one at a time and in arrival order (a warning is logged if they wait for too long, nothing is discarded).
Filters are applied by each component **after** its local copy has been updated, and only to the values: the description is **never** filtered.

## Digital Adapter

The las component that we have to implement to complete our first simple Digital Twin definition through the WLDT library is a
Digital Adapter in charge of:

- Receiving event from the DT's Core related to the variation of properties, events, available actions and relationships
- Expose received information to the external world according to its implementation and the supported protocol
- Handle incoming digital action and forward them to the Core in order to be validated and processed by the `DigitalTwinModel`

The basic library class that we are going to extend is called ```DigitalAdapter``` and creating a new class
named ```TestDigitalAdapter```. The DigitalTwinAdapter class can take as Generic Type the type of Configuration used to configure its behaviours.
In this simplified example we are defining a DigitalAdapter without any Configuration.

A Digital Adapter has direct access to the current DT's State through callbacks or directly in a synchronous way using the
two protected methods ```getDigitalTwinStateDescription()``` and ```getDigitalTwinStateValues()```: through them it is possible to navigate everything that currently composes the state of our Digital Twin.
They always return the **complete** local copy of the state (never filtered), refreshed on every state update.

The Digital Adapter class has a set of callback methods to allow the adapter to be updated about the variations and changes on the twin.
Available callbacks can be summarized as follows:

- Digital Adapter Start/Stop:
    - ```onAdapterStart()```: Feedback when the Digital Adapter correctly starts
    - ```onAdapterStop()```: Feedback when the Digital Adapter has been stopped
- Digital Twin Life Cycle Notifications:
    - ```onDigitalTwinCreate()```: The DT has been created
    - ```onDigitalTwinStart()```: The DT started
    - ```onDigitalTwinSync(DigitalTwinStateDescription digitalTwinStateDescription, DigitalTwinStateValues digitalTwinStateValues)```: The DT is Synchronized with its physical counterpart.
      The current description and values of the state are passed as parameters to allow the Digital Adapter to know the current state and consequently
      implement its behaviour
    - ```onDigitalTwinUnSync(DigitalTwinStateDescription digitalTwinStateDescription, DigitalTwinStateValues digitalTwinStateValues)```: The DT is not synchronized anymore with its physical counterpart.
      The last known description and values are passed as parameters
    - ```onDigitalTwinStop()```: The DT is stopped
    - ```onDigitalTwinDestroy()```: The DT has been destroyed and the application stopped
- Digital Twin State Variation (see [Observing the Digital Twin State](#observing-the-digital-twin-state)):
    - ```onStateDescriptionUpdate(DigitalTwinStateDescription description, List<DigitalTwinStateChange> descriptionChanges)```:
      Called every time the **description** (structure) of the DT's State changes.
    - ```onStateValuesUpdate(DigitalTwinStateValues values, List<DigitalTwinStateChange> relevantChanges)```:
      Called every time the **values** of the DT's State change.
- Single Event Notification
    - ```onEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification)```: Called when a notification of an observed DT's State Event is received.

The core method where a Digital Adapter receive the description of the DT'State is ```onDigitalTwinSync(DigitalTwinStateDescription digitalTwinStateDescription, DigitalTwinStateValues digitalTwinStateValues)```.
The Adapter using the parameters can analyze available properties, actions, events and relationships and decide how to implement its internal behaviour with the methods presented in [DigitalTwinModel](#digitaltwinmodel---handling-the-shadowing-process).

To control what is observed a Digital Adapter has the following methods:

- DT's State:
    - ```observeDigitalTwinState()```: Enable the observation of all the Digital Twin State variations (properties, actions, events and relationships).
      It is called automatically when the DT is synchronized, unless the adapter already enabled the observation.
    - ```observeDigitalTwinState(DigitalTwinStateFilter filter)``` and ```observeDigitalTwinState(DigitalTwinStateFilter filter, DigitalTwinStateChangeFilter changeFilter)```:
      Enable the observation applying a content filter (by name on properties, actions, events and relationships) and optionally a trigger filter on the values updates (see below).
      A new call replaces the previous filters.
    - ```clearStateFilter()```: Remove the filters, the adapter receives again every values update, complete.
    - ```unObserveDigitalTwinState()```: Cancel the observation of the Digital Twin State variations.
- Events notifications (the payload of an event, that is not part of the state updates):
    - ```observeAllDigitalTwinEventsNotifications(DigitalTwinStateDescription description)```: Enable the observation of the notifications of all the events declared in the description.
    - ```unObserveAllDigitalTwinEventsNotifications(DigitalTwinStateDescription description)```: Cancel the observation of the notifications of all the events declared in the description.
    - ```observeDigitalTwinEventsNotifications(List<String> eventsList)```: Enable the observation of the notifications associated to a specific list of Digital Twin State events.
    - ```unObserveDigitalTwinEventsNotifications(List<String> eventsList)```: Cancel the observation of a target list of events.
    - ```observeDigitalTwinEventNotification(String eventKey)```: Enable the observation of the notifications associated to a single Digital Twin State event.
    - ```unObserveDigitalTwinEventNotification(String eventKey)```: Cancel the observation of a single target event.

The resulting code will be the following after adding the required
methods (still empty) and the basic constructor with the id String parameter is the following:

```java
import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.core.state.*;

import java.util.List;

public class TestDigitalAdapter extends DigitalAdapter<Void> {

    public TestDigitalAdapter(String id) {
        super(id, null);
    }

    /**
     * Callback to notify the adapter on its correct startup
     */
    @Override
    public void onAdapterStart() {

    }

    /**
     * Callback to notify the adapter that has been stopped
     */
    @Override
    public void onAdapterStop() {

    }

    /**
     * Notification that the description (structure) of the DT State changed
     * @param description the complete new description
     * @param descriptionChanges the changes that altered the structure
     */
    @Override
    protected void onStateDescriptionUpdate(DigitalTwinStateDescription description,
                                            List<DigitalTwinStateChange> descriptionChanges) {

    }

    /**
     * Notification that the values of the DT State changed
     * @param values the complete current values
     * @param relevantChanges the changes that altered the values
     */
    @Override
    protected void onStateValuesUpdate(DigitalTwinStateValues values,
                                       List<DigitalTwinStateChange> relevantChanges) {

    }

    /**
     * DT Life Cycle notification that the DT is correctly on Sync
     * @param digitalTwinStateDescription the description of the DT State
     * @param digitalTwinStateValues the values of the DT State
     */
    @Override
    public void onDigitalTwinSync(DigitalTwinStateDescription digitalTwinStateDescription,
                                  DigitalTwinStateValues digitalTwinStateValues) {

    }

    /**
     * DT Life Cycle notification that the DT is currently Not Sync
     * @param digitalTwinStateDescription the last known description of the DT State
     * @param digitalTwinStateValues the last known values of the DT State
     */
    @Override
    public void onDigitalTwinUnSync(DigitalTwinStateDescription digitalTwinStateDescription,
                                    DigitalTwinStateValues digitalTwinStateValues) {

    }

    /**
     * DT Life Cycle notification that the DT has been created
     */
    @Override
    public void onDigitalTwinCreate() {

    }

    /**
     * DT Life Cycle Notification that the DT has correctly Started
     */
    @Override
    public void onDigitalTwinStart() {

    }

    /**
     * DT Life Cycle Notification that the DT has been stopped
     */
    @Override
    public void onDigitalTwinStop() {

    }

    /**
     * DT Life Cycle Notification that the DT has destroyed
     */
    @Override
    public void onDigitalTwinDestroy() {

    }

    /**
     * Notification that a Notification for a specific Event has been received
     * @param digitalTwinStateEventNotification
     */
    @Override
    protected void onEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) {

    }
}
```

``onStateDescriptionUpdate(...)`` and ``onStateValuesUpdate(...)`` are abstract: a Digital Adapter must implement both (even with an empty body).

### Observing the Digital Twin State

By default, a Digital Adapter observes all the variations on the DT's State in terms of Properties, Relationships, Actions and Events.
The state travels on the event bus as two separate events (see [Digital Twin State events on the event bus](#digital-twin-state-events-on-the-event-bus)):
the adapter applies each of them to its local, complete copy of the state and then calls one of the two callbacks, with the list of the changes (```DigitalTwinStateChange```: operation, resource type and resource) of the event:

| Callback | Called when | What it receives | Filtered |
|---|---|---|---|
| `onStateDescriptionUpdate` | The structure changes: a property, action, event or relationship is added, updated or removed (the update really changes the structure) | The **complete** `DigitalTwinStateDescription` (with its id) and the changes that altered the structure | **Never**: always the complete description, whatever the filters |
| `onStateValuesUpdate` | A value changes: a property value, the instances of a relationship, the last invocation of an action / last notification of an event | The **complete** `DigitalTwinStateValues` of the local copy (limited to the accepted resources if a content filter is set) and the changes that altered the values | Yes: the callback is called only if at least one change passes the content filter **and** matches the trigger filter (if set) |

The adapter receives the complete values at every call, even if the event carries only the changed ones: the delta is merged with its local copy, that is always updated even when the callback is filtered out.

```java
@Override
protected void onStateDescriptionUpdate(DigitalTwinStateDescription description, List<DigitalTwinStateChange> descriptionChanges) {
    // e.g., rebuild the interface exposed to the external world
    description.getProperties().forEach(p -> System.out.println("Property: " + p.getKey() + " type: " + p.getType() + " writable: " + p.isWritable()));
    description.getActions().forEach(a -> System.out.println("Action: " + a.getKey()));
    description.getEvents().forEach(e -> System.out.println("Event: " + e.getKey()));
    description.getRelationships().forEach(r -> System.out.println("Relationship: " + r.getName()));
}

@Override
protected void onStateValuesUpdate(DigitalTwinStateValues values, List<DigitalTwinStateChange> relevantChanges) {
    // e.g., publish the new values on the target protocol (values.getDescriptionId() is the id of the description they belong to)
    values.getPropertyValues().forEach((key, value) -> System.out.println("Property " + key + " = " + value));
    values.getLastEventNotifications().forEach((key, instant) -> System.out.println("Event " + key + " last notified at " + instant));
}
```

#### Filtering the observed values

The filters restrict when ```onStateValuesUpdate``` is called and which values it receives. The **content filter** (```DigitalTwinStateFilter```) lists, for each category
(properties, actions, events and relationships), the names of the accepted resources: an unset category accepts everything, an empty list accepts nothing.
The optional **trigger filter** (```DigitalTwinStateChangeFilter```) accepts only the changes with the given operation, resource type and/or resource name.
They can be passed to the constructor of the adapter:

```java
public class TestDigitalAdapter extends DigitalAdapter<Void> {

    public TestDigitalAdapter(String id) {
        super(id,
              DigitalTwinStateFilter.all().withPropertyNames("temperature"),                 // content filter
              DigitalTwinStateChangeFilter.onPropertyValueUpdate("temperature"));            // trigger filter (null = none)
    }
    // ...
}
```

or set at any time (for example in ```onDigitalTwinSync```, before the automatic observation starts); a new call replaces the previous filters:

```java
@Override
public void onDigitalTwinSync(DigitalTwinStateDescription digitalTwinStateDescription, DigitalTwinStateValues digitalTwinStateValues) {
    try {

        // Only the property "temperature" (and every action, event and relationship, since they are not filtered)
        observeDigitalTwinState(DigitalTwinStateFilter.all().withPropertyNames("temperature"));

        // Only the properties "temperature" and "humidity" and nothing else, notified only when their value is updated
        // observeDigitalTwinState(DigitalTwinStateFilter.all()
        //         .withPropertyNames("temperature", "humidity")
        //         .withActionNames()
        //         .withEventNames()
        //         .withRelationshipNames(),
        //         DigitalTwinStateChangeFilter.onPropertyValueUpdate("temperature", "humidity"));

    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

With the first filter of the example above:

- ```onStateValuesUpdate``` receives only the values of ```temperature``` (plus the instances of every relationship and the last triggered actions and events) and is called only when one accepted resource changes
  (e.g., the update of the value of ```humidity``` only does not call it, but the local copy of the adapter is updated anyway).
- ```onStateDescriptionUpdate``` is not affected by the filters.

```clearStateFilter()``` removes both filters.

#### Description and values of the DT's State

The description and the values of the state are available at any moment in the adapter, in the Shadowing Function and in the Augmentation Functions:

```java
// In a Digital Adapter (complete local copy, never filtered)
DigitalTwinStateDescription description = getDigitalTwinStateDescription();
DigitalTwinStateValues values = getDigitalTwinStateValues();

// In the Shadowing Function (DigitalTwinModel)
DigitalTwinStateDescription description = this.digitalTwinStateManager.getDigitalTwinStateDescription();
DigitalTwinStateValues values = this.digitalTwinStateManager.getDigitalTwinStateValues();

// A copy of the values limited to the accepted resources (the same instance if no filter is active)
DigitalTwinStateValues filteredValues = values.filter(DigitalTwinStateFilter.all().withPropertyNames("temperature"));

// Last time an action has been invoked / an event has been notified (not present if it never happened)
Instant lastInvocation = values.getLastActionInvocations().get("set-temperature-action-key");
Instant lastNotification = values.getLastEventNotifications().get("overheating");
```

- ```DigitalTwinStateDescription``` contains the descriptors of properties (key, type, content type, readable, writable, exposed), actions, events and relationships, and the unique id of the description.
- ```DigitalTwinStateValues``` is an immutable snapshot with the property values, the relationship instances, and the last invocation of the actions and last notification of the events, associated to the id of the description.
- Both keep the ```evaluationInstant``` of the state they come from.
- The last invocation of an action is recorded with ```notifyActionExecuted``` and the last notification of an event when it is notified (both inside a state transaction): they are values of the state, so they produce a values update.

### Observing Events Notifications

The notifications of the events registered in the DT's State carry a payload and are not part of the state updates: to receive them the adapter has to observe them
with the methods presented above and implement the callback ```onEventNotificationReceived```.

The only thing that we should add in the ```onDigitalTwinSync(DigitalTwinStateDescription, DigitalTwinStateValues)``` callback is therefore the direct observation of the Events.
(We will talk about relationships management in the next Section)

Following this approach we can change our Digital Adapter in the following methods:

In ```onDigitalTwinSync``` we observe in this first simple implementation only the incoming notifications for declared Events in the DT'State.
As previously mentioned the observation of any variation of the State, with the values of the Properties, is by default enabled for any Digital Adapter.
In this method we use the parameter ```digitalTwinStateDescription``` to find available Events declaration that we would like to observe.

````java
@Override
public void onDigitalTwinSync(DigitalTwinStateDescription digitalTwinStateDescription, DigitalTwinStateValues digitalTwinStateValues) {

      try {

          //Retrieve the list of available events and observe all variations
          List<String> eventKeys = digitalTwinStateDescription.getEvents().stream()
                  .map(DigitalTwinStateDescription.EventDescription::getKey)
                  .collect(Collectors.toList());

          if (!eventKeys.isEmpty())
              observeDigitalTwinEventsNotifications(eventKeys);

      } catch (Exception e) {
          e.printStackTrace();
      }

  }
````

Since the observation of the state is active by default, we can implement the following method to receive variations of the DT'State Properties.
In this simple case our implementation is just a Log on the console, but of course it can be changed to enable to communication of the Digital Twin
with the external world for example sending the received data on a specific protocol and with a target data format.

```java
@Override
protected void onStateValuesUpdate(DigitalTwinStateValues values, List<DigitalTwinStateChange> relevantChanges) {
    System.out.println("[TestDigitalAdapter] -> onStateValuesUpdate(): " + values.getPropertyValues());
}
```

Following the same approach we implement the following method to be notified about incoming Events from the DT's Core.

```java
@Override
protected void onEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification) {
    System.out.println("Event notification received - Event: " + digitalTwinStateEventNotification.getDigitalEventKey() + " body: " + digitalTwinStateEventNotification.getBody());
}
```

Both Physical Adapters and Digital Adapters can be defined natively with a custom configuration provided by the developer
as illustrated in the dedicated Section: [Configurable Physical & Digital Adapters](#configurable-physical-and-digital-adapters).

## Digital Twin Process

Now that we have created the main fundamental element of a DT (Physical Adapter, `DigitalTwinModel` and Digital Adapter) we can create Class file with a main to create the WLDT Engine
with the created components and start the DT.

Create a new Java file called ```TestDigitalTwin``` adding the following code:

```java
import it.wldt.core.engine.DigitalTwin;

public class TestDigitalTwin {

    public static void main(String[] args)  {
        try{

            WldtEngine digitalTwinEngine = new WldtEngine(new TestDigitalTwinModel("test-digital-twin-model"), "test-digital-twin");
            digitalTwinEngine.addPhysicalAdapter(new TestPhysicalAdapter("test-physical-adapter"));
            digitalTwinEngine.addDigitalAdapter(new TestDigitalAdapter("test-digital-adapter"));
            digitalTwinEngine.startLifeCycle();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
```

As a first step we create an instance of the ```WldtEngine``` passing our ```TestDigitalTwinModel``` and the Id of our DT (e.g., test-digital-twin).
Then we can use the created ```digitalTwinEngine``` variable to call the following methods:

- ```addPhysicalAdapter(...)```: To add a new Physical Adapter to the engine. In our case, we use the class ```TestPhysicalAdapter``` previously created
- ```addDigitalAdapter(...)```: To add a new Digital Adapter to the engine. In our case, we use the class ```TestDigitalAdapter``` previously created

At the end we are ready to start the DT and we can call the method ```digitalTwinEngine.startLifeCycle()``` to start the engine together with all the defined and configured components.

## Digital Action Management

In this demo implementation, we are going to emulate an incoming Digital Action on the Digital Adapter in order to show how it can be handled by the adapter and
properly forwarded to the `DigitalTwinModel` for validation and the consequent interaction with the Physical Adapter and then with the physical twin.

In order to add a demo Digital Action trigger on the Digital Adapter we add the following method to the ```TestDigitalAdapter``` class:

```java
private Runnable emulateIncomingDigitalAction(){
    return () -> {
        try {

            System.out.println("Sleeping before Emulating Incoming Digital Action ...");
            Thread.sleep(5000);
            Random random = new Random();

            //Emulate the generation on 'n' temperature measurements
            for(int i = 0; i < 10; i++){

                //Sleep to emulate sensor measurement
                Thread.sleep(1000);

                double randomTemperature = 25.0 + (30.0 - 25.0) * random.nextDouble();
                publishDigitalActionWldtEvent("set-temperature-action-key", randomTemperature);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    };
}
```

This method uses the Digital Adapter internal function denoted as ```publishDigitalActionWldtEvent(String actionKey, T body)``` allowing the adapter
to send a notification to the DT's Core (and consequently the `DigitalTwinModel`) about the arrival of a Digital Action with a specific ```key``` and ```body```.
In our case the ```key``` is ```set-temperature-action-key``` as declared in the Physical Adapter and in the PAD and the resource is a simple Double with the new temperature resource.

Then we call this method in the following way at the end ot the ```onDigitalTwinSync(DigitalTwinStateDescription, DigitalTwinStateValues)``` method.

```java
//Start Digital Action Emulation
new Thread(emulateIncomingDigitalAction()).start();
```

Now the `DigitalTwinModel` should be updated in order to handle the incoming Action request from the Digital Adapter.
In our case the DT model does not apply any validation or check and just forward to action to the Physical Adapter
in order to be then forwarded to the physical twin. Of course advanced implementation can be introduced for example to
validate action, adapt payload and data-formats or to augment functionalities (e.g., trigger multiple physical actions from a single digital request).

In our simple demo implementation the updated `DigitalTwinModel` method ```onDigitalActionEvent(DigitalActionWldtEvent<?> digitalActionWldtEvent)``` results as follows:

```java
@Override
protected void onDigitalActionEvent(DigitalActionWldtEvent<?> digitalActionWldtEvent) {
    try {
        this.publishPhysicalAssetActionWldtEvent(digitalActionWldtEvent.getActionKey(), digitalActionWldtEvent.getBody());
    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

This forwarding of the action triggers the corresponding Physical Adapter method ```onIncomingPhysicalAction(PhysicalAssetActionWldtEvent<?> physicalAssetActionWldtEvent)```
that in our case is emulated just with a Log on the console. Also in that case advanced Physical Adapter implementation can be introduced
for example to adapt the request from a high-level (and potentially standard) DT action description to the custom requirements of the
specific physical twin managed by the adapter.

```java
@Override
public void onIncomingPhysicalAction(PhysicalAssetActionWldtEvent<?> physicalAssetActionWldtEvent) {
    try{

        if(physicalAssetActionWldtEvent != null
                && physicalAssetActionWldtEvent.getActionKey().equals(SET_TEMPERATURE_ACTION_KEY)
                && physicalAssetActionWldtEvent.getBody() instanceof Double) {
            System.out.println("Received Action Request: " + physicalAssetActionWldtEvent.getActionKey()
                    + " with Body: " + physicalAssetActionWldtEvent.getBody());
        }
        else
            System.err.println("Wrong Action Received !");

    }catch (Exception e){
        e.printStackTrace();
    }
}
```