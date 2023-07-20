# WhiteLabel Digital Twin Framework - Version 1.0

## Maven Dependency Import

TODO ...

## Motivation & Digital Twin "Definition" and Main Concepts

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
Main Scientific references supporting these definition are the following: 

TODO

### Digital Twin Abstraction & Model 

With respect to the element present in the real world, 
it is defined as a physical asset (PA) with the intention of referring to any entity 
that has a manifestation or relevance in the physical world and a well-defined lifespan.

<div align="center">
  <img class="center" src="images/abstract_dt_structure.jpeg" width="80%">
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

### The Shadowing Process

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

### Digital Twin Life Cycle

<p align="center">
  <img class="center" src="images/life_cycle.jpeg" width="80%">
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

#### From Unbound to Bound

Taking into account the target reference Life Cycle the first point to address is how we can move from an `UnBound` state
to a `Bound` condition with respect to the relationship with the Physical Layer. 

<p align="center">
  <img class="center" src="images/unbound_to_bound.jpeg" width="80%">
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

#### From Bound to Shadowed

Following the same approach described in the previous step we need to define a procedure to allow the DT to move from  a `Bound` state
to a `Shadowed` condition where the twin identified the interesting capabilities of the Physical Asset that has to be 
digitalized and according to the received Physical Asset Descriptions start the shadowing procedure to be synchronized with the physical world.

<p align="center">
  <img class="center" src="images/bound_to_shadowed.jpeg" width="80%">
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
  <img class="center" src="images/wldt_digital_events.jpeg" width="80%">
</p>


## Library Structure & Basic Concepts

The WLDT framework intends to maximize modularity, re-usability and flexibility in order to effectively mirror
physical smart objects in their digital counterparts.  The proposed library focuses on the simplification of twins
design and development aiming to provide a set of core features and functionalities for the widespread
adoption of Internet of Things DTs applications.

A WLDT instance is a general purpose software entity
implementing all the features and functionalities of a Digital Twin running
in cloud or on the edge. It has the peculiar characteristic
to be generic and ``attachable'' to any physical thing in order to
impersonate and maintain its digital replica and extend the provided functionalities
for example through the support of additional protocols or a specific translation
or normalization for data and formats.

Hereafter, the requirements that led the design and development of the WLDT framework are:
- i) Simplicity - with WLDT developers must have the possibility to easily create a new instance by using
  existing modules or customizing the behavior according the need of their application scenario;
- ii) Extensibility - while WLDT must be as simple and light as possible,
  the API should be also easily extendible in order to let programmers to personalize
  the configuration and/or to add new features loading and executing multiple modules at the same times;
- iii) Portability & Micorservice Readiness - a digital twin implemented through WLDT must
  be able to run on any platform without changes and customization. Our goal is to have a simple and light core engine
  with a strategic set of IoT-oriented features allowing the developer to easily create DT applications modeled
  as independent software agents and packed as microservices.

In the following Figure, the main components that make up the architecture of WLDT are represented, 
and thus through which the individual Digital Twin is implemented. 
Specifically, from the image it is possible to identify the three levels on which the 
architecture is developed: the one related to the ``core`` of the library, the one that ``models`` the DT, 
and finally, that of the ``adapters``.

<p align="center">
  <img class="center" src="images/wldt_structure.jpeg" width="80%">
</p>

Each of this core components has the following main characteristics:

- **WLDT Engine**: Defines the multi-thread engine of the library allowing the execution and monitoring of 
multiple workers simultaneously. Therefore, the it is also responsible for orchestrating 
the different internal modules of the architecture while keeping track of each one, and it can be 
considered the core of the DT itself
- **WLDT Event Bus**: Represents the internal Event Bus, designed to support communication between 
the different components of the DT's instance. It allows defining customized events to model 
both physical and digital input and outputs. Each WLDT's component can publish on the shared Event Bus and define 
an Event Filter to specify which types of events it is interested in managing, 
associating a specific callback to each one to process the different messages.
- **WLDT Workers**: Models the basic internal component and actually constitutes 
the single executable element by the WLDT Engine. 
Except for the Digital Twin State, each of the modules described later defines a specific implementation of a WLDT Worker.
- **Digital Twin State**:  It structures the state of the DT by defining the list of properties, events, and actions. 
The different instances included in the lists can correspond directly to elements of the physical asset 
or can derive from their combination, in any case, it is the Shadowing Model Function (SMF) that defines 
the mapping, following the model defined by the designer. 
This component also exposes a set of methods to allow SMF manipulation. 
Every time the Digital Twin State is modified, the latter generates the corresponding DT's event to notify all the components 
about the variation. 
- **Shadowing Model Function**: It is the library component responsible for defining the behavior of 
the Digital Twin by interacting with the Digital Twin State. 
Specifically, it implements the shadowing process that allows keeping the 
DT synchronized with its physical entity. 
This component is based on a specific implementation of a WLDT Worker called Model Engine,
in order to be executed by the WLDT Engine.
The Shadowing Model Function is the fundamental component that must be extended by the DT designer 
to concretize its model. 
The shadowing function observes the life cycle of the Digital Twin to be notified of the different state changes. 
For example, it is informed when the DT enters the Bound state, i.e. when its Physical Adapters
have completed the binding procedure with the physical asset. This component also allows the designer
 to define the behavior of the DT in case a property is modified, an event is triggered, or an action is invoked.
- **Physical Adapter**: It defines the essential functionalities that the individual extensions, 
related to specific protocols, must implement. 
As provided by the DT definition, a DT can be equipped with multiple Physical Adapters 
in order to manage communication with the corresponding physical entity. 
Each will produce a Physical Asset Description (PAD), 
i.e., a description of the properties, events, actions, and relationships 
 that the physical asset exposes through the specific protocol. 
The DT transitions from the Unbound to the Bound state when all its Physical Adapters 
have produced their respective PADs. 
The Shadowing Model Function, following the DT model, 
selects the components of the various PADs that it is interested in managing.
- **Digital Adapter**: It provides the set of callbacks that each specific implementation can use
to be notified of changes in the DT state. 
Symmetrically to what happens with Physical Adapters, a Digital Twin can define 
multiple Digital Adapters to expose its state and functionality through different protocols.

Therefore, to create a Digital Twin using WLDT, it is necessary to define a Shadowing Model Function and 
at least one Physical Adapter and one Digital Adapter, in order to enable connection with the physical 
entity and allow the DT to be used by external applications. Once the 3 components are defined, 
it is possible to instantiate the WLDT Engine and, subsequently, start the lifecycle of the DT. 
In the following sections we will go through the fundamental steps to start working with the library and creating all 
the basic modules to design, develop and execute our first Java Digital Twin.

[//]: # (## Getting started)

[//]: # ()
[//]: # (For developing a Digital Twin instance using the WLDT framework you have only to:)

[//]: # ()
[//]: # (* download the WLDT library through the .jar file or download the wldt-core maven project and install it in your locale repository;)

[//]: # (* develop your app using the ZWT library; for example if you are using a PC with Java SE, you can you the corresponding ZWT jar for this phase;)

[//]: # (* develop the entry class used for starting your app in the target platform&#40;s&#41;.)

[//]: # ()
[//]: # (### WLDT Worker Configuration)

[//]: # ()
[//]: # (The interface WldtWorkerConfiguration allows to create your personal data structure containing all the needed information for)

[//]: # (your new Worker to clone its physical counterpart and start operating. In the following example a new DummyConfiguration is created with some)

[//]: # (parameters, constructors and get/set methods.)

[//]: # ()
[//]: # (```java)

[//]: # (public class DummyWorkerConfiguration implements WldtWorkerConfiguration {)

[//]: # ()
[//]: # (    private String username = null;)

[//]: # (    private String password = null;)

[//]: # (    private String twinIp = null;)

[//]: # (    private int twinPort = 8080;)

[//]: # ()
[//]: # (    public DummyWorkerConfiguration&#40;&#41; {)

[//]: # (    })

[//]: # ()
[//]: # (    public DummyWorkerConfiguration&#40;String username, String password, String twinIp, int twinPort&#41; {)

[//]: # (        this.username = username;)

[//]: # (        this.password = password;)

[//]: # (        this.twinIp = twinIp;)

[//]: # (        this.twinPort = twinPort;)

[//]: # (    })

[//]: # ()
[//]: # (    public String getUsername&#40;&#41; { return username;})

[//]: # ()
[//]: # (    public void setUsername&#40;String username&#41; { this.username = username; })

[//]: # ()
[//]: # (    public String getPassword&#40;&#41; { return password; })

[//]: # ()
[//]: # (    public void setPassword&#40;String password&#41; { this.password = password;})

[//]: # ()
[//]: # (    public String getTwinIp&#40;&#41; { return twinIp; })

[//]: # ()
[//]: # (    public void setTwinIp&#40;String twinIp&#41; { this.twinIp = twinIp; })

[//]: # ()
[//]: # (    public int getTwinPort&#40;&#41; { return twinPort; })

[//]: # ()
[//]: # (    public void setTwinPort&#40;int twinPort&#41; { this.twinPort = twinPort; })

[//]: # (})

[//]: # (```)

[//]: # ()
[//]: # (### Worker Implementation)

[//]: # ()
[//]: # (WLDT allows developers to create new custom workers to handle and define specific behaviors and properly manage)

[//]: # (the automatic synchronization with physical counterparts. A new worker class extends the  class **WldtWorker** specifying the)

[//]: # (class types related to: the configuration through &#40;by extending of the base class *WldtWorkerConfiguration* and the class)

[//]: # (types for the key and value that can be used for the worker's cache.)

[//]: # (The main method that the developer must override in order to implement the behavior of its module is *startWorkerJob&#40;&#41;*.)

[//]: # (This function is called by the engine a the startup instant when it is ready to execute the worker&#40;s&#41; on the thread pool.)

[//]: # (Internally the worker can implement its own logic with additional multi-threading solutions and the import of any required libraries.)

[//]: # ()
[//]: # (In the following example we are creating a Dummy worker emulating a set of GET request to an external object.)

[//]: # (The complete example can be found in the repository [WLDT Dummy Worker]&#40;https://github.com/wldt/wldt-dummy-example&#41;.)

[//]: # (The WldtDummyWorker class extends WldtWorker specifying DummyWorkerConfiguration as configuration reference and String and Integer)

[//]: # (as Key and Value of the associated Worker caching system.)

[//]: # ()
[//]: # (```java)

[//]: # (public class WldtDummyWorker extends WldtWorker<DummyWorkerConfiguration, String, Integer> {)

[//]: # ()
[//]: # (    public WldtDummyWorker&#40;String wldtId, DummyWorkerConfiguration dummyWorkerConfiguration&#41; {)

[//]: # (        super&#40;dummyWorkerConfiguration&#41;;)

[//]: # (        this.random = new Random&#40;&#41;;)

[//]: # (        this.wldtId = wldtId;)

[//]: # (    })

[//]: # ()
[//]: # (    public WldtDummyWorker&#40;String wldtId, DummyWorkerConfiguration dummyWorkerConfiguration, IWldtCache<String, Integer> wldtCache&#41; {)

[//]: # (        super&#40;dummyWorkerConfiguration, wldtCache&#41;;)

[//]: # (        this.random = new Random&#40;&#41;;)

[//]: # (        this.wldtId = wldtId;)

[//]: # (    })

[//]: # ()
[//]: # (    @Override)

[//]: # (    public void startWorkerJob&#40;&#41; throws WldtConfigurationException, WldtRuntimeException {)

[//]: # ()
[//]: # (        try{)

[//]: # (            for&#40;int i = 0; i < RUN_COUNT_LIMIT; i++&#41;)

[//]: # (                emulateExternalGetRequest&#40;i&#41;;)

[//]: # (        }catch &#40;Exception e&#41;{)

[//]: # (            e.printStackTrace&#40;&#41;;)

[//]: # (        })

[//]: # (    })

[//]: # ()
[//]: # (    private void emulateExternalGetRequest&#40;int roundIndex&#41; {)

[//]: # (        ...)

[//]: # (    })

[//]: # ()
[//]: # (})

[//]: # (```)

[//]: # ()
[//]: # (### Internal Data Caching System)

[//]: # ()
[//]: # (The framework provides an internal shared caching system that can be adopted by each worker specifying the typology)

[//]: # (of its cache in terms of key and value class types. The interface \code{IWldtCache<K,V>})

[//]: # (defines the methods for a \NAME cache and a default implementation is provided by the)

[//]: # (framework through the class \code{WldtCache<K, V>}.)

[//]: # (Main exposed methods are:)

[//]: # ()
[//]: # (* void initCache&#40;&#41;)

[//]: # (* void deleteCache&#40;&#41;)

[//]: # (* void putData&#40;K key, V value&#41;)

[//]: # (* Optional<V> getData&#40;K key&#41;)

[//]: # (* void removeData&#40;K key&#41;)

[//]: # ()
[//]: # (Each cache instance is characterized by a string identifier and optionally)

[//]: # (by an expiration time and a size limit.)

[//]: # (An instance can be directly passed as construction parameter of)

[//]: # (a worker or it can be internally created for example inside a processing pipeline to handle cached data during data synchronization.)

[//]: # ()
[//]: # (```java)

[//]: # (if&#40;this.workerCache != null && this.workerCache.getData&#40;CACHE_VALUE_KEY&#41;.isPresent&#40;&#41;&#41; {)

[//]: # (    physicalObjectValue = this.workerCache.getData&#40;CACHE_VALUE_KEY&#41;.get&#40;&#41;;)

[//]: # (})

[//]: # (else{)

[//]: # (    physicalObjectValue = retrieveValueFromPhysicalObject&#40;&#41;;)

[//]: # (    if&#40;this.workerCache != null&#41;)

[//]: # (        this.workerCache.putData&#40;CACHE_VALUE_KEY, physicalObjectValue&#41;;)

[//]: # (})

[//]: # (```)

[//]: # ()
[//]: # (The cache is configured when the single worker is created in the setup phase. See the following code and the next sections.)

[//]: # ()
[//]: # (```java)

[//]: # (WldtDummyWorker wldtDummyWorker = new WldtDummyWorker&#40;)

[//]: # (        wldtEngine.getWldtId&#40;&#41;,)

[//]: # (        new DummyWorkerConfiguration&#40;&#41;,)

[//]: # (        new WldtCache<>&#40;5, TimeUnit.SECONDS&#41;&#41;;)

[//]: # (```)

[//]: # ()
[//]: # (### Processing Pipelines)

[//]: # ()
[//]: # (As previously mentioned, the Processing Pipeline layer has been introduced to allow developer to easily and dynamically)

[//]: # (customize the behavior of WLDT workers through the use of configurable processing chains.)

[//]: # (The pipeline is defined through the interface ***IProcessingPipeline*** and the step definition)

[//]: # (class ***ProcessingStep***. Main methods to work and configure the pipeline are:)

[//]: # (* addStep&#40;ProcessingStep step&#41;)

[//]: # (* removeStep&#40;ProcessingStep step&#41;)

[//]: # (* start&#40;PipelineData initialData, ProcessingPipelineListener listener&#41;)

[//]: # ()
[//]: # (***ProcessingStep*** and ***PipelineData*** classes are used to describe and implement each single step and to model the data passed trough the chain.)

[//]: # (Each step takes as input an initial PipelineData value and produces as output a new one.)

[//]: # (Two listeners classes have been also defined &#40;***ProcessingPipelineListener*** and ***ProcessingStepListener***&#41;)

[//]: # (to notify interested actors about the status of each step and/or the final results of the processing pipeline)

[//]: # (through the use of methods ***onPipelineDone&#40;Optional<PipelineData> result&#41;*** and ***onPipelineError&#40;&#41;***.)

[//]: # ()
[//]: # (An example of PipelineData Implementation is the following:)

[//]: # ()
[//]: # (```java)

[//]: # (public class DummyPipelineData implements PipelineData {)

[//]: # ()
[//]: # (    private int value = 0;)

[//]: # ()
[//]: # (    public DummyPipelineData&#40;int value&#41; { this.value = value; })

[//]: # ()
[//]: # (    public int getValue&#40;&#41; { return value; })

[//]: # ()
[//]: # (    public void setValue&#40;int value&#41; { this.value = value; })

[//]: # ()
[//]: # (})

[//]: # (```)

[//]: # ()
[//]: # (A dummy processing step using the DummyPipelineData class created is presented in the following code snippet.)

[//]: # (The developer can override the ***execute*** method in order to define the single Step implementation receiving as parameters)

[//]: # (the incoming *Pipeline* data and the *ProcessingStepListener*.)

[//]: # (The listener is used to notify when the step has been correctly &#40;*onStepDone*&#41; completed and the new PipelineData output value has been generated.)

[//]: # (In case of an error the method *onStepError* can be used to notify the event.)

[//]: # ()
[//]: # (```java)

[//]: # (public class DummyProcessingStep implements ProcessingStep {)

[//]: # ()
[//]: # (    @Override)

[//]: # (    public void execute&#40;PipelineCache pipelineCache, PipelineData data, ProcessingStepListener listener&#41; {)

[//]: # (        if&#40;data instanceof DummyPipelineData && listener != null&#41; {)

[//]: # ()
[//]: # (            DummyPipelineData pipelineData = &#40;DummyPipelineData&#41;data;)

[//]: # ()
[//]: # (            //Updating pipeline data)

[//]: # (            pipelineData.setValue&#40;pipelineData.getValue&#40;&#41;*2&#41;;)

[//]: # ()
[//]: # (            listener.onStepDone&#40;this, Optional.of&#40;pipelineData&#41;&#41;;)

[//]: # (        })

[//]: # (        else {)

[//]: # (            if&#40;listener != null&#41; {)

[//]: # (                String errorMessage = "PipelineData Error !";)

[//]: # (                listener.onStepError&#40;this, data, errorMessage&#41;;)

[//]: # (            })

[//]: # (            else)

[//]: # (                logger.error&#40;"Processing Step Listener = Null ! Skipping processing step"&#41;;)

[//]: # (        })

[//]: # (    })

[//]: # (})

[//]: # (```)

[//]: # ()
[//]: # (### Monitor Metrics and Performance)

[//]: # ()
[//]: # (The framework allows the developer to easily define, measure,)

[//]: # (track and send to a local collector all the application's metrics and logs.)

[//]: # (This information can be also useful to dynamically balance the load on active)

[//]: # (twins operating on distributed clusters or to detect unexpected behaviors or performance degradation.)

[//]: # (The framework implements a singleton class called ***WldtMetricsManager*** exposing the methods:)

[//]: # (* getTimer&#40;String metricId, String timerKey&#41;)

[//]: # (* measureValue&#40;String metricId, String key, int value&#41;)

[//]: # ()
[//]: # (These methods can be used to track elapsed time of a specific processing code block or with the second option to measure)

[//]: # (a value of interest associated to a key identifier.)

[//]: # ()
[//]: # (```java)

[//]: # (private void emulateExternalGetRequest&#40;int roundIndex&#41; {)

[//]: # ()
[//]: # (    Timer.Context metricsContext = WldtMetricsManager.getInstance&#40;&#41;.getTimer&#40;String.format&#40;"%s.%s", METRIC_BASE_IDENTIFIER, this.wldtId&#41;, WORKER_EXECUTION_TIME_METRICS_FIELD&#41;;)

[//]: # ()
[//]: # (    try{)

[//]: # ()
[//]: # (        //YOUR CODE)

[//]: # ()
[//]: # (    }catch &#40;Exception e&#41;{)

[//]: # (        e.printStackTrace&#40;&#41;;)

[//]: # (    })

[//]: # (    finally {)

[//]: # (        if&#40;metricsContext != null&#41;)

[//]: # (            metricsContext.stop&#40;&#41;;)

[//]: # (    })

[//]: # (})

[//]: # (```)

[//]: # ()
[//]: # (<p align="center">)

[//]: # (  <img class="center" src="images/example_metrics.jpeg" width="80%">)

[//]: # (</p>)

[//]: # ()
[//]: # (The WLDT metric system provides by default two reporting option allowing the developer)

[//]: # (to periodically save the metrics on a CSV file or to send them directly to a Graphite collector.)

[//]: # (An example of WldtConfiguration enabling both CSV and Graphite monitoring is the following:)

[//]: # ()
[//]: # (```java)

[//]: # (WldtConfiguration wldtConfiguration = new WldtConfiguration&#40;&#41;;)

[//]: # (wldtConfiguration.setDeviceNameSpace&#40;"it.unimore.dipi.things"&#41;;)

[//]: # (wldtConfiguration.setWldtBaseIdentifier&#40;"wldt"&#41;;)

[//]: # (wldtConfiguration.setWldtStartupTimeSeconds&#40;10&#41;;)

[//]: # (wldtConfiguration.setApplicationMetricsEnabled&#40;true&#41;;)

[//]: # (wldtConfiguration.setApplicationMetricsReportingPeriodSeconds&#40;10&#41;;)

[//]: # (wldtConfiguration.setMetricsReporterList&#40;Arrays.asList&#40;"csv", "graphite"&#41;&#41;;)

[//]: # (wldtConfiguration.setGraphitePrefix&#40;"wldt"&#41;;)

[//]: # (wldtConfiguration.setGraphiteReporterAddress&#40;"127.0.0.1"&#41;;)

[//]: # (wldtConfiguration.setGraphiteReporterPort&#40;2003&#41;;)

[//]: # (```)

[//]: # ()
[//]: # (### Execute the new Worker)

[//]: # ()
[//]: # (A new worker can be executed through the use of the main class WldtEngine.)

[//]: # (The class WldtConfiguration is used to configure the behavior of the WLDT framework as illustrated in the followinf simple example.)

[//]: # ()
[//]: # (```java)

[//]: # (public static void main&#40;String[] args&#41;  {)

[//]: # ()
[//]: # (    try{)

[//]: # ()
[//]: # (        //Manual creation of the WldtConfiguration)

[//]: # (        WldtConfiguration wldtConfiguration = new WldtConfiguration&#40;&#41;;)

[//]: # (        wldtConfiguration.setDeviceNameSpace&#40;"it.unimore.dipi.things"&#41;;)

[//]: # (        wldtConfiguration.setWldtBaseIdentifier&#40;"it.unimore.dipi.iot.wldt.example.dummy"&#41;;)

[//]: # (        wldtConfiguration.setWldtStartupTimeSeconds&#40;10&#41;;)

[//]: # (        wldtConfiguration.setApplicationMetricsEnabled&#40;true&#41;;)

[//]: # (        wldtConfiguration.setApplicationMetricsReportingPeriodSeconds&#40;10&#41;;)

[//]: # (        wldtConfiguration.setMetricsReporterList&#40;Collections.singletonList&#40;"csv"&#41;&#41;;)

[//]: # ()
[//]: # (        //Init the Engine)

[//]: # (        WldtEngine wldtEngine = new WldtEngine&#40;wldtConfiguration&#41;;)

[//]: # ()
[//]: # (        //Init Dummy Worker with Cache)

[//]: # (        WldtDummyWorker wldtDummyWorker = new WldtDummyWorker&#40;)

[//]: # (                wldtEngine.getWldtId&#40;&#41;,)

[//]: # (                new DummyWorkerConfiguration&#40;&#41;,)

[//]: # (                new WldtCache<>&#40;5, TimeUnit.SECONDS&#41;&#41;;)

[//]: # ()
[//]: # (        //Set a Processing Pipeline)

[//]: # (        wldtDummyWorker.addProcessingPipeline&#40;WldtDummyWorker.DEFAULT_PROCESSING_PIPELINE, new ProcessingPipeline&#40;new DummyProcessingStep&#40;&#41;&#41;&#41;;)

[//]: # (        )
[//]: # (        //Add the New Worker)

[//]: # (        wldtEngine.addNewWorker&#40;wldtDummyWorker&#41;;)

[//]: # (        )
[//]: # (        //Start workers)

[//]: # (        wldtEngine.startWorkers&#40;&#41;;)

[//]: # ()
[//]: # (    }catch &#40;Exception | WldtConfigurationException e&#41;{)

[//]: # (        e.printStackTrace&#40;&#41;;)

[//]: # (    })

[//]: # (})

[//]: # (```)

[//]: # (## Built-in MQTT-to-MQTT Mirroring)

[//]: # ()
[//]: # (The first built-in IoT dedicated worker is implemented through the class `Mqtt2MqttWorker`)

[//]: # (providing a configurable way to automatically synchronize data between twins over MQTT.)

[//]: # (The protocol is based on a Pub/Sub approach where categorization of data and resources)

[//]: # (are inferred by topics definition &#40;e.g., a device '01' can send a telemetry data for a resource 'abcd')

[//]: # (to the following topic /dev/01/res/abcd/&#41;. An MQTT physical device can be at the same time a data producer)

[//]: # (&#40;e.g., to publish for example telemetry data&#41; or a consumer &#40;e.g., to receive external commands&#41;.)

[//]: # ()
[//]: # (The topics structure is fully customizable allowing the developer to define how the Digital Twin can)

[//]: # (consume and/or send messages over MQTT. Configurable topics are associated to the following two categories:)

[//]: # ()
[//]: # (- `MQTT_TOPIC_TYPE_DEVICE_OUTGOING`: Topics used by the physical device to publish packets and data towards an external)

[//]: # (  broker and consequently external subscribers &#40;in our case the DT acts as a consumer&#41;. Telemetry or Events topics belong to this category)

[//]: # (  and can be used by the DT to shadow the status of the physical device. For this category the DT acts both as a subscriber to receive)

[//]: # (  data coming from the device and as a publisher to send them out to a target broker.)

[//]: # (- `MQTT_TOPIC_TYPE_DEVICE_INCOMING`: Topics used by the physical device to subscribe to incoming messages generated by)

[//]: # (  external publishers and associated to a target MQTT broker. These topics can be associated to external commands or)

[//]: # (  actions sent to the device. For this category the DT acts as a subscriber to receive)

[//]: # (  data coming external entities and as a publisher to forward received commands/actions/messages to the associated physical counterpart.)

[//]: # ()
[//]: # (The following Figure depicts an example where the physical device has two outgoing topics associated to)

[//]: # (the telemetry of a temperature sensor and overheating events. The DT is interested to shadow both infomation)

[//]: # (and the related topics should be configured within the WLDT library as `MQTT_TOPIC_TYPE_DEVICE_OUTGOING`.)

[//]: # (In that case the DT will be a subscriber for the two topics and at the same time a publisher to expose them to other)

[//]: # (external digital applications.)

[//]: # ()
[//]: # (<p align="center">)

[//]: # (  <img class="center" src="images/mqtt_device_outgoing_topic.jpg" width="80%">)

[//]: # (</p>)

[//]: # ()
[//]: # (In the following second example we highlight how the physical device supports two incoming topics in order)

[//]: # (to receive information associated to external commands &#40;e.g., turn on or off a light&#41; and/or re-configuration information.)

[//]: # (These two topics should be handled and configured within the WLDT library as `MQTT_TOPIC_TYPE_DEVICE_INCOMING` topics since)

[//]: # (the DT will be a subscriber for the two topics and the data coming from external applications and a publisher to)

[//]: # (forward received information to the physical device.)

[//]: # ()
[//]: # (<p align="center">)

[//]: # (  <img class="center" src="images/mqtt_device_incoming_topic.jpg" width="80%">)

[//]: # (</p>)

[//]: # ()
[//]: # (The WLDT class used to configure and shape the DT to handle target MQTT topics is `MqttTopicDescriptor`.)

[//]: # (It allows the developer to specify and configure the following aspect of the topic:)

[//]: # ()
[//]: # (- `type`: if the topic is `MQTT_TOPIC_TYPE_DEVICE_OUTGOING` or `MQTT_TOPIC_TYPE_DEVICE_INCOMING`)

[//]: # (- `id`: an internal id useful to uniquely identify the topics configuration. It will be used to introduce dedicated)

[//]: # (  ProcessingPipeline on the target topi)

[//]: # (- `topic`: the actual value of the topic used by the physical device &#40;e.g., /telemetry/temp or /command&#41;)

[//]: # (- `resourceId`: an optional field to specify if the topic is associated to a resource. This approach can be useful if)

[//]: # (  the developer wants to keep a resource oriented modelling of the MQTT topic &#40;inspired by a RESTful approach&#41;.)

[//]: # (  This resource id can be also used within the built-in placeholder/wildcard WLDT tools in order to simplify the)

[//]: # (  configuration of resource oriented topics &#40;see a following example&#41;.)

[//]: # (- `subscribeQosLevel`: specify the MQTT QoS level that the DT should use to subscribe to the target topic)

[//]: # (- `publishQosLevel`: specify the MQTT QoS level that the DT should use to publish on the target topic)

[//]: # ()
[//]: # (MQTT QoS levels have been also mapped into the enum called `MqttQosLevel` and expose the following three values to be)

[//]: # (easily used by the developers: i&#41;  `MqttQosLevel.MQTT_QOS_0`; ii&#41; `MqttQosLevel.MQTT_QOS_1`; iii&#41; and `MqttQosLevel.MQTT_QOS_2`.)

[//]: # ()
[//]: # (The following example shows a WLDT implementation using the built-in MQTT to MQTT worker)

[//]: # (to automatically create a digital twin of an existing MQTT physical object.)

[//]: # ()
[//]: # (As a first step we create the in order to shape how the DT should handle the MQTT communication with the device in terms)

[//]: # (of source &#40;physical&#41; and destination &#40;digital&#41; brokers. In that example they are different but they can also be the same.)

[//]: # ()
[//]: # (```java)

[//]: # (Mqtt2MqttConfiguration mqtt2MqttConfiguration = new Mqtt2MqttConfiguration&#40;&#41;;)

[//]: # (mqtt2MqttConfiguration.setBrokerAddress&#40;"127.0.0.1"&#41;;)

[//]: # (mqtt2MqttConfiguration.setBrokerPort&#40;1883&#41;;)

[//]: # (mqtt2MqttConfiguration.setDestinationBrokerAddress&#40;"127.0.0.1"&#41;;)

[//]: # (mqtt2MqttConfiguration.setDestinationBrokerPort&#40;1884&#41;;)

[//]: # (mqtt2MqttConfiguration.setDeviceId&#40;"id:97b904ada0f9"&#41;;)

[//]: # (```)

[//]: # ()
[//]: # (The next step is to add to the configuration the list of target topics:)

[//]: # ()
[//]: # (```java)

[//]: # (mqtt2MqttConfiguration.setTopicList&#40;)

[//]: # (    Arrays.asList&#40;)

[//]: # (        new MqttTopicDescriptor&#40;"temperature_topic_id",)

[//]: # (            "temperature_resource_id",)

[//]: # (            "device/id:97b904ada0f9/telemetry/temp",)

[//]: # (            MqttTopicDescriptor.MQTT_TOPIC_TYPE_DEVICE_OUTGOING,)

[//]: # (            MqttQosLevel.MQTT_QOS_2,)

[//]: # (            MqttQosLevel.MQTT_QOS_2&#41;,)

[//]: # (        new MqttTopicDescriptor&#40;"event_topic_id",)

[//]: # (            "overheating_event_id",)

[//]: # (            "device/id:97b904ada0f9/event/overheating",)

[//]: # (            MqttTopicDescriptor.MQTT_TOPIC_TYPE_DEVICE_OUTGOING,)

[//]: # (            MqttQosLevel.MQTT_QOS_2,)

[//]: # (            MqttQosLevel.MQTT_QOS_2&#41;,)

[//]: # (        new MqttTopicDescriptor&#40;"command_topic_id",)

[//]: # (            "command_resource_id",)

[//]: # (            "device/id:97b904ada0f9/command",)

[//]: # (            MqttTopicDescriptor.MQTT_TOPIC_TYPE_DEVICE_INCOMING,)

[//]: # (            MqttQosLevel.MQTT_QOS_2,)

[//]: # (            MqttQosLevel.MQTT_QOS_2&#41;)

[//]: # (        &#41;)

[//]: # (    &#41;;)

[//]: # (```)

[//]: # ()
[//]: # (The next step is to create the WLDT Configuration and the associated Engine, create the `Mqtt2MqttWorker` with the)

[//]: # (new defined configuration, add the worker to the engine and then start it !)

[//]: # ()
[//]: # (```java)

[//]: # (WldtConfiguration wldtConfiguration = new WldtConfiguration&#40;&#41;;)

[//]: # (wldtConfiguration.setDeviceNameSpace&#40;"it.unimore.dipi.things"&#41;;)

[//]: # (wldtConfiguration.setWldtBaseIdentifier&#40;"wldt"&#41;;)

[//]: # (wldtConfiguration.setWldtStartupTimeSeconds&#40;10&#41;;)

[//]: # (wldtConfiguration.setApplicationMetricsEnabled&#40;false&#41;;)

[//]: # ()
[//]: # (WldtEngine wldtEngine = new WldtEngine&#40;wldtConfiguration&#41;;)

[//]: # ()
[//]: # (Mqtt2MqttWorker mqtt2MqttWorker = new Mqtt2MqttWorker&#40;wldtEngine.getWldtId&#40;&#41;, mqtt2MqttConfiguration&#41;;)

[//]: # ()
[//]: # (wldtEngine.addNewWorker&#40;mqtt2MqttWorker&#41;;)

[//]: # (wldtEngine.startWorkers&#40;&#41;;)

[//]: # (```)

[//]: # ()
[//]: # (### Dynamic Topic Management & Configuration)

[//]: # ()
[//]: # (As previously anticipate the WLDT Library includes a built-in placeholder/wildcard WLDT tools in order to simplify the)

[//]: # (configuration of resource oriented topics. It uses the [Mustache]&#40;https://mustache.github.io/&#41; library)

[//]: # (to dynamically synchronize MQTT topics according to available device and resource information.)

[//]: # ()
[//]: # (The developer can use and automatically include into the configuration strings values associated to:)

[//]: # (- `device_id`: The values configured into the `Mqtt2MqttConfiguration` &#40;placeholder string `{{device_id}}`&#41;)

[//]: # (- `resource_id`: The resource identifier specified directly into the `MqttTopicDescriptor` &#40;placeholder string `{{resource_id}}`&#41;)

[//]: # ()
[//]: # (Through this approach the previous example can be simplified as follows:)

[//]: # ()
[//]: # (```java)

[//]: # (mqtt2MqttConfiguration.setTopicList&#40;)

[//]: # (    Arrays.asList&#40;)

[//]: # (        new MqttTopicDescriptor&#40;"temperature_topic_id",)

[//]: # (            "temp",)

[//]: # (            "device/{{device_id}}/telemetry/{{resource_id}}",)

[//]: # (            MqttTopicDescriptor.MQTT_TOPIC_TYPE_DEVICE_OUTGOING,)

[//]: # (            MqttQosLevel.MQTT_QOS_2,)

[//]: # (            MqttQosLevel.MQTT_QOS_2&#41;,)

[//]: # (        new MqttTopicDescriptor&#40;"event_topic_id",)

[//]: # (            "overheating",)

[//]: # (            "device/{{device_id}}/telemetry/{{resource_id}}",)

[//]: # (            MqttTopicDescriptor.MQTT_TOPIC_TYPE_DEVICE_OUTGOING,)

[//]: # (            MqttQosLevel.MQTT_QOS_2,)

[//]: # (            MqttQosLevel.MQTT_QOS_2&#41;,)

[//]: # (        new MqttTopicDescriptor&#40;"command_topic_id",)

[//]: # (            "command_resource_id",)

[//]: # (            "device/{{device_id}}/telemetry/command",)

[//]: # (            MqttTopicDescriptor.MQTT_TOPIC_TYPE_DEVICE_INCOMING,)

[//]: # (            MqttQosLevel.MQTT_QOS_2,)

[//]: # (            MqttQosLevel.MQTT_QOS_2&#41;)

[//]: # (        &#41;)

[//]: # (    &#41;;)

[//]: # (```)

[//]: # ()
[//]: # (### MQTT Processing Pipelines)

[//]: # ()
[//]: # (Dedicated WLDT ProcessingPipeline can be associated to each topic in order to customize the)

[//]: # (management of received and forwarded messages through the WDLT Digital Twin.)

[//]: # (Each ProcessingPipeline can be defined as the composition of one or multiple ProcessingStep)

[//]: # (defined by the developer.)

[//]: # ()
[//]: # (Each step use as reference data structure the class `MqttPipelineData`)

[//]: # (containing the following information to handle and manipulate MQTT Data through Processing Pipelines:)

[//]: # ()
[//]: # (- `topic`: the value of the topic associated to the target data. It can be changed to dynamically handle incoming and outgoing)

[//]: # (  topics from and to the DT)

[//]: # (- `payload`: the content of the current MQTT message. It can be changed by the pipeline)

[//]: # (- `mqttTopicDescriptor`: the reference to the configured `MqttTopicDescriptor` defined in the `Mqtt2MqttWorker`)

[//]: # (- `isRetained`: a flag to specify if the message should be handled as retained or note. It can also be changed by the pipeline)

[//]: # ()
[//]: # (Two examples of two different pipelines are presented below.)

[//]: # ()
[//]: # (In that first example a ProcessingPipeline is associated to the temperature telemetry topic)

[//]: # (providing three steps:)

[//]: # ()
[//]: # (- An `IdentityProcessingStep` that is just used to show a log of the incoming packet and payload)

[//]: # (- The `MqttAverageProcessingStep` dedicated to evaluate the average value of the last 10 received temperature samples)

[//]: # (- The `MqttTopicChangeStep` changes the output topic)

[//]: # ()
[//]: # (```java             )

[//]: # (//Add Processing Pipeline for target topics)

[//]: # (mqtt2MqttWorker.addTopicProcessingPipeline&#40;"temperature_topic_id",)

[//]: # (        new ProcessingPipeline&#40;)

[//]: # (                new IdentityProcessingStep&#40;&#41;,)

[//]: # (                new MqttAverageProcessingStep&#40;&#41;,)

[//]: # (                new MqttTopicChangeStep&#40;&#41;)

[//]: # (        &#41;)

[//]: # (&#41;;)

[//]: # (```)

[//]: # ()
[//]: # (The definition of the `MqttTopicChangeStep` implementing the Java Interface `ProcessingStep`)

[//]: # (is illustrated in the following code example:)

[//]: # ()
[//]: # (```java)

[//]: # (public class MqttTopicChangeStep implements ProcessingStep {)

[//]: # ()
[//]: # (    private static final Logger logger = LoggerFactory.getLogger&#40;MqttTopicChangeStep.class&#41;;)

[//]: # (    )
[//]: # (    public MqttTopicChangeStep&#40;&#41; {)

[//]: # (    })

[//]: # ()
[//]: # (    @Override)

[//]: # (    public void execute&#40;PipelineCache pipelineCache, PipelineData incomingData, ProcessingStepListener listener&#41; {)

[//]: # ()
[//]: # (        MqttPipelineData data = null;)

[//]: # ()
[//]: # (        if&#40;incomingData instanceof MqttPipelineData&#41;)

[//]: # (            data = &#40;MqttPipelineData&#41;incomingData;)

[//]: # (        else if&#40;listener != null&#41;)

[//]: # (            listener.onStepError&#40;this, incomingData, String.format&#40;"Wrong PipelineData for MqttAverageProcessingStep ! Data type: %s", incomingData.getClass&#40;&#41;&#41;&#41;;)

[//]: # (        else)

[//]: # (            logger.error&#40;"Wrong PipelineData for MqttAverageProcessingStep ! Data type: {}", incomingData.getClass&#40;&#41;&#41;;)

[//]: # ()
[//]: # (        try{)

[//]: # (            if&#40;listener != null && Objects.requireNonNull&#40;data&#41;.getPayload&#40;&#41; != null&#41; {)

[//]: # (                String newTopic = String.format&#40;"%s/%s", "wldt-pipeline", data.getTopic&#40;&#41;&#41;;)

[//]: # (                listener.onStepDone&#40;this, Optional.of&#40;new MqttPipelineData&#40;newTopic, data.getMqttTopicDescriptor&#40;&#41;, data.getPayload&#40;&#41;, data.isRetained&#40;&#41;&#41;&#41;&#41;;)

[//]: # (            })

[//]: # (            else)

[//]: # (                logger.error&#40;"Processing Step Listener or MqttProcessingInfo Data = Null ! Skipping processing step"&#41;;)

[//]: # (        }catch &#40;Exception e&#41;{)

[//]: # (            logger.error&#40;"MQTT Processing Step Error: {}", e.getLocalizedMessage&#40;&#41;&#41;;)

[//]: # (            if&#40;listener != null&#41;)

[//]: # (                listener.onStepError&#40;this, data, e.getLocalizedMessage&#40;&#41;&#41;;)

[//]: # (        })

[//]: # (    })

[//]: # (})

[//]: # (```)

[//]: # ()
[//]: # (In the second example a ProcessingPipeline is associated to the command topic providing two steps:)

[//]: # (- An `IdentityProcessingStep` that is just used to show a log of the incoming packet and payload)

[//]: # (- The `MqttPayloadChangeStep` changes the payload in order to adapt the command received from)

[//]: # (  an external application into the format supported by the physical device)

[//]: # ()
[//]: # (```java )

[//]: # (mqtt2MqttWorker.addTopicProcessingPipeline&#40;DEMO_COMMAND_TOPIC_ID,)

[//]: # (        new ProcessingPipeline&#40;)

[//]: # (                new IdentityProcessingStep&#40;&#41;,)

[//]: # (                new MqttPayloadChangeStep&#40;&#41;)

[//]: # (        &#41;)

[//]: # (&#41;;)

[//]: # (```)

[//]: # ()
[//]: # (The implementation of the `MqttPayloadChangeStep` is illustrated in the following code block.)

[//]: # (It uses an additional demo class `DemoDataStructure` to change the payload into a new data structure and dynamically)

[//]: # (change the payload that will be handled by the DT)

[//]: # ()
[//]: # (```java)

[//]: # (public class MqttPayloadChangeStep implements ProcessingStep {)

[//]: # ()
[//]: # (    private static final Logger logger = LoggerFactory.getLogger&#40;MqttPayloadChangeStep.class&#41;;)

[//]: # ()
[//]: # (    private ObjectMapper mapper;)

[//]: # ()
[//]: # (    public MqttPayloadChangeStep&#40;&#41; {)

[//]: # (        this.mapper = new ObjectMapper&#40;&#41;;)

[//]: # (        this.mapper.setSerializationInclusion&#40;JsonInclude.Include.NON_NULL&#41;;)

[//]: # (    })

[//]: # ()
[//]: # (    @Override)

[//]: # (    public void execute&#40;PipelineCache pipelineCache, PipelineData incomingData, ProcessingStepListener listener&#41; {)

[//]: # ()
[//]: # (        MqttPipelineData data = null;)

[//]: # ()
[//]: # (        if&#40;incomingData instanceof MqttPipelineData&#41;)

[//]: # (            data = &#40;MqttPipelineData&#41;incomingData;)

[//]: # (        else if&#40;listener != null&#41;)

[//]: # (            listener.onStepError&#40;this, incomingData, String.format&#40;"Wrong PipelineData for MqttAverageProcessingStep ! Data type: %s", incomingData.getClass&#40;&#41;&#41;&#41;;)

[//]: # (        else)

[//]: # (            logger.error&#40;"Wrong PipelineData for MqttAverageProcessingStep ! Data type: {}", incomingData.getClass&#40;&#41;&#41;;)

[//]: # ()
[//]: # (        try{)

[//]: # ()
[//]: # (            if&#40;listener != null && Objects.requireNonNull&#40;data&#41;.getPayload&#40;&#41; != null&#41; {)

[//]: # (                listener.onStepDone&#40;this,)

[//]: # (                        Optional.of&#40;new MqttPipelineData&#40;data.getTopic&#40;&#41;,)

[//]: # (                                data.getMqttTopicDescriptor&#40;&#41;,)

[//]: # (                                mapper.writeValueAsBytes&#40;new DemoDataStructure&#40;data.getPayload&#40;&#41;&#41;&#41;, data.isRetained&#40;&#41;&#41;&#41;&#41;;)

[//]: # (            })

[//]: # (            else)

[//]: # (                logger.error&#40;"Processing Step Listener or MqttProcessingInfo Data = Null ! Skipping processing step"&#41;;)

[//]: # ()
[//]: # (        }catch &#40;Exception e&#41;{)

[//]: # (            logger.error&#40;"MQTT Processing Step Error: {}", e.getLocalizedMessage&#40;&#41;&#41;;)

[//]: # ()
[//]: # (            if&#40;listener != null&#41;)

[//]: # (                listener.onStepError&#40;this, data, e.getLocalizedMessage&#40;&#41;&#41;;)

[//]: # (        })

[//]: # (    })

[//]: # (})

[//]: # (```)

[//]: # ()
[//]: # (### Additional Configuration Options)

[//]: # ()
[//]: # (#### MQTT Clients IDs)

[//]: # ()
[//]: # (If required developer can specify the client ids for both the physical and the digital broker)

[//]: # ()
[//]: # (```java)

[//]: # (mqtt2MqttConfiguration.setBrokerClientId&#40;"physicalBrokerTestClientId"&#41;;)

[//]: # (mqtt2MqttConfiguration.setDestinationBrokerClientId&#40;"digitalBrokerTestClientId"&#41;;)

[//]: # (```)

[//]: # ()
[//]: # (#### Authentication & Security Configurations)

[//]: # ()
[//]: # (For both source &#40;e.g., physical&#41; and destination &#40;e.g., digital&#41; brokers it is possible to specify)

[//]: # (authentication and secure communication through the specification of server certificate and TLS context information.)

[//]: # ()
[//]: # (Exposed methods in the Mqtt2MqttConfiguration class are the following:)

[//]: # ()
[//]: # (- `setBrokerClientUsername&#40;String username&#41;`)

[//]: # (- `setBrokerClientPassword&#40;String password&#41;`)

[//]: # (- `setBrokerSecureCommunicationRequired&#40;boolean isActive&#41;`)

[//]: # (- `setBrokerTlsContext&#40;String tlsContext&#41;`)

[//]: # (- `setDestinationBrokerClientUsername&#40;String username&#41;`)

[//]: # (- `setDestinationBrokerClientPassword&#40;String password&#41;`)

[//]: # (- `setDestinationBrokerSecureCommunicationRequired&#40;boolean isActive&#41;`)

[//]: # (- `setDestinationBrokerTlsContext&#40;String tlsContext&#41;`)

[//]: # ()
[//]: # (An example for the secure configuration of the destination broker with username/password, server certificate and TLS)

[//]: # (is the following:)

[//]: # ()
[//]: # (```java)

[//]: # (mqtt2MqttConfiguration.setDestinationBrokerClientUsername&#40;MQTT_USERNAME&#41;;)

[//]: # (mqtt2MqttConfiguration.setDestinationBrokerClientPassword&#40;sasToken&#41;;)

[//]: # ()
[//]: # (mqtt2MqttConfiguration.setDestinationBrokerSecureCommunicationRequired&#40;true&#41;;)

[//]: # (mqtt2MqttConfiguration.setDestinationBrokerServerCertPath&#40;caCertFile&#41;;)

[//]: # (mqtt2MqttConfiguration.setBrokerTlsContext&#40;"TLSv1.2"&#41;;)

[//]: # (```)

[//]: # ()
[//]: # (## Built-in CoAP-to-CoAP Mirroring)

[//]: # ()
[//]: # (The second core built-in IoT worker is dedicated to the seamless mirroring)

[//]: # (of standard CoAP physical objects. The CoAP protocol through the use of CoRE Link Format)

[//]: # (and CoRE Interface provides by default both)

[//]: # (resource discovery and descriptions. It is possible for example to easily)

[//]: # (understand if a resource is a sensor or an actuator and which RESTful)

[//]: # (operations are allowed for an external client.)

[//]: # (Therefore, a WLDT instance can be automatically attached)

[//]: # (to a standard CoAP object without the need of any additional information.)

[//]: # (As illustrated in the following example, the class ***Coap2CoapWorker*** implements)

[//]: # (the logic to create and keep synchronized the two counterparts using standard)

[//]: # (methods and resource discovery through the use of ``/.well-known/core'' URI in)

[//]: # (order to retrieve the list of available resources and and mirror the corresponding digital replicas.)

[//]: # ()
[//]: # (Executed steps are:)

[//]: # ()
[//]: # (1&#41; The WLDT instance sends a GET on the standard ``/.well-known/core'' URI in order to retrieve the list of available resources;)

[//]: # (2&#41; the object responds with available resources using CoRE Link Format and CoRE Interface;)

[//]: # (3&#41; the worker creates for the twin the digital copy of each resource keeping the same descriptive)

[//]: # (   attributes &#40;e.g., id, URI, interface type, resource type, observability etc ...&#41;;)

[//]: # (4&#41; when an external client sends a CoAP Request to the WLDT it will forward the)

[//]: # (   request to the physical object with the same attributes and payload &#40;if present&#41;;)

[//]: # (5&#41; the physical device handles the request and sends the response back to the digital)

[//]: # (   replica that forwards the response to the requesting external client.)

[//]: # (   The worker implements also a caching system that can be enabled through the)

[//]: # (   ***Coap2CoapConfiguration*** class together with the information related to)

[//]: # (   the physical object network endpoint &#40;IP address and ports for CoAP and CoAPs&#41;.)

[//]: # ()
[//]: # (The following example shows a WLDT implementation using the built-in CoAP to CoAP worker to automatically)

[//]: # (create a digital twin of an existing CoAP physical object.)

[//]: # ()
[//]: # (```java  )

[//]: # (Coap2CoapConfiguration coapConf = new Coap2CoapConfiguration&#40;&#41;;)

[//]: # (coapConf.setResourceDiscovery&#40;true&#41;;)

[//]: # (coapConf.setDeviceAddress&#40;"127.0.0.1"&#41;;)

[//]: # (coapConf.setDevicePort&#40;5683&#41;;)

[//]: # ()
[//]: # (WldtEngine wldtEngine = new WldtEngine&#40;new WldtConfiguration&#40;&#41;&#41;;)

[//]: # (wldtEngine.addNewWorker&#40;new Coap2CoapWorker&#40;coapConf&#41;&#41;;)

[//]: # (wldtEngine.startWorkers&#40;&#41;;)

[//]: # (```)