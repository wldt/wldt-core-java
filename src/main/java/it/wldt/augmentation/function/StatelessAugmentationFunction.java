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
package it.wldt.augmentation.function;

import it.wldt.augmentation.context.AugmentationFunctionContext;
import it.wldt.augmentation.context.AugmentationFunctionContextRequest;
import it.wldt.augmentation.error.AugmentationFunctionError;
import it.wldt.augmentation.listener.StatefulAugmentationListener;
import it.wldt.augmentation.listener.StatelessAugmentationListener;
import it.wldt.augmentation.request.AugmentationFunctionRequest;
import it.wldt.augmentation.result.AugmentationFunctionResult;
import it.wldt.exception.AugmentationFunctionException;
import it.wldt.log.WldtLogger;
import it.wldt.log.WldtLoggerProvider;
import jdk.jpackage.internal.Log;

import java.security.PrivateKey;
import java.util.List;

public abstract class StatelessAugmentationFunction extends AugmentationFunction{

    private static final WldtLogger logger = WldtLoggerProvider.getLogger(StatelessAugmentationFunction.class);

    private StatelessAugmentationListener statelessAugmentationListener;

    private AugmentationFunctionRequest request;

    /**
     * Constructor of the AugmentationFunction class with all the parameters.
     *
     * @param id the unique id of the augmentation function
     * @param name the name of the augmentation function
     * @param description the description of the augmentation function
     * @param version the version of the augmentation function
     * @param contextRequest the context request of the augmentation function
     */
    public StatelessAugmentationFunction(String id,
                                         String name,
                                         String description,
                                         String version,
                                         AugmentationFunctionContextRequest contextRequest) {

        super(id, name, description, version, AugmentationFunctionType.STATELESS, contextRequest);
    }

    /**
     * Constructor of the AugmentationFunction class with minimum parameters.
     *
     * @param id the unique id of the augmentation function
     * @param name the name of the augmentation function
     */
    public StatelessAugmentationFunction(String id,
                                         String name) {
        super(id,
                name,
                null,
                null,
                AugmentationFunctionType.STATELESS,
                new AugmentationFunctionContextRequest());
    }

    /**
     * Constructor of the AugmentationFunction class with minimum parameters.
     *
     * @param id the unique id of the augmentation function
     * @param name the name of the augmentation function
     */
    public StatelessAugmentationFunction(String id,
                                         String name,
                                         String description,
                                         String version) {
        super(id,
                name,
                description,
                version,
                AugmentationFunctionType.STATELESS,
                new AugmentationFunctionContextRequest());
    }

    /**
     * TODO: ...
     * @return
     */
    public StatelessAugmentationListener getStatelessAugmentationListener() {
        return statelessAugmentationListener;
    }

    /**
     * TODO: ...
     * @param statelessAugmentationListener
     */
    public void setStatelessAugmentationListener(StatelessAugmentationListener statelessAugmentationListener) {
        this.statelessAugmentationListener = statelessAugmentationListener;
    }

    public List<AugmentationFunctionResult<?>> handleRun(AugmentationFunctionRequest augmentationFunctionRequest) throws AugmentationFunctionException {
        this.request = augmentationFunctionRequest;
        List<AugmentationFunctionResult<?>> results = this.run(augmentationFunctionRequest);
        for(AugmentationFunctionResult<?> result : results) {
            result.setRequest(augmentationFunctionRequest);
        }
        return results;
    }

    protected abstract List<AugmentationFunctionResult<?>> run(AugmentationFunctionRequest request) throws AugmentationFunctionException;

    /**
     * TODO
     * @param augmentationFunctionError
     */
    protected void notifyError(AugmentationFunctionError augmentationFunctionError) {
        if (statelessAugmentationListener != null) {
            augmentationFunctionError.setAugmentationFunctionRequestId(this.request != null ? this.request.getRequestId() : null);
            statelessAugmentationListener.onStatelessAugmentationFunctionError(this.getId(), augmentationFunctionError);
        }
        else
            logger.error("Cannot notify error of the Stateful Augmentation Function with id {}: result listener is null.", this.getId());
    }
}
