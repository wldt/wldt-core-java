package it.wldt.processing;

import it.wldt.exception.ProcessingPipelineException;
import it.wldt.processing.cache.PipelineCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class ProcessingPipeline implements IProcessingPipeline, ProcessingStepListener {

    private static final Logger logger = LoggerFactory.getLogger(ProcessingPipeline.class);

    private List<ProcessingStep> processingStepList = null;
    private Iterator<ProcessingStep> listIterator = null;
    private ProcessingPipelineListener listener = null;
    private PipelineCache pipelineCache = null;

    public ProcessingPipeline() {
        this.processingStepList = new ArrayList<>();
        this.pipelineCache = new PipelineCache();
    }

    public ProcessingPipeline(ProcessingStep... steps) {

        this.processingStepList = new ArrayList<>();
        this.pipelineCache = new PipelineCache();

        if(steps != null && steps.length > 0)
            this.processingStepList.addAll(Arrays.asList(steps));
    }

    @Override
    public void addStep(ProcessingStep step) {
        this.processingStepList.add(step);
    }

    @Override
    public void removeStep(ProcessingStep step) {
        this.processingStepList.remove(step);
    }

    @Override
    public void start(PipelineData initialData, ProcessingPipelineListener listener) throws ProcessingPipelineException {

        logger.debug("Starting Pipeline ... !");

        this.listener = listener;
        this.listIterator = this.processingStepList.iterator();

        if(listIterator.hasNext()) {
            ProcessingStep nextStep = listIterator.next();
            logger.debug("Scheduling next step: {}", nextStep.getClass().getName());
            nextStep.execute(pipelineCache, initialData, this);
        }
        else
            throw new ProcessingPipelineException("Empty Pipeline !");
    }

    @Override
    public void onStepDone(ProcessingStep step, Optional<PipelineData> result) {

        logger.debug("Step {} Done ! Result Available : {}", step.getClass(), result.isPresent());

        if(listIterator.hasNext() && result.isPresent()) {
            ProcessingStep nextStep = listIterator.next();
            logger.debug("Scheduling next step: {}", nextStep.getClass().getName());
            nextStep.execute(pipelineCache, result.get(), this);
        }
        else {

            logger.debug("Pipeline Completed !");

            if(this.listener != null)
                listener.onPipelineDone(result);
        }
    }

    @Override
    public void onStepError(ProcessingStep step, PipelineData originalData, String errorMessage) {
        logger.error("Step: {} -> Error {} ! Original Data: {}", step.getClass(), errorMessage, originalData);
    }

    @Override
    public void onStepSkip(ProcessingStep step, PipelineData previousStepData) {

        logger.debug("Step {} Skipped ! Previous Data: {}", step.getClass(), previousStepData);
        if(listIterator.hasNext()) {
            ProcessingStep nextStep = listIterator.next();
            logger.debug("Scheduling next step: {}", nextStep.getClass().getName());
            nextStep.execute(pipelineCache, previousStepData, this);
        }
        else {
            logger.debug("Pipeline correctly Completed !");
            if(this.listener != null)
                listener.onPipelineDone(java.util.Optional.empty());
        }
    }

    @Override
    public int getSize(){
        return this.processingStepList.size();
    }
}
