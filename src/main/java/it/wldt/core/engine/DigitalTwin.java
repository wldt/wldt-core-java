package it.wldt.core.engine;


import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.adapter.digital.DigitalAdapterListener;
import it.wldt.adapter.physical.PhysicalAdapter;
import it.wldt.adapter.physical.PhysicalAdapterListener;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.core.event.DefaultWldtEventLogger;
import it.wldt.core.event.EventManager;
import it.wldt.core.event.WldtEventBus;
import it.wldt.core.model.DigitalTwinModel;
import it.wldt.core.model.ShadowingFunction;
import it.wldt.core.model.ShadowingModelListener;
import it.wldt.core.state.DigitalTwinState;
import it.wldt.core.state.DigitalTwinStateManager;
import it.wldt.exception.*;

import it.wldt.management.ManagementInterface;
import it.wldt.management.ResourceManager;
import it.wldt.storage.StorageManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/02/2023
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 * Digital Twin class of the library responsible for encapsulating the behaviour and the coordination of a Digital Twin
 * instance in the library. Each instance of a Digital Twin class can be executed on the Digital Twin Engine.
 */
public class DigitalTwin implements ShadowingModelListener, PhysicalAdapterListener, DigitalAdapterListener {

    private static final Logger logger = LoggerFactory.getLogger(DigitalTwin.class);

    private static final String EVENT_PUBLISHER_ID = "dt_core";

    // Internal TAG fo Logs
    private static final String TAG = "[WLDT-DigitalTwin]";

    /**
     * Number of Thread limit for DT Physical Adapters
     */
    private static final int PHYSICAL_ADAPTERS_THREAD_POOL_SIZE_LIMIT = 5;

    /**
     * Number of Thread limit for DT Digital Adapters
     */
    private static final int DIGITAL_ADAPTERS_THREAD_POOL_SIZE_LIMIT = 5;

    /**
     * Executor Service for Physical Adapters
     */
    private ExecutorService physicalAdapterExecutor = null;

    /**
     * Executor Service for Digital Adapters
     */
    private ExecutorService digitalAdapterExecutor = null;

    /**
     * Executor Service for Digital Adapters
     */
    private List<String> digitalizedPhysicalAssets;

    /**
     * List of Physical Adapters
     */
    private List<PhysicalAdapter> physicalAdapterList;

    /**
     * List of Digital Adapters
     */
    private List<DigitalAdapter<?>> digitalAdapterList;

    /**
     * Map of PhysicalAssetDescription associated to each Physical Adapter
     */
    private Map<String, PhysicalAssetDescription> physicalAdaptersPhysicalAssetDescriptionMap;

    /**
     * Data Structure to keep track of the binding status of Physical Adapters
     */
    private Map<String, Boolean> physicalAdaptersBoundStatusMap = null;

    /**
     * Data Structure to keep track of the binding status of Digital Adapters
     */
    private Map<String, Boolean> digitalAdaptersBoundStatusMap = null;

    /**
     * Digital Twin State Manager instance for managing the current DT's State
     */
    private DigitalTwinStateManager digitalTwinStateManager = null;

    /**
     * Instance of the Model Engine of the current Digital Twin
     */
    private DigitalTwinModel digitalTwinModel = null;

    /**
     * List of Life Cycle Listener for the current Digital Twin
     */
    private List<LifeCycleListener> lifeCycleListenerList = null;

    /**
     * Current Life Cycle State of the Digital Twin
     */
    private LifeCycleState currentLifeCycleState = LifeCycleState.NONE;

    /**
     * Thread for the Model Engine Execution
     */
    private Thread modelEngineThread = null;

    /**
     * Thread for the Storage Manager Execution
     */
    private Thread storageManagerThread = null;

    /**
     * Resource Manager for the Digital Twin
     * It is used to manage the resources associated with the Digital Twin.
     */
    private ResourceManager resourceManager = null;

    /**
     * Management Interface for the Digital Twin
     * It is used to manage the Digital Twin Resource and its components.
     */
    private ManagementInterface managementInterface = null;

    /**
     * Thread for the Management Interface Execution
     */
    private Thread managementInterfaceThread = null;

    /**
     * Reference to the Shadowing Function used by the Digital Twin and its Model Engine
     */
    private ShadowingFunction shadowingFunction = null;

    /**
     * Id of the target Digital Twin
     */
    private String digitalTwinId;

    /**
     * Object used to synchronize the state of the Digital Twin
     */
    private Object syncStateObject = new Object();

    /**
     * Storage Manager for the Digital Twin
     */
    private StorageManager storageManager = null;

    /**
     * Constructor for creating a DigitalTwin instance.
     *
     * @param digitalTwinId                 The unique identifier for the Digital Twin.
     * @param digitalizedPhysicalAssetsIdList List of IDs for digitalized physical assets.
     * @param shadowingFunction             The shadowing function for the Digital Twin.
     * @throws ModelException                If there is an issue with the model.
     * @throws EventBusException             If there is an issue with the event bus.
     * @throws WldtRuntimeException           If a runtime exception occurs during Digital Twin creation.
     * @throws WldtWorkerException            If there is an issue with the DigitalTwinWorker.
     * @throws WldtDigitalTwinStateException If there is an issue with the state of the Digital Twin.
     */
    public DigitalTwin(String digitalTwinId, List<String> digitalizedPhysicalAssetsIdList, ShadowingFunction shadowingFunction) throws ModelException, EventBusException, WldtRuntimeException, WldtWorkerException, WldtDigitalTwinStateException {

        if(digitalTwinId == null)
            throw new WldtRuntimeException("Error ! Digital Twin ID = Null !");

        //TODO Add a check to force passing a list of Physical Asset Id ? Check if digitalizedPhysicalAssetsIdList.isEmpty() ?
        if(digitalizedPhysicalAssetsIdList == null)
            digitalizedPhysicalAssetsIdList = new ArrayList<>();
            //throw new WldtRuntimeException("Error ! List of Physical Asset Ids = Null or EMPTY!");

        if(shadowingFunction == null)
            throw new WldtRuntimeException("Error ! Digital Twin Shadowing Function = Null !");

        this.digitalTwinId = digitalTwinId;

        this.digitalizedPhysicalAssets = digitalizedPhysicalAssetsIdList;

        this.physicalAdapterList = new ArrayList<>();

        this.digitalAdapterList = new ArrayList<>();

        init(shadowingFunction);
    }

    /**
     * Constructor for creating a DigitalTwin instance with an empty list of digitalized physical assets.
     *
     * @param digitalTwinId     The unique identifier for the Digital Twin.
     * @param shadowingFunction The shadowing function for the Digital Twin.
     * @throws ModelException                If there is an issue with the model.
     * @throws EventBusException             If there is an issue with the event bus.
     * @throws WldtRuntimeException           If a runtime exception occurs during Digital Twin creation.
     * @throws WldtWorkerException            If there is an issue with the DigitalTwinWorker.
     * @throws WldtDigitalTwinStateException If there is an issue with the state of the Digital Twin.
     */
    public DigitalTwin(String digitalTwinId, ShadowingFunction shadowingFunction) throws ModelException, EventBusException, WldtRuntimeException, WldtWorkerException, WldtDigitalTwinStateException {
        this(digitalTwinId, new ArrayList<>(), shadowingFunction);
    }

    /**
     * Initializes the Digital Twin with the provided shadowing function.
     *
     * @param shadowingFunction The shadowing function for the Digital Twin.
     * @throws ModelException           If there is an issue with the model.
     * @throws WldtRuntimeException      If a runtime exception occurs during Digital Twin initialization.
     * @throws WldtWorkerException       If there is an issue with the DigitalTwinWorker.
     * @throws WldtDigitalTwinStateException If there is an issue with the state of the Digital Twin.
     */
    private void init(ShadowingFunction shadowingFunction) throws ModelException, WldtRuntimeException, WldtWorkerException, WldtDigitalTwinStateException {

        if(shadowingFunction == null)
            throw new WldtRuntimeException("Error ! Shadowing Function = NULL !");

        //Init Life Cycle Listeners & Status Map
        this.lifeCycleListenerList = new ArrayList<>();
        this.physicalAdaptersBoundStatusMap = new HashMap<>();
        this.digitalAdaptersBoundStatusMap = new HashMap<>();

        //Initialize the Digital Twin State
        this.digitalTwinStateManager = new DigitalTwinStateManager(this.digitalTwinId);

        // Initialize the Storage Manager of the current Digital Twin instance
        this.storageManager = new StorageManager(this.digitalTwinId);

        // Initialize the Resource Manager of the current Digital Twin instance
        this.resourceManager = new ResourceManager(this.digitalTwinId);

        //Init DT Initial Life Cycle Phase
        this.currentLifeCycleState = LifeCycleState.NONE;

        //Setup EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Create a Map to hold the last PhysicalAssetDescription for each active adapter
        this.physicalAdaptersPhysicalAssetDescriptionMap = new HashMap<>();

        //Set ShadowingListener, Init Model Engine & Add to the List of Workers
        this.shadowingFunction = shadowingFunction;
        this.shadowingFunction.setShadowingModelListener(this);

        // Initialize the Digital Twin Model with digital twin ID, state manager, shadowing function, and storage manager
        this.digitalTwinModel = new DigitalTwinModel(this.digitalTwinId, this.digitalTwinStateManager, this.shadowingFunction, this.storageManager);

        //Save the Model Engine as Digital Twin Life Cycle Listener
        addLifeCycleListener(this.digitalTwinModel);

        // Execute Storage Manager
        executeStorageManager();
    }

    /**
     * Executes the model engine in a dedicated thread.
     */
    private void executeModelEngine(){
        modelEngineThread = new Thread(this.digitalTwinModel);
        modelEngineThread.setName(String.format("%s-model-engine", this.getId()));
        modelEngineThread.start();
    }

    /**
     * Executes the storage manager in a dedicated thread.
     * This method is responsible for starting the storage manager thread, which handles data storage operations for the digital twin.
     */
    private void executeStorageManager(){
        storageManagerThread = new Thread(this.storageManager);
        storageManagerThread.setName(String.format("%s-storage-manager", this.getId()));
        storageManagerThread.start();
    }

    /**
     * Executes the management interface in a dedicated thread.
     * This method is responsible for starting the management interface thread, which handles management operations for the digital twin.
     */
    private void executeManagementInterface() {

        if(this.managementInterface != null){

            // Set the Resource Manager for the Management Interface
            this.managementInterface.setResourceManager(this.resourceManager);

            // Run the Management Interface on a dedicated thread
            managementInterfaceThread = new Thread(this.managementInterface);
            managementInterfaceThread.setName(String.format("%s-management-interface", this.getId()));
            managementInterfaceThread.start();
        }
        else
            logger.warn("{} Management Interface is not initialized !", TAG);
    }

    /**
     * Adds a life cycle listener to the list.
     *
     * @param listener The life cycle listener to be added.
     */
    public void addLifeCycleListener(LifeCycleListener listener){
        if(listener != null && this.lifeCycleListenerList != null && !this.lifeCycleListenerList.contains(listener))
            this.lifeCycleListenerList.add(listener);
    }

    /**
     * Removes a life cycle listener from the list.
     *
     * @param listener The life cycle listener to be removed.
     */
    public void removeLifeCycleListener(LifeCycleListener listener){
        if(listener != null && this.lifeCycleListenerList != null)
            this.lifeCycleListenerList.remove(listener);
    }

    /**
     * Notifies listeners when the digital twin is created.
     */
    private void notifyLifeCycleOnCreate(){

        synchronized (this.syncStateObject){

            this.currentLifeCycleState = LifeCycleState.CREATED;

            // Notify the change in the DT Life Cycle
            EventManager.publishLifeCycleEvent(
                    digitalTwinId,
                    EVENT_PUBLISHER_ID,
                    this.currentLifeCycleState);
        }

        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onCreate();
    }

    /**
     * Notifies listeners when the digital twin is started.
     */
    private void notifyLifeCycleOnStart(){

        synchronized (this.syncStateObject){

            this.currentLifeCycleState = LifeCycleState.STARTED;

            // Notify the change in the DT Life Cycle
            EventManager.publishLifeCycleEvent(
                    digitalTwinId,
                    EVENT_PUBLISHER_ID,
                    this.currentLifeCycleState);
        }

        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onStart();
    }

    /**
     * Notifies listeners when the digital twin is bound.
     */
    private void notifyLifeCycleOnBound(){
        synchronized (this.syncStateObject){

            this.currentLifeCycleState = LifeCycleState.BOUND;

            // Notify the change in the DT Life Cycle
            EventManager.publishLifeCycleEvent(
                    digitalTwinId,
                    EVENT_PUBLISHER_ID,
                    this.currentLifeCycleState);
        }

        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onDigitalTwinBound(this.physicalAdaptersPhysicalAssetDescriptionMap);
    }

    /**
     * Notifies listeners when the digital twin is unbound.
     *
     * @param errorMessage The error message associated with the unbound event.
     */
    private void notifyLifeCycleOnUnBound(String errorMessage){
        synchronized (this.syncStateObject){
            this.currentLifeCycleState = LifeCycleState.UN_BOUND;

            // Notify the change in the DT Life Cycle
            EventManager.publishLifeCycleEvent(
                    digitalTwinId,
                    EVENT_PUBLISHER_ID,
                    this.currentLifeCycleState);
        }

        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onDigitalTwinUnBound(this.physicalAdaptersPhysicalAssetDescriptionMap, errorMessage);
    }

    /**
     * Notifies listeners when a physical adapter is bound.
     *
     * @param adapterId               The ID of the bound physical adapter.
     * @param physicalAssetDescription The description of the associated physical asset.
     */
    private void notifyLifeCycleOnPhysicalAdapterBound(String adapterId, PhysicalAssetDescription physicalAssetDescription){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onPhysicalAdapterBound(adapterId, physicalAssetDescription);
    }

    /**
     * Notifies listeners when a physical adapter's binding is updated.
     *
     * @param adapterId               The ID of the updated physical adapter.
     * @param physicalAssetDescription The updated description of the associated physical asset.
     */
    private void notifyLifeCycleOnPhysicalAdapterBindingUpdate(String adapterId, PhysicalAssetDescription physicalAssetDescription){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onPhysicalAdapterBindingUpdate(adapterId, physicalAssetDescription);
    }

    /**
     * Notifies listeners when a physical adapter is unbound.
     *
     * @param adapterId               The ID of the unbound physical adapter.
     * @param physicalAssetDescription The description of the associated physical asset.
     * @param errorMessage            The error message associated with the unbound event.
     */
    private void notifyLifeCycleOnPhysicalAdapterUnBound(String adapterId, PhysicalAssetDescription physicalAssetDescription, String errorMessage){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onPhysicalAdapterUnBound(adapterId, physicalAssetDescription, errorMessage);
    }

    /**
     * Notifies listeners when a digital adapter is bound.
     *
     * @param adapterId The ID of the bound digital adapter.
     */
    private void notifyLifeCycleOnDigitalAdapterBound(String adapterId){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onDigitalAdapterBound(adapterId);
    }


    /**
     * Notifies listeners when a digital adapter is unbound.
     *
     * @param adapterId    The ID of the unbound digital adapter.
     * @param errorMessage The error message associated with the unbound event.
     */
    private void notifyLifeCycleOnDigitalAdapterUnBound(String adapterId, String errorMessage){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onDigitalAdapterUnBound(adapterId, errorMessage);
    }

    /**
     * Notifies listeners when the digital twin is synchronized.
     *
     * @param digitalTwinState The state of the digital twin after synchronization.
     */
    private void notifyLifeCycleOnSync(DigitalTwinState digitalTwinState){
        synchronized (this.syncStateObject){
            this.currentLifeCycleState = LifeCycleState.SYNCHRONIZED;

            // Notify the change in the DT Life Cycle
            EventManager.publishLifeCycleEvent(
                    digitalTwinId,
                    EVENT_PUBLISHER_ID,
                    this.currentLifeCycleState);
        }

        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onSync(digitalTwinState);
    }

    /**
     * Notifies listeners when the digital twin is out of synchronization.
     *
     * @param digitalTwinState The state of the digital twin after going out of synchronization.
     */
    private void notifyLifeCycleOnUnSync(DigitalTwinState digitalTwinState){
        synchronized (this.syncStateObject){
            this.currentLifeCycleState = LifeCycleState.NOT_SYNCHRONIZED;

            // Notify the change in the DT Life Cycle
            EventManager.publishLifeCycleEvent(
                    digitalTwinId,
                    EVENT_PUBLISHER_ID,
                    this.currentLifeCycleState);
        }

        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onUnSync(digitalTwinState);
    }

    /**
     * Notifies listeners when the digital twin is stopped.
     */
    private void notifyLifeCycleOnStop(){
        synchronized (this.syncStateObject){
            this.currentLifeCycleState = LifeCycleState.STOPPED;

            // Notify the change in the DT Life Cycle
            EventManager.publishLifeCycleEvent(
                    digitalTwinId,
                    EVENT_PUBLISHER_ID,
                    this.currentLifeCycleState);
        }

        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onStop();
    }

    /**
     * Notifies listeners when the digital twin is destroyed.
     */
    private void notifyLifeCycleOnDestroy(){
        synchronized (this.syncStateObject){
            this.currentLifeCycleState = LifeCycleState.DESTROYED;

            // Notify the change in the DT Life Cycle
            EventManager.publishLifeCycleEvent(
                    digitalTwinId,
                    EVENT_PUBLISHER_ID,
                    this.currentLifeCycleState);
        }

        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onDestroy();
    }

    /**
     * Add a new Physical Adapter to the Digital Twin in order to be executed through a dedicated Thread.
     * The method validates the request checking if the adapter is already in the list and if there is enough
     * thread to handle it within the thread pool
     *
     * @param physicalAdapter
     * @throws WldtConfigurationException
     */
    public void addPhysicalAdapter(PhysicalAdapter physicalAdapter) throws WldtConfigurationException, WldtWorkerException {

        if(physicalAdapter != null
                && this.getPhysicalAdapterList() != null
                && !this.getPhysicalAdapterList().contains(physicalAdapter)
                && this.getPhysicalAdapterList().size() < PHYSICAL_ADAPTERS_THREAD_POOL_SIZE_LIMIT) {

            physicalAdapter.setDigitalTwinId(this.digitalTwinId);
            physicalAdapter.setPhysicalAdapterListener(this);
            this.getPhysicalAdapterList().add(physicalAdapter);

            //Save BoundStatus to False. It will be changed through a call back by the adapter
            this.physicalAdaptersBoundStatusMap.put(physicalAdapter.getId(), false);

            logger.debug("{} New PhysicalAdapter ({}) Added to the Worker List ! Physical Adapters - Worker List Size: {}", TAG, physicalAdapter.getClass().getName(), this.getPhysicalAdapterList().size());
        }
        else
            throw new WldtConfigurationException("Invalid PhysicalAdapter, Already added or List Limit Reached !");
    }

    /**
     * Clear the list of configured Physical Adapters
     *
     * @throws WldtConfigurationException
     */
    public void clearPhysicalAdapterList() throws WldtConfigurationException{
        if(this.getPhysicalAdapterList() != null){
            this.getPhysicalAdapterList().clear();
        }
        else
            throw new WldtConfigurationException("Error Cleaning Physical Adapters ! List is Null !");
    }

    /**
     * Add a new Digital Adapter to the Digital Twin in order to be executed through a dedicated Thread.
     * The method validates the request checking if the adapter is already in the list and if there is enough
     * thread to handle it within the thread pool
     *
     * @param digitalAdapter
     * @throws WldtConfigurationException
     */
    public void addDigitalAdapter(DigitalAdapter<?> digitalAdapter) throws WldtConfigurationException, WldtWorkerException {

        if(digitalAdapter != null
                && this.getDigitalAdapterList() != null
                && !this.getDigitalAdapterList().contains(digitalAdapter)
                && this.getDigitalAdapterList().size() < DIGITAL_ADAPTERS_THREAD_POOL_SIZE_LIMIT) {

            digitalAdapter.setDigitalTwinId(this.digitalTwinId);
            digitalAdapter.setDigitalAdapterListener(this);
            this.getDigitalAdapterList().add(digitalAdapter);

            //Save BoundStatus to False. It will be changed through a call back by the adapter
            this.digitalAdaptersBoundStatusMap.put(digitalAdapter.getId(), false);

            //Save the Model Engine as Digital Twin Life Cycle Listener
            addLifeCycleListener(digitalAdapter);

            logger.debug("{} New DigitalAdapter ({}) Added to the Worker List ! Digital Adapters - Worker List Size: {}", TAG, digitalAdapter.getClass().getName(), this.getDigitalAdapterList().size());
        }
        else
            throw new WldtConfigurationException("Invalid PhysicalAdapter, Already added or List Limit Reached !");
    }

    /**
     * Clear the list of configured Digital Adapters
     *
     * @throws WldtConfigurationException
     */
    public void clearDigitalAdapterList() throws WldtConfigurationException{
        if(this.getDigitalAdapterList() != null){

            for(DigitalAdapter<?> digitalAdapter : this.getDigitalAdapterList())
                removeLifeCycleListener(digitalAdapter);

            this.getDigitalAdapterList().clear();
        }
        else
            throw new WldtConfigurationException("Error Cleaning Physical Adapters ! List is Null !");
    }

    /**
     * Starts the life cycle of the digital twin. This method initiates the execution of the model engine and physical/digital adapters.
     * It ensures that both physical and digital adapters are available before starting the life cycle.
     * This method is used by the Digital Twin Engine to coordinate the execution of its configured DTs.
     * @throws WldtConfigurationException If there is an issue with the configuration of physical or digital adapters.
     */
    protected void startLifeCycle() throws WldtConfigurationException {

        // Start Executing as first component the Model Engine
        executeModelEngine();

        // Execute Management Interface if it is set
        executeManagementInterface();

        //In order to start its LifeCycle the Digital Twin need at least one Physical and one Digital Adapter in order
        //to properly bridge the physical and the digital world
        //TODO Check -> Does it make sense to force to have at least one Digital Adapter in order to start the Life Cycle ?
        if(this.getPhysicalAdapterList() == null || this.getPhysicalAdapterList().isEmpty() || this.getDigitalAdapterList() == null || this.getDigitalAdapterList().isEmpty())
            throw new WldtConfigurationException("Empty PhysicalAdapter o DigitalAdapter List !");

        notifyLifeCycleOnCreate();

        //Init PhysicalAdapter Executor
        physicalAdapterExecutor = Executors.newFixedThreadPool(this.getPhysicalAdapterList().size());

        this.getPhysicalAdapterList().forEach(physicalAdapter -> {
            logger.info("Executing PhysicalAdapter: {}", physicalAdapter.getClass());
            physicalAdapterExecutor.execute(physicalAdapter);
        });

        //Init DigitalAdapter Executor
        digitalAdapterExecutor = Executors.newFixedThreadPool(this.getDigitalAdapterList().size());

        this.getDigitalAdapterList().forEach(digitalAdapter -> {
            logger.info("Executing DigitalAdapter: {}", digitalAdapter.getClass());
            digitalAdapterExecutor.execute(digitalAdapter);
        });

        //When all Physical and Digital Adapters have been started the DT moves to the Start State
        notifyLifeCycleOnStart();

        physicalAdapterExecutor.shutdown();

        while (!physicalAdapterExecutor.isTerminated() && ! digitalAdapterExecutor.isTerminated()) {}

    }

    /**
     * Stops the life cycle of the digital twin. This method interrupts the model engine thread and shuts down the physical and digital
     * adapters. It notifies registered life cycle listeners about the stop and destroy events.
     * This method is used by the Digital Twin Engine to coordinate the execution of its configured DTs.
     */
    protected void stopLifeCycle(){
        try{

            //Stop and Notify Model Engine
            this.modelEngineThread.interrupt();
            this.modelEngineThread = null;
            this.digitalTwinModel.onWorkerStop();
            removeLifeCycleListener(this.digitalTwinModel);

            //Stop and Notify Physical Adapters
            this.physicalAdapterExecutor.shutdownNow();
            this.physicalAdapterExecutor = null;
            for(PhysicalAdapter physicalAdapter : this.getPhysicalAdapterList())
                physicalAdapter.onWorkerStop();

            //Stop and Notify Digital Adapters
            this.digitalAdapterExecutor.shutdownNow();
            this.digitalAdapterExecutor = null;
            for(DigitalAdapter<?> digitalAdapter : this.getDigitalAdapterList())
                digitalAdapter.onWorkerStop();

            notifyLifeCycleOnStop();
            notifyLifeCycleOnDestroy();

            // Wait to receive the latest events for the Storage Manager
            Thread.sleep(2000);

            // Stop Storage Manager
            this.storageManagerThread.interrupt();
            this.storageManagerThread = null;
            this.storageManager.onWorkerStop();

            // Stop Management Interface
            if(this.managementInterface != null) {
                this.managementInterfaceThread.interrupt();
                this.managementInterfaceThread = null;
                this.managementInterface.onWorkerStop();
            }

        } catch (Exception e){
            logger.error("ERROR Stopping DT LifeCycle ! Error: {}", e.getLocalizedMessage());
        }
    }

    /**
     * Gets the identifier of the digital twin.
     *
     * @return The identifier of the digital twin.
     */
    public String getId() {
        return this.getDigitalTwinId();
    }

    /**
     * Check if all the registered Physical Adapters are correctly bound
     * @return
     */
    private boolean isDtBound(){
        return this.physicalAdaptersBoundStatusMap.values().stream().allMatch(b -> b);
//        for(boolean status : this.physicalAdaptersBoundStatusMap.values())
//            if(!status)
//                return false;
//        return true;
    }

    /**
     * Callback method invoked when the digital twin is synchronized.
     *
     * @param digitalTwinState The state of the digital twin after synchronization.
     */
    @Override
    public void onShadowingSync(DigitalTwinState digitalTwinState) {
        notifyLifeCycleOnSync(digitalTwinState);
    }

    /**
     * Callback method invoked when the digital twin is out of sync.
     *
     * @param digitalTwinState The state of the digital twin after going out of sync.
     */
    @Override
    public void onShadowingOutOfSync(DigitalTwinState digitalTwinState) {
        notifyLifeCycleOnUnSync(digitalTwinState);
    }

    /**
     * Callback method invoked when a physical adapter is bound to the digital twin.
     *
     * @param adapterId                The identifier of the physical adapter.
     * @param physicalAssetDescription The description of the physical asset associated with the adapter.
     */
    @Override
    public void onPhysicalAdapterBound(String adapterId, PhysicalAssetDescription physicalAssetDescription) {

        logger.info("PhysicalAdapter {} BOUND ! PhysicalAssetDescription: {}", adapterId, physicalAssetDescription);

        //Store the information that the adapter is correctly bound
        this.physicalAdaptersBoundStatusMap.put(adapterId, true);

        //Save the last Physical Asset Description from the adapter
        this.physicalAdaptersPhysicalAssetDescriptionMap.put(adapterId, physicalAssetDescription);

        notifyLifeCycleOnPhysicalAdapterBound(adapterId, physicalAssetDescription);

        if(isDtBound()) {
            logger.info("Digital Twin BOUND !");
            notifyLifeCycleOnBound();
        }
    }

    /**
     * Callback method invoked when the binding of a physical adapter is updated.
     *
     * @param adapterId                The identifier of the physical adapter.
     * @param physicalAssetDescription The updated description of the physical asset associated with the adapter.
     */
    @Override
    public void onPhysicalBindingUpdate(String adapterId, PhysicalAssetDescription physicalAssetDescription) {

        logger.info("PhysicalAdapter {} Binding Update ! New PA-Descriptor: {}", adapterId, physicalAssetDescription);

        //Update the last Physical Asset Description from the adapter
        this.physicalAdaptersPhysicalAssetDescriptionMap.put(adapterId, physicalAssetDescription);

        //Notify Biding Change
        notifyLifeCycleOnPhysicalAdapterBindingUpdate(adapterId, physicalAssetDescription);
    }

    /**
     * Callback method invoked when a physical adapter is unbound from the digital twin.
     *
     * @param adapterId                The identifier of the physical adapter.
     * @param physicalAssetDescription The last known description of the physical asset associated with the adapter.
     * @param errorMessage             An error message if unbinding fails.
     */
    @Override
    public void onPhysicalAdapterUnBound(String adapterId, PhysicalAssetDescription physicalAssetDescription, String errorMessage) {

        //Set the current adapter to unbound
        logger.info("PhysicalAdapter {} UN-BOUND ! Error: {}", adapterId, errorMessage);

        //Store the information that the adapter is UnBound
        this.physicalAdaptersBoundStatusMap.put(adapterId, false);

        //Retrieve the last physical asset description of the associated physical asset
        PhysicalAssetDescription currentPhysicalAssetDescription = this.physicalAdaptersPhysicalAssetDescriptionMap.get(adapterId);

        //Notify the adapter unbound status
        notifyLifeCycleOnPhysicalAdapterUnBound(adapterId, currentPhysicalAssetDescription, errorMessage);

        //Check if the DT is still bound
        if(!isDtBound()) {
            logger.info("Digital Twin UN-BOUND !");
            notifyLifeCycleOnUnBound(String.format("Adapter %s UnBound - Error ?: %b", adapterId, errorMessage));
        }
    }

    /**
     * Callback method invoked when a digital adapter is bound to the digital twin.
     *
     * @param adapterId The identifier of the digital adapter.
     */
    @Override
    public void onDigitalAdapterBound(String adapterId) {

        logger.info("DigitalAdapter {} BOUND !", adapterId);

        //Store the information that the adapter is correctly bound
        this.digitalAdaptersBoundStatusMap.put(adapterId, true);

        //Notify the adapter bound status
        notifyLifeCycleOnDigitalAdapterBound(adapterId);
    }

    /**
     * Callback method invoked when a digital adapter is unbound from the digital twin.
     *
     * @param adapterId    The identifier of the digital adapter.
     * @param errorMessage An error message if unbinding fails.
     */
    @Override
    public void onDigitalAdapterUnBound(String adapterId, String errorMessage) {

        //Set the current adapter to unbound
        logger.info("DigitalAdapter {} UN-BOUND ! Error: {}", adapterId, errorMessage);

        //Store the information that the adapter is UnBound
        this.digitalAdaptersBoundStatusMap.put(adapterId, false);

        //Notify the adapter unbound status
        notifyLifeCycleOnDigitalAdapterUnBound(adapterId, errorMessage);
    }

    /**
     * Gets the current life cycle state of the digital twin.
     *
     * @return The current life cycle state.
     */
    public LifeCycleState getCurrentLifeCycleState() {
        return this.currentLifeCycleState;
    }

    /**
     * Gets the identifier of the digital twin.
     *
     * @return The identifier of the digital twin.
     */
    public String getDigitalTwinId() {
        return digitalTwinId;
    }

    /**
     * Gets a list of digitalized physical assets associated with the digital twin.
     *
     * @return An unmodifiable list of digitalized physical assets.
     */
    public List<String> getDigitalizedPhysicalAssets() {
        return Collections.unmodifiableList(this.digitalizedPhysicalAssets);
    }

    /**
     * Gets a list of identifiers of registered physical adapters.
     *
     * @return An unmodifiable list of physical adapter identifiers.
     */
    public List<String> getPhysicalAdapterIds(){
        return Collections.unmodifiableList(this.physicalAdapterList.stream().map(PhysicalAdapter::getId).collect(Collectors.toList()));
    }

    /**
     * Gets a list of identifiers of registered digital adapters.
     *
     * @return An unmodifiable list of digital adapter identifiers.
     */
    public List<String> getDigitalAdapterIds(){
        return Collections.unmodifiableList(this.digitalAdapterList.stream().map(DigitalAdapter::getId).collect(Collectors.toList()));
    }

    /**
     * Gets a list of registered physical adapters.
     *
     * @return An unmodifiable list of physical adapters.
     */
    protected List<PhysicalAdapter> getPhysicalAdapterList() {
        return physicalAdapterList;
    }


    /**
     * Gets a list of registered digital adapters.
     *
     * @return An unmodifiable list of digital adapters.
     */
    protected List<DigitalAdapter<?>> getDigitalAdapterList() {
        return digitalAdapterList;
    }

    /**
     * Returns the Storage Manager of the current Digital Twin
     * @return
     */
    public StorageManager getStorageManager() {
        return storageManager;
    }

    /**
     * Returns the management interface of the Digital Twin.
     * @return ManagementInterface instance associated with the Digital Twin.
     */
    public ManagementInterface getManagementInterface() {
        return managementInterface;
    }

    /**
     * Sets the management interface for the Digital Twin.
     * @param managementInterface The ManagementInterface instance to be set.
     */
    public void setManagementInterface(ManagementInterface managementInterface) {
        try{
            // Set the management interface for the Digital Twin
            this.managementInterface = managementInterface;
            // Add the Digital Twin Id to the Management Interface in order to properly start it as DigitalTwin Worker
            this.managementInterface.setDigitalTwinId(this.digitalTwinId);
        }catch (Exception e){
            logger.error("Error setting Management Interface: {}", e.getLocalizedMessage());
        }
    }

    /**
     * Returns the Resource Manager of the current Digital Twin.
     * @return ResourceManager instance associated with the Digital Twin.
     */
    public ResourceManager getResourceManager() {
        return resourceManager;
    }
}
