package it.wldt.processing;

import it.wldt.exception.ProcessingPipelineException;

public interface IProcessingPipeline {

    public void addStep(ProcessingStep step);

    public void removeStep(ProcessingStep step);

    public void start(PipelineData initialData, ProcessingPipelineListener listener) throws ProcessingPipelineException;

    int getSize();
}
