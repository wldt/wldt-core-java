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
The state is described by the class ```DigitalTwinState``` and it is owned by a ```DigitalTwinStateManager```, that the
`DigitalTwinModel` can access through the variable called: ```digitalTwinStateManager```.

- **Reading** the state: ```this.digitalTwinStateManager.getDigitalTwinState()``` returns the current ```DigitalTwinState```.
- **Writing** the state: all the edits are performed on the manager through a **transaction**. After calling ```startStateTransaction()```
  the edit methods collect the changes, and ```commitStateTransaction()``` applies all of them together and publishes a single state update
  (together with the list of changes) to Digital Adapters, Augmentation Functions and storage. ```rollbackTransaction()``` discards the collected changes.
  Calling an edit method without an open transaction throws a ```WldtDigitalTwinStateException```.

Available main read methods on the ```DigitalTwinState``` instance are:

- Properties:
    - ```getProperty(String propertyKey)```: Retrieves if present the target DigitalTwinStateProperty by Key
    - ```containsProperty(String propertyKey)```: Checks if a target Property Key is already available in the current Digital Twin's State
    - ```getPropertyList()```: Loads the list of available Properties (described by the class DigitalTwinStateProperty) available on the Digital Twin's State
    - ```readProperty(String propertyKey)```: Retrieves if present the target DigitalTwinStateProperty by Key
- Actions:
    - ```containsAction(String actionKey)```: Checks if a Digital Twin State Action with the specified key is correctly registered
    - ```getAction(String actionKey)```: Loads the target DigitalTwinStateAction by key
    - ```getActionList()```: Gets the list of available Actions registered on the Digital Twin's State
- Events:
    - ```containsEvent(String eventKey)```: Check if a Digital Twin State Event with the specified key is correctly registered
    - ```getEvent(String eventKey)```: Return the description of a registered Digital Twin State Event according to its Key
    - ```getEventList()```: Return the list of existing and registered Digital Twin State Events
- Relationships:
    - ```containsRelationship(String relationshipName)```: Checks if a Relationship Name is already available in the current Digital Twin's State
    - ```containsRelationshipInstance(String relationshipName, String instanceKey)```: Checks if a Relationship Instance is available
    - ```getRelationshipList()```: Loads the list of existing relationships on the Digital Twin's State through a list of DigitalTwinStateRelationship
    - ```getRelationship(String name)```: Gets a target Relationship identified through its name and described through the class DigitalTwinStateRelationship
- Description, values and last triggered:
    - ```getDescription()``` / ```getDescription(DigitalTwinStateFilter filter)```: The structure of the state without values (```DigitalTwinStateDescription```)
    - ```getValues()``` / ```getValues(DigitalTwinStateFilter filter)```: A snapshot of the values of the state (```DigitalTwinStateValues```)
    - ```filter(DigitalTwinStateFilter filter)```: A partial state limited to the resources selected by the filter
    - ```getLastActionInvocation(String actionKey)``` / ```getLastEventNotification(String eventKey)```: The last time an action has been invoked or an event has been notified

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

Two methods of the manager do not need a transaction and do not produce a state update:

- ```notifyDigitalTwinStateEvent(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification)```: Method to notify the occurrence of the target Digital Twin State Event (it also records the time of the last notification of the event)
- ```recordActionInvocation(String actionKey)```: Records the time of the last invocation of an action (the `DigitalTwinModel` already does it for every received Digital Action request)

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
        this.digitalTwinStateManager.notifyDigitalTwinStateEvent(new DigitalTwinStateEventNotification<>(physicalAssetEventWldtEvent.getPhysicalEventKey(), physicalAssetEventWldtEvent.getBody(), physicalAssetEventWldtEvent.getCreationTimestamp()));
    } catch (WldtDigitalTwinStateEventNotificationException e) {
        e.printStackTrace();
    }
}
```

With respect to events management, we use the Shadowing Function method ```this.digitalTwinStateManager.notifyDigitalTwinStateEvent``` (that does not need a transaction) to notify
the other DT Components (e.g., Digital Adapters) the incoming Physical Event by creating a new instance of a ```DigitalTwinStateEventNotification``` class
containing all the information associated to the event. Of course, additional controls and checks can be introduced in this
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
internal variable called: ```digitalTwinState```. Through it is possibile to navigate all the fields currently composing the state of our Digital Twin.
The variable always contains the **complete** state (it is never filtered) and it is refreshed on every state update.

The Digital Adapter class has a set of callback methods to allow the adapter to be updated about the variations and changes on the twin.
Available callbacks can be summarized as follows:

- Digital Adapter Start/Stop:
    - ```onAdapterStart()```: Feedback when the Digital Adapter correctly starts
    - ```onAdapterStop()```: Feedback when the Digital Adapter has been stopped
- Digital Twin Life Cycle Notifications:
    - ```onDigitalTwinCreate()```: The DT has been created
    - ```onDigitalTwinStart()```: The DT started
    - ```onDigitalTwinSync(DigitalTwinState digitalTwinState)```: The DT is Synchronized with its physical counterpart.
      The current DigitalTwinState is passed as parameter to allow the Digital Adapter to know the current state and consequently
      implement its behaviour
    - ```onDigitalTwinUnSync(DigitalTwinState digitalTwinState)```: The DT is not synchronized anymore with its physical counterpart.
      The last current DigitalTwinState is passed as parameter to allow the Digital Adapter to know the last state and consequently
      implement its behaviour
    - ```onDigitalTwinStop()```: The DT is stopped
    - ```onDigitalTwinDestroy()```: The DT has been destroyed and the application stopped
- Digital Twin State Variation (see [Observing the Digital Twin State](#observing-the-digital-twin-state)):
    - ```onStateUpdate(DigitalTwinState newDigitalTwinState, DigitalTwinState previousDigitalTwinState, ArrayList<DigitalTwinStateChange> digitalTwinStateChangeList)```:
      Called on every update of the DT's State (properties, actions, events and relationships), together with the list of changes that produced it.
    - ```onStateDescriptionUpdate(DigitalTwinStateDescription description, List<DigitalTwinStateChange> descriptionChanges)```:
      Called every time the **description** (structure) of the DT's State changes. Optional, the default implementation does nothing.
    - ```onStateValuesUpdate(DigitalTwinStateValues values, List<DigitalTwinStateChange> relevantChanges)```:
      Called every time the **values** of the DT's State change. Optional, the default implementation does nothing.
- Single Event Notification
    - ```onEventNotificationReceived(DigitalTwinStateEventNotification<?> digitalTwinStateEventNotification)```: Called when a notification of an observed DT's State Event is received.

The core method where a Digital Adapter receive the description of the DT'State is ```onDigitalTwinSync(DigitalTwinState digitalTwinState)```.
The Adapter using the parameter ```digitalTwinState``` can analyze available properties, actions, events and relationships and decide how to implement its internal behaviour with the methods presented in [DigitalTwinModel](#digitaltwinmodel---handling-the-shadowing-process).

To control what is observed a Digital Adapter has the following methods:

- DT's State:
    - ```observeDigitalTwinState()```: Enable the observation of all the Digital Twin State variations (properties, actions, events and relationships).
      It is called automatically when the DT is synchronized, unless the adapter already enabled the observation.
    - ```observeDigitalTwinState(DigitalTwinStateFilter filter)```: Enable the observation applying a filter by name on properties, actions, events and relationships (see below).
      A new call replaces the previous filter.
    - ```clearStateFilter()```: Remove the filter, the adapter receives again the complete state updates.
    - ```unObserveDigitalTwinState()```: Cancel the observation of the Digital Twin State variations.
- Events notifications (the payload of an event, that is not part of the state updates):
    - ```observeAllDigitalTwinEventsNotifications(DigitalTwinState digitalTwinState)```: Enable the observation of the notifications of all the events declared in the state.
    - ```unObserveAllDigitalTwinEventsNotifications(DigitalTwinState digitalTwinState)```: Cancel the observation of the notifications of all the events declared in the state.
    - ```observeDigitalTwinEventsNotifications(List<String> eventsList)```: Enable the observation of the notifications associated to a specific list of Digital Twin State events.
    - ```unObserveDigitalTwinEventsNotifications(List<String> eventsList)```: Cancel the observation of a target list of events.
    - ```observeDigitalTwinEventNotification(String eventKey)```: Enable the observation of the notifications associated to a single Digital Twin State event.
    - ```unObserveDigitalTwinEventNotification(String eventKey)```: Cancel the observation of a single target event.

The resulting code will be the following after adding the required
methods (still empty) and the basic constructor with the id String parameter is the following:

```java
import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.core.state.*;

import java.util.ArrayList;

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
     * Notification of an update of the DT State together with the list of changes that produced it
     * @param newDigitalTwinState the new DT State
     * @param previousDigitalTwinState the DT State before the update
     * @param digitalTwinStateChangeList the changes applied by the update
     */
    @Override
    protected void onStateUpdate(DigitalTwinState newDigitalTwinState,
                                 DigitalTwinState previousDigitalTwinState,
                                 ArrayList<DigitalTwinStateChange> digitalTwinStateChangeList) {

    }

    /**
     * DT Life Cycle notification that the DT is correctly on Sync
     * @param digitalTwinState
     */
    @Override
    public void onDigitalTwinSync(DigitalTwinState digitalTwinState) {

    }

    /**
     * DT Life Cycle notification that the DT is currently Not Sync
     * @param digitalTwinState
     */
    @Override
    public void onDigitalTwinUnSync(DigitalTwinState digitalTwinState) {

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

``onStateDescriptionUpdate(...)`` and ``onStateValuesUpdate(...)`` are not abstract: override them only if the adapter is interested in them.

### Observing the Digital Twin State

By default, a Digital Adapter observes all the variations on the DT's State in terms of Properties, Relationships, Actions and Events.
Every committed update of the state is delivered to the adapter together with the list of the changes (```DigitalTwinStateChange```: operation, resource type and resource)
that produced it, and the adapter receives three callbacks that are **always active**, with or without a filter:

| Callback | Called when | What it receives | Filtered by `DigitalTwinStateFilter` |
|---|---|---|---|
| `onStateUpdate` | Every state update (with a filter: only if at least one change concerns an accepted resource) | New and previous `DigitalTwinState` and the list of changes | Yes: states and changes are limited to the accepted resources |
| `onStateDescriptionUpdate` | The structure changes: a property, action, event or relationship is added, updated or removed | The complete `DigitalTwinStateDescription` and all the description changes | **No**: always the complete description, whatever the filter |
| `onStateValuesUpdate` | A value changes: a property value or a relationship instance (also when a whole property or relationship is added, replaced or removed) | The `DigitalTwinStateValues` snapshot and the changes that altered values | Yes: only accepted resources, only accepted changes trigger it |

The description and values callbacks are called only if the update contains a change of that kind (e.g., the update of a property value does not call
```onStateDescriptionUpdate```), and they are not called for an update without a change list.
The filter is applied by the adapter: the complete state keeps travelling on the event bus.

```java
@Override
protected void onStateUpdate(DigitalTwinState newDigitalTwinState,
                             DigitalTwinState previousDigitalTwinState,
                             ArrayList<DigitalTwinStateChange> digitalTwinStateChangeList) {
    for (DigitalTwinStateChange change : digitalTwinStateChangeList)
        System.out.println("[TestDigitalAdapter] -> onStateUpdate(): " + change.getOperation() + " " + change.getResourceType() + " " + change.getResource());
}

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
    // e.g., publish the new values on the target protocol
    values.getPropertyValues().forEach((key, value) -> System.out.println("Property " + key + " = " + value));
    values.getLastEventNotifications().forEach((key, instant) -> System.out.println("Event " + key + " last notified at " + instant));
}
```

#### Filtering the observed state

```observeDigitalTwinState(DigitalTwinStateFilter filter)``` restricts what the adapter receives. The filter lists, for each category
(properties, actions, events and relationships), the names of the accepted resources: an unset category accepts everything, an empty list accepts nothing.
It can be set at any time (for example in ```onDigitalTwinSync```, before the automatic observation starts) and a new call replaces the previous filter.

```java
@Override
public void onDigitalTwinSync(DigitalTwinState digitalTwinState) {
    try {

        // Only the property "temperature" (and every action, event and relationship, since they are not filtered)
        observeDigitalTwinState(DigitalTwinStateFilter.all().withPropertyNames("temperature"));

        // Only the properties "temperature" and "humidity" and nothing else
        // observeDigitalTwinState(DigitalTwinStateFilter.all()
        //         .withPropertyNames("temperature", "humidity")
        //         .withActionNames()
        //         .withEventNames()
        //         .withRelationshipNames());

    } catch (Exception e) {
        e.printStackTrace();
    }
}
```

With the filter of the example above:

- ```onStateUpdate``` receives states that contain only the property ```temperature``` (plus all actions, events and relationships) and only the changes concerning the accepted resources.
  It is not called if no change of the update concerns an accepted resource (e.g., the update of the value of ```humidity``` only is not delivered if properties are filtered).
- ```onStateValuesUpdate``` receives only the values of the accepted resources and is called only when one of them changes.
- ```onStateDescriptionUpdate``` is not affected by the filter.

```clearStateFilter()``` removes the filter.

#### Description and values of the DT's State

The same views are available on any ```DigitalTwinState``` instance (in the adapter, in the Shadowing Function or in an Augmentation Function), with an optional filter:

```java
DigitalTwinStateDescription description = digitalTwinState.getDescription();          // structure only, no values
DigitalTwinStateValues values = digitalTwinState.getValues(
        DigitalTwinStateFilter.all().withPropertyNames("temperature"));                 // values of the accepted resources

// A partial state limited to the accepted resources (the same instance if no filter is active)
DigitalTwinState filteredState = digitalTwinState.filter(DigitalTwinStateFilter.all().withPropertyNames("temperature"));

// Last time an action has been invoked / an event has been notified (empty if it never happened)
Optional<Instant> lastInvocation = digitalTwinState.getLastActionInvocation("set-temperature-action-key");
Optional<Instant> lastNotification = digitalTwinState.getLastEventNotification("overheating");
```

- ```DigitalTwinStateDescription``` contains the descriptors of properties (key, type, content type, readable, writable, exposed), actions, events and relationships.
- ```DigitalTwinStateValues``` is an immutable snapshot with the property values, the relationship instances, and the last invocation of the actions and last notification of the events.
  It is not altered by later updates of the state.
- Both keep the ```evaluationInstant``` of the state they come from.
- The last invocation of an action is recorded when the digital action request reaches the DT's Core, and the last notification of an event when it is notified.
  They are not state updates: they do not produce a ```onStateUpdate``` callback and they are read on demand.

### Observing Events Notifications

The notifications of the events registered in the DT's State carry a payload and are not part of the state updates: to receive them the adapter has to observe them
with the methods presented above and implement the callback ```onEventNotificationReceived```.

The only thing that we should add in the ```onDigitalTwinSync(DigitalTwinState digitalTwinState)``` callback is therefore the direct observation of the Events.
(We will talk about relationships management in the next Section)

Following this approach we can change our Digital Adapter in the following methods:

In ```onDigitalTwinSync``` we observe in this first simple implementation only the incoming notifications for declared Events in the DT'State.
As previously mentioned the observation of any variation of the State, with the values of the Properties, is by default enabled for any Digital Adapter.
In this method we use the internal variable ```digitalTwinState``` to access the DT's state and find available Events declaration that we would like to observe.

````java
@Override
public void onDigitalTwinSync(DigitalTwinState startDigitalTwinState) {

      try {

          //Retrieve the list of available events and observe all variations
          digitalTwinState.getEventList()
                  .map(eventList -> eventList.stream()
                          .map(DigitalTwinStateEvent::getKey)
                          .collect(Collectors.toList()))
                  .ifPresent(eventKeys -> {
                      try {
                          observeDigitalTwinEventsNotifications(eventKeys);
                      } catch (EventBusException e) {
                          e.printStackTrace();
                      }
                  });

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

Then we call this method in the following way at the end ot the ```onDigitalTwinSync(DigitalTwinState startDigitalTwinState)``` method.

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