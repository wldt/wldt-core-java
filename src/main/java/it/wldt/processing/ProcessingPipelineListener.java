package it.wldt.processing;

import java.util.Optional;

/**
 * Author:
 *      Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 01/03/2023
 *
 * Project: White Label Digital Twin Java Framework - (whitelabel-digitaltwin)
 */
public interface ProcessingPipelineListener {

    public void onPipelineDone(Optional<PipelineData> result);

    public void onPipelineError();

}
