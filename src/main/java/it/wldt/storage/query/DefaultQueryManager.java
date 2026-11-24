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
package it.wldt.storage.query;

/**
 * Authors:
 *          Marco Picone, Ph.D. (picone.m@gmail.com)
 * Date: 25/07/2024
 * Default Query Manager Class
 * This class is responsible to manage the query received by the Query Engine.
 * It uses the behaviour of the {@link QueryManager} as is: the target storage is the first storage available and the
 * monitoring metrics (STORAGE_QUERY_*) are updated by the base class. A custom query manager extends
 * {@link QueryManager} and overrides {@code getTargetStorage}.
 */
public class DefaultQueryManager extends QueryManager{

    public DefaultQueryManager() {
        super();
    }

    public DefaultQueryManager(String digitalTwinId) {
        super(digitalTwinId);
    }
}
