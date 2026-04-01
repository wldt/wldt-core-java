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

/**
 * Extends the base class {@link AugmentationFunction} and represents a specific type of augmentation function that is executed in a stateless manner.
 * In a stateless augmentation function, each execution is independent and does not maintain any state or context between
 * executions. This means that each time the function is executed, it starts with a clean slate, without any knowledge
 * of previous executions or any shared state. This can be useful for functions that perform simple, self-contained
 * tasks that do not require any context or state to be maintained across executions. The StatelessAugmentationFunction
 * class provides a structure for implementing such functions, allowing developers to focus on the specific logic of the
 * function without worrying about managing state or context between executions. It also includes a listener mechanism
 * to notify about errors that may occur during the execution of the function, enabling better error handling and
 * management in a stateless execution context.
 */
public abstract class StatelessAugmentationFunction extends AugmentationFunction{

    /**
     * Logger instance for logging messages related to the StatelessAugmentationFunction class. This logger is used to
     * log important information, errors, and other relevant messages during the execution of stateless augmentation
     * functions, providing insights into the behavior and performance of the functions, as well as aiding in debugging
     * and troubleshooting when issues arise.
     */
    private static final WldtLogger logger = WldtLoggerProvider.getLogger(StatelessAugmentationFunction.class);

    /**
     * Listener for handling events and notifications related to the execution of stateless augmentation functions.
     * This listener can be used to receive notifications about errors that occur during the execution of the function,
     * allowing for better error handling and management in a stateless execution context. By implementing the
     * StatelessAugmentationListener interface, developers can define custom behavior for handling errors and other
     * events that may arise during the execution of stateless augmentation functions, enabling more robust and
     * effective management of the execution process and improving the overall reliability and performance of the functions.
     */
    private StatelessAugmentationListener statelessAugmentationListener;

    /**
     * The request associated with the current execution of the stateless augmentation function. This request contains
     * all the necessary information and parameters for executing the function, allowing the function to perform its
     * tasks based on the specific context and requirements of the execution. By maintaining a reference to the current
     * request, the StatelessAugmentationFunction can access relevant information and parameters during the execution
     * process, enabling it to perform its tasks effectively and efficiently while also providing the necessary context
     * for error handling and other notifications through the StatelessAugmentationListener.
     */
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
     * Getter for the stateless augmentation listener. This method allows access to the listener that is responsible for
     * handling events and notifications related to the execution of stateless augmentation functions.
     * @return The stateless augmentation listener associated with this stateless augmentation function, which can be used to receive notifications
     * about errors and other events that may occur during the execution of the function, enabling better error handling and management in a stateless execution context.
     */
    public StatelessAugmentationListener getStatelessAugmentationListener() {
        return statelessAugmentationListener;
    }

    /**
     * Setter for the stateless augmentation listener. This method allows setting the listener that is responsible for
     * handling events and notifications related to the execution of stateless augmentation functions.
     * @param statelessAugmentationListener The stateless augmentation listener to be associated with this stateless augmentation function, which can be used to receive notifications
     * about errors and other events that may occur during the execution of the function, enabling better error handling and management in a stateless execution context.
     */
    public void setStatelessAugmentationListener(StatelessAugmentationListener statelessAugmentationListener) {
        this.statelessAugmentationListener = statelessAugmentationListener;
    }

    /**
     * Handles the execution of the stateless augmentation function based on the provided request. This method is
     * responsible for executing the function's logic and returning the results of the execution. It also sets the
     * request for each result, allowing for better context and management of the execution process. If any errors
     * occur during the execution of the function, they can be handled and notified through the notifyError method,
     * providing a mechanism for better error handling and management in a stateless execution context.
     * @param augmentationFunctionRequest The request for the execution of the stateless augmentation function.
     * @return A list of results from the execution of the stateless augmentation function, with each result containing
     * the necessary information and context for further processing and management of the execution outcomes.
     * @throws AugmentationFunctionException if there is an issue with executing the stateless augmentation function,
     * such as invalid parameters, execution errors, or any other issues that may arise during the execution process.
     */
    public List<AugmentationFunctionResult<?>> handleRun(AugmentationFunctionRequest augmentationFunctionRequest) throws AugmentationFunctionException {
        this.request = augmentationFunctionRequest;
        List<AugmentationFunctionResult<?>> results = this.run(augmentationFunctionRequest);
        for(AugmentationFunctionResult<?> result : results) {
            result.setRequest(augmentationFunctionRequest);
        }
        return results;
    }

    /**
     * Abstract method that defines the logic for executing the stateless augmentation function based on the provided request.
     * @param request The request for the execution of the stateless augmentation function, containing all the necessary
     *                information and parameters for executing the function based on the specific context and requirements of the execution.
     * @return A list of results from the execution of the stateless augmentation function, with each result containing
     * the necessary information and context for further processing and management of the execution outcomes.
     * @throws AugmentationFunctionException if there is an issue with executing the stateless augmentation function,
     * such as invalid parameters, execution errors, or any other issues that may arise during the execution process.
     */
    protected abstract List<AugmentationFunctionResult<?>> run(AugmentationFunctionRequest request) throws AugmentationFunctionException;

    /**
     * Notifies about an error that occurred during the execution of the stateless augmentation function. This method
     * is responsible for sending notifications about errors to the associated StatelessAugmentationListener, allowing
     * for better error handling and management in a stateless execution context. If the listener is not set, it logs
     * an error message indicating that the error cannot be notified due to the absence of the listener, providing
     * insights into potential issues with error handling and management in the execution process.
     * @param augmentationFunctionError The error that occurred during the execution of the stateless augmentation function,
     *        containing all the necessary information and context about the error for effective handling and management of the issue.
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
