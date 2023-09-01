package it.wldt.processing.cache;

import it.wldt.processing.ProcessingStep;

import java.util.HashMap;
import java.util.Map;

/**
 * Author:
 *      Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/03/2023
 *
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public class PipelineCache {

    private Map<String, Map<String, Object>> cacheMap;

    public PipelineCache() {
        this.cacheMap = new HashMap<>();
    }

    public void addData(ProcessingStep processingStep, String key, Object value){
        if(!cacheMap.containsKey(processingStep.getClass().getName()))
            cacheMap.put(processingStep.getClass().getName(), new HashMap<>());

        cacheMap.get(processingStep.getClass().getName()).put(key, value);
    }

    public Object getData(ProcessingStep processingStep, String key){
        if(!cacheMap.containsKey(processingStep.getClass().getName()))
            return null;

        return cacheMap.get(processingStep.getClass().getName()).get(key);
    }

    public Map<String, Object> getProcessingStepMap(ProcessingStep processingStep){
        return cacheMap.get(processingStep.getClass().getName());
    }

    public void removeData(ProcessingStep processingStep, String key){
        if(cacheMap.containsKey(processingStep.getClass().getName()))
            cacheMap.get(processingStep.getClass().getName()).remove(key);
    }
}
