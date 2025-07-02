# Handling Physical & Digital Relationships

The same management that we have illustrated for Properties, Events and Action can be applied also to Digital Twin Relationships.
Relationships represent the links that exist between the modeled physical assets and other physical entity
of the organizations through links to their corresponding Digital Twins.
Like properties, relationships can be observed, dynamically created, and change over time,
but unlike properties, they are not properly part of the PA's state but of its operational context
(e.g., a DT of a robot within a production line).

It is necessary to distinguish between two concepts: i) ```Relationship```; and ii) ```Relationship Instance```.
The first one models the relationship from a semantic point of view,
defining its ```name``` and target ```type```.
The second one represents an instantiation of the concept in reality.
For example, in the context of a Smart Home,
the Home Digital Twin (DT) will define a Relationship called ```has_room```
which has possible targets represented by DTs that represent different rooms of the house.
The actual link between the Home DT and the Bedroom DT
will be modeled by a specific Relationship Instance of the ```has_room``` relationship.

Within the state of the DT, it is necessary to
differentiate between the concept of a relationship and that of an instance of a relationship.
In the first case, we refer to a semantic concept where each relationship,
through its ```name``` and the semantic ```type``` of its target,
determines the different type of link that the DT can establish.
On the other hand, an ```instanc``` of a relationship represents the concrete
link present between the DT that establishes it and the target DT.
For instance, in the case of a Smart Home,
the Bedroom DT may have two relationships in its model: one named ```is_room_of``` and another called ```has_device```.
An instance of the first type of relationship could, for example,
have the Home DT as its target, while the ```has_device``` relationship could have
multiple instances, one for each device present in the room.
An example of a possible instance is one targeting the Air Conditioner DT.

From an implementation perspective, in the Physical Adapter and in particular where we handle the definition of the PAD we can also
specify the existing relationships. In our case, since the Relationship is useful also to define its future instance we
keep a reference of the relationship as in internal variable called ```insideInRelationship```.

Then we can update the code as follows:

```java
private PhysicalAssetRelationship<String> insideInRelationship = null;

@Override
public void onIncomingPhysicalAction(PhysicalAssetActionWldtEvent<?> physicalAssetActionWldtEvent) {
    try{
        
        [...]
        
        //Create Test Relationship to describe that the Physical Device is inside a building
        this.insideInRelationship=new PhysicalAssetRelationship<>("insideId");
        pad.getRelationships().add(insideInRelationship);
        
        [...]
        
    } catch (Exception e){
        e.printStackTrace();
    }
}
```

Of course always in the Physical Adapter we need to publish an effective instance of the definite Relationship.
To do that, we have defined a dedicated method that we can call inside the adapter to notify the DT's Core and in
particular the Shadowing Function on the presence of a new Relationship.

The following method can be added for example at the beginning of the Device Emulation:

```java
private void publishPhysicalRelationshipInstance() {
    try{

        String relationshipTarget = "building-hq";

        Map<String, Object> relationshipMetadata = new HashMap<>();
        relationshipMetadata.put("floor", "f0");
        relationshipMetadata.put("room", "r0");

        PhysicalAssetRelationshipInstance<String> relInstance = this.insideInRelationship.createRelationshipInstance(relationshipTarget, relationshipMetadata);

        PhysicalAssetRelationshipInstanceCreatedWldtEvent<String> relInstanceEvent = new PhysicalAssetRelationshipInstanceCreatedWldtEvent<>(relInstance);
        publishPhysicalAssetRelationshipCreatedWldtEvent(relInstanceEvent);

    }catch (Exception e){
        e.printStackTrace();
    }
}
```

On the other hand, as already done for all the other Properties, Actions and Events we have to handle them on the
Shadowing Function and in particular updating the ```onDigitalTwinBound(...)``` method managing Relationship declaration.
Also for the Relationships there is the method denoted as ```observePhysicalAssetRelationship(relationship)``` to observe the variation
of the target entity.

```java
@Override
protected void onDigitalTwinBound(Map<String, PhysicalAssetDescription> adaptersPhysicalAssetDescriptionMap) {

    try{

        //Iterate over all the received PAD from connected Physical Adapters
        adaptersPhysicalAssetDescriptionMap.values().forEach(pad -> {
            
            [...]

            //Iterate over Physical Relationships
            pad.getRelationships().forEach(relationship -> {
                try{
                    if(relationship != null && relationship.getName().equals("insideIn")){
                        DigitalTwinStateRelationship<String> insideInDtStateRelationship = new DigitalTwinStateRelationship<>(relationship.getName(), relationship.getName());
                        this.digitalTwinState.createRelationship(insideInDtStateRelationship);
                        observePhysicalAssetRelationship(relationship);
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
            });

        });

        [...]

    }catch (Exception e){
        e.printStackTrace();
    }
}
```

When an Instance for a target observed Relationship has been notified by the Physical Adapter, we will receive a call back on the
Shadowing Function method called: ```onPhysicalAssetRelationshipEstablished(PhysicalAssetRelationshipInstanceCreatedWldtEvent<?> physicalAssetRelationshipInstanceCreatedWldtEvent)```.
The object ```PhysicalAssetRelationshipInstanceCreatedWldtEvent``` describes the events and contains an object ```PhysicalAssetRelationshipInstance```
with all the information about the new Relationship Instance.

The Shadowing Function analyzes the instance and create the corresponding Digital Relationship instance on the DT'State
through the class ```DigitalTwinStateRelationshipInstance``` and the method ```this.digitalTwinState.addRelationshipInstance(relName, instance);```.
The resulting implemented method is the following:

```java
//// Physical Relationships Notification Callbacks ////
@Override
protected void onPhysicalAssetRelationshipEstablished(PhysicalAssetRelationshipInstanceCreatedWldtEvent<?> physicalAssetRelationshipInstanceCreatedWldtEvent) {
    try{

        if(physicalAssetRelationshipInstanceCreatedWldtEvent != null
        && physicalAssetRelationshipInstanceCreatedWldtEvent.getBody() != null){
    
            PhysicalAssetRelationshipInstance<?> paRelInstance = physicalAssetRelationshipInstanceCreatedWldtEvent.getBody();
        
            if(paRelInstance.getTargetId() instanceof String){
        
                String relName = paRelInstance.getRelationship().getName();
                String relKey = paRelInstance.getKey();
                String relTargetId = (String)paRelInstance.getTargetId();
            
                DigitalTwinStateRelationshipInstance<String> instance = new DigitalTwinStateRelationshipInstance<String>(relName, relTargetId, relKey);
            
                this.digitalTwinState.addRelationshipInstance(relName, instance);
            }
        }
    }catch (Exception e){
        e.printStackTrace();
    }
}

@Override
protected void onPhysicalAssetRelationshipDeleted(PhysicalAssetRelationshipInstanceDeletedWldtEvent<?> physicalAssetRelationshipInstanceDeletedWldtEvent) {

}
```

At the end the new DT's Relationships and the associated instances can be managed on a Digital Adapter using the ```onDigitalTwinSync(IDigitalTwinState startDigitalTwinState)``` method and
the following callback methods:

- ```onStateChangeRelationshipCreated(DigitalTwinStateRelationship digitalTwinStateRelationship)```: A Relationship description has been added to the DT's State. The Relationship is passed to the method.
- ```onStateChangeRelationshipInstanceCreated(DigitalTwinStateRelationshipInstance digitalTwinStateRelationshipInstance)```: A Relationship Instance has been added to the DT's State. The Relationship Instance is passed to the method.
- ```onStateChangeRelationshipDeleted(DigitalTwinStateRelationship digitalTwinStateRelationship)```: A Relationship has been removed from the DT's State. The last Relationship is passed to the method.
- ```onStateChangeRelationshipInstanceDeleted(DigitalTwinStateRelationshipInstance digitalTwinStateRelationshipInstance)```: A Relationship Instance has been removed from the DT's State. The last Relationship Instance is passed to the method.

For example a simple implementation logging on the console can be:

```java
/**
 * Notification that a new Relationship has been created on the DT State
 * @param digitalTwinStateRelationship
 */
@Override
protected void onStateChangeRelationshipCreated(DigitalTwinStateRelationship digitalTwinStateRelationship) {
    System.out.println("[TestDigitalAdapter] -> onStateChangeRelationshipCreated(): " + digitalTwinStateRelationship);
}

/**
 * Notification that a new Relationship Instance has been created on the DT State
 * @param digitalTwinStateRelationshipInstance
 */
@Override
protected void onStateChangeRelationshipInstanceCreated(DigitalTwinStateRelationshipInstance digitalTwinStateRelationshipInstance) {
    System.out.println("[TestDigitalAdapter] -> onStateChangeRelationshipInstanceCreated(): " + digitalTwinStateRelationshipInstance);
}

/**
 * Notification that an existing Relationship has been removed from the DT State
 * @param digitalTwinStateRelationship
 */
@Override
protected void onStateChangeRelationshipDeleted(DigitalTwinStateRelationship digitalTwinStateRelationship) {
    System.out.println("[TestDigitalAdapter] -> onStateChangeRelationshipDeleted(): " + digitalTwinStateRelationship);
}

/**
 * Notification that an existing Relationships Instance has been removed
 * @param digitalTwinStateRelationshipInstance
 */
@Override
protected void onStateChangeRelationshipInstanceDeleted(DigitalTwinStateRelationshipInstance digitalTwinStateRelationshipInstance) {
    System.out.println("[TestDigitalAdapter] -> onStateChangeRelationshipInstanceDeleted(): " + digitalTwinStateRelationshipInstance);
}
```