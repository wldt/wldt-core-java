package it.wldt.processing;

import it.wldt.processing.cache.PipelineCache;

/**
 * Author:
 *      Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/03/2023
 *
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public interface ProcessingStep {

    public void execute(PipelineCache pipelineCache, PipelineData data, ProcessingStepListener listener);

}
