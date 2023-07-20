package it.wldt.processing;

import it.wldt.processing.cache.PipelineCache;

public interface ProcessingStep {

    public void execute(PipelineCache pipelineCache, PipelineData data, ProcessingStepListener listener);

}
