package it.wldt.processing;

import java.util.Optional;

/**
 * Author:
 *      Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/03/2023
 *
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public interface ProcessingStepListener {

    public void onStepDone(ProcessingStep step, Optional<PipelineData> result);

    public void onStepError(ProcessingStep step, PipelineData originalData, String errorMessage);

    public void onStepSkip(ProcessingStep step, PipelineData previousStepData);

}
