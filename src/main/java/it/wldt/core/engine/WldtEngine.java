package it.wldt.core.engine;


import it.wldt.adapter.digital.DigitalAdapter;
import it.wldt.adapter.digital.DigitalAdapterListener;
import it.wldt.adapter.physical.PhysicalAdapter;
import it.wldt.adapter.physical.PhysicalAdapterListener;
import it.wldt.adapter.physical.PhysicalAssetDescription;
import it.wldt.core.event.DefaultWldtEventLogger;
import it.wldt.core.event.WldtEventBus;
import it.wldt.core.model.ModelEngine;
import it.wldt.core.model.ShadowingModelFunction;
import it.wldt.core.model.ShadowingModelListener;
import it.wldt.core.state.DefaultDigitalTwinState;
import it.wldt.core.state.IDigitalTwinState;
import it.wldt.exception.EventBusException;
import it.wldt.exception.ModelException;
import it.wldt.exception.WldtConfigurationException;
import it.wldt.exception.WldtRuntimeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Author: Marco Picone, Ph.D. (marco.picone@unimore.it)
 * Date: 24/03/2020
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class WldtEngine implements ShadowingModelListener, PhysicalAdapterListener, DigitalAdapterListener {

    private static final Logger logger = LoggerFactory.getLogger(WldtEngine.class);

    private static final int PHYSICAL_ADAPTERS_THREAD_POOL_SIZE_LIMIT = 5;

    private static final int DIGITAL_ADAPTERS_THREAD_POOL_SIZE_LIMIT = 5;

    private static final String TAG = "[WLDT-Engine]";

    private ExecutorService physicalAdapterExecutor = null;

    private ExecutorService digitalAdapterExecutor = null;

    private static WldtInstance wldtInstance;

    private Map<String, PhysicalAssetDescription> physicalAdaptersPhysicalAssetDescriptionMap;

    /**
     * Data Structure to keep track of the binding status of Physical Adapters
     */
    private Map<String, Boolean> physicalAdaptersBoundStatusMap = null;

    /**
     * Data Structure to keep track of the binding status of Digital Adapters
     */
    private Map<String, Boolean> digitalAdaptersBoundStatusMap = null;

    private IDigitalTwinState digitalTwinState = null;

    private ModelEngine modelEngine = null;

    private List<LifeCycleListener> lifeCycleListenerList = null;

    private Thread modelEngineThread = null;

    private ShadowingModelFunction shadowingModelFunction = null;

    public WldtEngine(ShadowingModelFunction shadowingModelFunction, String digitalTwinId, List<String> digitalizedPhysicalAssets) throws ModelException, EventBusException, WldtRuntimeException {
        wldtInstance = new WldtInstance(digitalTwinId, digitalizedPhysicalAssets);
        init(shadowingModelFunction);
    }

    public WldtEngine(ShadowingModelFunction shadowingModelFunction, String digitalTwinId) throws ModelException, EventBusException, WldtRuntimeException {
        this(shadowingModelFunction, digitalTwinId, new ArrayList<>());
    }

    private void init(ShadowingModelFunction shadowingModelFunction) throws ModelException, EventBusException, WldtRuntimeException {

        if(shadowingModelFunction == null)
            throw new WldtRuntimeException("Error ! Shadowing Function = NULL !");

        //Init Life Cycle Listeners & Status Map
        this.lifeCycleListenerList = new ArrayList<>();
        this.physicalAdaptersBoundStatusMap = new HashMap<>();
        this.digitalAdaptersBoundStatusMap = new HashMap<>();

        //Initialize the Digital Twin State
        this.digitalTwinState = new DefaultDigitalTwinState();

        //Setup EventBus Logger
        WldtEventBus.getInstance().setEventLogger(new DefaultWldtEventLogger());

        //Create a Map to hold the last PhysicalAssetDescription for each active adapter
        this.physicalAdaptersPhysicalAssetDescriptionMap = new HashMap<>();

        //Set ShadowingListener, Init Model Engine & Add to the List of Workers
        this.shadowingModelFunction = shadowingModelFunction;
        this.shadowingModelFunction.setShadowingModelListener(this);
        this.modelEngine = new ModelEngine(this.digitalTwinState, this.shadowingModelFunction);

        //Save the Model Engine as Digital Twin Life Cycle Listener
        addLifeCycleListener(this.modelEngine);

        executeModelEngine();
    }

    private void executeModelEngine(){
        modelEngineThread = new Thread(this.modelEngine);
        modelEngineThread.start();
    }

    public void addLifeCycleListener(LifeCycleListener listener){
        if(listener != null && this.lifeCycleListenerList != null && !this.lifeCycleListenerList.contains(listener))
            this.lifeCycleListenerList.add(listener);
    }

    public void removeLifeCycleListener(LifeCycleListener listener){
        if(listener != null && this.lifeCycleListenerList != null)
            this.lifeCycleListenerList.remove(listener);
    }

    private void notifyLifeCycleOnCreate(){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onCreate();
    }

    private void notifyLifeCycleOnStart(){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onStart();
    }

    private void notifyLifeCycleOnBound(){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onDigitalTwinBound(this.physicalAdaptersPhysicalAssetDescriptionMap);
    }

    private void notifyLifeCycleOnUnBound(String errorMessage){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onDigitalTwinUnBound(this.physicalAdaptersPhysicalAssetDescriptionMap, errorMessage);
    }

    private void notifyLifeCycleOnPhysicalAdapterBound(String adapterId, PhysicalAssetDescription physicalAssetDescription){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onPhysicalAdapterBound(adapterId, physicalAssetDescription);
    }

    private void notifyLifeCycleOnPhysicalAdapterBindingUpdate(String adapterId, PhysicalAssetDescription physicalAssetDescription){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onPhysicalAdapterBindingUpdate(adapterId, physicalAssetDescription);
    }

    private void notifyLifeCycleOnPhysicalAdapterUnBound(String adapterId, PhysicalAssetDescription physicalAssetDescription, String errorMessage){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onPhysicalAdapterUnBound(adapterId, physicalAssetDescription, errorMessage);
    }

    private void notifyLifeCycleOnDigitalAdapterBound(String adapterId){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onDigitalAdapterBound(adapterId);
    }

    private void notifyLifeCycleOnDigitalAdapterUnBound(String adapterId, String errorMessage){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onDigitalAdapterUnBound(adapterId, errorMessage);
    }

    private void notifyLifeCycleOnSync(IDigitalTwinState digitalTwinState){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onSync(digitalTwinState);
    }

    private void notifyLifeCycleOnUnSync(IDigitalTwinState digitalTwinState){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onUnSync(digitalTwinState);
    }

    private void notifyLifeCycleOnStop(){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onStop();
    }

    private void notifyLifeCycleOnDestroy(){
        for(LifeCycleListener listener : this.lifeCycleListenerList)
            listener.onDestroy();
    }

    /**
     * Add a new Physical Adapter to the WLDT Engine in order to be executed through a dedicated Thread.
     * The method validates the request checking if the adapter is already in the list and if there is enough
     * thread to handle it within the thread pool
     *
     * @param physicalAdapter
     * @throws WldtConfigurationException
     */
    public void addPhysicalAdapter(PhysicalAdapter physicalAdapter) throws WldtConfigurationException {

        if(physicalAdapter != null
                && wldtInstance.getPhysicalAdapterList() != null
                && !wldtInstance.getPhysicalAdapterList().contains(physicalAdapter)
                && wldtInstance.getPhysicalAdapterList().size() < PHYSICAL_ADAPTERS_THREAD_POOL_SIZE_LIMIT) {

            physicalAdapter.setPhysicalAdapterListener(this);
            wldtInstance.getPhysicalAdapterList().add(physicalAdapter);

            //Save BoundStatus to False. It will be changed through a call back by the adapter
            this.physicalAdaptersBoundStatusMap.put(physicalAdapter.getId(), false);

            logger.debug("{} New PhysicalAdapter ({}) Added to the Worker List ! Physical Adapters - Worker List Size: {}", TAG, physicalAdapter.getClass().getName(), wldtInstance.getPhysicalAdapterList().size());
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
        if(wldtInstance.getPhysicalAdapterList() != null){
            wldtInstance.getPhysicalAdapterList().clear();
        }
        else
            throw new WldtConfigurationException("Error Cleaning Physical Adapters ! List is Null !");
    }

    /**
     * Add a new Digital Adapter to the WLDT Engine in order to be executed through a dedicated Thread.
     * The method validates the request checking if the adapter is already in the list and if there is enough
     * thread to handle it within the thread pool
     *
     * @param digitalAdapter
     * @throws WldtConfigurationException
     */
    public void addDigitalAdapter(DigitalAdapter<?> digitalAdapter) throws WldtConfigurationException {

        if(digitalAdapter != null
                && wldtInstance.getDigitalAdapterList() != null
                && !wldtInstance.getDigitalAdapterList().contains(digitalAdapter)
                && wldtInstance.getDigitalAdapterList().size() < DIGITAL_ADAPTERS_THREAD_POOL_SIZE_LIMIT) {

            digitalAdapter.setDigitalAdapterListener(this);
            wldtInstance.getDigitalAdapterList().add(digitalAdapter);

            //Save BoundStatus to False. It will be changed through a call back by the adapter
            this.digitalAdaptersBoundStatusMap.put(digitalAdapter.getId(), false);

            //Save the Model Engine as Digital Twin Life Cycle Listener
            addLifeCycleListener(digitalAdapter);

            logger.debug("{} New DigitalAdapter ({}) Added to the Worker List ! Digital Adapters - Worker List Size: {}", TAG, digitalAdapter.getClass().getName(), wldtInstance.getDigitalAdapterList().size());
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
        if(wldtInstance.getDigitalAdapterList() != null){

            for(DigitalAdapter<?> digitalAdapter : wldtInstance.getDigitalAdapterList())
                removeLifeCycleListener(digitalAdapter);

            wldtInstance.getDigitalAdapterList().clear();
        }
        else
            throw new WldtConfigurationException("Error Cleaning Physical Adapters ! List is Null !");
    }

    public void startLifeCycle() throws WldtConfigurationException {

        //In order to start its LifeCycle the Digital Twin need at least one Physical and one Digital Adapter in order
        //to properly bridge the physical and the digital world
        //TODO Check -> Does it make sense to force to have at least one Digital Adapter in order to start the Life Cycle ?
        if(wldtInstance.getPhysicalAdapterList() == null || wldtInstance.getPhysicalAdapterList().isEmpty() || wldtInstance.getDigitalAdapterList() == null || wldtInstance.getDigitalAdapterList().isEmpty())
            throw new WldtConfigurationException("Empty PhysicalAdapter o DigitalAdapter List !");

        notifyLifeCycleOnCreate();

        //Init PhysicalAdapter Executor
        physicalAdapterExecutor = Executors.newFixedThreadPool(wldtInstance.getPhysicalAdapterList().size());

        wldtInstance.getPhysicalAdapterList().forEach(physicalAdapter -> {
            logger.info("Executing PhysicalAdapter: {}", physicalAdapter.getClass());
            physicalAdapterExecutor.execute(physicalAdapter);
        });

        //Init DigitalAdapter Executor
        digitalAdapterExecutor = Executors.newFixedThreadPool(wldtInstance.getDigitalAdapterList().size());

        wldtInstance.getDigitalAdapterList().forEach(digitalAdapter -> {
            logger.info("Executing DigitalAdapter: {}", digitalAdapter.getClass());
            digitalAdapterExecutor.execute(digitalAdapter);
        });

        //When all Physical and Digital Adapters have been started the DT moves to the Start State
        notifyLifeCycleOnStart();

        physicalAdapterExecutor.shutdown();

        while (!physicalAdapterExecutor.isTerminated() && ! digitalAdapterExecutor.isTerminated()) {}

    }

    public void stopLifeCycle(){
        try{

            //Stop and Notify Model Engine
            this.modelEngineThread.interrupt();
            this.modelEngineThread = null;
            this.modelEngine.onWorkerStop();
            removeLifeCycleListener(this.modelEngine);

            //Stop and Notify Physical Adapters
            this.physicalAdapterExecutor.shutdownNow();
            this.physicalAdapterExecutor = null;
            for(PhysicalAdapter physicalAdapter : wldtInstance.getPhysicalAdapterList())
                physicalAdapter.onWorkerStop();

            //Stop and Notify Digital Adapters
            this.digitalAdapterExecutor.shutdownNow();
            this.digitalAdapterExecutor = null;
            for(DigitalAdapter<?> digitalAdapter : wldtInstance.getDigitalAdapterList())
                digitalAdapter.onWorkerStop();

            notifyLifeCycleOnStop();
            notifyLifeCycleOnDestroy();

        } catch (Exception e){
            logger.error("ERROR Stopping DT LifeCycle ! Error: {}", e.getLocalizedMessage());
        }

    }

    public String getWldtId() {
        return wldtInstance.getWldtId();
    }

    public ModelEngine getModelEngine() {
        return modelEngine;
    }

    public static WldtInstance getWldtInstance(){
        return wldtInstance;
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

    @Override
    public void onShadowingSync(IDigitalTwinState digitalTwinState) {
        notifyLifeCycleOnSync(digitalTwinState);
    }

    @Override
    public void onShadowingOutOfSync(IDigitalTwinState digitalTwinState) {
        notifyLifeCycleOnUnSync(digitalTwinState);
    }

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

    @Override
    public void onPhysicalBindingUpdate(String adapterId, PhysicalAssetDescription physicalAssetDescription) {

        logger.info("PhysicalAdapter {} Binding Update ! New PA-Descriptor: {}", adapterId, physicalAssetDescription);

        //Update the last Physical Asset Description from the adapter
        this.physicalAdaptersPhysicalAssetDescriptionMap.put(adapterId, physicalAssetDescription);

        //Notify Biding Change
        notifyLifeCycleOnPhysicalAdapterBindingUpdate(adapterId, physicalAssetDescription);
    }

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

    @Override
    public void onDigitalAdapterBound(String adapterId) {

        logger.info("DigitalAdapter {} BOUND !", adapterId);

        //Store the information that the adapter is correctly bound
        this.digitalAdaptersBoundStatusMap.put(adapterId, true);

        //Notify the adapter bound status
        notifyLifeCycleOnDigitalAdapterBound(adapterId);
    }

    @Override
    public void onDigitalAdapterUnBound(String adapterId, String errorMessage) {

        //Set the current adapter to unbound
        logger.info("DigitalAdapter {} UN-BOUND ! Error: {}", adapterId, errorMessage);

        //Store the information that the adapter is UnBound
        this.digitalAdaptersBoundStatusMap.put(adapterId, false);

        //Notify the adapter unbound status
        notifyLifeCycleOnDigitalAdapterUnBound(adapterId, errorMessage);
    }
}
