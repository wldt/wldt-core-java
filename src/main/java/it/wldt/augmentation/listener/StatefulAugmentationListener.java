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
package it.wldt.augmentation.listener;

import it.wldt.augmentation.error.AugmentationFunctionError;
import it.wldt.augmentation.result.AugmentationFunctionResult;
import it.wldt.exception.AugmentationFunctionException;
import it.wldt.storage.query.QueryRequest;

import java.util.List;

/**
 * Interface representing a listener for stateful augmentation functions. This listener is designed to handle events
 * related to the execution of stateful augmentation functions, such as receiving results, handling errors, and
 * refreshing query results. Implementing this interface allows for better management of the execution flow of
 * stateful augmentation functions, enabling the system to respond appropriately to different outcomes of the
 * function execution, such as successful completion, errors, or the need to refresh query results based on changes
 * in the underlying data or context.
 */
public interface StatefulAugmentationListener {

    public void onStatefulAugmentationFunctionResult(String augmentationFunctionId, List<AugmentationFunctionResult<?>> resultList);

    public void onStatefulAugmentationFunctionError(String augmentationFunctionId, AugmentationFunctionError augmentationFunctionError);

    public void onStatefulAugmentationFunctionQueryResultRefresh(String augmentationFunctionId, QueryRequest queryRequest);
}
