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

/**
 * @author Marco Picone, Ph.D. - picone.m@gmail.com
 */
public class AverageHrAugmentationFunction extends AugmentationFunction<Double>{

    private int secTimeWindow;

    public AverageHrAugmentationFunction(String id, int secTimeWindow, AugmentationFunctionListener<Double> listener) {
        super(id, listener);
        this.secTimeWindow = secTimeWindow;
    }

    @Override
    public Double execute() {
        return 25.0;
    }

    public int getSecTimeWindow() {
        return secTimeWindow;
    }

    public void setSecTimeWindow(int secTimeWindow) {
        this.secTimeWindow = secTimeWindow;
    }
}
