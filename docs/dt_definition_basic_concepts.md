# Digital Twin "Definition" & Main Concepts

The White Label Digital Twin (WLDT) library aims to support the design, development and deployment of Digital Twins (DTs)
within the Internet of Thing (IoT) ecosystems. The library has been designed following the latest DT definitions
coming from both Industrial and Scientific domains and identifying DTs as active software components.
The Digital Twin definition that we use as reference for the upcoming definitions and library's architecture and main
modules is the following:

```text
A Digital Twin (DT) is a comprehensive software representation of an individual physical asset (PA). 
It includes the properties, conditions, relationships and behavior(s) of the real-life object through models and data. 
A Digital Twin is a set of realistic models that can digitalize an object’s behavior in the deployed environment. 
The Digital Twin represents and reflects its physical twin and remains its virtual counterpart across the object’s entire lifecycle.
```

## Digital Twin Abstraction & Model

With respect to the element present in the real world,
it is defined as a physical asset (PA) with the intention of referring to any entity
that has a manifestation or relevance in the physical world and a well-defined lifespan.

<div align="center">
  <img class="center" src="../images/abstract_dt_structure.jpeg" width="80%">
</div>

The previous Figure schematically illustrates the main component of an abstract Digital Twin and clarifies its
responsibility to be a bridge between the cyber and the physical world.
The blueprint components (then mapped into the WLDT Library) are:

- **Physical Interface** The entity in charge of both the initial *digitalization* o *shadowing* process and the perpetual responsibility
  to keep the DT and PA in synch during its life cycle. It can execute multiple **Physical Asset Adapters** to interact with the PA and detect and
  digitalize the physical event coming from the physical entity according to its nature and the supported protocols and data formats (e.g., through HTTP and JSON).
- **Digital Interface** The component complementary to the Physical Interface and in charge of handling DT's internal variations and events
  towards external digital entities and consumers.  It executes multiple and reusable **Digital Adapters** in charge of handling digital interactions and events
  and responsible for making the DT interoperable with external applications.
- **DT's Model** The module defining the DT's behaviour and its augmented functionalities. It supports the execution of different configurable
  and reusable modules and functionalities handling both physical and digial events according to the implemented behaviour.
  Furthermore, the Model is the component responsible to handle and keep updated the Digital Twin State as described in the following sections.

The Digital Twin ``Model``(M) allows capturing and representing the PA at an appropriate level of abstraction,
i.e., avoiding irrelevant aspects for its purpose and modeling only domain-level information rather than technological ones.
Finally, the link between the physical and digital copy is defined as shadowing. Specifically,
the term defines the process that enables continuous and (almost) real-time updating of the internal state of the DT in relation to changes
that occur in the PA.

Each DT is thus equipped with an internal model, which defines how the PA is represented in the digital level.
The DT's representation denoted as **Digital Twin State** supported and defined through M is defined in terms of:

- **Properties**: represent the observable attributes of the corresponding PA as labeled data
  whose values can dynamically change over time, in accordance with the evolution of the PA's state.
- **Events**: represent the domain-level events that can be observed in the PA.
- **Relationships**: represent the links that exist between the modeled PA and other physical
  assets of the organizations through links to their corresponding Digital Twins.
  Like properties, relationships can be observed, dynamically created, and change over time,
  but unlike properties, they are not properly part of the PA's state but of its operational context
  (e.g., a DT of a robot within a production line).
- **Actions**: represent the actions that can be invoked on the PA through interaction with the DT or directly
  on the DT if they are not directly available on the PA (the DT is augmenting the physical capabilities).

Once the model M is defined, the dynamic state of the DT (SDT) can be defined by through the combination of
its *properties*, *events*, *relationships* and *actions* associated to the DT *timestamp* that represents the current time of
synchronization between the physical and digital counterparts.

## The Shadowing Process

The *shadowing* process (also known as replication of digitalization) allows to keep the
Digital Twin State synchronized with that of the corresponding physical resource
according to what is defined by the model M. Specifically, each relevant update of the PA state (SPA)
is translated into a sequence of 3 main steps:

- each relevant change in physical asset state is modeled by a ``physical_event`` (``e_pa``);
- the event is propagated to the DT;
- given the new ``physical_event``, the DT's is updated through the application of a *shadowing function*,
  which depends on the model M

The shadowing process allows also the DT to reflect and invoke possible actions of the PA.
The DT receives an action request (denoted as ``digital_action``) on its digital interface, applies the shadowing function to validate it and then
propagates the request through its physical interface.
An important aspect to emphasize is that the request for a ``digital_action`` does not
directly change the state of the DT since any changes can only occur as a result of the
shadowing function from the PA to the DT, as described earlier.

## Digital Twin Life Cycle

<p align="center">
  <img class="center" src="../images/life_cycle.jpeg" width="80%">
</p>

The modeling of the concept of DT includes also the definition and characterization of its life cycle.
Based on the scientific literature, we model (and then map into the library) a life cycle with 5 states
through which the DT goes from when it is executed to when it is stopped.
The previous Figure shows a graphical representation of the life cycle with the following steps:

- **Operating & Not Bound**: this is the state in which the DT is located following the initialization phase,
  indicating that all internal modules of the DT are active but there is no association yet with the corresponding PA.
- **Bound**: this is the state in which the DT transitions following the correct execution of the binding procedure.
  The binding procedure allows to connect the two parts and enables bidirectional flow of events.
- **Shadowed**: this is the state reached by the DT when the shadowing process begins and its state
  is correctly synchronized with that of the PA.
- **Out of Sync**: this is the state that determines the presence of errors in the shadowing process.
  When in this state, the DT is not able to handle either state alignment events or those generated
  by the application layer.
- **Done**: this is the state that the DT reaches when the shadowing process is stopped,
  but the DT continues to be active to handle requests coming from external applications.

### From Unbound to Bound

Taking into account the target reference Life Cycle the first point to address is how we can move from an `UnBound` state
to a `Bound` condition with respect to the relationship with the Physical Layer.

<p align="center">
  <img class="center" src="../images/unbound_to_bound.jpeg" width="80%">
</p>

The previous Figure illustrates a simple scenario where a Physical Asset uses two protocols (P1 and P2) to communicate and it is
connected to the Digital Twin through a DT's Physical Interface enabled with two dedicated Adapters for protocol P1 and P2.
In order to move from the Unbound to Bound state the DT should be aware of the description of the target asset with respect to
the two protocols. For example through P1 the asset exposes telemetry data (e.g., light bulb status and energy consumption)
while on P2 allows incoming action requests (e.g., turn on/off the light). The Digital Twin can start the shadowing process
only when it is bound and has a description of the properties and capabilities of the associated physical counterpart.
The schematic procedure is illustrated in the following Figure:

<p align="center">
  <img class="center" src="images/unbound_to_bound_steps.jpeg" width="80%">
</p>

Involved steps are:

1. The Adapter P1 communicates with the PA through Protocol 1 and provides a ``Physical Asset Description`` from its perspective
2. The Adapter P2 communicates with the PA through Protocol 2 and provides a ``Physical Asset Description`` from its perspective
3. Only when all Physical Adapters have been correctly bound (it may require time) to the Physical Asset and the associated ``Physical Asset Descriptions``
   have been generated, the DT can move from UnBound to Bound

Main core aspects associated to the concept of Physical Asset Description (PAD) are the following:

- It is used to describe the list of **properties**, **actions** and **relationships** of a Physical Asset
- Each Physical Adapter generates a dedicated PAD associated to its perspective on the Physical Assets
  and its capabilities to read data and execute actions
- It is a responsibility of the DT to handle multiple descriptions in order to build the digital replica
- It will be used by the DT to handle the shadowing process and keep the digital replica synchronized with the physical counterpart

### From Bound to Shadowed

Following the same approach described in the previous step we need to define a procedure to allow the DT to move from  a `Bound` state
to a `Shadowed` condition where the twin identified the interesting capabilities of the Physical Asset that has to be
digitalized and according to the received Physical Asset Descriptions start the shadowing procedure to be synchronized with the physical world.

<p align="center">
  <img class="center" src="../images/bound_to_shadowed.jpeg" width="80%">
</p>

As schematically illustrated in the previous Figure, involved steps are:

1. The Model defines which properties should be monitored on the Physical Asset and start observing
   them through the target adapters
2. Involved Physical Adapters communicate with the Physical Asset, receive data and generate Events (ePA)
   to notify about physical property changes
3. Received ePA will be used by the Digital Twin Model in order to run the
   Shadowing function and compute the new DT State
4. The DT can move from the Bound to Shadowed phase until it is able to maintain a proper synchronization
   with the physical asset over time through its shadowing process and the generation and maintenance of the DT's State

The Digital Twin State is structured and characterized by the following elements:

- A list of properties
- A list of actions
- A list of relationships

Listed elements can be directly associated to the corresponding element of the Physical Asset or generated by DT Model
combining multiple physical properties, actions or relationships at the same time. The Digital Twin State can be managed
through the Shadowing Function and exposes a set of methods for its manipulated. When there is a change in the DT State an event (eDT) will be generated

The manipulation of DT's State generates a set of DT's events (eDT) associated to each specific variation and evolution of the
twin during its life cyle. These events are used by the Digital Interface and in particular by its Digital Adapters to
expose the DT's State, its properties and capabilities to the external digital world. At the same time, eDT can be used by
Digital Adapters to trigger action on the DT and consequently to propagate (if acceptable and/or needed)
the incoming request to the physical assets bound with the target DT. Supported events are illustrated in the following
schema.

<p align="center">
  <img class="center" src="../images/wldt_digital_events.jpeg" width="80%">
</p>