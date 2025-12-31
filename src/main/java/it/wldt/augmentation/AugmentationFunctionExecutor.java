/*
 * Copyright (c) 2025 - Current Year
 * Marco Picone Ph.D
 * Email: picone.m@gmail.com
 * Website: https://www.marcopicone.net/
 * All rights reserved.
 *
 * This program is provided under a Dual Licensing model:
 * 1) GNU General Public License version 3.0 (GPL-3.0) for open-source, academic,
 *    research, non-profit, and other non-commercial use; or
 * 2) Commercial License, for any commercial use, proprietary development, or
 *    closed-source distribution. To obtain a Commercial License, please contact: Marco Picone (picone.m@gmail.com)
 *
 * By using this software, you agree to comply with the terms of the applicable license.
 * This applies to all forms of the software, including source code and compiled/binary forms.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
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
