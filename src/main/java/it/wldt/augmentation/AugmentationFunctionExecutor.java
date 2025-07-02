/*
 * Copyright [2025] [Marco Picone, Ph.D. - picone.m@gmail.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author: Marco Picone <picone.m@gmail.com> - https://www.marcopicone.net/
 */
package it.wldt.augmentation;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
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
