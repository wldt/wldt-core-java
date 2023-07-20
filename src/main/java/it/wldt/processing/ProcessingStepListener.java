package it.wldt.processing;

import java.util.Optional;

public interface ProcessingStepListener {

    public void onStepDone(ProcessingStep step, Optional<PipelineData> result);

    public void onStepError(ProcessingStep step, PipelineData originalData, String errorMessage);

    public void onStepSkip(ProcessingStep step, PipelineData previousStepData);

}
