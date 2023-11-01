package it.wldt.augmentation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 * @project wldt-core
 * @created 26/10/2023 - 16:06
 */
public class AugmentationFunctionExecutor {

    private static final int AUGMENTATION_FUNCTION_THREAD_SIZE = 5;

    private ExecutorService functionExecutorService = null;

    private static AugmentationFunctionExecutor instance;

    private List<AugmentationFunction<?>> augmentationFunctionList = null;

    private AugmentationFunctionExecutor(){
        this.functionExecutorService = Executors.newFixedThreadPool(AUGMENTATION_FUNCTION_THREAD_SIZE);
        this.augmentationFunctionList = new ArrayList<>();
    }

    public static AugmentationFunctionExecutor getInstance(){
        if(instance == null)
            instance = new AugmentationFunctionExecutor();

        return instance;
    }

    public void addFunction(AugmentationFunction<?> augmentationFunction){
        this.augmentationFunctionList.add(augmentationFunction);
    }

    public void deleteFunction(String functionId){
        //TODO Implement
    }

    public void execute(){
        this.augmentationFunctionList.forEach(function -> {
            this.functionExecutorService.execute(function);
        });

        this.functionExecutorService.shutdown();
    }

}
