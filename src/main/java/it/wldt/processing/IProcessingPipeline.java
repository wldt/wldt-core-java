package it.wldt.processing;

import it.wldt.exception.ProcessingPipelineException;

/**
 * Author:
 *      Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/03/2023
 *
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public interface IProcessingPipeline {

    public void addStep(ProcessingStep step);

    public void removeStep(ProcessingStep step);

    public void start(PipelineData initialData, ProcessingPipelineListener listener) throws ProcessingPipelineException;

    int getSize();
}
